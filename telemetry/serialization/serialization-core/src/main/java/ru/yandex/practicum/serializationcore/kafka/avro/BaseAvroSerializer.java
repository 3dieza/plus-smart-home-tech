package ru.yandex.practicum.serializationcore.kafka.avro;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BaseAvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private final Class<T> targetClass;

    public BaseAvroSerializer(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) return null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
            SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(targetClass);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Avro serialization failed for " + targetClass.getName(), e);
        }
    }
}