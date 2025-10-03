package ru.yandex.practicum.analyzer.service;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.SensorEntity;
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
    private final ScenarioPersistenceService scenarioTx;

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
            sensorRepo.findById(da.getId()).ifPresentOrElse(
                    s -> {
                        s.setHubId(hubId);
                        sensorRepo.save(s);
                    },
                    () -> {
                        var s = new SensorEntity();
                        s.setId(da.getId());
                        s.setHubId(hubId);
                        sensorRepo.save(s);
                    }
            );

        } else if (payload instanceof DeviceRemovedEventAvro dr) {
            sensorRepo.findByIdAndHubId(dr.getId(), hubId).ifPresent(sensorRepo::delete);

        } else if (payload instanceof ScenarioAddedEventAvro sa) {
            scenarioTx.upsertScenario(hubId, sa);

        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            // scenarioRepo.findByHubIdAndName(hubId, sr.getName()).ifPresent(scenarioRepo::delete);
        }
    }
}