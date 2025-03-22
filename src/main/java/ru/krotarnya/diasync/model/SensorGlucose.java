package ru.krotarnya.diasync.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorGlucose {
    private Float mgdl;
    private String sensorId;

    @Embedded
    @Nullable
    private Calibration calibration;
}
