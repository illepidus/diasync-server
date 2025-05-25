package ru.krotarnya.diasync.repository;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ru.krotarnya.diasync.model.DataPoint;
import ru.krotarnya.diasync.service.UserLockService;

/**
 * @author ivblinov
 */
public interface DataPointRepositoryCustom {
    @Transactional
    List<DataPoint> addDataPoints(String userId, List<DataPoint> dataPoints, UserLockService lockService);
}
