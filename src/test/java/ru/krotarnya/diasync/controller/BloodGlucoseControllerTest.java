package ru.krotarnya.diasync.controller;

import java.time.Duration;
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

        BloodPoint.BloodPointBuilder bloodPointBuilder = BloodPoint.builder()
                .userId(userId)
                .sensorId(sensorId)
                .timestamp(timestamp)
                .glucose(Glucose.ofMgdl(mgdl))
                .calibration(Calibration.empty());

        Flux<BloodPoint> subscription = controller.onBloodPointAdded(userId);
        StepVerifier.create(subscription)
                .then(() -> controller.addBloodPoints(List.of(bloodPointBuilder.build())))
                .expectNextMatches(saved -> saved.getUserId().equals(userId)
                        && saved.getSensorId().equals(sensorId)
                        && saved.getTimestamp().equals(timestamp)
                        && saved.getGlucose().equals(new Glucose(mgdl))
                        && saved.getCalibration().equals(new Calibration())
                )
                .thenCancel()
                .verify();

        List<BloodPoint> points = controller.getBloodPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(1, points.size());
        BloodPoint retrievedPoint = points.getFirst();
        assertEquals(userId, retrievedPoint.getUserId());
        assertEquals(sensorId, retrievedPoint.getSensorId());
        assertEquals(timestamp, retrievedPoint.getTimestamp());
        assertEquals(new Glucose(mgdl), retrievedPoint.getGlucose());
        assertEquals(new Calibration(1.0, 0.0), retrievedPoint.getCalibration());

        controller.addBloodPoints(List.of(bloodPointBuilder.timestamp(timestamp.plus(Duration.ofMinutes(1))).build()));
        List<BloodPoint> updatedPoints = controller.getBloodPoints(userId,
                timestamp.minusSeconds(3600),
                timestamp.plusSeconds(3600));
        assertEquals(2, updatedPoints.size());
    }
}
