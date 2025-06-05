package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.krotarnya.diasync.model.Carbs;
import ru.krotarnya.diasync.model.DataPoint;

@Component
public class DemoCarbsDataGenerator extends DemoDataGenerator {
    private static final Duration BASE_PERIOD = Duration.ofMinutes(30);
    private static final Duration PERIOD_STD_DEVIATION = Duration.ofSeconds(10);
    private static final double MAX_GRAMS = 20.0;

    @Override
    protected Duration basePeriod() {
        return BASE_PERIOD;
    }

    @Override
    protected Duration periodStdDev() {
        return PERIOD_STD_DEVIATION;
    }

    @Override
    protected DataPoint generate(Instant timestamp) {
        return DataPoint.builder()
                .userId(userId())
                .timestamp(timestamp)
                .carbs(Carbs.builder()
                        .grams(random().nextDouble() * MAX_GRAMS)
                        .build())
                .build();
    }
}
