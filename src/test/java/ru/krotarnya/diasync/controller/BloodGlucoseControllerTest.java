package ru.krotarnya.diasync.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.krotarnya.diasync.model.BloodPoint;
import ru.krotarnya.diasync.model.Calibration;
import ru.krotarnya.diasync.model.Glucose;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BloodGlucoseControllerTest {
    @Autowired
    private BloodGlucoseController controller;

    @Test
    void testFullCycle() {
        String userId = "user1";
        String sensorId = "sensor1";
        double mgdl = 100;

        Instant timestamp = Instant.now();
        Flux<BloodPoint> subscription = controller.bloodPointAdded(userId);
        StepVerifier.create(subscription)
                .then(() -> controller.addBloodPoint(userId, sensorId, timestamp, mgdl, 1.0, 0.0))
                .expectNextMatches(saved -> saved.getUserId().equals(userId)
                        && saved.getSensorId().equals(sensorId)
                        && saved.getTimestamp().equals(timestamp)
                        && saved.getGlucose().equals(new Glucose(mgdl))
                        && saved.getCalibration().equals(new Calibration())
                )
                .thenCancel()
                .verify();

        List<BloodPoint> points = controller.bloodPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(1, points.size());
        BloodPoint retrievedPoint = points.getFirst();
        assertEquals(userId, retrievedPoint.getUserId());
        assertEquals(sensorId, retrievedPoint.getSensorId());
        assertEquals(timestamp, retrievedPoint.getTimestamp());
        assertEquals(new Glucose(mgdl), retrievedPoint.getGlucose());
        assertEquals(new Calibration(1.0, 0.0), retrievedPoint.getCalibration());

        controller.addBloodPoint(userId, sensorId, timestamp.plusSeconds(1), mgdl, 1.0, 0.0);
        List<BloodPoint> updatedPoints = controller.bloodPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(2, updatedPoints.size());
    }
}
