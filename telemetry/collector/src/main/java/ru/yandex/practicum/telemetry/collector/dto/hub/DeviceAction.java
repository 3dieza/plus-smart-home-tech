package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceAction {

    @NotBlank
    private String sensorId;

    @NotBlank
    private String type;

    private Integer value;
}