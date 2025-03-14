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
    private double mgdl;

    public static Glucose ofMgdl(double mgdl) {
        return new Glucose(mgdl);
    }
}
