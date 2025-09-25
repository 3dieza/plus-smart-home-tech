package ru.yandex.practicum.telemetry.collector.dto.hub;

import lombok.Data;

@Data
public class ScenarioCondition {
    private String sensorId;
    private String type;       // MOTION/LUMINOSITY/...
    private String operation;  // EQUALS/GREATER_THAN/LOWER_THAN
    private Boolean valueBool; // для булевых условий
    private Integer valueInt;  // для числовых условий
}