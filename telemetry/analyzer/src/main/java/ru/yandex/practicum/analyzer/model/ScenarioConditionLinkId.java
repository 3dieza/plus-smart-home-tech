package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// PK для scenario_conditions
@Embeddable
@Data // включает equals/hashCode/toString
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioConditionLinkId implements Serializable {
    @Column(name = "scenario_id")
    private Long scenarioId;
    @Column(name = "sensor_id")
    private String sensorId;
    @Column(name = "condition_id")
    private Long conditionId;
}
