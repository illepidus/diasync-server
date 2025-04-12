package ru.krotarnya.diasync.controller;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import ru.krotarnya.diasync.model.DataPoint;

public interface DataPointController {
    List<DataPoint> getDataPoints(String userId, @Nullable Instant from, @Nullable Instant to);

    List<DataPoint> addDataPoints(List<DataPoint> dataPoints);

    int truncateDataPoints(String userId);
}
