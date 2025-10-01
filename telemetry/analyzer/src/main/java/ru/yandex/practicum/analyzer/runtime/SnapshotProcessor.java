package ru.yandex.practicum.analyzer.runtime;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.engine.ScenarioEngine;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    @Qualifier("snapshotsConsumer")
    private final KafkaConsumer<String, ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro> consumer;
    private final ScenarioRepository scenarioRepo;
    private final ScenarioEngine engine;
    @Value("${kafka.topics.snapshots}")
    private String topic;

    public void start() {
        consumer.subscribe(List.of(topic));
        while (!Thread.currentThread().isInterrupted()) {
            var recs = consumer.poll(Duration.ofSeconds(1));
            for (var r : recs) {
                var snap = r.value();
                var hubId = snap.getHubId();
                var scenarios = scenarioRepo.findWithDetailsByHubId(hubId);
                if (!scenarios.isEmpty()) {
                    engine.evaluateAndExecute(snap, scenarios);
                }
            }
            consumer.commitSync();
        }
    }
}