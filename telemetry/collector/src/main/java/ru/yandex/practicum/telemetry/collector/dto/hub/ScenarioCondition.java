package ru.yandex.practicum.telemetry.collector.dto.hub;

import lombok.Data;

@Data
public class ScenarioCondition {
    private String sensorId;
    private String type;
    private String operation;
    private Integer value;
}