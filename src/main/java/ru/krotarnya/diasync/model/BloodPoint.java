package ru.krotarnya.diasync.model;

import java.time.Instant;

import jakarta.persistence.*;

/**
 * @author ivblinov
 */

@Entity
@Table(name = "blood_points")
public class BloodPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String userId;
    public String sensorId;
    public Instant timestamp;

    @Embedded
    public Glucose glucose;

    public BloodPoint() {}

    public BloodPoint(String userId, String sensorId, Instant timestamp, Glucose glucose) {
        this.userId = userId;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.glucose = glucose;
    }
}
