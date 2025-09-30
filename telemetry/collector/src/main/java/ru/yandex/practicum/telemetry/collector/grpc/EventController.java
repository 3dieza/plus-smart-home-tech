package ru.yandex.practicum.telemetry.collector.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorResponse;
import ru.yandex.practicum.grpc.telemetry.collector.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.collector.SensorEventProto;

@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    @Override
    public void collectSensorEvent(SensorEventProto request,
                                   StreamObserver<CollectorResponse> responseObserver) {
        try {
            log.info("Sensor from hub={} id={} type={}",
                    request.getHubId(), request.getId(), request.getPayloadCase());

            switch (request.getPayloadCase()) {
                case MOTION_SENSOR_EVENT -> {
                    var m = request.getMotionSensorEvent();
                    log.info("motion: linkQuality={}, motion={}, voltage={}",
                            m.getLinkQuality(), m.getMotion(), m.getVoltage());
                }
                case TEMPERATURE_SENSOR_EVENT -> {
                    var t = request.getTemperatureSensorEvent();
                    log.info("temp: C={}, F={}", t.getTemperatureC(), t.getTemperatureF());
                }
                case LIGHT_SENSOR_EVENT -> {
                    var l = request.getLightSensorEvent();
                    log.info("light: LUX={}, LQ={}", l.getLuminosity(), l.getLinkQuality());
                }
                case CLIMATE_SENSOR_EVENT -> {
                    var c = request.getClimateSensorEvent();
                    log.info("climate: C={}, humidity={}, co2={}",
                            c.getTemperatureC(), c.getHumidity(), c.getCo2Level());
                }
                case SWITCH_SENSOR_EVENT -> {
                    var s = request.getSwitchSensorEvent();
                    log.info("switch: state={}", s.getState());
                }
                case PAYLOAD_NOT_SET -> log.warn("payload is empty");
            }

            var resp = CollectorResponse.newBuilder()
                    .setStatus(CollectorResponse.Status.OK)
                    .setMessage("accepted: " + request.getId())
                    .setProcessedAt(nowTs())               // ← заполнить timestamp
                    .build();

            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request,
                                StreamObserver<CollectorResponse> responseObserver) {
        log.info("Hub event hub_id={} type={}", request.getHubId(), request.getPayloadCase());

        var resp = CollectorResponse.newBuilder()
                .setStatus(CollectorResponse.Status.OK)
                .setMessage("hub event accepted: " + request.getHubId())
                .setProcessedAt(nowTs())                   // ← заполнить timestamp
                .build();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    private static com.google.protobuf.Timestamp nowTs() {
        var t = java.time.Instant.now();
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(t.getEpochSecond())
                .setNanos(t.getNano())
                .build();
    }
}