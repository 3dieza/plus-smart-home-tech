package ru.yandex.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SnapshotAggregationService {

    // hubId -> snapshot
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    /**
     * Обновляет состояние снапшота по событию. Возвращает Optional с новым снапшотом,
     * если он действительно изменился (нужно отправлять в Kafka). Иначе Optional.empty().
     */
    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        final String hubId = event.getHubId();
        final String sensorId = event.getId(); // sensor id из события
        final Instant eventTs = event.getTimestamp();

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, k -> {
            SensorsSnapshotAvro s = new SensorsSnapshotAvro();
            s.setHubId(hubId);
            s.setTimestamp(eventTs);
            s.setSensorsState(new HashMap<>());
            return s;
        });

        Map<String, SensorStateAvro> stateMap = snapshot.getSensorsState();
        SensorStateAvro oldState = stateMap.get(sensorId);

        // Если есть прошлое состояние — проверяем таймштамп и payload
        if (oldState != null) {
            Instant oldTs = oldState.getTimestamp();
            // если старое время >= нового (т.е. событие не новее) — игнорим
            if (!oldTs.isBefore(eventTs)) {
                return Optional.empty();
            }
            // Если payload одинаковый — ничего не меняем
            if (equalPayload(oldState, event)) {
                return Optional.empty();
            }
        }

        // Новые данные: собираем SensorStateAvro из события
        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(eventTs);
        newState.setData(event.getPayload()); // union совместим

        stateMap.put(sensorId, newState);
        snapshot.setTimestamp(eventTs); // «текущий» ts снапшота = ts последнего обновления
        return Optional.of(snapshot);
    }

    private boolean equalPayload(SensorStateAvro oldState, SensorEventAvro event) {
        Object oldPayload = oldState.getData();
        Object newPayload = event.getPayload();
        if (oldPayload == null && newPayload == null) return true;
        if (oldPayload == null || newPayload == null) return false;
        return oldPayload.equals(newPayload);
    }
}