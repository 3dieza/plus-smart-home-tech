package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scenario_actions")
@Getter
@Setter
public class ScenarioActionLink {

    @EmbeddedId
    private ScenarioActionLinkId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    private ScenarioEntity scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    private SensorEntity sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
    @JoinColumn(name = "action_id")
    private ActionEntity action;
}

@Embeddable
class ScenarioActionLinkId implements Serializable {
    @Column(name = "scenario_id")
    private Long scenarioId;
    @Column(name = "sensor_id")
    private String sensorId;
    @Column(name = "action_id")
    private Long actionId;

}