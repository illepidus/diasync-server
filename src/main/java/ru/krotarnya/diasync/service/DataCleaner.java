package ru.krotarnya.diasync.service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DataCleaner {
    private static final Logger logger = LoggerFactory.getLogger(DataCleaner.class);
    private final DataPointService dataPointService;
    private final int daysToKeep;

    public DataCleaner(
            DataPointService dataPointService,
            @Value("${diasync.database.cleanup.days-to-keep}") int daysToKeep)
    {
        this.dataPointService = dataPointService;
        this.daysToKeep = daysToKeep;
    }

    @Transactional
    @Scheduled(fixedDelayString = "#{T(java.time.Duration).parse('${diasync.database.cleanup.interval}').toMillis()}")
    public void cleanOldRecords() {
        Instant before = Instant.now().minus(Duration.ofDays(daysToKeep));
        int pointCount = dataPointService.deleteByTimestampBefore(before);
        logger.info("Deleted {} records older than {} days at {}", pointCount, daysToKeep, Instant.now());
    }
}
