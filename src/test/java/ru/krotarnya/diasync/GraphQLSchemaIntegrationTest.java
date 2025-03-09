package ru.krotarnya.diasync;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphQLSchemaIntegrationTest {
    private static final String GRAPHQL_ENDPOINT = "/graphql";
    private static final String SCHEMA_QUERY = "{\"query\": \"{ __schema { types { name } } }\"}";
    private static final String INVALID_QUERY = "{\"query\": \"invalid\"}";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGraphQLSchemaQuery() {
        ResponseEntity<String> response = executeGraphQLRequest(SCHEMA_QUERY);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody()).contains("\"__schema\"", "\"data\"", "\"types\"");
    }

    @Test
    void testInvalidGraphQLQuery() {
        ResponseEntity<String> response = executeGraphQLRequest(INVALID_QUERY);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody()).contains("\"errors\"").doesNotContain("\"data\"");
    }

    private HttpEntity<String> createRequest(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(query, headers);
    }

    private ResponseEntity<String> executeGraphQLRequest(String query) {
        HttpEntity<String> request = createRequest(query);
        return restTemplate.exchange(
                GRAPHQL_ENDPOINT,
                HttpMethod.POST,
                request,
                String.class);
    }
}
