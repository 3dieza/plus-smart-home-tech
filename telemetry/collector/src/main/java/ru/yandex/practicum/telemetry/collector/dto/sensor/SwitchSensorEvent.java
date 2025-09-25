package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {
    private boolean state;
}