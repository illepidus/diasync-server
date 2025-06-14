# AGENTS.md

Этот репозиторий содержит Spring Boot-приложение на Java 21, собираемое с помощью Gradle.  
Используй следующие рекомендации при изменении проекта.

## Тестирование

- Всегда запускай `./gradlew test` перед коммитом. Исправляй любые ошибки.
- Для новых тестов используй JUnit 5 и Mockito. Размещай их в `src/test/java`.

## Сборка и запуск

- Сборку и запуск выполняй через Gradle wrapper (`./gradlew`).
- Запусти сервер локально через `./gradlew bootRun` или выполнив jar-файл, собранный командой `./gradlew bootJar`.

## Структура проекта

- Код приложения находится в `src/main/java/ru/krotarnya/diasync`.
- Ресурсы (статические файлы, конфигурация) — в `src/main/resources`.
- Тесты — в `src/test/java/ru/krotarnya/diasync`.
- Документация — в `README.MD`.

## Язык и codestyle

- Весь код и комментарии внутри него должны быть на английском языке.
- Основное правило для комментариев в коде: если сомневаешься нужен ли тут комментарий, значит он точно не нужен.
- Отвечай на русском языке. Придерживайся неформального стиля.
