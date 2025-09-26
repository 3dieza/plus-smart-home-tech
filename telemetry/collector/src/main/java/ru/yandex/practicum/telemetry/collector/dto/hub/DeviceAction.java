package ru.yandex.practicum.telemetry.collector.dto.hub;

import lombok.Data;

@Data
public class DeviceAction {
    private String sensorId;
    private String type;
    private Integer value;
}