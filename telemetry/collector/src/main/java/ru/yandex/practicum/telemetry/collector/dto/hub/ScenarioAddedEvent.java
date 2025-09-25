package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    @NotBlank private String name;
    @NotEmpty private List<ScenarioCondition> conditions;
    @NotEmpty private List<DeviceAction> actions;
}