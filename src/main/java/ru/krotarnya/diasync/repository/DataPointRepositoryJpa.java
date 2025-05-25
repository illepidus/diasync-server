package ru.krotarnya.diasync.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.krotarnya.diasync.model.DataPoint;

interface DataPointRepositoryJpa extends JpaRepository<DataPoint, Long> {
    @Transactional
    int deleteByTimestampBefore(Instant before);

    @Transactional
    int deleteByUserId(String userId);

    Optional<DataPoint> findByUserIdAndTimestamp(String userId, Instant timestamp);

    List<DataPoint> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to);
}
