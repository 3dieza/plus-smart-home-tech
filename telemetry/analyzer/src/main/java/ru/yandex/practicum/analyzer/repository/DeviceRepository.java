package ru.yandex.practicum.analyzer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.DeviceEntity;

public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {
    Optional<DeviceEntity> findByIdAndHubId(String id, String hubId);
}