package ru.yandex.practicum.analyzer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;

public interface ScenarioRepository extends JpaRepository<ScenarioEntity, Long> {

    Optional<ScenarioEntity> findByHubIdAndName(String hubId, String name);

    @EntityGraph(attributePaths = {"conditions"})
    List<ScenarioEntity> findAllWithConditionsByHubId(String hubId);

    @EntityGraph(attributePaths = {"actions"})
    List<ScenarioEntity> findAllWithActionsByHubId(String hubId);
}