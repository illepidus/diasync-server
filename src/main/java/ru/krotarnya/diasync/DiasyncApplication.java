package ru.krotarnya.diasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class DiasyncApplication {

    private static final Logger log = LoggerFactory.getLogger(DiasyncApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DiasyncApplication.class, args);
    }

    // Модели данных
    public record BloodPoint(String userId, String sensorId, Instant timestamp, Glucose glucose) {}

    public record Glucose(double mgdl) {}

    @Controller
    public static class BloodGlucoseController {
        private final Map<String, List<BloodPoint>> bloodPoints = new ConcurrentHashMap<>();
        private final Map<String, List<FluxSink<BloodPoint>>> subscribers = new ConcurrentHashMap<>();

        @QueryMapping
        public List<BloodPoint> bloodPoints(
                @Argument String userId,
                @Argument Instant from,
                @Argument Instant to)
        {
            return bloodPoints.getOrDefault(userId, new ArrayList<>()).stream()
                    .filter(bp -> !bp.timestamp().isBefore(from) && !bp.timestamp().isAfter(to))
                    .collect(Collectors.toList());
        }

        @MutationMapping
        public BloodPoint addBloodPoint(
                @Argument String userId,
                @Argument String sensorId,
                @Argument double glucose)
        {
            BloodPoint point = new BloodPoint(
                    userId,
                    sensorId,
                    Instant.now(),
                    new Glucose(glucose)
            );

            bloodPoints.computeIfAbsent(userId, k -> new ArrayList<>()).add(point);

            Collection<FluxSink<BloodPoint>> sinks = subscribers.getOrDefault(userId, new ArrayList<>());
            sinks.forEach(sink -> sink.next(point));

            return point;
        }

        @SubscriptionMapping
        public Flux<BloodPoint> bloodPointAdded(@Argument String userId) {
            log.info("Subscription started for userId: {}", userId);
            return Flux.create(sink -> {
                subscribers.computeIfAbsent(userId, k -> new ArrayList<>()).add(sink);
                sink.onDispose(() -> {
                    subscribers.get(userId).remove(sink);
                    log.info("Subscription disposed for userId: {}", userId);
                });
            });
        }
    }

    @Component
    public static class BloodPointGenerator {
        private final BloodGlucoseController controller;
        private final Random random = new Random();

        public BloodPointGenerator(BloodGlucoseController controller) {
            this.controller = controller;
        }

        @Scheduled(fixedRate = 5000)
        public void generateBloodPoint() {
            double glucoseValue = 70 + (random.nextDouble() * 110);
            BloodPoint point = controller.addBloodPoint("demo", "sensor1", glucoseValue);
            System.out.println("Generated new blood point: " + point);
        }
    }

    @Bean
    public WebFilter corsWebFilter() {
        return (exchange, chain) -> {

            log.info("Processing request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
            log.info("Request headers: {}", exchange.getRequest().getHeaders());

            String origin = exchange.getRequest().getHeaders().getOrigin();
            if (origin != null) {
                exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", origin);
            }

            exchange.getResponse().getHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponse()
                    .getHeaders()
                    .add("Access-Control-Allow-Headers",
                            "Origin, Content-Type, Accept, Connection, Upgrade, Sec-WebSocket-Key, " +
                                    "Sec-WebSocket-Version");
            exchange.getResponse().getHeaders().add("Access-Control-Allow-Credentials", "true");

            if ("OPTIONS".equals(exchange.getRequest().getMethod().toString())) {
                exchange.getResponse().getHeaders().add("Access-Control-Max-Age", "3600");
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}
