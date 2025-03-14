package ru.krotarnya.diasync.service;

import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krotarnya.diasync.repository.BloodPointRepository;

@Service
public class BloodDataCleaner {
    private static final Logger logger = LoggerFactory.getLogger(BloodDataCleaner.class);
    private static final int DAYS_TO_KEEP = 30;
    private final BloodPointRepository bloodPointRepository;

    public BloodDataCleaner(BloodPointRepository bloodPointRepository) {
        this.bloodPointRepository = bloodPointRepository;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void cleanOldRecords() {
        Instant before = Instant.now().minus(Duration.ofDays(DAYS_TO_KEEP));
        bloodPointRepository.deleteByTimestampBefore(before);
        logger.info("Deleted records older than {} days at {}", DAYS_TO_KEEP,  Instant.now());
    }
}
