package ru.krotarnya.diasync.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class Carbs {
    private Double grams;

    @Nullable
    private String description;
}
