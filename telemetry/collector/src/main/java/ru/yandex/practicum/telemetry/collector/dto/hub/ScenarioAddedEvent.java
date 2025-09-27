package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {

    @NotBlank
    private String name;

    @Valid
    @NotEmpty
    private List<ScenarioCondition> conditions;

    @Valid
    @NotEmpty
    private List<DeviceAction> actions;
}