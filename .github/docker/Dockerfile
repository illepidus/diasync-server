FROM eclipse-temurin:21-jdk-jammy

WORKDIR /diasync

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew dependencies
RUN echo "Building project..." && ./gradlew build -x test || { echo "Build failed"; exit 1; }

RUN mkdir -p /diasync/db

EXPOSE 15080

ENTRYPOINT ["java", "-jar", "/diasync/build/libs/diasync.jar"]
