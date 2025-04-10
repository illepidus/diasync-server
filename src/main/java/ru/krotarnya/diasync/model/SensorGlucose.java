package ru.krotarnya.diasync.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class SensorGlucose {
    private Double mgdl;
    private String sensorId;

    @Embedded
    @Nullable
    private Calibration calibration;
}
