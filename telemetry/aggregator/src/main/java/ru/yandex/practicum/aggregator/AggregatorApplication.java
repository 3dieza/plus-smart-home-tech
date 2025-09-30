package ru.yandex.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

import ru.yandex.practicum.aggregator.runtime.AggregationStarter;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregatorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(AggregatorApplication.class, args);
        AggregationStarter starter = ctx.getBean(AggregationStarter.class);
        starter.start(); // запускаем наш «двигатель» poll-loop
    }
}