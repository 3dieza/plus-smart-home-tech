package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {

    private boolean state;
}