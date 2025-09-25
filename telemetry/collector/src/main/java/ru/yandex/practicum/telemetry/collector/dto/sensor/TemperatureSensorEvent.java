package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Событие от температурного сенсора.
 * Наследует общие поля SensorEvent (id, hubId, timestamp, type).
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TemperatureSensorEvent extends SensorEvent {
    private int temperature;
    private int temperatureF;
}