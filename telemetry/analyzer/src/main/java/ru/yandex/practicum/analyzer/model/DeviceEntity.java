package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device")
@Getter
@Setter
public class DeviceEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String hubId;
    @Column(nullable = false)
    private String type; // MOTION_SENSOR
}