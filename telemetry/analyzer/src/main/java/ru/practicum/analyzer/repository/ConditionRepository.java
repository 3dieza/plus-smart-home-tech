package ru.practicum.analyzer.repository;

import ru.practicum.analyzer.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}
