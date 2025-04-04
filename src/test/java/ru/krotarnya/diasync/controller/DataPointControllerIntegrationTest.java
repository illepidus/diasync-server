package ru.krotarnya.diasync.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.WebGraphQlTester;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataPointControllerIntegrationTest {
    private static final String ADD_DATA_POINTS_QUERY = """
            mutation {
                addDataPoints(
                    dataPoints: [{ userId: "test-user", timestamp: "2025-01-01T00:00:00Z" }]
                ) {
                    id
                }
            }
            """;
    private static final String GET_DATA_POINTS_QUERY = """
            query {
                getDataPoints(userId: "test-user", from: "2020-01-01T00:00:00Z") {
                    id
                }
            }
            """;
    private static final String TRUNCATE_QUERY = """
            mutation {
              truncateDataPoints(userId: "test-user")
            }
            """;

    private static final String ADD_DATA_POINTS_PATH = "data.addDataPoints[0].id";
    private static final String GET_DATA_POINTS_PATH = "data.getDataPoints[0].id";
    private static final String TRUNCATE_PATH = "data.truncateDataPoints";

    private final WebGraphQlTester graphQlTester;

    DataPointControllerIntegrationTest(@Autowired WebGraphQlTester graphQlTester) {
        this.graphQlTester = graphQlTester;
    }

    @BeforeEach
    void setUp() {
        executeGraphQl(TRUNCATE_QUERY);
    }

    @Test
    void testAddDataPoints() {
        int id = executeAndGetIntegerResult(ADD_DATA_POINTS_QUERY, ADD_DATA_POINTS_PATH);
        assertTrue(id >= 0);
    }

    @Test
    void testGetDataPoints() {
        executeGraphQl(ADD_DATA_POINTS_QUERY);
        int id = executeAndGetIntegerResult(GET_DATA_POINTS_QUERY, GET_DATA_POINTS_PATH);
        assertTrue(id >= 0);
    }

    @Test
    void testTruncateDataPoints() {
        executeGraphQl(ADD_DATA_POINTS_QUERY);

        int firstTruncateResult = executeAndGetIntegerResult(TRUNCATE_QUERY, TRUNCATE_PATH);
        assertEquals(1, firstTruncateResult);

        int secondTruncateResult = executeAndGetIntegerResult(TRUNCATE_QUERY, TRUNCATE_PATH);
        assertEquals(0, secondTruncateResult);
    }

    private void executeGraphQl(String query) {
        graphQlTester.document(query).execute();
    }

    private int executeAndGetIntegerResult(String query, String path) {
        return graphQlTester.document(query)
                .execute()
                .path(path)
                .entity(Integer.class)
                .get();
    }
}
