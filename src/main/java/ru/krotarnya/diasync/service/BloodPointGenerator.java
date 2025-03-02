package ru.krotarnya.diasync.service;

import java.util.Random;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.krotarnya.diasync.controller.BloodGlucoseController;

/**
 * @author ivblinov
 */
@Component
public class BloodPointGenerator {
    private final BloodGlucoseController controller;
    private final Random random = new Random();

    public BloodPointGenerator(BloodGlucoseController controller) {
        this.controller = controller;
    }

    @Scheduled(fixedRate = 5000)
    public void generateBloodPoint() {
        controller.addBloodPoint("demo", "sensor1", 0 + (random.nextDouble() * 110));
    }
}
