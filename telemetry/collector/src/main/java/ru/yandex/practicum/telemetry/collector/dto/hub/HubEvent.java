package ru.yandex.practicum.telemetry.collector.dto.hub;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use      = JsonTypeInfo.Id.NAME,
        include  = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible  = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedEvent.class,     name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedEvent.class,   name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedEvent.class,   name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemovedEvent.class, name = "SCENARIO_REMOVED")
})
@Getter @Setter @ToString
public abstract class HubEvent {
    @NotBlank private String hubId;
    @NotNull  private Instant timestamp;
    @NotNull  private HubEventType type;
}