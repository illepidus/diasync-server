FROM eclipse-temurin:21-jdk-jammy

WORKDIR /diasync

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
COPY .git ./.git

RUN chmod +x ./gradlew && ./gradlew dependencies
RUN ./gradlew build -x test

RUN mkdir -p /diasync/db

EXPOSE 15080

ENTRYPOINT ["java", "-jar", "/diasync/build/libs/diasync.jar"]
