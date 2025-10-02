package ru.yandex.practicum.analyzer.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.collector.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.Message;

@Slf4j
@Service
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendAction(String hubId, String scenarioName, String deviceId, String actionType, Integer value) {
        // 1) Нормализуем и валидируем actionType через enum (бросит IllegalArgumentException при ошибке)
        ActionTypeProto typeEnum = ActionTypeProto.valueOf(actionType); // валидация
        String typeStr = typeEnum.name(); // строка, которую ждёт hub-router

        // 2) Дефолты для булевых команд (оставь те, что вам нужны по контракту)
        Integer v = value;
        if (v == null) {
            if (typeEnum == ActionTypeProto.ACTIVATE)    v = 0; // или 1 — в зависимости от вашего тестового контракта
            else if (typeEnum == ActionTypeProto.DEACTIVATE) v = 1; // или 0 — см. ваш контракт
        }

        Message.DeviceActionProto.Builder action = Message.DeviceActionProto.newBuilder()
                .setDeviceId(deviceId)
                .setType(typeStr);    // <- СТРОКА!
        if (v != null) action.setValue(v);

        Message.DeviceActionRequest req = Message.DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(action.build())
                .setTimestamp(nowTs())
                .build();

        sendAction(req);
    }

    public void sendAction(Message.DeviceActionRequest req) {
        try {
            stub.withDeadlineAfter(2, TimeUnit.SECONDS).handleDeviceAction(req);
        } catch (StatusRuntimeException e) {
            log.warn("HubRouter недоступен: {}", e.getStatus(), e);
        }
    }

    private static Timestamp nowTs() {
        var i = java.time.Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond())
                .setNanos(i.getNano())
                .build();
    }
}