FROM eclipse-temurin:21-jdk-jammy

WORKDIR /diasync

COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew dependencies

RUN ./gradlew build -x test

EXPOSE 15080

ENTRYPOINT ["java", "-jar", "/diasync/build/libs/diasync.jar"]
