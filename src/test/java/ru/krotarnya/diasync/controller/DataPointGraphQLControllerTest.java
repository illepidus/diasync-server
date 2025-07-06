package ru.krotarnya.diasync.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@SuppressWarnings("DataFlowIssue")
@Timeout(value = 5, unit = TimeUnit.SECONDS)
class DataPointGraphQLControllerTest {

    @Autowired
    private DataPointGraphQLController controller;

    private String userId;
    private String sensorId;
    private Instant timestamp;
    private double mgdl;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        sensorId = UUID.randomUUID().toString();
        timestamp = Instant.now();
        mgdl = 100.0;
    }

    private DataPoint createDataPoint(Instant time) {
        return DataPoint.builder()
                .userId(userId)
                .timestamp(time)
                .sensorGlucose(SensorGlucose.builder()
                        .mgdl(mgdl)
                        .sensorId(sensorId)
                        .build())
                .build();
    }

    @Test
    void shouldReceiveDataPointViaSubscription() {
        Flux<DataPoint> subscription = controller.onDataPointAdded(userId);

        StepVerifier.create(subscription)
                .then(() -> controller.addDataPoints(List.of(createDataPoint(timestamp))))
                .expectNextMatches(dp ->
                        dp.getUserId().equals(userId) &&
                                dp.getTimestamp().equals(timestamp) &&
                                dp.getSensorGlucose() != null &&
                                dp.getSensorGlucose().getSensorId().equals(sensorId) &&
                                dp.getSensorGlucose().getMgdl() == mgdl &&
                                dp.getSensorGlucose().getCalibration() == null &&
                                dp.getManualGlucose() == null &&
                                dp.getCarbs() == null
                )
                .thenCancel()
                .verify();
    }

    @Test
    void shouldAddAndRetrieveSingleDataPoint() {
        controller.addDataPoints(List.of(createDataPoint(timestamp)));

        List<DataPoint> points = controller.getDataPoints(
                userId,
                timestamp.minusSeconds(60),
                timestamp.plusSeconds(60)
        );

        assertEquals(1, points.size());

        DataPoint dp = points.getFirst();
        assertEquals(userId, dp.getUserId());
        assertEquals(timestamp, dp.getTimestamp());
        assertEquals(mgdl, dp.getSensorGlucose().getMgdl());
        assertEquals(sensorId, dp.getSensorGlucose().getSensorId());
        assertNull(dp.getSensorGlucose().getCalibration());
        assertNull(dp.getManualGlucose());
        assertNull(dp.getCarbs());
    }

    @Test
    void shouldStoreMultipleDataPoints() {
        controller.addDataPoints(List.of(
                createDataPoint(timestamp),
                createDataPoint(timestamp.plus(Duration.ofMinutes(1)))
        ));

        List<DataPoint> points = controller.getDataPoints(
                userId,
                timestamp.minusSeconds(60),
                timestamp.plusSeconds(120)
        );

        assertEquals(2, points.size());
    }

    @Test
    void shouldTruncateAllUserDataPoints() {
        controller.addDataPoints(List.of(
                createDataPoint(timestamp),
                createDataPoint(timestamp.plus(Duration.ofMinutes(1)))
        ));

        int removed = controller.truncateDataPoints(userId);
        assertEquals(2, removed);

        List<DataPoint> afterTruncate = controller.getDataPoints(
                userId,
                timestamp.minusSeconds(60),
                timestamp.plusSeconds(120)
        );

        assertTrue(afterTruncate.isEmpty());
    }
}
