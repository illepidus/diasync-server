package ru.krotarnya.diasync.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.krotarnya.diasync.model.BloodPoint;

/**
 * @author ivblinov
 */
public interface BloodPointRepository extends JpaRepository<BloodPoint, Long> {
    List<BloodPoint> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to);

    void deleteByTimestampBefore(Instant thirtyDaysAgo);
}
