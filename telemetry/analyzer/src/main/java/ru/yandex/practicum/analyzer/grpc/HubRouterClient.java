package ru.yandex.practicum.analyzer.grpc;

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
        Message.DeviceActionProto.Builder action = Message.DeviceActionProto.newBuilder()
                .setDeviceId(deviceId)
                .setType(actionType);
        if (value != null) action.setValue(value);

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

    private static com.google.protobuf.Timestamp nowTs() {
        var i = java.time.Instant.now();
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond()).setNanos(i.getNano()).build();
    }
}