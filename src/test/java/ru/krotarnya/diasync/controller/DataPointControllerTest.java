package ru.krotarnya.diasync.controller;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class DataPointControllerTest {
    @Autowired
    private DataPointController controller;

    @Test
    @Transactional
    @SuppressWarnings("DataFlowIssue")
    void testFullCycle() {
        String userId = UUID.randomUUID().toString();
        String sensorId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        double mgdl = 100;

        DataPoint.DataPointBuilder dataPointBuilder = DataPoint.builder()
                .userId(userId)
                .timestamp(timestamp)
                .sensorGlucose(SensorGlucose.builder()
                        .mgdl(mgdl)
                        .sensorId(sensorId)
                        .build());

        Flux<DataPoint> subscription = controller.onDataPointAdded(userId);
        StepVerifier.create(subscription)
                .then(() -> controller.addDataPoints(List.of(dataPointBuilder.build())))
                .expectNextMatches(saved -> saved.getUserId().equals(userId)
                        && saved.getSensorGlucose().getMgdl().equals(mgdl)
                        && saved.getSensorGlucose().getSensorId().equals(sensorId)
                        && saved.getTimestamp().equals(timestamp)
                        && Objects.isNull(saved.getSensorGlucose().getCalibration())
                        && Objects.isNull(saved.getManualGlucose())
                        && Objects.isNull(saved.getCarbs())
                )
                .thenCancel()
                .verify();

        List<DataPoint> points = controller.getDataPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(1, points.size());
        DataPoint retrievedPoint = points.getFirst();
        assertEquals(userId, retrievedPoint.getUserId());
        assertEquals(timestamp, retrievedPoint.getTimestamp());
        assertEquals(mgdl, retrievedPoint.getSensorGlucose().getMgdl());
        assertEquals(sensorId, retrievedPoint.getSensorGlucose().getSensorId());
        assertNull(retrievedPoint.getSensorGlucose().getCalibration());
        assertNull(retrievedPoint.getManualGlucose());
        assertNull(retrievedPoint.getCarbs());

        controller.addDataPoints(List.of(dataPointBuilder.timestamp(timestamp.plus(Duration.ofMinutes(1))).build()));
        List<DataPoint> updatedPoints = controller.getDataPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(2, updatedPoints.size());
    }
}
