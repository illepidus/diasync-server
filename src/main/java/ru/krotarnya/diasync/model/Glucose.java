package ru.krotarnya.diasync.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Glucose {
    private Double mgdl;

    public static Glucose ofMgdl(Double mgdl) {
        return new Glucose(mgdl);
    }
}
