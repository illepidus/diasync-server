package ru.krotarnya.diasync.repository;

import jakarta.annotation.Nullable;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.krotarnya.diasync.model.DataPoint;

public interface DataPointRepository extends JpaRepository<DataPoint, Long> {
    @Transactional
    List<DataPoint> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    int deleteByTimestampBefore(Instant before);

    @Transactional
    int deleteByUserId(String userId);

    @Transactional
    default List<DataPoint> addDataPoints(List<DataPoint> dataPoints) {
        return dataPoints.stream().map(this::upsertDataPoint).flatMap(Optional::stream).toList();
    }

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DataPoint> findByUserIdAndTimestamp(String userId, Instant timestamp);


    @Transactional
    default Optional<DataPoint> upsertDataPoint(DataPoint newPoint) {
        Optional<DataPoint> maybeExisting = findByUserIdAndTimestamp(newPoint.getUserId(), newPoint.getTimestamp());

        if (maybeExisting.isEmpty()) {
            return Optional.of(save(newPoint));
        }

        DataPoint existing = maybeExisting.get();

        if (existing.withoutId().equals(newPoint.withoutId())) {
            return Optional.empty();
        }

        DataPoint updated = updateExisting(existing, newPoint);
        return Optional.of(save(updated));
    }

    @Transactional
    default DataPoint updateExisting(DataPoint existing, DataPoint dataPoint) {
        DataPoint updated = dataPoint.toBuilder()
                .id(existing.getId())
                .carbs(updateField(existing, dataPoint, DataPoint::getCarbs))
                .manualGlucose(updateField(existing, dataPoint, DataPoint::getManualGlucose))
                .sensorGlucose(updateField(existing, dataPoint, DataPoint::getSensorGlucose))
                .build();

        return save(updated);
    }

    private <T> @Nullable T updateField(DataPoint existing, DataPoint dataPoint, Function<DataPoint, T> extractor) {
        if (extractor.apply(dataPoint) != null) return extractor.apply(dataPoint);
        if (extractor.apply(existing) != null) return extractor.apply(existing);
        return null;
    }
}
