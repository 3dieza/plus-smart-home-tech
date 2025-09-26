package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    private int  linkQuality;

    private boolean motion;

    private int  voltage;
}