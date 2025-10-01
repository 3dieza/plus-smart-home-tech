package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data // включает equals/hashCode/toString
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioActionLinkId implements Serializable {
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "action_id")
    private Long actionId;
}
