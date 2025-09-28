package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.avro.AvroBytes;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventMapper;

@RestController
public class HubController {

    private static final Logger log = LoggerFactory.getLogger(HubController.class);

    private final KafkaTemplate<String, byte[]> kafka;
    private final String topic;

    public HubController(
            KafkaTemplate<String, byte[]> kafka,
            @Value("${app.kafka.topics.hubs}") String topic
    ) {
        this.kafka = kafka;
        this.topic = topic;
    }

    /**
     * Принимает событие от хаба и публикует его в Kafka.
     */
    @PostMapping(path = "/events/hubs", consumes = "application/json")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("HUB EVENT received: hubId={}, type={}, ts={}",
                event.getHubId(), event.getType(), event.getTimestamp());

        var avro = HubEventMapper.toAvro(event);
        var bytes = AvroBytes.toBytes(avro);

        var record = new ProducerRecord<>(
                topic,
                null, // partition: пусть распределяет продюсер по ключу
                event.getTimestamp().toEpochMilli(),
                event.getHubId(), // key
                bytes             // value
        );

        kafka.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("HUB EVENT publish failed: hubId={}, topic={}, reason={}",
                        event.getHubId(), topic, ex, ex);
            } else {
                var md = result.getRecordMetadata();
                log.info("HUB EVENT published: hubId={}, topic={}, partition={}, offset={}, ts={}",
                        event.getHubId(), md.topic(), md.partition(), md.offset(), record.timestamp());
            }
        });

        return ResponseEntity.ok().build();
    }
}