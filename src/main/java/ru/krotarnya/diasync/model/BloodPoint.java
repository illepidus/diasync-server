package ru.krotarnya.diasync.model;

import java.time.Instant;

import jakarta.persistence.*;

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
