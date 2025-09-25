package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.avro.AvroBytes;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventMapper;

@RestController
public class SensorController {

    private final KafkaTemplate<String, byte[]> kafka;
    private final String topic;

    public SensorController(KafkaTemplate<String, byte[]> kafka,
                            @Value("${app.kafka.topics.sensors}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    /**
     * OpenAPI: POST /events/sensors -> 200 OK
     */
    @PostMapping(path = "/events/sensors", consumes = "application/json")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        var avro = SensorEventMapper.toAvro(event);
        var bytes = AvroBytes.toBytes(avro);
        // SensorController
        kafka.send(topic, event.getHubId(), bytes).whenComplete((res, ex) -> {
            if (ex != null) {
                System.err.println("Kafka send FAILED (sensors): " + ex.getMessage());
            } else {
                System.out.println("Kafka send OK (sensors): " + res.getRecordMetadata().topic()
                        + "@" + res.getRecordMetadata().partition() + "/" + res.getRecordMetadata().offset());
            }
        });
        return ResponseEntity.ok().build();
    }
}