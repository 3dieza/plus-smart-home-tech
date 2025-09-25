package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    private int temperature; // °C
    private int humidity;
    private int co2;
}