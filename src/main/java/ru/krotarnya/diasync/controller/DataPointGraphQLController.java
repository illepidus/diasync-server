package ru.krotarnya.diasync.controller;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.service.DataPointService;

@Controller
public final class DataPointGraphQLController {
    private final DataPointService dataPointService;

    @Autowired
    public DataPointGraphQLController(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
    }

    @QueryMapping
    public List<DataPoint> getDataPoints(
            @Argument String userId,
            @Nullable @Argument Instant from,
            @Nullable @Argument Instant to) {
        return dataPointService.getDataPoints(userId, from, to);
    }

    @MutationMapping
    public List<DataPoint> addDataPoints(@Argument List<DataPoint> dataPoints) {
        return dataPointService.addDataPoints(dataPoints);
    }

    @MutationMapping
    public int truncateDataPoints(@Argument String userId) {
        return dataPointService.truncateDataPoints(userId);
    }

    @SubscriptionMapping
    public Flux<DataPoint> onDataPointAdded(@Argument String userId) {
        return dataPointService.onDataPointAdded(userId);
    }
}
