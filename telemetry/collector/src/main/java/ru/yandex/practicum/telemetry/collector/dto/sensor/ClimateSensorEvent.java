package ru.yandex.practicum.telemetry.collector.dto.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {

    @JsonProperty("temperatureC")
    private int temperature_c;

    private int humidity;

    @JsonProperty("co2Level")
    private int co2_level;
}