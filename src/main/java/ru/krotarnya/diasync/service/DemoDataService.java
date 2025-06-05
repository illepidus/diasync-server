package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.model.DataPoint;


@Service
public class DemoDataService {
    private static final Duration BACKOFF_INTERVAL = Duration.ofSeconds(5);
    private static final Logger logger = LoggerFactory.getLogger(DemoDataService.class);

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final DataPointService dataPointService;
    private final List<DemoDataGenerator> generators;
    private final String userId;
    private final Duration prefillInterval;

    public DemoDataService(
            DataPointService dataPointService,
            List<DemoDataGenerator> generators,
            @Value("${demo.userId}") String userId,
            @Value("${demo.prefill.interval}") Duration prefillInterval)
    {
        this.dataPointService = dataPointService;
        this.generators = generators;
        this.userId = userId;
        this.prefillInterval = prefillInterval;

        cleanUp();
        prefill();
        schedule();
    }

    private void cleanUp() {
        dataPointService.truncateDataPoints(userId);
    }

    private void prefill() {
        generators.forEach(this::prefill);
    }

    private void prefill(DemoDataGenerator generator) {
        Instant finish = Instant.now();
        Instant start = finish.minus(prefillInterval);

        List<DataPoint> dataPoints = Stream.iterate(start, i -> i.isBefore(finish), i -> i.plus(generator.period()))
                .map(generator::generate)
                .toList();

        dataPointService.addDataPoints(dataPoints);
    }


    private void schedule() {
        generators.forEach(this::schedule);
    }

    private void schedule(DemoDataGenerator generator) {
        executor.submit(generatorTask(generator));
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private Runnable generatorTask(DemoDataGenerator generator) {
        return () -> {
            try {
                while (true) {
                    Thread.sleep(generator.period());
                    dataPointService.addDataPoint(generator.generate(Instant.now()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logAndReschedule(generator, e);
            }
        };
    }

    private void logAndReschedule(DemoDataGenerator generator, Exception e) {
        logger.error("Generator {} failed with {}: Restarting in {}",
                generator.getClass().getSimpleName(),
                e.getMessage(),
                BACKOFF_INTERVAL);
        executor.submit(() -> {
            try {
                Thread.sleep(BACKOFF_INTERVAL);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
            schedule(generator);
        });
    }
}
