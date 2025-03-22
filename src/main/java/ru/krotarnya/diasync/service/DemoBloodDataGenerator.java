package ru.krotarnya.diasync.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.controller.DataPointController;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

import java.time.Duration;
import java.time.Instant;
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
    private static final float MAX_MGDL_SHIFT = 20;

    private final DataPointController controller;
    private final Random random = new Random();

    private Float previousMgdl;

    public DemoBloodDataGenerator(DataPointController controller) {
        this.controller = controller;
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void generateBloodPoint() {
        Instant now = Instant.now();

        float mgdl = Optional.ofNullable(previousMgdl)
                .map(prev -> prev + random.nextFloat() * MAX_MGDL_SHIFT - MAX_MGDL_SHIFT / 2)
                .orElseGet(() -> controller.getDataPoints(USER_ID, now.minus(Duration.ofHours(1)), now)
                        .stream()
                        .max(Comparator.comparing(DataPoint::getTimestamp))
                        .map(DataPoint::getSensorGlucose)
                        .map(SensorGlucose::getMgdl)
                        .orElse(MIN_MGDL + (random.nextFloat() * (MAX_MGDL - MIN_MGDL))));

        mgdl = Math.max(MIN_MGDL, mgdl);
        mgdl = Math.min(MAX_MGDL, mgdl);
        previousMgdl = mgdl;

        controller.addDataPoints(List.of(DataPoint.builder()
                .userId(USER_ID)
                .timestamp(Instant.now())
                .sensorGlucose(SensorGlucose.builder()
                        .mgdl(mgdl)
                        .sensorId(SENSOR_ID)
                        .build())
                .build()));
    }
}
