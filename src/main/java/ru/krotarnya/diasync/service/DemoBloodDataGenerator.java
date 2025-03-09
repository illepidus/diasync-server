package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.controller.BloodGlucoseController;
import ru.krotarnya.diasync.model.BloodPoint;
import ru.krotarnya.diasync.model.Calibration;
import ru.krotarnya.diasync.model.Glucose;

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
                .orElseGet(() -> controller.getBloodPoints(USER_ID, now.minus(Duration.ofHours(1)), now)
                        .stream()
                        .max(Comparator.comparing(BloodPoint::getTimestamp))
                        .map(BloodPoint::getGlucose)
                        .map(Glucose::getMgdl)
                        .orElse(MIN_MGDL + (random.nextDouble() * (MAX_MGDL - MIN_MGDL))));

        mgdl = Math.max(MIN_MGDL, mgdl);
        mgdl = Math.min(MAX_MGDL, mgdl);
        previousMgdl = mgdl;

        controller.addBloodPoints(List.of(new BloodPoint(
                null,
                USER_ID,
                SENSOR_ID,
                Instant.now(),
                Glucose.ofMgdl(mgdl),
                Calibration.empty())));
    }
}
