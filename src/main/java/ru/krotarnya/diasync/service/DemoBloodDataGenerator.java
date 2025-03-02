package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.controller.BloodGlucoseController;

/**
 * @author ivblinov
 */
@Service
public class DemoBloodDataGenerator {
    private static final String USER_ID = "demo";
    private static final String SENSOR_ID = "demo-sensor-1";
    private static final double MIN_MGDL = 40;
    private static final double MAX_MGDL = 270;
    private static final double MAX_MGDL_SHIFT = 20;

    private final BloodGlucoseController controller;
    private final Random random = new Random();

    @Nullable
    private Double previousMgdl;

    public DemoBloodDataGenerator(BloodGlucoseController controller) {
        this.controller = controller;
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void generateBloodPoint() {
        Instant now = Instant.now();

        double mgdl = Optional.ofNullable(previousMgdl)
                .map(prev -> prev + random.nextDouble() * MAX_MGDL_SHIFT - MAX_MGDL_SHIFT / 2)
                .orElseGet(() -> controller.bloodPoints(USER_ID, now.minus(Duration.ofHours(1)), now)
                        .stream()
                        .max(Comparator.comparingLong(x -> x.timestamp.toEpochMilli()))
                        .map(x -> x.glucose.mgdl)
                        .orElse(MIN_MGDL + (random.nextDouble() * (MAX_MGDL - MIN_MGDL))));

        mgdl = Math.max(MIN_MGDL, mgdl);
        mgdl = Math.min(MAX_MGDL, mgdl);
        previousMgdl = mgdl;

        controller.addBloodPoint(USER_ID, SENSOR_ID, Instant.now(), mgdl);
    }
}
