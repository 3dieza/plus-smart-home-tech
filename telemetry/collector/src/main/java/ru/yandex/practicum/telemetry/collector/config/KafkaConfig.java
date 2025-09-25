package ru.yandex.practicum.telemetry.collector.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, byte[]> producerFactory(
            @Value("${app.kafka.bootstrap-servers}") String bootstrap) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.ByteArraySerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.LINGER_MS_CONFIG, 5,
                ProducerConfig.BATCH_SIZE_CONFIG, 32768,
                ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> pf) {
        return new KafkaTemplate<>(pf);
    }
}