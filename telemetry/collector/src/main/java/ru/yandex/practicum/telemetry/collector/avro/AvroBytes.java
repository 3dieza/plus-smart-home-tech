package ru.yandex.practicum.telemetry.collector.avro;

import java.io.ByteArrayOutputStream;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

public final class AvroBytes {
    private AvroBytes() {
    }

    public static byte[] toBytes(SpecificRecord record) {
        try (var baos = new ByteArrayOutputStream()) {
            var writer = new SpecificDatumWriter<SpecificRecord>(record.getSchema());
            BinaryEncoder enc = EncoderFactory.get().binaryEncoder(baos, null);
            writer.write(record, enc);
            enc.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Avro serialization failed", e);
        }
    }
}