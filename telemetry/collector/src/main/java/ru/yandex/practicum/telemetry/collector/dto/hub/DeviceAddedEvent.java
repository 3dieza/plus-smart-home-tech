package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    @NotBlank private String id;
    @NotNull  private DeviceType deviceType;
}