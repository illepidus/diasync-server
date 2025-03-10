package ru.krotarnya.diasync.controller;

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
import ru.krotarnya.diasync.model.BloodPoint;
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
    public List<BloodPoint> getBloodPoints(@Argument String userId, @Argument Instant from, @Argument Instant to) {
        return bloodPointRepository.findByUserIdAndTimestampBetween(userId, from, to);
    }

    @MutationMapping
    public List<BloodPoint> addBloodPoints(@Argument List<BloodPoint> bloodPoints) {
        bloodPointRepository.saveAll(bloodPoints)
                .forEach(p -> subscribers.getOrDefault(p.getUserId(), List.of()).forEach(sink -> sink.next(p)));

        return bloodPoints;
    }

    @SubscriptionMapping
    public Flux<BloodPoint> onBloodPointAdded(@Argument String userId) {
        return Flux.create(sink -> {
            subscribers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sink);
            sink.onDispose(() -> Optional.ofNullable(subscribers.get(userId))
                    .filter(sinks -> sinks.remove(sink))
                    .filter(List::isEmpty)
                    .ifPresent(sinks -> subscribers.remove(userId))
            );
        });
    }
}
