FROM eclipse-temurin:21-jdk-jammy

WORKDIR /diasync

COPY build/libs/diasync.jar .
RUN mkdir -p /diasync/db

EXPOSE 15080

ENTRYPOINT ["java", "-jar", "/diasync/diasync.jar"]
