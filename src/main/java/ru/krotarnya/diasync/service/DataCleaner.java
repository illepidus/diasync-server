package ru.krotarnya.diasync.service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.repository.DataPointRepository;

@Service
public class DataCleaner {
    private static final Logger logger = LoggerFactory.getLogger(DataCleaner.class);
    private static final int DAYS_TO_KEEP = 30;
    private final DataPointRepository dataPointRepository;

    public DataCleaner(DataPointRepository dataPointRepository) {
        this.dataPointRepository = dataPointRepository;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void cleanOldRecords() {
        Instant before = Instant.now().minus(Duration.ofDays(DAYS_TO_KEEP));
        int pointCount = dataPointRepository.deleteByTimestampBefore(before);
        logger.info("Deleted {} records older than {} days at {}", pointCount, DAYS_TO_KEEP, Instant.now());
    }
}
