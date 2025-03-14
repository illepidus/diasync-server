package ru.krotarnya.diasync.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.krotarnya.diasync.model.BloodPoint;

public interface BloodPointRepository extends JpaRepository<BloodPoint, Long> {
    List<BloodPoint> findByUserIdAndTimestampBetween(String userId, Instant from, Instant to);

    void deleteByTimestampBefore(Instant before);

    @Transactional
    default List<BloodPoint> addBloodPoints(List<BloodPoint> bloodPoints) {
        return bloodPoints.stream().map(this::addBloodPoint).flatMap(Optional::stream).toList();
    }

    @Transactional
    default Optional<BloodPoint> addBloodPoint(BloodPoint bloodPoint) {
        BloodPoint existing = findByUserIdAndSensorIdAndTimestamp(
                bloodPoint.getUserId(),
                bloodPoint.getSensorId(),
                bloodPoint.getTimestamp());

        if (existing != null) {
            return Optional.empty();
        } else {
            return Optional.of(save(bloodPoint));
        }
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    BloodPoint findByUserIdAndSensorIdAndTimestamp(String userId, String sensorId, Instant timestamp);
}
