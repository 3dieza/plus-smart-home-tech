package ru.yandex.practicum.hubrouter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sensor")
public class SensorProps {

    private String hubId;

    private List<Motion> motionSensors;
    private List<Temp>   temperatureSensors;
    private List<Light>  lightSensors;
    private List<Climate> climateSensors;
    private List<Sw>     switchSensors;

    @Data
    public static class Range {
        private int minValue;
        private int maxValue;
    }

    @Data
    public static class Motion {
        private String id;
        private Range linkQuality;
        private Range voltage;
    }

    @Data
    public static class Temp {
        private String id;
        private Range temperature;
    }

    @Data
    public static class Light {
        private String id;
        private Range luminosity;
    }

    @Data
    public static class Climate {
        private String id;
        private Range temperature;
        private Range humidity;
        private Range co2Level;
    }

    @Data
    public static class Sw {
        private String id;
    }
}