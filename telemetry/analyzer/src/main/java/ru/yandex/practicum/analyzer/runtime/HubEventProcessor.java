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
import ru.yandex.practicum.analyzer.model.ConditionEntity;
import ru.yandex.practicum.analyzer.model.DeviceEntity;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;
import ru.yandex.practicum.analyzer.repository.DeviceRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
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
    private final DeviceRepository deviceRepo;
    private final ScenarioRepository scenarioRepo;

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
            var d = deviceRepo.findById(da.getId()).orElseGet(DeviceEntity::new);
            d.setId(da.getId());
            d.setHubId(hubId);
            d.setType(da.getType().name());
            deviceRepo.save(d);
        } else if (payload instanceof DeviceRemovedEventAvro dr) {
            deviceRepo.findByIdAndHubId(dr.getId(), hubId).ifPresent(deviceRepo::delete);
        } else if (payload instanceof ScenarioAddedEventAvro sa) {
            var sc = scenarioRepo.findByHubIdAndName(hubId, sa.getName())
                    .orElseGet(() -> {
                        var s = new ScenarioEntity();
                        s.setHubId(hubId);
                        s.setName(sa.getName());
                        return s;
                    });

            sc.getConditions().clear();
            sc.getActions().clear();

            // conditions
            for (var c : sa.getConditions()) {
                var ce = new ConditionEntity();
                ce.setScenario(sc);
                ce.setSensorId(c.getSensorId());
                ce.setType(c.getType().name());
                ce.setOperation(c.getOperation().name());

                Object v = c.getValue();
                if (v instanceof Boolean b) ce.setBoolValue(b);
                else if (v instanceof Integer i) ce.setIntValue(i);

                sc.getConditions().add(ce);
            }

            // actions
            for (var a : sa.getActions()) {
                var ae = new ActionEntity();
                ae.setScenario(sc);
                ae.setSensorId(a.getSensorId());
                ae.setType(a.getType().name());

                Object v = a.getValue();
                if (v instanceof Integer i) ae.setValue(i);

                sc.getActions().add(ae);
            }

            scenarioRepo.save(sc);
        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            scenarioRepo.findByHubIdAndName(hubId, sr.getName()).ifPresent(scenarioRepo::delete);
        }
    }
}