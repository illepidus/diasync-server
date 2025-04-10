package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

@Service
public final class DemoBloodDataGenerator {
    private static final String USER_ID = "demo";
    private static final String SENSOR_ID = "demo-sensor-1";
    private static final double MIN_MGDL = 40;
    private static final double MAX_MGDL = 270;
    private static final double TARGET_MGDL = 120;
    private static final double MAX_GAUSSIAN_SHIFT = 10;
    private static final double MAX_CHANGE_PER_STEP = 6.0f;
    private static final double ALPHA = 0.5f;
    private static final double RETURN_TO_MEAN_FACTOR = 0.05f;
    private static final double CIRCADIAN_AMPLITUDE = 3.5f;
    private static final Duration MAX_GAP = Duration.ofMinutes(180);
    private static final Duration STEP = Duration.ofSeconds(60);

    private final DataPointService dataPointService;
    private final Random random = new Random();
    private Double previousMgdl;

    public DemoBloodDataGenerator(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
        fillMissingData();
    }

    private void fillMissingData() {
        Instant now = Instant.now();

        Optional<DataPoint> lastPoint = dataPointService.getDataPoints(USER_ID, now.minus(MAX_GAP), now)
                .stream()
                .max(Comparator.comparing(DataPoint::getTimestamp));

        previousMgdl = lastPoint.map(DataPoint::getSensorGlucose)
                .map(SensorGlucose::getMgdl)
                .orElse(null);

        Instant start = lastPoint.map(DataPoint::getTimestamp)
                .filter(timestamp -> Duration.between(timestamp, now).compareTo(MAX_GAP) <= 0)
                .orElse(now.minus(MAX_GAP));

        List<DataPoint> missingPoints = new ArrayList<>();
        Instant timestamp = start;
        while (timestamp.isBefore(now)) {
            double mgdl = generateGlucoseValue(timestamp);
            missingPoints.add(createDataPoint(timestamp, mgdl));
            previousMgdl = mgdl;
            timestamp = timestamp.plus(STEP);
        }
        if (!missingPoints.isEmpty()) {
            dataPointService.addDataPoints(missingPoints);
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void generateBloodPoint() {
        Instant now = Instant.now();
        double mgdl = generateGlucoseValue(now);
        previousMgdl = mgdl;
        dataPointService.addDataPoints(List.of(createDataPoint(now, mgdl)));
    }

    private double generateGlucoseValue(Instant timestamp) {
        double mgdl = Optional.ofNullable(previousMgdl)
                .map(prev -> prev + random.nextGaussian() * MAX_GAUSSIAN_SHIFT)
                .orElse(TARGET_MGDL);

        mgdl = Math.max(previousMgdl == null ? MIN_MGDL : previousMgdl - MAX_CHANGE_PER_STEP,
                Math.min(previousMgdl == null ? MAX_MGDL : previousMgdl + MAX_CHANGE_PER_STEP, mgdl));

        double circadianEffect = Math.sin(timestamp.getEpochSecond() / 3600.0 * 2 * Math.PI) * CIRCADIAN_AMPLITUDE +
                Math.sin(timestamp.getEpochSecond() / 7400.0 * 2 * Math.PI) * (CIRCADIAN_AMPLITUDE / 3) +
                Math.sin(timestamp.getEpochSecond() / 12800.0 * 2 * Math.PI) * (CIRCADIAN_AMPLITUDE / 4);

        mgdl += circadianEffect * (0.5 + random.nextDouble());
        mgdl += (TARGET_MGDL - mgdl) * RETURN_TO_MEAN_FACTOR;
        mgdl = ALPHA * mgdl + (1 - ALPHA) * Optional.ofNullable(previousMgdl).orElse(mgdl);

        return Math.max(MIN_MGDL, Math.min(MAX_MGDL, mgdl));
    }

    private DataPoint createDataPoint(Instant timestamp, double mgdl) {
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
