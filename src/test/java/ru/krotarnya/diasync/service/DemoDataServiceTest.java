package ru.krotarnya.diasync.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.krotarnya.diasync.model.DataPoint;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author ivblinov
 */
class DemoDataServiceTest {
    @Test
    void shouldPrefillAndScheduleGenerators() {
        DataPointService mockService = mock(DataPointService.class);
        DemoDataGenerator mockGen = mock(DemoDataGenerator.class);

        Instant baseTime = Instant.now();
        when(mockGen.period()).thenReturn(Duration.ofSeconds(1));
        when(mockGen.generate(any())).thenReturn(DataPoint.builder()
                .userId("demo-user")
                .timestamp(baseTime)
                .build());

        new DemoDataService(
                mockService,
                List.of(mockGen),
                "demo-user",
                Duration.ofSeconds(3));

        verify(mockService, atLeastOnce()).addDataPoints(any());
    }
}
