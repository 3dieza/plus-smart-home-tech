package ru.yandex.practicum.telemetry.collector.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, byte[]> producerFactory(
            @Value("${app.kafka.bootstrap-servers}") String bootstrap) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.LINGER_MS_CONFIG, 5,
                ProducerConfig.BATCH_SIZE_CONFIG, 32_768,
                ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

    // ProducerListener — логируем транспортный слой
    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> pf) {
        KafkaTemplate<String, byte[]> kt = new KafkaTemplate<>(pf);
        kt.setProducerListener(new ProducerListener<>() {
            private final Logger log = LoggerFactory.getLogger("KafkaSend");

            @Override
            public void onSuccess(ProducerRecord<String, byte[]> record, RecordMetadata metadata) {
                log.info("Kafka send OK: topic={}, partition={}, offset={}, ts={}",
                        metadata.topic(), metadata.partition(), metadata.offset(), record.timestamp());
            }

            @Override
            public void onError(ProducerRecord<String, byte[]> record, RecordMetadata metadata, Exception ex) {
                log.error("Kafka send FAILED: topic={}, key={}, ts={}, reason={}",
                        record.topic(), record.key(), record.timestamp(), ex.toString(), ex);
            }
        });
        return kt;
    }
}