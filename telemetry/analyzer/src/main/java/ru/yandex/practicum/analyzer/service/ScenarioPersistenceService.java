package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.ActionEntity;
import ru.yandex.practicum.analyzer.model.enums.ActionType;
import ru.yandex.practicum.analyzer.model.ConditionEntity;
import ru.yandex.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.analyzer.model.enums.Operation;
import ru.yandex.practicum.analyzer.model.ScenarioActionLink;
import ru.yandex.practicum.analyzer.model.ScenarioActionLinkId;
import ru.yandex.practicum.analyzer.model.ScenarioConditionLink;
import ru.yandex.practicum.analyzer.model.ScenarioConditionLinkId;
import ru.yandex.practicum.analyzer.model.ScenarioEntity;
import ru.yandex.practicum.analyzer.model.SensorEntity;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioPersistenceService {

    private final SensorRepository sensorRepo;
    private final ScenarioRepository scenarioRepo;
    private final ConditionRepository conditionRepo;
    private final ActionRepository actionRepo;

    @Transactional
    public void upsertScenario(String hubId, ScenarioAddedEventAvro sa) {
        log.info("⚙️ Обновление сценария: hubId={}, name={}", hubId, sa.getName());
        // найти или создать "скелет" сценария
        var sc = scenarioRepo.findByHubIdAndName(hubId, sa.getName())
                .orElseGet(() -> {
                    var s = new ScenarioEntity();
                    s.setHubId(hubId);
                    s.setName(sa.getName());
                    return s;
                });

        // если новый — сначала сохранить, чтобы получить sc.id
        if (sc.getId() == null) {
            sc = scenarioRepo.saveAndFlush(sc);
        }

        // подчистить старые линки через orphanRemoval
        sc.getConditionLinks().clear();
        sc.getActionLinks().clear();
        scenarioRepo.flush();

        // ----- CONDITIONS -----
        for (var c : sa.getConditions()) {
            var ce = new ConditionEntity();
            ce.setType(ConditionType.valueOf(c.getType().name()));
            ce.setOperation(Operation.valueOf(c.getOperation().name()));
            Object v = c.getValue();
            if (v instanceof Integer i) ce.setValue(i);
            else if (v instanceof Boolean b) ce.setValue(b ? 1 : 0);
            ce = conditionRepo.save(ce);

            var sensor = sensorRepo.findByIdAndHubId(c.getSensorId(), hubId)
                    .orElseGet(() -> {
                        var s = new SensorEntity();
                        s.setId(c.getSensorId());
                        s.setHubId(hubId);
                        return sensorRepo.save(s);
                    });

            var link = new ScenarioConditionLink();
            link.setId(new ScenarioConditionLinkId());
            link.setScenario(sc);
            link.setSensor(sensor);
            link.setCondition(ce);

            sc.getConditionLinks().add(link);
        }

// ----- ACTIONS -----
        for (var a : sa.getActions()) {
            var ae = new ActionEntity();
            ae.setType(ActionType.valueOf(a.getType().name()));

            Integer rawValue = (a.getValue() instanceof Integer i) ? i : null;

            // дефолты прямо тут
            Integer v = rawValue;
            if (v == null) {
                if (ae.getType() == ActionType.ACTIVATE)      v = 1;
                else if (ae.getType() == ActionType.DEACTIVATE) v = 0;
            }

            log.info("➡️ Action from SCENARIO_ADDED: sensorId={}, type={}, rawValue={}, resolvedValue={}",
                    a.getSensorId(), ae.getType(), rawValue, v);

            ae.setValue(v);
            ae = actionRepo.save(ae);

            var sensor = sensorRepo.findByIdAndHubId(a.getSensorId(), hubId)
                    .orElseGet(() -> {
                        var s = new SensorEntity();
                        s.setId(a.getSensorId());
                        s.setHubId(hubId);
                        return sensorRepo.save(s);
                    });

            var link = new ScenarioActionLink();
            link.setId(new ScenarioActionLinkId());
            link.setScenario(sc);
            link.setSensor(sensor);
            link.setAction(ae);

            sc.getActionLinks().add(link);
        }

        // финальное сохранение (каскад для линков)
        scenarioRepo.save(sc);
    }
}