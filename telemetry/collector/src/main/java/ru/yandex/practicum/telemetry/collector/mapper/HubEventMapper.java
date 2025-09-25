package ru.yandex.practicum.telemetry.collector.mapper;

import java.util.ArrayList;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.dto.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.ScenarioRemovedEvent;

public class HubEventMapper {

    public static HubEventAvro toAvro(HubEvent dto) {
        var av = new HubEventAvro();
        av.setHubId(dto.getHubId());
        av.setTimestamp(dto.getTimestamp().toEpochMilli());

        switch (dto.getType()) {
            case DEVICE_ADDED -> {
                var e = (DeviceAddedEvent) dto;
                var p = new DeviceAddedEventAvro();
                p.setId(e.getId());
                p.setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()));
                av.setPayload(p);
            }
            case DEVICE_REMOVED -> {
                var e = (DeviceRemovedEvent) dto;
                var p = new DeviceRemovedEventAvro();
                p.setId(e.getId());
                av.setPayload(p);
            }
            case SCENARIO_ADDED -> {
                var e = (ScenarioAddedEvent) dto;
                var p = new ScenarioAddedEventAvro();
                p.setName(e.getName());

                var conds = new ArrayList<ScenarioConditionAvro>();
                for (var c : e.getConditions()) {
                    var ac = new ScenarioConditionAvro();
                    ac.setSensorId(c.getSensorId());
                    ac.setType(ConditionTypeAvro.valueOf(c.getType()));
                    ac.setOperation(ConditionOperationAvro.valueOf(c.getOperation()));
                    if (c.getValueBool() != null) ac.setValue(c.getValueBool());
                    else if (c.getValueInt() != null) ac.setValue(c.getValueInt());
                    else ac.setValue(null);
                    conds.add(ac);
                }
                p.setConditions(conds);

                var acts = new ArrayList<DeviceActionAvro>();
                for (var a : e.getActions()) {
                    var aa = new DeviceActionAvro();
                    aa.setSensorId(a.getSensorId());
                    aa.setType(ActionTypeAvro.valueOf(a.getType()));
                    aa.setValue(a.getValue() != null ? a.getValue() : null);
                    acts.add(aa);
                }
                p.setActions(acts);

                av.setPayload(p);
            }
            case SCENARIO_REMOVED -> {
                var e = (ScenarioRemovedEvent) dto;
                var p = new ScenarioRemovedEventAvro();
                p.setName(e.getName());
                av.setPayload(p);
            }
        }
        return av;
    }
}