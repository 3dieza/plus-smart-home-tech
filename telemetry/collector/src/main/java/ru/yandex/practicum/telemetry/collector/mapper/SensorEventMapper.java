package ru.yandex.practicum.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.dto.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.TemperatureSensorEvent;

public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent dto) {
        var avro = new SensorEventAvro();
        avro.setId(dto.getId());
        avro.setHubId(dto.getHubId());
        avro.setTimestamp(dto.getTimestamp().toEpochMilli());

        switch (dto.getType()) {
            case LIGHT_SENSOR_EVENT -> {
                var p = new LightSensorAvro();
                p.setLinkQuality(((LightSensorEvent) dto).getLinkQuality());
                p.setLuminosity(((LightSensorEvent) dto).getLuminosity());
                avro.setPayload(p);
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                var t = (TemperatureSensorEvent) dto;
                var p = new TemperatureSensorAvro();
                p.setId(t.getId());
                p.setHubId(t.getHubId());
                p.setTimestamp(t.getTimestamp().toEpochMilli());
                p.setTemperatureC(t.getTemperature());
                p.setTemperatureF((int) Math.round(t.getTemperature() * 9.0 / 5.0 + 32)); // заглушка
                avro.setPayload(p);
            }
            case SWITCH_SENSOR_EVENT -> {
                var p = new SwitchSensorAvro();
                p.setState(((SwitchSensorEvent) dto).isState());
                avro.setPayload(p);
            }
            case CLIMATE_SENSOR_EVENT -> {
                var c = (ClimateSensorEvent) dto;
                var p = new ClimateSensorAvro();
                p.setTemperatureC(c.getTemperature());
                p.setHumidity(c.getHumidity());
                p.setCo2Level(c.getCo2());
                avro.setPayload(p);
            }
            case MOTION_SENSOR_EVENT -> {
                var m = (MotionSensorEvent) dto;
                var p = new MotionSensorAvro();
                p.setLinkQuality(m.getLinkQuality());
                p.setMotion(m.isMotionDetected());
                p.setVoltage(0);
                avro.setPayload(p);
            }
        }
        return avro;
    }
}