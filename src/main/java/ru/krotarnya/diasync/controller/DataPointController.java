package ru.krotarnya.diasync.controller;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.repository.DataPointRepository;

@Controller
public class DataPointController {
    private final DataPointRepository dataPointRepository;
    private final Map<String, List<FluxSink<DataPoint>>> subscribers = new ConcurrentHashMap<>();

    @Autowired
    public DataPointController(DataPointRepository dataPointRepository) {
        this.dataPointRepository = dataPointRepository;
    }

    @QueryMapping
    public List<DataPoint> getDataPoints(
            @Argument String userId,
            @Nullable @Argument Instant from,
            @Nullable @Argument Instant to) {
        return dataPointRepository.findByUserIdAndTimestampBetween(
                userId,
                Optional.ofNullable(from).orElse(Instant.EPOCH),
                Optional.ofNullable(to).orElse(Instant.now()));
    }

    @MutationMapping
    public List<DataPoint> addDataPoints(@Argument List<DataPoint> dataPoints) {
        List<DataPoint> result = dataPointRepository.addDataPoints(dataPoints);
        result.forEach(p -> subscribers.getOrDefault(p.getUserId(), List.of()).forEach(sink -> sink.next(p)));
        return result;
    }

    @SubscriptionMapping
    public Flux<DataPoint> onDataPointAdded(@Argument String userId) {
        return Flux.create(sink -> {
            subscribers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sink);
            sink.onDispose(() -> Optional.ofNullable(subscribers.get(userId))
                    .filter(sinks -> sinks.remove(sink))
                    .filter(List::isEmpty)
                    .ifPresent(sinks -> subscribers.remove(userId)));
        });
    }
}
