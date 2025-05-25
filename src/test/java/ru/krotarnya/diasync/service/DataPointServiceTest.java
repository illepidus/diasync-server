package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.krotarnya.diasync.model.Carbs;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.model.ManualGlucose;
import ru.krotarnya.diasync.model.SensorGlucose;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SuppressWarnings("DataFlowIssue")
class DataPointServiceTest {
    private static final String TEST_USER_ID = "test-user";
    private static final Instant TEST_TIMESTAMP = Instant.parse("2023-01-01T10:00:00Z");

    @Autowired
    private DataPointService service;

    @BeforeEach
    public void init() {
        service.truncateDataPoints(TEST_USER_ID);
    }

    @RepeatedTest(3)
    void shouldHandleMultipleConcurrentUpsert() {
        DataPoint.DataPointBuilder builder = DataPoint.builder().userId(TEST_USER_ID).timestamp(TEST_TIMESTAMP);

        DataPoint p1 = builder.carbs(Carbs.builder().grams(20.).build()).build();
        DataPoint p2 = builder.sensorGlucose(SensorGlucose.builder().mgdl(100.).build()).build();
        DataPoint p3 = builder.manualGlucose(ManualGlucose.builder().mgdl(110.).build()).build();

        addConcurrently(p1, p2, p3);

        List<DataPoint> result = service.getDataPoints(TEST_USER_ID, TEST_TIMESTAMP, TEST_TIMESTAMP);

        assertEquals(1, result.size());
        assertEquals(20., result.getFirst().getCarbs().getGrams());
        assertEquals(100., result.getFirst().getSensorGlucose().getMgdl());
        assertEquals(110., result.getFirst().getManualGlucose().getMgdl());
    }

    private void addConcurrently(DataPoint... points) {
        List<Runnable> tasks = Arrays.stream(points)
                .map(point -> (Runnable) () -> service.addDataPoint(point))
                .toList();

        executeConcurrently(tasks);
    }

    private void executeConcurrently(Collection<Runnable> tasks) {
        List<Thread> threads = tasks.stream().map(Thread::new).toList();
        threads.forEach(Thread::start);
        try {
            for (Thread thread : threads) {
                thread.join(Duration.ofSeconds(10));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
