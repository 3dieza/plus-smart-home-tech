package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    private int  linkQuality;
    private boolean motionDetected;
    private int  voltage;
}