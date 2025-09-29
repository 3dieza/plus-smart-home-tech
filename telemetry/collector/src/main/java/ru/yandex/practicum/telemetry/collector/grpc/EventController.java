package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorResponse;
import ru.yandex.practicum.grpc.telemetry.collector.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.collector.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.avro.AvroBytes;

@GrpcService
@RequiredArgsConstructor
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final KafkaTemplate<String, byte[]> kafka;

    @Value("${app.kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${app.kafka.topics.hubs}")
    private String hubsTopic;

    @Override
    public void collectSensorEvent(SensorEventProto req,
                                   StreamObserver<CollectorResponse> responseObserver) {
        try {
            var avro = mapSensorProtoToAvro(req);
            var bytes = AvroBytes.toBytes(avro);

            var ts = toInstant(req.getTimestamp());
            var record = new ProducerRecord<>(
                    sensorsTopic,
                    null,
                    ts.toEpochMilli(),
                    req.getHubId(),    // key = hubId
                    bytes
            );

            kafka.send(record);

            var resp = ok("accepted: " + req.getId());
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(status(e));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto req,
                                StreamObserver<CollectorResponse> responseObserver) {
        try {
            var avro = mapHubProtoToAvro(req);
            var bytes = AvroBytes.toBytes(avro);

            var ts = toInstant(req.getTimestamp());
            var record = new ProducerRecord<>(
                    hubsTopic,
                    null,
                    ts.toEpochMilli(),
                    req.getHubId(),   // key = hubId
                    bytes
            );

            kafka.send(record);

            var resp = ok("hub event accepted: " + req.getHubId());
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(status(e));
        }
    }

    // ===== helpers =====

    private static CollectorResponse ok(String msg) {
        return CollectorResponse.newBuilder()
                .setStatus(CollectorResponse.Status.OK)
                .setMessage(msg)
                .setProcessedAt(nowTs())
                .build();
    }

    private static StatusRuntimeException status(Exception e) {
        return new StatusRuntimeException(Status.INTERNAL.withDescription(e.getMessage()).withCause(e));
    }

    private static Timestamp nowTs() {
        var t = java.time.Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(t.getEpochSecond())
                .setNanos(t.getNano())
                .build();
    }

    private static Instant toInstant(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    // === proto -> Avro маппинг ===

    private static SensorEventAvro mapSensorProtoToAvro(SensorEventProto src) {
        var av = new SensorEventAvro();
        av.setId(src.getId());
        av.setHubId(src.getHubId());
        av.setTimestamp(toInstant(src.getTimestamp()));

        switch (src.getPayloadCase()) {
            case LIGHT_SENSOR_EVENT -> {
                var p = new LightSensorAvro();
                p.setLinkQuality(src.getLightSensorEvent().getLinkQuality());
                p.setLuminosity(src.getLightSensorEvent().getLuminosity());
                av.setPayload(p);
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                var t = src.getTemperatureSensorEvent();
                var p = new TemperatureSensorAvro();
                // в нашей Avro-схеме TemperatureSensorAvro содержит эти поля
                p.setId(src.getId());
                p.setHubId(src.getHubId());
                p.setTimestamp(toInstant(src.getTimestamp()));
                p.setTemperatureC(t.getTemperatureC());
                p.setTemperatureF(t.getTemperatureF());
                av.setPayload(p);
            }
            case SWITCH_SENSOR_EVENT -> {
                var p = new SwitchSensorAvro();
                p.setState(src.getSwitchSensorEvent().getState());
                av.setPayload(p);
            }
            case CLIMATE_SENSOR_EVENT -> {
                var c = src.getClimateSensorEvent();
                var p = new ClimateSensorAvro();
                p.setTemperatureC(c.getTemperatureC());
                p.setHumidity(c.getHumidity());
                p.setCo2Level(c.getCo2Level());
                av.setPayload(p);
            }
            case MOTION_SENSOR_EVENT -> {
                var m = src.getMotionSensorEvent();
                var p = new MotionSensorAvro();
                p.setLinkQuality(m.getLinkQuality());
                p.setMotion(m.getMotion());
                p.setVoltage(m.getVoltage());
                av.setPayload(p);
            }
            case PAYLOAD_NOT_SET -> { /* оставим payload null */ }
        }
        return av;
    }

    private static HubEventAvro mapHubProtoToAvro(HubEventProto src) {
        var av = new HubEventAvro();
        av.setHubId(src.getHubId());
        av.setTimestamp(toInstant(src.getTimestamp()));

        switch (src.getPayloadCase()) {
            case DEVICE_ADDED -> {
                var p = new DeviceAddedEventAvro();
                p.setId(src.getDeviceAdded().getId());
                p.setType(DeviceTypeAvro.valueOf(src.getDeviceAdded().getType().name()));
                av.setPayload(p);
            }
            case DEVICE_REMOVED -> {
                var p = new DeviceRemovedEventAvro();
                p.setId(src.getDeviceRemoved().getId());
                av.setPayload(p);
            }
            case SCENARIO_ADDED -> {
                var sa = src.getScenarioAdded();
                var p = new ScenarioAddedEventAvro();
                p.setName(sa.getName());

                // conditions: oneof { bool_value, int_value }
                p.setConditions(sa.getConditionsList().stream().map(c -> {
                    var cc = new ScenarioConditionAvro();
                    cc.setSensorId(c.getSensorId());
                    cc.setType(ConditionTypeAvro.valueOf(c.getType().name()));
                    cc.setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));
                    // oneof value
                    switch (c.getValueCase()) {
                        case INT_VALUE -> cc.setValue(c.getIntValue());     // Integer
                        case BOOL_VALUE -> cc.setValue(c.getBoolValue());   // Boolean
                        case VALUE_NOT_SET -> cc.setValue(null);            // null
                    }
                    return cc;
                }).toList());

                // actions: optional int32 value
                p.setActions(sa.getActionsList().stream().map(a -> {
                    var aa = new DeviceActionAvro();
                    aa.setSensorId(a.getSensorId());
                    aa.setType(ActionTypeAvro.valueOf(a.getType().name()));
                    aa.setValue(a.hasValue() ? a.getValue() : null); // Integer|null
                    return aa;
                }).toList());

                av.setPayload(p);
            }
            case SCENARIO_REMOVED -> {
                var p = new ScenarioRemovedEventAvro();
                p.setName(src.getScenarioRemoved().getName());
                av.setPayload(p);
            }
            case PAYLOAD_NOT_SET -> { /* payload оставим null */ }
        }
        return av;
    }
}