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

    void deleteByTimestampBefore(Instant before);

    @Transactional
    default List<DataPoint> addDataPoints(List<DataPoint> dataPoints) {
        return dataPoints.stream().map(this::addDataPoint).flatMap(Optional::stream).toList();
    }

    @Transactional
    default Optional<DataPoint> addDataPoint(DataPoint dataPoint) {
        DataPoint existing = findByUserIdAndTimestamp(
                dataPoint.getUserId(),
                dataPoint.getTimestamp());

        if (existing != null) {
            return Optional.empty();
        } else {
            return Optional.of(save(dataPoint));
        }
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DataPoint findByUserIdAndTimestamp(String userId, Instant timestamp);
}
