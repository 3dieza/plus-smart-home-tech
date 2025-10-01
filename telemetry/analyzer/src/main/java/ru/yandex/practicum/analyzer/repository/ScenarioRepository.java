package ru.yandex.practicum.analyzer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;

public interface ScenarioRepository extends JpaRepository<ScenarioEntity, Long> {

    @Query("""
            select distinct s
            from ScenarioEntity s
            left join fetch s.conditions
            left join fetch s.actions
            where s.hubId = :hubId
            """)
    List<ScenarioEntity> findWithDetailsByHubId(@Param("hubId") String hubId);

    List<ScenarioEntity> findByHubId(String hubId);
    Optional<ScenarioEntity> findByHubIdAndName(String hubId, String name);
}