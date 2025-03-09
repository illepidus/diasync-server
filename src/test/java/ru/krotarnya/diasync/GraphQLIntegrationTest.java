package ru.krotarnya.diasync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GraphQLIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGraphQLSchemaQuery() {
        // Подготовка GraphQL запроса
        String query = "{\"query\": \"{ __schema { types { name } } }\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(query, headers);

        // Отправка запроса к эндпоинту
        ResponseEntity<String> response = restTemplate.exchange(
                "/graphql",
                HttpMethod.POST,
                request,
                String.class
        );

        // Проверка статуса ответа
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        // Проверка тела ответа
        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        // Простая проверка структуры ответа (можно использовать JSON парсер)
        assertThat(responseBody).contains("\"data\"");
        assertThat(responseBody).contains("\"__schema\"");
        assertThat(responseBody).contains("\"types\"");
        assertThat(responseBody).contains("\"Query\"");
        assertThat(responseBody).contains("\"String\"");
    }

    @Test
    public void testInvalidGraphQLQuery() {
        // Некорректный запрос
        String invalidQuery = "{\"query\": \"invalid query syntax\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(invalidQuery, headers);

        // Отправка запроса
        ResponseEntity<String> response = restTemplate.exchange(
                "/graphql",
                HttpMethod.POST,
                request,
                String.class
        );

        // Проверка, что сервер вернул 200 с ошибками
        assertThat(response.getStatusCode().value()).isEqualTo(200); // Ожидаем 200, а не 400
        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).contains("\"errors\""); // Проверяем наличие ошибок
        assertThat(responseBody).doesNotContain("\"data\""); // Убеждаемся, что данных нет
    }
}
