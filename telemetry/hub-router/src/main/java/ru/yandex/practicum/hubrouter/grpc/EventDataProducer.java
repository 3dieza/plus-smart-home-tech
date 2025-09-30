package ru.yandex.practicum.hubrouter.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import java.time.Instant;
import java.util.Random;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.hubrouter.config.SensorProps;
import ru.yandex.practicum.grpc.telemetry.collector.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorResponse;
import ru.yandex.practicum.grpc.telemetry.collector.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.collector.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.collector.TemperatureSensorProto;


@Component
public class EventDataProducer {
    private static final Logger log = LoggerFactory.getLogger(EventDataProducer.class);
    private final SensorProps props;
    private final Random rnd = new Random();

    public EventDataProducer(SensorProps props) {
        this.props = props;
    }

    @GrpcClient("collector")
    private CollectorControllerGrpc.CollectorControllerBlockingStub collector;

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void tick() {
        try {
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

    private int rndRange(int min, int max) {
        return min + rnd.nextInt((max - min) + 1);
    }
}