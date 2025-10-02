package ru.yandex.practicum.hubrouter.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.Message;

/**
 * gRPC-сервер HubRouter: принимает команды устройств от Analyzer.
 * RPC: handleDeviceAction(DeviceActionRequest) -> Empty
 */
@Slf4j
@GrpcService
public class HubRouterControllerService extends HubRouterControllerGrpc.HubRouterControllerImplBase {

    @Override
    public void handleDeviceAction(Message.DeviceActionRequest request,
                                   StreamObserver<Empty> responseObserver) {

        final String hubId = request.getHubId();
        final String scenario = request.getScenarioName();
        final Message.DeviceActionProto action = request.getAction();

        // Пока просто логируем входящее действие
        log.info(
                "HubRouter: received action -> hubId={}, scenario='{}', deviceId={}, type={}, value={}, ts={}",
                hubId,
                scenario,
                action.getDeviceId(),
                action.getType(),
                action.getValue(),
                request.getTimestamp()
        );

        // TODO: здесь — вызов драйвера / шины устройств, MQTT, Zigbee и т.п.

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}