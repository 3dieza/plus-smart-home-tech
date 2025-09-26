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
     * Принимает событие от сенсора и публикует его в Kafka.
     */
    @PostMapping(path = "/events/sensors", consumes = "application/json")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        var avro = SensorEventMapper.toAvro(event);
        var bytes = AvroBytes.toBytes(avro);
        kafka.send(topic, event.getHubId(), bytes);
        return ResponseEntity.ok().build();
    }
}