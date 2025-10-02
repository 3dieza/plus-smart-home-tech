package ru.yandex.practicum.analyzer.config;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.serializationcore.kafka.avro.HubEventDeserializer;
import ru.yandex.practicum.serializationcore.kafka.avro.SensorsSnapshotDeserializer;

@Configuration
public class KafkaConfig {

    @Bean(name = "snapshotsConsumer")
    public KafkaConsumer<String, SensorsSnapshotAvro>
    snapshotsConsumer(@Value("${kafka.bootstrap-servers}") String bs,
                      @Value("${kafka.groups.snapshots}") String group) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bs);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getName());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return new KafkaConsumer<>(p);
    }

    @Bean(name = "hubEventsConsumer")
    public KafkaConsumer<String, HubEventAvro>
    hubEventsConsumer(@Value("${kafka.bootstrap-servers}") String bs,
                      @Value("${kafka.groups.hubs}") String group) {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bs);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return new KafkaConsumer<>(p);
    }
}