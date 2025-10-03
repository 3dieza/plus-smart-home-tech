package ru.practicum.analyzer.repository;

import ru.practicum.analyzer.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findAllByHubId(String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);
}
