package ru.yandex.practicum.analyzer.runtime;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;

@Service
@RequiredArgsConstructor
public class ScenarioLoader {
    private final ScenarioRepository scenarioRepo;

    @Transactional(readOnly = true)
    public List<ScenarioEntity> loadForHub(String hubId) {
        // грузим сразу весь граф: сценарий -> линки -> (sensor, condition/action)
        return scenarioRepo.findByHubId(hubId);
    }
}