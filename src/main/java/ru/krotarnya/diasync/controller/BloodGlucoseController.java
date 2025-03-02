package ru.krotarnya.diasync.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.krotarnya.diasync.model.BloodPoint;
import ru.krotarnya.diasync.model.Glucose;

/**
 * @author ivblinov
 */
@Controller
public class BloodGlucoseController {
    private final Map<String, List<BloodPoint>> bloodPoints = new ConcurrentHashMap<>();
    private final Map<String, List<FluxSink<BloodPoint>>> subscribers = new ConcurrentHashMap<>();

    @QueryMapping
    public List<BloodPoint> bloodPoints(
            @Argument String userId,
            @Argument Instant from,
            @Argument Instant to)
    {
        return bloodPoints.getOrDefault(userId, new ArrayList<>()).stream()
                .filter(bp -> !bp.timestamp().isBefore(from) && !bp.timestamp().isAfter(to))
                .collect(Collectors.toList());
    }

    @MutationMapping
    public BloodPoint addBloodPoint(
            @Argument String userId,
            @Argument String sensorId,
            @Argument double glucose)
    {
        BloodPoint point = new BloodPoint(
                userId,
                sensorId,
                Instant.now(),
                new Glucose(glucose)
        );

        bloodPoints.computeIfAbsent(userId, k -> new ArrayList<>()).add(point);
        Collection<FluxSink<BloodPoint>> sinks = subscribers.getOrDefault(userId, new ArrayList<>());
        sinks.forEach(sink -> sink.next(point));

        return point;
    }

    @SubscriptionMapping
    public Flux<BloodPoint> bloodPointAdded(@Argument String userId) {
        return Flux.create(sink -> {
            subscribers.computeIfAbsent(userId, k -> new ArrayList<>()).add(sink);
            sink.onDispose(() -> subscribers.get(userId).remove(sink));
        });
    }
}
