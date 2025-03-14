package ru.krotarnya.diasync.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Embeddable
@AllArgsConstructor
public class Calibration {
    private final double slope;
    private final double intercept;

    public Calibration() {
        this(1.0, 0.0);
    }
}
