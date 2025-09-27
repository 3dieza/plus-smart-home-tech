package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.avro.AvroBytes;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventMapper;

@RestController
@Validated
@Slf4j
public class SensorController {

    private final KafkaTemplate<String, byte[]> kafka;
    private final String topic;

    public SensorController(
            KafkaTemplate<String, byte[]> kafka,
            @Value("${app.kafka.topics.sensors}") String topic
    ) {
        this.kafka = kafka;
        this.topic = topic;
    }

    /**
     * Принимает событие от сенсора в формате JSON, преобразует его в Avro и публикует в Kafka.
     * В контроллере логируется бизнес-контекст (id, hubId, тип, timestamp), чтобы фиксировать
     * факт приёма события и ключевые атрибуты.
     * Технические детали доставки (partition, offset, ошибки продюсера) логируются через ProducerListener.
     */
    @PostMapping(path = "/events/sensors", consumes = "application/json")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("SENSOR EVENT received: id={}, hubId={}, type={}, ts={}",
                event.getId(), event.getHubId(), event.getType(), event.getTimestamp());

        var avro = SensorEventMapper.toAvro(event);
        var bytes = AvroBytes.toBytes(avro);

        var record = new ProducerRecord<>(
                topic,
                null,
                event.getTimestamp().toEpochMilli(),
                event.getHubId(),
                bytes
        );

        kafka.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("SENSOR EVENT publish failed: id={}, hubId={}, topic={}, reason={}",
                        event.getId(), event.getHubId(), topic, ex, ex);
            } else {
                var md = result.getRecordMetadata();
                log.info("SENSOR EVENT published: id={}, hubId={}, topic={}, partition={}, offset={}, ts={}",
                        event.getId(), event.getHubId(), md.topic(), md.partition(), md.offset(), record.timestamp());
            }
        });

        return ResponseEntity.ok().build();
    }
}