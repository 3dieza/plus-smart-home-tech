package ru.yandex.practicum.analyzer.service;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ScenarioLoader scenarioLoader;
    private final ScenarioEngine engine;
    @Value("${kafka.topics.snapshots}")
    private String topic;

    public SnapshotProcessor(
            @Qualifier("snapshotsConsumer")
            KafkaConsumer<String, SensorsSnapshotAvro> consumer,
            ScenarioLoader scenarioLoader,
            ScenarioEngine engine) {
        this.consumer = consumer;
        this.scenarioLoader = scenarioLoader;
        this.engine = engine;
    }

    public void start() {
        consumer.subscribe(List.of(topic));
        while (!Thread.currentThread().isInterrupted()) {
            var recs = consumer.poll(Duration.ofSeconds(1));
            for (var r : recs) {
                var snap = r.value();
                var hubId = snap.getHubId();
                var scenarios = scenarioLoader.loadForHub(hubId);
                if (!scenarios.isEmpty()) {
                    engine.evaluateAndExecute(snap, scenarios);
                }
            }
            consumer.commitSync();
        }
    }
}