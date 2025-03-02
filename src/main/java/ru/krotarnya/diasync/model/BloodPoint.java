package ru.krotarnya.diasync.model;

import java.time.Instant;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * @author ivblinov
 */

@Entity
@Table(
        name = "blood_points",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_blood_points_user_sensor_time", columnNames = {
                        "user_id",
                        "sensor_id",
                        "timestamp"})
        },
        indexes = {
                @Index(name = "idx_userid_timestamp", columnList = "userId, timestamp"),
                @Index(name = "idx_timestamp", columnList = "timestamp")
        })
public class BloodPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public final String userId;
    public final String sensorId;
    public final Instant timestamp;

    @Embedded
    public final Glucose glucose;

    @Embedded
    public final Calibration calibration;

    public BloodPoint() {
        this(null, null, null, null, null);
    }

    public BloodPoint(
            String userId,
            String sensorId,
            Instant timestamp,
            Glucose glucose,
            Calibration calibration)
    {
        this.userId = userId;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.glucose = glucose;
        this.calibration = calibration;
    }
}
