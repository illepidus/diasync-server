package ru.krotarnya.diasync.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "data_points",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_data_points_user_time", columnNames = {
                        "user_id",
                        "timestamp"})
        },
        indexes = {
                @Index(name = "idx_userid_timestamp", columnList = "userId, timestamp"),
                @Index(name = "idx_timestamp", columnList = "timestamp")
        })
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public final class DataPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Instant timestamp;

    @Embedded
    @Nullable
    @AttributeOverrides({
            @AttributeOverride(name = "mgdl", column = @Column(name = "sensor_mgdl")),
            @AttributeOverride(name = "sensorId", column = @Column(name = "sensor_id"))
    })
    private SensorGlucose sensorGlucose;

    @Embedded
    @Nullable
    @AttributeOverride(name = "mgdl", column = @Column(name = "manual_mgdl"))
    private ManualGlucose manualGlucose;

    @Embedded
    @Nullable
    private Carbs carbs;

    public DataPoint withoutId() {
        return toBuilder().id(null).build();
    }
}
