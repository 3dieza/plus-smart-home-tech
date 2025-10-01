package ru.yandex.practicum.serializationcore.kafka.avro;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SensorsSnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {
    public SensorsSnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}