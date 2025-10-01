package ru.yandex.practicum.analyzer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.SensorEntity;

public interface SensorRepository extends JpaRepository<SensorEntity, String> {
    Optional<SensorEntity> findByIdAndHubId(String id, String hubId);
}