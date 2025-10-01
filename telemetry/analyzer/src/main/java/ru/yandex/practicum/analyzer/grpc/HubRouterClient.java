package ru.yandex.practicum.analyzer.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.Message;

@Slf4j
@Service
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendAction(String hubId, String scenarioName, String deviceId, String actionType, Integer value) {
        String type = normalizeType(actionType);

        Message.DeviceActionProto.Builder action = Message.DeviceActionProto.newBuilder()
                .setDeviceId(deviceId)
                .setType(type);

        // ВАЖНО: всегда задаём value, чтобы хаб не подставил включение по умолчанию
        switch (type) {
            case "SET_LEVEL" -> {
                if (value == null) {
                    throw new IllegalArgumentException("SET_LEVEL requires non-null value");
                }
                action.setValue(value);
            }
            case "DEACTIVATE" -> action.setValue(0);
            case "ACTIVATE" -> action.setValue(1);
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        }

        Message.DeviceActionRequest req = Message.DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(action.build())
                .setTimestamp(nowTs())
                .build();

        sendActionWithRetry(req);
    }

    private static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private void sendActionWithRetry(Message.DeviceActionRequest req) {
        int attempts = 0;
        long backoffMs = 200;
        while (true) {
            try {
                stub.withDeadlineAfter(2, TimeUnit.SECONDS).handleDeviceAction(req);
                return;
            } catch (StatusRuntimeException e) {
                attempts++;
                // Мягко ждём, пока поднимется hub-router из тестового раннера
                if (e.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE && attempts < 8) {
                    log.warn("HubRouter недоступен ({}). Ретрай #{}...", e.getStatus(), attempts);
                    try {
                        TimeUnit.MILLISECONDS.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    backoffMs = Math.min(backoffMs * 2, 2000);
                    continue;
                }
                log.warn("Ошибка при отправке команды в HubRouter: {}", e.getStatus(), e);
                return;
            }
        }
    }

    private static Timestamp nowTs() {
        var i = java.time.Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond()).setNanos(i.getNano()).build();
    }
}