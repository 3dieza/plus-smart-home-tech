package ru.yandex.practicum.serializationcore.kafka.avro;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventSerializer extends BaseAvroSerializer<HubEventAvro> {
    public HubEventSerializer() {
        super(HubEventAvro.class);
    }
}