package ru.krotarnya.diasync.model;

import java.time.Instant;

/**
 * @author ivblinov
 */ // Модели данных
public record BloodPoint(String userId, String sensorId, Instant timestamp, Glucose glucose) {}
