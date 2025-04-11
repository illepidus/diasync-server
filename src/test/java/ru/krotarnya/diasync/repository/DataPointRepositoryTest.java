package ru.krotarnya.diasync.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.krotarnya.diasync.model.Carbs;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.SensorGlucose;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("DataFlowIssue")
@DataJpaTest
class DataPointRepositoryTest {

    @Autowired
    private DataPointRepository repository;

    private final String testUserId = "test-user";
    private final Instant testTimestamp = Instant.parse("2023-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldFindPointsBetweenTimestamps() {
        DataPoint dp1 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        DataPoint dp2 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp.plusSeconds(3600))
                .sensorGlucose(SensorGlucose.builder().mgdl(200.).build())
                .build();
        repository.saveAll(List.of(dp1, dp2));

        Instant from = testTimestamp.minusSeconds(1800);
        Instant to = testTimestamp.plusSeconds(7200);
        List<DataPoint> result = repository.findByUserIdAndTimestampBetween(testUserId, from, to);

        assertEquals(2, result.size());
        assertTrue(result.contains(dp1));
        assertTrue(result.contains(dp2));
    }

    @Test
    void shouldDeletePointsBeforeTimestamp() {
        DataPoint dp1 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        DataPoint dp2 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp.plusSeconds(3600))
                .sensorGlucose(SensorGlucose.builder().mgdl(200.).build())
                .build();
        repository.saveAll(List.of(dp1, dp2));

        int deleted = repository.deleteByTimestampBefore(testTimestamp.plusSeconds(1800));

        assertEquals(1, deleted);
        assertEquals(1, repository.count());
    }

    @Test
    void shouldDeletePointsByUserId() {
        DataPoint dp1 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        DataPoint dp2 = DataPoint.builder()
                .userId("other-user")
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(200.).build())
                .build();
        repository.saveAll(List.of(dp1, dp2));

        repository.deleteByUserId(testUserId);

        assertEquals(1, repository.count());
        assertEquals("other-user", repository.findAll().getFirst().getUserId());
    }

    @Test
    void shouldNotUpdateIfDataIsSame() {
        DataPoint dp = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        repository.save(dp);

        Optional<DataPoint> result = repository.upsertDataPoint(dp.withoutId());

        assertTrue(result.isEmpty());
        assertEquals(1, repository.count());
    }

    @Test
    void shouldUpdateIfDataIsDifferent() {
        DataPoint original = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        DataPoint saved = repository.save(original);

        DataPoint incoming = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(200.).build())
                .build();

        Optional<DataPoint> result = repository.upsertDataPoint(incoming);

        assertTrue(result.isPresent());
        assertEquals(200.0, result.get().getSensorGlucose().getMgdl(), 0.001);
        assertEquals(saved.getId(), result.get().getId());
        assertEquals(1, repository.count());
    }

    @Test
    void shouldInsertNewDataPointIfNotExists() {
        DataPoint dp = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();

        Optional<DataPoint> result = repository.upsertDataPoint(dp);

        assertTrue(result.isPresent());
        assertEquals(100.0, result.get().getSensorGlucose().getMgdl(), 0.001);
        assertEquals(1, repository.count());
    }

    @Test
    void shouldAddMultipleDataPoints() {
        DataPoint dp1 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        DataPoint dp2 = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp.plusSeconds(3600))
                .sensorGlucose(SensorGlucose.builder().mgdl(200.).build())
                .build();
        List<DataPoint> result = repository.addDataPoints(List.of(dp1, dp2));

        assertEquals(2, result.size());
        assertEquals(2, repository.count());
    }

    @Test
    void shouldNotUpdateIfAllComparedFieldsAreNull() {
        DataPoint existing = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .build();
        repository.save(existing);

        DataPoint incoming = existing.withoutId();

        Optional<DataPoint> result = repository.upsertDataPoint(incoming);

        assertTrue(result.isEmpty());
        assertEquals(1, repository.count());
    }

    @Test
    void shouldRetainExistingFieldsIfNewOnesAreNull() {
        DataPoint existing = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .carbs(Carbs.builder().grams(25.).build())
                .sensorGlucose(SensorGlucose.builder().mgdl(100.).build())
                .build();
        repository.save(existing);

        DataPoint incoming = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .build();

        Optional<DataPoint> result = repository.upsertDataPoint(incoming);

        assertTrue(result.isPresent());
        DataPoint updated = result.get();
        assertEquals(25.0, updated.getCarbs().getGrams(), 0.001);
        assertEquals(100.0, updated.getSensorGlucose().getMgdl(), 0.001);
        assertNull(updated.getManualGlucose());
    }

    @Test
    void shouldUpdateOnlyNonNullFields() {
        DataPoint existing = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .sensorGlucose(SensorGlucose.builder().mgdl(100.0).build())
                .build();
        repository.save(existing);

        DataPoint incoming = DataPoint.builder()
                .userId(testUserId)
                .timestamp(testTimestamp)
                .carbs(Carbs.builder().grams(25.).build())
                .build();

        Optional<DataPoint> result = repository.upsertDataPoint(incoming);

        assertTrue(result.isPresent());
        DataPoint updated = result.get();
        assertEquals(25.0, updated.getCarbs().getGrams(), 0.001);
        assertEquals(100.0, updated.getSensorGlucose().getMgdl(), 0.001);
        assertNull(updated.getManualGlucose());
    }
}
