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
        name = "DATA_POINTS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_DATA_POINTS_USER_TIME",
                        columnNames = {"USER_ID", "TIMESTAMP"})
        },
        indexes = {
                @Index(name = "IDX_USER_ID_TIMESTAMP", columnList = "USER_ID, TIMESTAMP"),
                @Index(name = "IDX_TIMESTAMP", columnList = "TIMESTAMP"),
                @Index(name = "IDX_USER_ID_UPDATE_TS_ID", columnList = "USER_ID, UPDATE_TIMESTAMP, ID")
        })
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public final class DataPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "TIMESTAMP", columnDefinition = "TIMESTAMP(9)")
    private Instant timestamp;

    @Nullable
    @Column(name = "UPDATE_TIMESTAMP", columnDefinition = "TIMESTAMP(9)")
    private Instant updateTimestamp;

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

    public DataPoint withUpdateTimestamp(Instant timestamp) {
        return toBuilder().updateTimestamp(timestamp).build();
    }

    public DataPoint withoutIdAndUpdateTimestamp() {
        return toBuilder().id(null).updateTimestamp(null).build();
    }
}
