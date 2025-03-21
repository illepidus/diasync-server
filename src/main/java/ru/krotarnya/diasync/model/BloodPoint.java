package ru.krotarnya.diasync.model;

import jakarta.annotation.Nullable;
import java.time.Instant;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BloodPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String sensorId;
    private Instant timestamp;

    @Embedded
    private Glucose glucose;

    @Embedded
    @Nullable
    private Calibration calibration;
}
