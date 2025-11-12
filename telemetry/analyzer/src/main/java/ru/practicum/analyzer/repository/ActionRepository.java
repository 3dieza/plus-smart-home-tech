package ru.practicum.analyzer.repository;

import ru.practicum.analyzer.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Long> {
}