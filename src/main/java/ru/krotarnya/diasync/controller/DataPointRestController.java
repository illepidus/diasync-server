package ru.krotarnya.diasync.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.service.DataPointService;

@RestController
public final class DataPointRestController extends RestApiController implements DataPointController {
    private final DataPointService dataPointService;

    @Autowired
    public DataPointRestController(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
    }

    @GetMapping("getDataPointsLongPoll")
    public Mono<List<DataPoint>> getDataPointsLongPoll(
            @RequestParam("userId") String userId,
            @RequestParam("since") Instant since,
            @RequestParam(value = "timeoutMs", defaultValue = "75000") long timeoutMs
    )
    {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return Mono.fromSupplier(() -> dataPointService.getDataPointsUpdatedAfter(userId, since))
                .flatMap(now -> {
                    if (!now.isEmpty()) {
                        return Mono.just(now);
                    }

                    return dataPointService.onDataPointAdded(userId)
                            .filter(dp -> dp.getUpdateTimestamp() != null && dp.getUpdateTimestamp().isAfter(since))
                            .next()
                            .timeout(timeout)
                            .flatMap(dp -> Mono.fromSupplier(
                                    () -> dataPointService.getDataPointsUpdatedAfter(userId, since)))
                            .onErrorResume(throwable -> Mono.just(List.of()));
                });
    }

    @Override
    @GetMapping("getDataPoints")
    public List<DataPoint> getDataPoints(
            @RequestParam("userId") String userId,
            @RequestParam(value = "from", required = false) Instant from,
            @RequestParam(value = "to", required = false) Instant to)
    {
        return dataPointService.getDataPoints(userId, from, to);
    }

    @Override
    @PostMapping("addDataPoints")
    public List<DataPoint> addDataPoints(@RequestBody List<DataPoint> dataPoints) {
        return dataPointService.addDataPoints(dataPoints);
    }

    @Override
    @DeleteMapping("truncateDataPoints")
    public int truncateDataPoints(@RequestParam("userId") String userId) {
        return dataPointService.truncateDataPoints(userId);
    }
}
