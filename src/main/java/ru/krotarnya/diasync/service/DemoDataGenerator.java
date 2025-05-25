package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import ru.krotarnya.diasync.model.DataPoint;

public abstract class DemoDataGenerator {
    @Value("${demo.userId}")
    private String userId;

    private final Random random = new Random();

    protected DemoDataGenerator() {
    }

    protected abstract Duration basePeriod();

    protected abstract Duration periodStdDev();

    protected abstract DataPoint generate(Instant timestamp);

    protected final String userId() {
        return userId;
    }

    protected final Random random() {
        return random;
    }

    public Duration period() {
        long base = basePeriod().toMillis();
        long noise = (long) (periodStdDev().toMillis() * random().nextGaussian());
        return (base + noise > 1000) ? Duration.ofMillis(Math.max(0L, base + noise)) : period();
    }
}
