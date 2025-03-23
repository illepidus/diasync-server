FROM eclipse-temurin:21-jdk-jammy

WORKDIR /diasync

COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew dependencies

RUN ./gradlew build -x test

# Открываем порт приложения (например, 8080)
EXPOSE 15080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/build/libs/diasync-unspecified.jar"]
