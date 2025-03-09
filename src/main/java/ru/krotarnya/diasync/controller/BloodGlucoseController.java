package ru.krotarnya.diasync.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.krotarnya.diasync.model.BloodPoint;
import ru.krotarnya.diasync.model.Calibration;
import ru.krotarnya.diasync.model.Glucose;
import ru.krotarnya.diasync.repository.BloodPointRepository;

@Controller
public class BloodGlucoseController {
    private final BloodPointRepository bloodPointRepository;
    private final Map<String, List<FluxSink<BloodPoint>>> subscribers = new ConcurrentHashMap<>();

    @Autowired
    public BloodGlucoseController(BloodPointRepository bloodPointRepository) {
        this.bloodPointRepository = bloodPointRepository;
    }

    @QueryMapping
    public List<BloodPoint> bloodPoints(
            @Argument String userId,
            @Argument Instant from,
            @Argument Instant to)
    {
        return bloodPointRepository.findByUserIdAndTimestampBetween(userId, from, to);
    }

    @MutationMapping
    public BloodPoint addBloodPoint(
            @Argument String userId,
            @Argument String sensorId,
            @Argument Instant timestamp,
            @Argument Double glucose,
            @Argument Double calibrationSlope,
            @Argument Double calibrationIntercept)
    {
        BloodPoint point = new BloodPoint(
                userId,
                sensorId,
                timestamp,
                new Glucose(glucose),
                new Calibration(calibrationSlope, calibrationIntercept));
        bloodPointRepository.save(point);
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
