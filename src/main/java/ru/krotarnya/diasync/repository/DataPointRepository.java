package ru.krotarnya.diasync.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.krotarnya.diasync.model.DataPoint;

public interface DataPointRepository extends JpaRepository<DataPoint, Long> {
    List<DataPoint> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to);

    @Transactional
    int deleteByTimestampBefore(Instant before);

    @Transactional
    void deleteByUserId(String userId);

    @Transactional
    default List<DataPoint> addDataPoints(List<DataPoint> dataPoints) {
        return dataPoints.stream().map(this::upsertDataPoint).flatMap(Optional::stream).toList();
    }

    @Transactional
    default Optional<DataPoint> upsertDataPoint(DataPoint dataPoint) {
        DataPoint existing = findByUserIdAndTimestamp(dataPoint.getUserId(), dataPoint.getTimestamp());

        if (existing != null) {
            if (existing.toBuilder().id(null).build().equals(dataPoint)) return Optional.empty();
            return Optional.of(save(dataPoint.toBuilder().id(existing.getId()).build()));
        } else {
            return Optional.of(save(dataPoint));
        }
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataPoint findByUserIdAndTimestamp(String userId, Instant timestamp);
}
