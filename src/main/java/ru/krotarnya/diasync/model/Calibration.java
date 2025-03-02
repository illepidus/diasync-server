package ru.krotarnya.diasync.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

/**
 * @author ivblinov
 */
@Embeddable
public class Calibration {
    public final double slope;
    public final double intercept;

    public Calibration() {
        this(1.0, 0.0);
    }

    public Calibration(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }
}
