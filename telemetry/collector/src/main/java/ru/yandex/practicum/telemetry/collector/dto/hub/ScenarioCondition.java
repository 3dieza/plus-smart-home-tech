package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScenarioCondition {

    @NotBlank
    private String sensorId;

    @NotBlank
    private String type;

    @NotBlank
    private String operation;

    private Integer value;
}