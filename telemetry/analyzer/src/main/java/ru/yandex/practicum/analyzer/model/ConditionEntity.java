package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "condition")
@Getter
@Setter
public class ConditionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false)
    private ScenarioEntity scenario;

    @Column(nullable = false)
    private String sensorId;
    @Column(nullable = false)
    private String type;        // MOTION/LUMINOSITY/SWITCH/TEMPERATURE/CO2LEVEL/HUMIDITY
    @Column(nullable = false)
    private String operation;   // EQUALS/GREATER_THAN/LOWER_THAN
    @Column
    private Integer intValue;
    @Column
    private Boolean boolValue;  // для bool (motion/switch)
}