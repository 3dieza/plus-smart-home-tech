package ru.yandex.practicum.aggregator.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aggregator.kafka")
public class AggregatorKafkaProperties {
    private String bootstrapServers;
    private String consumerGroupId;
    private String inTopic;
    private String outTopic;

    private int maxPollRecords;
    private int maxPollIntervalMs;
    private int fetchMaxBytes;
    private int maxPartitionFetchBytes;
    private int pollTimeoutMs;
}