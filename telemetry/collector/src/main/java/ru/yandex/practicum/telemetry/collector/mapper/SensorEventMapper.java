package ru.yandex.practicum.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.dto.sensor.*;

public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent dto) {
        var av = new SensorEventAvro();
        av.setId(dto.getId());
        av.setHubId(dto.getHubId());
        av.setTimestamp(dto.getTimestamp());

        switch (dto.getType()) {
            case LIGHT_SENSOR_EVENT -> {
                var s = (LightSensorEvent) dto;
                var p = new LightSensorAvro();
                p.setLinkQuality(s.getLinkQuality());
                p.setLuminosity(s.getLuminosity());
                av.setPayload(p);
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                var t = (TemperatureSensorEvent) dto;
                var p = new TemperatureSensorAvro();
                p.setId(t.getId());
                p.setHubId(t.getHubId());
                p.setTimestamp(t.getTimestamp());
                p.setTemperatureC(t.getTemperature_c());
                p.setTemperatureF(t.getTemperature_f());
                av.setPayload(p);
            }
            case SWITCH_SENSOR_EVENT -> {
                var s = (SwitchSensorEvent) dto;
                var p = new SwitchSensorAvro();
                p.setState(s.isState());
                av.setPayload(p);
            }
            case CLIMATE_SENSOR_EVENT -> {
                var c = (ClimateSensorEvent) dto;
                var p = new ClimateSensorAvro();
                p.setTemperatureC(c.getTemperature_c());
                p.setHumidity(c.getHumidity());
                p.setCo2Level(c.getCo2_level());
                av.setPayload(p);
            }
            case MOTION_SENSOR_EVENT -> {
                var m = (MotionSensorEvent) dto;
                var p = new MotionSensorAvro();
                p.setLinkQuality(m.getLinkQuality());
                p.setMotion(m.isMotion());
                p.setVoltage(m.getVoltage());
                av.setPayload(p);
            }
        }
        return av;
    }
}