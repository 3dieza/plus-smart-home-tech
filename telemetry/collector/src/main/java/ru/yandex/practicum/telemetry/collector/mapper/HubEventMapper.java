package ru.yandex.practicum.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.dto.hub.*;

import java.util.List;
import java.util.Objects;

public final class HubEventMapper {

    private HubEventMapper() {}

    public static HubEventAvro toAvro(HubEvent dto) {
        Objects.requireNonNull(dto, "dto is null");

        var av = new HubEventAvro();
        av.setHubId(dto.getHubId());
        av.setTimestamp(dto.getTimestamp());

        switch (dto.getType()) {
            case DEVICE_ADDED -> av.setPayload(deviceAdded((DeviceAddedEvent) dto));
            case DEVICE_REMOVED -> av.setPayload(deviceRemoved((DeviceRemovedEvent) dto));
            case SCENARIO_ADDED -> av.setPayload(scenarioAdded((ScenarioAddedEvent) dto));
            case SCENARIO_REMOVED -> av.setPayload(scenarioRemoved((ScenarioRemovedEvent) dto));
            default -> throw new IllegalArgumentException("Unsupported event type: " + dto.getType());
        }
        return av;
    }

    private static DeviceAddedEventAvro deviceAdded(DeviceAddedEvent e) {
        var p = new DeviceAddedEventAvro();
        p.setId(e.getId());
        p.setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()));
        return p;
    }

    private static DeviceRemovedEventAvro deviceRemoved(DeviceRemovedEvent e) {
        var p = new DeviceRemovedEventAvro();
        p.setId(e.getId());
        return p;
    }

    private static ScenarioAddedEventAvro scenarioAdded(ScenarioAddedEvent e) {
        var p = new ScenarioAddedEventAvro();
        p.setName(e.getName());
        p.setConditions(e.getConditions() == null ? List.of()
                : e.getConditions().stream().map(HubEventMapper::condition).toList());
        p.setActions(e.getActions() == null ? List.of()
                : e.getActions().stream().map(HubEventMapper::action).toList());
        return p;
    }

    private static ScenarioConditionAvro condition(ScenarioCondition src) {
        var c = new ScenarioConditionAvro();
        c.setSensorId(src.getSensorId());
        c.setType(ConditionTypeAvro.valueOf(src.getType()));
        c.setOperation(ConditionOperationAvro.valueOf(src.getOperation()));
        c.setValue(src.getValue());
        return c;
    }

    private static DeviceActionAvro action(DeviceAction src) {
        var a = new DeviceActionAvro();
        a.setSensorId(src.getSensorId());
        a.setType(ActionTypeAvro.valueOf(src.getType()));
        a.setValue(src.getValue());
        return a;
    }

    private static ScenarioRemovedEventAvro scenarioRemoved(ScenarioRemovedEvent e) {
        var p = new ScenarioRemovedEventAvro();
        p.setName(e.getName());
        return p;
    }
}