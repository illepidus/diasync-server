package ru.krotarnya.diasync.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DiagnosticsController {
    private final Environment environment;

    @Value("${springdoc.api-docs.path}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path}")
    private String swaggerUiPath;

    @Value("${spring.graphql.graphiql.enabled}")
    private String graphiqlEnabled;

    @Value("${spring.graphql.websocket.path}")
    private String webSocketPath;

    public DiagnosticsController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping(value = "/diagnostics", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();

        diagnostics.put("springdoc.api-docs.path", apiDocsPath);
        diagnostics.put("springdoc.swagger-ui.path", swaggerUiPath);
        diagnostics.put("spring.graphql.graphiql.enabled", graphiqlEnabled);
        diagnostics.put("spring.graphql.websocket.path", webSocketPath);

        diagnostics.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        diagnostics.put("java.version", System.getProperty("java.version"));
        diagnostics.put("server.port", environment.getProperty("server.port"));

        return diagnostics;
    }
}
