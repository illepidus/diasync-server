package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.krotarnya.diasync.model.Carbs;
import ru.krotarnya.diasync.model.DataPoint;

@Component
public class DemoCarbsDataGenerator extends DemoDataGenerator {
    @Override
    protected Duration basePeriod() {
        return Duration.ofMinutes(30);
    }

    @Override
    protected Duration periodStdDev() {
        return Duration.ofSeconds(10);
    }

    @Override
    protected DataPoint generate(Instant timestamp) {
        return DataPoint.builder()
                .userId(userId())
                .timestamp(timestamp)
                .carbs(Carbs.builder()
                        .grams(random().nextDouble() * 20)
                        .build())
                .build();
    }
}
