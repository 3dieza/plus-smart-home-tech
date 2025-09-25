package ru.yandex.practicum.telemetry.collector.dto.hub;

import lombok.Data;

/** Действие сценария. value используется при SET_VALUE. */
@Data
public class DeviceAction {
    private String sensorId;
    private String type;   // ACTIVATE/DEACTIVATE/INVERSE/SET_VALUE
    private Integer value; // optional
}