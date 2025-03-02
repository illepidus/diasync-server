package ru.krotarnya.diasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiasyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiasyncApplication.class, args);
    }
}
