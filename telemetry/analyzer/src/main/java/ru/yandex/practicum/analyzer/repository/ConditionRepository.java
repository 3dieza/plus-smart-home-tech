package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.analyzer.model.ConditionEntity;

public interface ConditionRepository extends JpaRepository<ConditionEntity, Long> {
}
