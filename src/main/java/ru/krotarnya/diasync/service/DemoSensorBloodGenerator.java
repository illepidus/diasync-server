package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.krotarnya.diasync.model.Calibration;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

@Component
public class DemoSensorBloodGenerator extends DemoDataGenerator {
    private static final String SENSOR_ID = "demo-sensor-id";

    private static final double BASE_LINE = 120;
    private static final double BASE_DEVIATION = 80;

    private static final double HARMONIC_1_AMPLITUDE = 0.5;
    private static final double HARMONIC_2_AMPLITUDE = 0.3;
    private static final double HARMONIC_3_AMPLITUDE = 0.2;

    private static final Duration BASE_PERIOD = Duration.ofMinutes(1);
    private static final Duration PERIOD_STD_DEVIATION = Duration.ofSeconds(1);
    private static final Duration HARMONIC_1_PERIOD = Duration.ofMinutes(115);
    private static final Duration HARMONIC_2_PERIOD = HARMONIC_1_PERIOD.multipliedBy(3);
    private static final Duration HARMONIC_3_PERIOD = HARMONIC_1_PERIOD.multipliedBy(7);

    private static final double NOISE_STD_DEV = 1.0;
    private static final double CALIBRATION_SLOPE = 1.1;
    private static final double CALIBRATION_INTERCEPT = -10;

    @Override
    public Duration basePeriod() {
        return BASE_PERIOD;
    }

    @Override
    public Duration periodStdDev() {
        return PERIOD_STD_DEVIATION;
    }

    @Override
    protected DataPoint generate(Instant timestamp) {
        double signal = signal(timestamp);
        double noise = NOISE_STD_DEV * random().nextGaussian();


        return DataPoint.builder()
                .userId(userId())
                .timestamp(timestamp)
                .sensorGlucose(SensorGlucose.builder()
                        .mgdl(signal + noise)
                        .sensorId(SENSOR_ID)
                        .calibration(
                                Calibration.builder()
                                        .slope(CALIBRATION_SLOPE)
                                        .intercept(CALIBRATION_INTERCEPT)
                                        .build())
                        .build())
                .build();
    }

    protected final double signal(Instant timestamp) {
        return BASE_LINE + BASE_DEVIATION * (
                signal(timestamp, HARMONIC_1_AMPLITUDE, HARMONIC_1_PERIOD) +
                        signal(timestamp, HARMONIC_2_AMPLITUDE, HARMONIC_2_PERIOD) +
                        signal(timestamp, HARMONIC_3_AMPLITUDE, HARMONIC_3_PERIOD));
    }

    private double signal(Instant timestamp, double amplitude, Duration period) {
        return amplitude * Math.sin(2 * Math.PI * timestamp.getEpochSecond() / period.getSeconds());
    }
}
