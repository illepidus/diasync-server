package ru.krotarnya.diasync.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DiagnosticsController {
    private static final Logger log = LoggerFactory.getLogger(DiagnosticsController.class);

    private final Environment environment;

    @Value("${springdoc.api-docs.path:/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path:/swagger}")
    private String swaggerUiPath;

    public DiagnosticsController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping(value = "/diagnostics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        diagnostics.put("springdoc.api-docs.path", apiDocsPath);
        diagnostics.put("springdoc.swagger-ui.path", swaggerUiPath);

        diagnostics.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));

        log.info("Diagnostics endpoint called. api-docs.path={}", apiDocsPath);

        diagnostics.put("java.version", System.getProperty("java.version"));
        diagnostics.put("server.port", environment.getProperty("server.port"));

        return Mono.just(diagnostics);
    }
}
