package ru.practicum.analyzer.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScenarioConditionId implements Serializable {
    @NotNull
    Long scenarioId;

    @NotNull
    String sensorId;

    @NotNull
    Long conditionId;
}
