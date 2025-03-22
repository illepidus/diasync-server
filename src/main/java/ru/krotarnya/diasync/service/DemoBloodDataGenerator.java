package ru.krotarnya.diasync.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.controller.DataPointController;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class DemoBloodDataGenerator {
    private static final String USER_ID = "demo";
    private static final String SENSOR_ID = "demo-sensor-1";
    private static final float MIN_MGDL = 40;
    private static final float MAX_MGDL = 270;
    private static final float TARGET_MGDL = 120; // Natural equilibrium point
    private static final float MAX_GAUSSIAN_SHIFT = 5; // Gaussian noise
    private static final float MAX_CHANGE_PER_STEP = 3.0f; // Limit rate of change
    private static final float ALPHA = 0.3f; // Smoothing factor
    private static final float RETURN_TO_MEAN_FACTOR = 0.1f; // Stronger tendency to return to 100 mg/dL
    private static final float CIRCADIAN_AMPLITUDE = 2.0f; // Reduced circadian effect
    private static final Duration MAX_GAP = Duration.ofMinutes(180); // Max gap to fill
    private static final Duration STEP = Duration.ofSeconds(10); // Step for fill

    private final DataPointController controller;
    private final Random random = new Random();
    private Float previousMgdl;

    public DemoBloodDataGenerator(DataPointController controller) {
        this.controller = controller;
        controller.truncateDataPoints(USER_ID);
        fillMissingData();
    }

    private void fillMissingData() {
        Instant now = Instant.now();
        Instant start = controller.getDataPoints(USER_ID, now.minus(Duration.ofHours(1)), now)
                .stream()
                .max(Comparator.comparing(DataPoint::getTimestamp))
                .map(DataPoint::getTimestamp)
                .filter(timestamp -> Duration.between(timestamp, now).compareTo(MAX_GAP) <= 0)
                .orElse(now.minus(MAX_GAP));

        List<DataPoint> missingPoints = new ArrayList<>();
        Instant timestamp = start;
        while (timestamp.isBefore(now)) {
            float mgdl = generateGlucoseValue(timestamp);
            missingPoints.add(createDataPoint(timestamp, mgdl));
            previousMgdl = mgdl;
            timestamp = timestamp.plus(STEP);
        }
        if (!missingPoints.isEmpty()) {
            controller.addDataPoints(missingPoints);
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void generateBloodPoint() {
        Instant now = Instant.now();
        float mgdl = generateGlucoseValue(now);
        previousMgdl = mgdl;
        controller.addDataPoints(List.of(createDataPoint(now, mgdl)));
    }

    private float generateGlucoseValue(Instant timestamp) {
        float mgdl = Optional.ofNullable(previousMgdl)
                .map(prev -> prev + (float) random.nextGaussian() * MAX_GAUSSIAN_SHIFT)
                .orElse(TARGET_MGDL);

        mgdl = Math.max(previousMgdl == null ? MIN_MGDL : previousMgdl - MAX_CHANGE_PER_STEP,
                        Math.min(previousMgdl == null ? MAX_MGDL : previousMgdl + MAX_CHANGE_PER_STEP, mgdl));

        float circadianEffect = (float) Math.sin(timestamp.getEpochSecond() / 10800.0 * 2 * Math.PI) * CIRCADIAN_AMPLITUDE;
        mgdl += circadianEffect;
        mgdl += (TARGET_MGDL - mgdl) * RETURN_TO_MEAN_FACTOR;
        mgdl = ALPHA * mgdl + (1 - ALPHA) * Optional.ofNullable(previousMgdl).orElse(mgdl);

        return Math.max(MIN_MGDL, Math.min(MAX_MGDL, mgdl));
    }

    private DataPoint createDataPoint(Instant timestamp, float mgdl) {
        return DataPoint.builder()
                .userId(USER_ID)
                .timestamp(timestamp)
                .sensorGlucose(SensorGlucose.builder()
                        .mgdl(mgdl)
                        .sensorId(SENSOR_ID)
                        .build())
                .build();
    }
}
