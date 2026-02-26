package ru.krotarnya.diasync.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.Disposable;
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
    public DeferredResult<List<DataPoint>> getDataPointsLongPoll(
            @RequestParam("userId") String userId,
            @RequestParam("since") Instant since,
            @RequestParam(value = "timeoutMs", defaultValue = "75000") long timeoutMs
    ) {
        DeferredResult<List<DataPoint>> dr = new DeferredResult<>(timeoutMs);

        List<DataPoint> now = dataPointService.getDataPointsUpdatedAfter(userId, since);
        if (!now.isEmpty()) {
            dr.setResult(now);
            return dr;
        }

        Disposable sub = dataPointService.onDataPointAdded(userId)
                .filter(dp -> dp.getUpdateTimestamp() != null && dp.getUpdateTimestamp().isAfter(since))
                .next()
                .subscribe(
                        dp -> dr.setResult(dataPointService.getDataPointsUpdatedAfter(userId, since)),
                        dr::setErrorResult);

        dr.onCompletion(sub::dispose);
        dr.onTimeout(() -> dr.setResult(List.of()));
        return dr;
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
