package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LightSensorEvent extends SensorEvent {
    private int linkQuality;
    private int luminosity;
}