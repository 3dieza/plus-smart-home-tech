package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    @NotBlank private String name;
}