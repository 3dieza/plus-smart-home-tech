package ru.yandex.practicum.analyzer.engine;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.grpc.HubRouterClient;
import ru.yandex.practicum.analyzer.model.ActionType;
import ru.yandex.practicum.analyzer.model.ConditionEntity;
import ru.yandex.practicum.analyzer.model.ConditionType;
import ru.yandex.practicum.analyzer.model.Operation;
import ru.yandex.practicum.analyzer.model.ScenarioActionLink;
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

    public void evaluateAndExecute(SensorsSnapshotAvro snapshot, List<ScenarioEntity> scenarios) {
        log.info("✅ === Incoming Snapshot from Kafka === hubId={}, sensorsState={}",
                snapshot.getHubId(), snapshot.getSensorsState());

        final String hubId = snapshot.getHubId();
        final Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        for (ScenarioEntity sc : scenarios) {
            boolean ok = sc.getConditionLinks().stream()
                    .allMatch(link -> testCondition(link.getSensor().getId(), link.getCondition(), states));

            if (!ok) continue;

            for (ScenarioActionLink al : sc.getActionLinks()) {
                var a = al.getAction();
                var sensorId = al.getSensor().getId();

                Integer v = a.getValue();
                if (v == null) {
                    switch (a.getType()) {
                        case ACTIVATE    -> v = 1;
                        case DEACTIVATE  -> v = 0;
                        default          -> v = null;
                    }
                }
                if (a.getType() == ActionType.SET_VALUE && v == null) {
                    log.warn("❗SET_VALUE без value: scenario={}, deviceId={}", sc.getName(), sensorId);
                }

                log.info("Action -> hubId={}, scenario={}, deviceId={}, type={}, value={}",
                        hubId, sc.getName(), sensorId, a.getType(), v);

                hubRouter.sendAction(hubId, sc.getName(), sensorId, a.getType().name(), v);
            }
        }
    }

    private boolean testCondition(String sensorId, ConditionEntity c, Map<String, SensorStateAvro> states) {
        var st = states.get(sensorId);
        if (st == null || st.getData() == null) return false;

        Object payload = st.getData();
        ConditionType type = c.getType();     // enum
        Operation op      = c.getOperation(); // enum

        switch (type) {
            case MOTION -> {
                var p = (MotionSensorAvro) payload;
                Boolean expected = boolValue(c);
                if (expected == null) return false;
                return opBoolean(op, p.getMotion(), expected);
            }
            case SWITCH -> {
                var p = (SwitchSensorAvro) payload;
                Boolean expected = boolValue(c);
                if (expected == null) return false;
                return opBoolean(op, p.getState(), expected);
            }
            case LUMINOSITY -> {
                var p = (LightSensorAvro) payload;
                Integer expected = c.getValue();
                if (expected == null) return false;
                return opInt(op, p.getLuminosity(), expected);
            }
            case TEMPERATURE -> {
                Integer expected = c.getValue();
                if (expected == null) return false;
                if (payload instanceof TemperatureSensorAvro t)
                    return opInt(op, t.getTemperatureC(), expected);
                if (payload instanceof ClimateSensorAvro cl)
                    return opInt(op, cl.getTemperatureC(), expected);
                return false;
            }
            case CO2LEVEL -> {
                var cl = (ClimateSensorAvro) payload;
                Integer expected = c.getValue();
                if (expected == null) return false;
                return opInt(op, cl.getCo2Level(), expected);
            }
            case HUMIDITY -> {
                var cl = (ClimateSensorAvro) payload;
                Integer expected = c.getValue();
                if (expected == null) return false;
                return opInt(op, cl.getHumidity(), expected);
            }
            default -> {
                return false;
            }
        }
    }

    private boolean opBoolean(Operation op, boolean actual, boolean expected) {
        return op == Operation.EQUALS && actual == expected;
    }

    private boolean opInt(Operation op, int actual, int expected) {
        return switch (op) {
            case EQUALS       -> actual == expected;
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN   -> actual < expected;
        };
    }

    // В DDL только INTEGER value: для булевых — 0/1.
    private Boolean boolValue(ConditionEntity c) {
        Integer v = c.getValue();
        if (v == null) return null;
        return v != 0;
    }
}