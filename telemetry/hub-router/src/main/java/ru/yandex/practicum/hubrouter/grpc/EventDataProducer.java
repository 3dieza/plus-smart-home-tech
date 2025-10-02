package ru.yandex.practicum.hubrouter.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.collector.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorResponse;
import ru.yandex.practicum.grpc.telemetry.collector.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.collector.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.TemperatureSensorProto;
import ru.yandex.practicum.hubrouter.config.SensorProps;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.serializationcore.kafka.avro.HubEventSerializer;

@Slf4j
@Component
public class EventDataProducer {

    private final SensorProps props;
    private final Random rnd = new Random();

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collector;

    // --- Kafka hub-events ---
    private final KafkaProducer<String, HubEventAvro> hubEventsProducer;
    private final String hubsTopic;
    private final AtomicBoolean bootstrapped = new AtomicBoolean(false);

    public EventDataProducer(
            SensorProps props,
            @Value("${app.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.topics.hubs:telemetry.hubs.v1}") String hubsTopic
    ) {
        this.props = props;
        this.hubsTopic = hubsTopic;

        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, HubEventSerializer.class.getName());
        p.put(ProducerConfig.ACKS_CONFIG, "all");

        this.hubEventsProducer = new KafkaProducer<>(p);
    }

    // ====== SENSOR EVENTS ======
    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void tick() {
        try {
            // 1) разовый бутстрап hub-событий
            bootstrapHubIfNeeded();

            // 2) генерация телеметрии сенсоров
            if (props.getMotionSensors() != null) props.getMotionSensors().forEach(m -> send(createMotion(m)));
            if (props.getTemperatureSensors() != null) props.getTemperatureSensors().forEach(t -> send(createTemp(t)));
            if (props.getLightSensors() != null) props.getLightSensors().forEach(l -> send(createLight(l)));
            if (props.getClimateSensors() != null) props.getClimateSensors().forEach(c -> send(createClimate(c)));
            if (props.getSwitchSensors() != null) props.getSwitchSensors().forEach(s -> send(createSwitch(s)));

        } catch (Exception e) {
            log.error("send batch failed", e);
        }
    }

    private void send(SensorEventProto event) {
        log.info("Sending: hubId={}, id={}, type={}", event.getHubId(), event.getId(), event.getPayloadCase());
        try {
            CollectorResponse resp = collector.collectSensorEvent(event);
            log.info("Collector response: status={}, message={}, processed_at={}",
                    resp.getStatus(), resp.getMessage(), resp.hasProcessedAt() ? resp.getProcessedAt() : "n/a");
        } catch (StatusRuntimeException e) {
            log.error("gRPC failed: {}", e.getStatus(), e);
        }
    }

    private static Timestamp nowTs() {
        Instant ts = Instant.now();
        return Timestamp.newBuilder().setSeconds(ts.getEpochSecond()).setNanos(ts.getNano()).build();
    }

    private int rand(SensorProps.Range r) {
        int lo = Math.min(r.getMinValue(), r.getMaxValue());
        int hi = Math.max(r.getMinValue(), r.getMaxValue());
        return lo + rnd.nextInt(hi - lo + 1);
    }

    private int rndRange(int min, int max) {
        return min + rnd.nextInt((max - min) + 1);
    }

    private SensorEventProto.Builder base(String id) {
        return SensorEventProto.newBuilder()
                .setId(id)
                .setHubId(props.getHubId())
                .setTimestamp(nowTs());
    }

    private SensorEventProto createMotion(SensorProps.Motion m) {
        MotionSensorProto payload = MotionSensorProto.newBuilder()
                .setLinkQuality(rand(m.getLinkQuality()))
                .setMotion(rnd.nextBoolean())
                .setVoltage(rand(m.getVoltage()))
                .build();

        return base(m.getId())
                .setMotionSensorEvent(payload)
                .build();
    }

    private SensorEventProto createTemp(SensorProps.Temp t) {
        int c = rand(t.getTemperature());
        int f = (int) Math.round(c * 1.8 + 32);
        TemperatureSensorProto payload = TemperatureSensorProto.newBuilder()
                .setTemperatureC(c)
                .setTemperatureF(f)
                .build();

        return base(t.getId())
                .setTemperatureSensorEvent(payload)
                .build();
    }

    private SensorEventProto createLight(SensorProps.Light l) {
        LightSensorProto payload = LightSensorProto.newBuilder()
                .setLinkQuality(rndRange(0, 100))
                .setLuminosity(rand(l.getLuminosity()))
                .build();

        return base(l.getId())
                .setLightSensorEvent(payload)
                .build();
    }

    private SensorEventProto createClimate(SensorProps.Climate c) {
        ClimateSensorProto payload = ClimateSensorProto.newBuilder()
                .setTemperatureC(rand(c.getTemperature()))
                .setHumidity(rand(c.getHumidity()))
                .setCo2Level(rand(c.getCo2Level()))
                .build();

        return base(c.getId())
                .setClimateSensorEvent(payload)
                .build();
    }

    private SensorEventProto createSwitch(SensorProps.Sw s) {
        SwitchSensorProto payload = SwitchSensorProto.newBuilder()
                .setState(rnd.nextBoolean())
                .build();

        return base(s.getId())
                .setSwitchSensorEvent(payload)
                .build();
    }

    // ====== HUB EVENTS BOOTSTRAP ======
    private void bootstrapHubIfNeeded() {
        if (!bootstrapped.compareAndSet(false, true)) return;

        final String hubId = props.getHubId();
        final Instant tsInstant = Instant.now();
        log.info("Hub bootstrap: publishing DEVICE_ADDED & SCENARIO_ADDED for hubId={}", hubId);

        // 1) DEVICE_ADDED + тип устройства
        List<String> deviceIds = new ArrayList<>();

        if (props.getMotionSensors() != null) {
            props.getMotionSensors().forEach(m -> {
                deviceIds.add(m.getId());
                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(m.getId())
                                .setType(DeviceTypeAvro.MOTION_SENSOR)
                                .build())
                        .build());
            });
        }
        if (props.getTemperatureSensors() != null) {
            props.getTemperatureSensors().forEach(t -> {
                deviceIds.add(t.getId());
                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(t.getId())
                                .setType(DeviceTypeAvro.TEMPERATURE_SENSOR)
                                .build())
                        .build());
            });
        }
        if (props.getLightSensors() != null) {
            props.getLightSensors().forEach(l -> {
                deviceIds.add(l.getId());
                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(l.getId())
                                .setType(DeviceTypeAvro.LIGHT_SENSOR)
                                .build())
                        .build());
            });
        }
        if (props.getClimateSensors() != null) {
            props.getClimateSensors().forEach(c -> {
                deviceIds.add(c.getId());
                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(c.getId())
                                .setType(DeviceTypeAvro.CLIMATE_SENSOR)
                                .build())
                        .build());
            });
        }
        if (props.getSwitchSensors() != null) {
            props.getSwitchSensors().forEach(s -> {
                deviceIds.add(s.getId());
                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(s.getId())
                                .setType(DeviceTypeAvro.SWITCH_SENSOR)
                                .build())
                        .build());
            });
        }

        // 2) Сценарий: Регулировка температуры (спальня)
        if (deviceIds.contains("climate-1") && deviceIds.contains("light-2")) {
            ScenarioConditionAvro condTemp = ScenarioConditionAvro.newBuilder()
                    .setSensorId("climate-1")
                    .setType(ConditionTypeAvro.TEMPERATURE)
                    .setOperation(ConditionOperationAvro.LOWER_THAN)
                    .setValue(15) // int
                    .build();

            DeviceActionAvro actHeater = DeviceActionAvro.newBuilder()
                    .setSensorId("light-2")
                    .setType(ActionTypeAvro.ACTIVATE)
                    .setValue(1)
                    .build();

            publish(hubId, HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(tsInstant)
                    .setPayload(ScenarioAddedEventAvro.newBuilder()
                            .setName("Регулировка температуры (спальня)")
                            .setConditions(List.of(condTemp))
                            .setActions(List.of(actHeater))
                            .build())
                    .build());
        }

        // 3) Сценарий: Автосвет (коридор)
        if (deviceIds.contains("motion-1") && deviceIds.contains("light-1")) {
            ScenarioConditionAvro condMotion = ScenarioConditionAvro.newBuilder()
                    .setSensorId("motion-1")
                    .setType(ConditionTypeAvro.MOTION)
                    .setOperation(ConditionOperationAvro.EQUALS)
                    .setValue(true) // boolean
                    .build();

            ScenarioConditionAvro condLux = ScenarioConditionAvro.newBuilder()
                    .setSensorId("light-1")
                    .setType(ConditionTypeAvro.LUMINOSITY)
                    .setOperation(ConditionOperationAvro.LOWER_THAN)
                    .setValue(500) // int
                    .build();

            DeviceActionAvro actLightOn = DeviceActionAvro.newBuilder()
                    .setSensorId("light-1")
                    .setType(ActionTypeAvro.ACTIVATE)
                    .setValue(1)
                    .build();

            publish(hubId, HubEventAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(tsInstant)
                    .setPayload(ScenarioAddedEventAvro.newBuilder()
                            .setName("Автосвет (коридор)")
                            .setConditions(List.of(condMotion, condLux))
                            .setActions(List.of(actLightOn))
                            .build())
                    .build());
        }

        // 4) Сценарий: Выключить весь свет
        if (deviceIds.contains("switch-1")) {
            List<DeviceActionAvro> actions = new ArrayList<>();
            if (deviceIds.contains("light-1")) {
                actions.add(DeviceActionAvro.newBuilder()
                        .setSensorId("light-1")
                        .setType(ActionTypeAvro.DEACTIVATE)
                        .setValue(0)
                        .build());
            }
            if (deviceIds.contains("light-2")) {
                actions.add(DeviceActionAvro.newBuilder()
                        .setSensorId("light-2")
                        .setType(ActionTypeAvro.DEACTIVATE)
                        .setValue(0)
                        .build());
            }
            if (!actions.isEmpty()) {
                ScenarioConditionAvro condSwitch = ScenarioConditionAvro.newBuilder()
                        .setSensorId("switch-1")
                        .setType(ConditionTypeAvro.SWITCH)
                        .setOperation(ConditionOperationAvro.EQUALS)
                        .setValue(true)
                        .build();

                publish(hubId, HubEventAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(tsInstant)
                        .setPayload(ScenarioAddedEventAvro.newBuilder()
                                .setName("Выключить весь свет")
                                .setConditions(List.of(condSwitch))
                                .setActions(actions)
                                .build())
                        .build());
            }
        }

        log.info("Hub bootstrap finished for hubId={}", hubId);
    }

    private void publish(String hubId, HubEventAvro event) {
        hubEventsProducer.send(new ProducerRecord<>(hubsTopic, hubId, event), (md, ex) -> {
            if (ex != null) {
                log.error("Kafka send FAILED (hub-event): topic={}, key={}, reason={}",
                        hubsTopic, hubId, ex, ex);
            } else {
                log.info("Kafka send OK (hub-event): topic={}, partition={}, offset={}",
                        md.topic(), md.partition(), md.offset());
            }
        });
    }
}