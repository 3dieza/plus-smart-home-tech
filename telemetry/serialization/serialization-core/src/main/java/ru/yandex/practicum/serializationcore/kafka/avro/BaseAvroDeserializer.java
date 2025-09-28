package ru.yandex.practicum.serializationcore.kafka.avro;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DecoderFactory decoderFactory;
    private final Schema schema;
    private final DatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) { /* no-op */ }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try {
            Decoder decoder = decoderFactory.binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new SerializationException("Avro deserialization failed for topic " + topic, e);
        }
    }

    @Override
    public void close() { /* no-op */ }
}