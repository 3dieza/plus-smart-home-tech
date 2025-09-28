package ru.yandex.practicum.aggregator.runtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.aggregator.service.SnapshotAggregationService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final SnapshotAggregationService aggregationService;
    private final AggregatorKafkaProperties props;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Aggregator already running");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            // Подписка с ребаланс-листенером, чтобы фиксировать оффсеты перед отбором партиций
            consumer.subscribe(Collections.singletonList(props.getInTopic()), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    try {
                        log.info("Partitions revoked: {}. Commit current offsets...", partitions);
                        consumer.commitSync(currentOffsets);
                    } catch (Exception e) {
                        log.warn("Commit on revoke failed", e);
                    }
                }
                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    log.info("Partitions assigned: {}", partitions);
                }
            });

            final Duration pollTimeout = Duration.ofMillis(props.getPollTimeoutMs());
            log.info("Aggregator started. InTopic={}, OutTopic={}", props.getInTopic(), props.getOutTopic());

            int processedInBatch = 0;
            while (running.get()) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(pollTimeout);
                if (records.isEmpty()) {
                    // можно добавить health-метрики/лог
                    continue;
                }

                for (ConsumerRecord<String, SensorEventAvro> rec : records) {
                    SensorEventAvro event = rec.value();
                    if (event == null) continue;

                    aggregationService.updateState(event).ifPresent(snapshot -> {
                        // ключ продюсера — hubId (логично для партиционирования по хабу)
                        ProducerRecord<String, SensorsSnapshotAvro> out =
                                new ProducerRecord<>(props.getOutTopic(), snapshot.getHubId(), snapshot);

                        // отправляем (fire-and-forget с логом/колбэком)
                        producer.send(out, (md, ex) -> {
                            if (ex != null) log.error("Failed...", ex);
                            else log.info("Snapshot sent: hub={}, topic={}, partition={}, offset={}",
                                    snapshot.getHubId(), md.topic(), md.partition(), md.offset());
                        });
                    });

                    // Обновляем оффсет для партиции
                    currentOffsets.put(
                            new TopicPartition(rec.topic(), rec.partition()),
                            new OffsetAndMetadata(rec.offset() + 1)
                    );

                    processedInBatch++;
                    if (processedInBatch % 10 == 0) { // как и в прошлой задаче
                        commitAsyncSafe();
                    }
                }

                // Финальный async-коммит итерации
                commitAsyncSafe();
            }

        } catch (WakeupException ignored) {
            // shutdown вызвал consumer.wakeup()
        } catch (Exception e) {
            log.error("Aggregator runtime error", e);
        } finally {
            // Гарантируем flush продюсера и синхронный коммит оффсетов
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } catch (Exception e) {
                log.warn("Final flush/commit failed", e);
            } finally {
                log.info("Closing consumer...");
                consumer.close();
                log.info("Closing producer...");
                producer.close();
                log.info("Aggregator stopped");
            }
        }
    }

    private void commitAsyncSafe() {
        consumer.commitAsync(new HashMap<>(currentOffsets), (offsets, ex) -> {
            if (ex != null) {
                log.warn("Async commit failed for {}", offsets, ex);
                try {
                    consumer.commitSync(offsets);
                } catch (Exception e2) {
                    log.warn("Sync commit retry failed", e2);
                }
            }
        });
    }

    private void shutdown() {
        if (running.compareAndSet(true, false)) {
            try {
                consumer.wakeup();
            } catch (Exception ignored) { }
        }
    }
}