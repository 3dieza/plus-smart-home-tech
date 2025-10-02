package ru.yandex.practicum.analyzer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;

public interface ScenarioRepository extends JpaRepository<ScenarioEntity, Long> {

    @EntityGraph(attributePaths = {
            "conditionLinks", "conditionLinks.condition", "conditionLinks.sensor",
            "actionLinks",    "actionLinks.action",       "actionLinks.sensor"
    })
    List<ScenarioEntity> findByHubId(String hubId);

    Optional<ScenarioEntity> findByHubIdAndName(String hubId, String name);
}