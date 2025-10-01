package ru.yandex.practicum.analyzer.engine;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.analyzer.model.ConditionEntity;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScenarioEngine {
    private final HubRouterClient hubRouter;

    public void evaluateAndExecute(
            SensorsSnapshotAvro snapshot,
            List<ScenarioEntity> scenarios) {

        final String hubId = snapshot.getHubId();
        final Map<String, SensorStateAvro> states =
                snapshot.getSensorsState();

        for (var sc : scenarios) {
            boolean ok = sc.getConditions().stream().allMatch(c -> testCondition(c, states));
            if (ok) {
                for (var a : sc.getActions()) {
                    log.info("Action -> hubId={}, scenario={}, deviceId={}, type={}, value={}",
                            hubId, sc.getName(), a.getSensorId(), a.getType(), a.getValue());
                    hubRouter.sendAction(hubId, sc.getName(), a.getSensorId(), a.getType(), a.getValue());
                }
            }
        }
    }

    private boolean testCondition(
            ConditionEntity c,
            Map<String, SensorStateAvro> states) {

        var st = states.get(c.getSensorId());
        if (st == null || st.getData() == null) return false;

        Object payload = st.getData();
        switch (c.getType()) {
            case "MOTION" -> {
                var p = (MotionSensorAvro) payload;
                Boolean expected = c.getBoolValue();
                if (expected == null) return false;
                return opBoolean(c.getOperation(), p.getMotion(), expected);
            }
            case "SWITCH" -> {
                var p = (SwitchSensorAvro) payload;
                Boolean expected = c.getBoolValue();
                if (expected == null) return false;
                return opBoolean(c.getOperation(), p.getState(), expected);
            }
            case "LUMINOSITY" -> {
                var p = (LightSensorAvro) payload;
                Integer expected = c.getIntValue();
                if (expected == null) return false;
                return opInt(c.getOperation(), p.getLuminosity(), expected);
            }
            case "TEMPERATURE" -> {
                Integer expected = c.getIntValue();
                if (expected == null) return false;
                if (payload instanceof TemperatureSensorAvro t)
                    return opInt(c.getOperation(), t.getTemperatureC(), expected);
                if (payload instanceof ClimateSensorAvro cl)
                    return opInt(c.getOperation(), cl.getTemperatureC(), expected);
                return false;
            }
            case "CO2LEVEL" -> {
                var cl = (ClimateSensorAvro) payload;
                Integer expected = c.getIntValue();
                if (expected == null) return false;
                return opInt(c.getOperation(), cl.getCo2Level(), expected);
            }
            case "HUMIDITY" -> {
                var cl = (ClimateSensorAvro) payload;
                Integer expected = c.getIntValue();
                if (expected == null) return false;
                return opInt(c.getOperation(), cl.getHumidity(), expected);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean opBoolean(String op, boolean actual, boolean expected) {
        if ("EQUALS".equals(op)) return actual == expected;
        return false;
    }

    private boolean opInt(String op, int actual, int expected) {
        return switch (op) {
            case "EQUALS" -> actual == expected;
            case "GREATER_THAN" -> actual > expected;
            case "LESS_THAN" -> actual < expected;
            default -> false;
        };
    }
}