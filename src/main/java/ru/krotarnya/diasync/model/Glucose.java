package ru.krotarnya.diasync.model;

import jakarta.persistence.Embeddable;

/**
 * @author ivblinov
 */
@Embeddable
public class Glucose {
    public final double mgdl;

    public Glucose() {
        this(0);
    }

    public Glucose(double mgdl) {
        this.mgdl = mgdl;
    }
}
