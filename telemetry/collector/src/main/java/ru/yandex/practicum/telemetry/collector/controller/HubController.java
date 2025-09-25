package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
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

    private final KafkaTemplate<String, byte[]> kafka;
    private final String topic;

    public HubController(KafkaTemplate<String, byte[]> kafka,
                         @Value("${app.kafka.topics.hubs}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    /**
     * OpenAPI: POST /events/hubs -> 200 OK
     */
    @PostMapping(path = "/events/hubs", consumes = "application/json")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        var avro = HubEventMapper.toAvro(event);
        var bytes = AvroBytes.toBytes(avro);
        kafka.send(topic, event.getHubId(), bytes).whenComplete((res, ex) -> {
            if (ex != null) {
                System.err.println("Kafka send FAILED (hubs): " + ex.getMessage());
            } else {
                System.out.println("Kafka send OK (hubs): " + res.getRecordMetadata().topic()
                        + "@" + res.getRecordMetadata().partition() + "/" + res.getRecordMetadata().offset());
            }
        });
        return ResponseEntity.ok().build();
    }
}