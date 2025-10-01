package ru.yandex.practicum.analyzer.runtime;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.ActionEntity;
import ru.yandex.practicum.analyzer.model.ActionType;
import ru.yandex.practicum.analyzer.model.ConditionEntity;
import ru.yandex.practicum.analyzer.model.ConditionType;
import ru.yandex.practicum.analyzer.model.Operation;
import ru.yandex.practicum.analyzer.model.ScenarioActionLink;
import ru.yandex.practicum.analyzer.model.ScenarioConditionLink;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;
import ru.yandex.practicum.analyzer.model.SensorEntity;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    @Qualifier("hubEventsConsumer")
    private final KafkaConsumer<String, HubEventAvro> consumer;

    private final SensorRepository sensorRepo;
    private final ScenarioRepository scenarioRepo;
    private final ConditionRepository conditionRepo;
    private final ActionRepository actionRepo;

    @Value("${kafka.topics.hubs}")
    private String topic;

    @Override
    public void run() {
        consumer.subscribe(List.of(topic));
        while (!Thread.currentThread().isInterrupted()) {
            var recs = consumer.poll(Duration.ofSeconds(1));
            for (var r : recs) handle(r.value());
        }
    }

    private void handle(HubEventAvro e) {
        String hubId = e.getHubId();
        Object payload = e.getPayload();

        if (payload instanceof DeviceAddedEventAvro da) {
            var d = sensorRepo.findById(da.getId()).orElseGet(SensorEntity::new);
            d.setId(da.getId());
            d.setHubId(hubId);
            sensorRepo.save(d);

        } else if (payload instanceof DeviceRemovedEventAvro dr) {
            sensorRepo.findByIdAndHubId(dr.getId(), hubId).ifPresent(sensorRepo::delete);

        } else if (payload instanceof ScenarioAddedEventAvro sa) {
            var sc = scenarioRepo.findByHubIdAndName(hubId, sa.getName())
                    .orElseGet(() -> {
                        var s = new ScenarioEntity();
                        s.setHubId(hubId);
                        s.setName(sa.getName());
                        return s;
                    });

            // подчистим линк-коллекции
            sc.getConditionLinks().clear();
            sc.getActionLinks().clear();

            // --- conditions ---
            for (var c : sa.getConditions()) {
                var ce = new ConditionEntity();
                ce.setType(ConditionType.valueOf(c.getType().name()));       // enum → enum
                ce.setOperation(Operation.valueOf(c.getOperation().name())); // enum → enum
                Object v = c.getValue();
                if (v instanceof Integer i) ce.setValue(i);
                else if (v instanceof Boolean b) ce.setValue(b ? 1 : 0);
                ce = conditionRepo.save(ce);

                var sensor = sensorRepo.findByIdAndHubId(c.getSensorId(), hubId)
                        .orElseGet(() -> {
                            var s = new SensorEntity();
                            s.setId(c.getSensorId());
                            s.setHubId(hubId);
                            return sensorRepo.save(s);
                        });

                var link = new ScenarioConditionLink();
                link.setScenario(sc);
                link.setSensor(sensor);
                link.setCondition(ce);
                sc.getConditionLinks().add(link);
            }

// --- actions ---
            for (var a : sa.getActions()) {
                var ae = new ActionEntity();
                ae.setType(ActionType.valueOf(a.getType().name())); // enum → enum
                if (a.getValue() instanceof Integer i) ae.setValue(i);
                ae = actionRepo.save(ae);

                var sensor = sensorRepo.findByIdAndHubId(a.getSensorId(), hubId)
                        .orElseGet(() -> {
                            var s = new SensorEntity();
                            s.setId(a.getSensorId());
                            s.setHubId(hubId);
                            return sensorRepo.save(s);
                        });

                var link = new ScenarioActionLink();
                link.setScenario(sc);
                link.setSensor(sensor);
                link.setAction(ae);
                sc.getActionLinks().add(link);
            }
            scenarioRepo.save(sc);
        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            scenarioRepo.findByHubIdAndName(hubId, sr.getName()).ifPresent(scenarioRepo::delete);
        }
    }
}