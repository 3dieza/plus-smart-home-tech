package ru.practicum.analyzer.repository;

import ru.practicum.analyzer.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, String> {
}
