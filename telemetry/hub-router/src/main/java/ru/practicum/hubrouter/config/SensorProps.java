package ru.practicum.hubrouter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sensor")
public class SensorProps {

    private String hubId;

    private List<Motion> motionSensors;
    private List<Temp>   temperatureSensors;
    private List<Light>  lightSensors;
    private List<Climate> climateSensors;
    private List<Sw>     switchSensors;

    // ===== nested =====

    public static class Range {
        private int minValue;
        private int maxValue;

        public int getMinValue() { return minValue; }
        public void setMinValue(int minValue) { this.minValue = minValue; }
        public int getMaxValue() { return maxValue; }
        public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
    }

    public static class Motion {
        private String id;
        private Range linkQuality;
        private Range voltage;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Range getLinkQuality() { return linkQuality; }
        public void setLinkQuality(Range linkQuality) { this.linkQuality = linkQuality; }
        public Range getVoltage() { return voltage; }
        public void setVoltage(Range voltage) { this.voltage = voltage; }
    }

    public static class Temp {
        private String id;
        private Range temperature;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Range getTemperature() { return temperature; }
        public void setTemperature(Range temperature) { this.temperature = temperature; }
    }

    public static class Light {
        private String id;
        private Range luminosity;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Range getLuminosity() { return luminosity; }
        public void setLuminosity(Range luminosity) { this.luminosity = luminosity; }
    }

    public static class Climate {
        private String id;
        private Range temperature;
        private Range humidity;
        private Range co2Level;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Range getTemperature() { return temperature; }
        public void setTemperature(Range temperature) { this.temperature = temperature; }
        public Range getHumidity() { return humidity; }
        public void setHumidity(Range humidity) { this.humidity = humidity; }
        public Range getCo2Level() { return co2Level; }
        public void setCo2Level(Range co2Level) { this.co2Level = co2Level; }
    }

    public static class Sw {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    // ===== getters/setters for root =====

    public String getHubId() { return hubId; }
    public void setHubId(String hubId) { this.hubId = hubId; }

    public List<Motion> getMotionSensors() { return motionSensors; }
    public void setMotionSensors(List<Motion> motionSensors) { this.motionSensors = motionSensors; }

    public List<Temp> getTemperatureSensors() { return temperatureSensors; }
    public void setTemperatureSensors(List<Temp> temperatureSensors) { this.temperatureSensors = temperatureSensors; }

    public List<Light> getLightSensors() { return lightSensors; }
    public void setLightSensors(List<Light> lightSensors) { this.lightSensors = lightSensors; }

    public List<Climate> getClimateSensors() { return climateSensors; }
    public void setClimateSensors(List<Climate> climateSensors) { this.climateSensors = climateSensors; }

    public List<Sw> getSwitchSensors() { return switchSensors; }
    public void setSwitchSensors(List<Sw> switchSensors) { this.switchSensors = switchSensors; }
}
