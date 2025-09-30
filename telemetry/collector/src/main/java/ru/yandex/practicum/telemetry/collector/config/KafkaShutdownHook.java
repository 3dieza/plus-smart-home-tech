package ru.yandex.practicum.telemetry.collector.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaShutdownHook {

    private static final Logger log = LoggerFactory.getLogger(KafkaShutdownHook.class);

    @PreDestroy
    public void onShutdown() {
        log.info("🛑 Shutting down application — Kafka producers will be closed by Spring");
    }
}