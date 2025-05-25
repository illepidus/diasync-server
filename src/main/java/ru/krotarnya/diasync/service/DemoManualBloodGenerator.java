package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.ManualGlucose;

//TODO: Fix upsert and enable
@Component
public final class DemoManualBloodGenerator extends DemoSensorBloodGenerator {
    private static final Duration BASE_PERIOD = Duration.ofMinutes(30);
    private static final Duration TIME_SHIFT = Duration.ofMinutes(10);
    private static final Duration PERIOD_STD_DEVIATION = Duration.ofMinutes(15);
    private static final double NOISE_STD_DEV = 3.0;

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
        double signal = super.signal(timestamp.plus(TIME_SHIFT));
        double noise = NOISE_STD_DEV * random().nextGaussian();

        return DataPoint.builder()
                .userId(userId())
                .timestamp(timestamp)
                .manualGlucose(ManualGlucose.builder()
                        .mgdl(signal + noise)
                        .build())
                .build();
    }
}
