FROM eclipse-temurin:21-jre-jammy

WORKDIR /diasync

COPY build/libs/diasync.jar .
RUN if [ ! -f diasync.jar ]; then echo "Error: executable jar not found" && exit 1; fi

RUN mkdir -p /diasync/db

EXPOSE 15080

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/diasync/diasync.jar"]
