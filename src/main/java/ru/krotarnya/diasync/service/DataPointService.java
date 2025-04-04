package ru.krotarnya.diasync.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.repository.DataPointRepository;

@Service
public class DataPointService {
    private final DataPointRepository dataPointRepository;
    private final Map<String, List<FluxSink<DataPoint>>> subscribers = new ConcurrentHashMap<>();

    @Autowired
    public DataPointService(DataPointRepository dataPointRepository) {
        this.dataPointRepository = dataPointRepository;
    }

    public List<DataPoint> getDataPoints(String userId, Instant from, Instant to) {
        return dataPointRepository.findByUserIdAndTimestampBetween(
                userId,
                Optional.ofNullable(from).orElse(Instant.EPOCH),
                Optional.ofNullable(to).orElse(Instant.now()));
    }

    public List<DataPoint> addDataPoints(List<DataPoint> dataPoints) {
        List<DataPoint> result = dataPointRepository.addDataPoints(dataPoints);
        result.forEach(p -> subscribers.getOrDefault(p.getUserId(), List.of())
                .forEach(sink -> sink.next(p)));
        return result;
    }

    public int truncateDataPoints(String userId) {
        return dataPointRepository.deleteByUserId(userId);
    }

    public Flux<DataPoint> onDataPointAdded(String userId) {
        return Flux.create(sink -> {
            subscribers.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(sink);
            sink.onDispose(() -> Optional.ofNullable(subscribers.get(userId))
                    .filter(sinks -> sinks.remove(sink))
                    .filter(List::isEmpty)
                    .ifPresent(sinks -> subscribers.remove(userId)));
        });
    }
}
