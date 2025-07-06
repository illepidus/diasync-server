package ru.krotarnya.diasync.repository;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.service.UserLockService;

@Repository
@Transactional
@SuppressWarnings("ClassEscapesDefinedScope")
public class DataPointRepositoryImpl implements DataPointRepositoryCustom {
    private final DataPointRepositoryJpa jpa;

    public DataPointRepositoryImpl(@Qualifier("dataPointRepositoryJpa") DataPointRepositoryJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public List<DataPoint> addDataPoints(String userId, List<DataPoint> dataPoints, UserLockService lockService) {
        lockService.lockOrCreate(userId);

        return dataPoints.stream()
                .map(this::upsertDataPoint)
                .toList();
    }

    @Transactional
    protected DataPoint upsertDataPoint(DataPoint newPoint) {
        Optional<DataPoint> maybeExisting = jpa.findByUserIdAndTimestamp(newPoint.getUserId(), newPoint.getTimestamp());

        if (maybeExisting.isEmpty()) return jpa.save(newPoint);

        DataPoint existing = maybeExisting.get();
        if (existing.withoutIdAndUpdateTimestamp().equals(newPoint.withoutIdAndUpdateTimestamp())) return existing;

        return updateExisting(existing, newPoint);
    }

    @Transactional
    protected DataPoint updateExisting(DataPoint existingPoint, DataPoint newPoint) {
        DataPoint updated = newPoint.toBuilder()
                .id(existingPoint.getId())
                .carbs(updateField(existingPoint, newPoint, DataPoint::getCarbs))
                .manualGlucose(updateField(existingPoint, newPoint, DataPoint::getManualGlucose))
                .sensorGlucose(updateField(existingPoint, newPoint, DataPoint::getSensorGlucose))
                .build();
        return jpa.save(updated);
    }

    private <T> @Nullable T updateField(DataPoint existingPoint, DataPoint newPoint, Function<DataPoint, T> extractor) {
        if (extractor.apply(newPoint) != null) return extractor.apply(newPoint);
        if (extractor.apply(existingPoint) != null) return extractor.apply(existingPoint);
        return null;
    }
}
