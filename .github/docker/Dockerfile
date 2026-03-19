FROM eclipse-temurin:21-jre-jammy

WORKDIR /diasync

COPY build/libs/diasync.jar .
RUN if [ ! -f diasync.jar ]; then echo "Error: executable jar not found" && exit 1; fi

RUN mkdir -p /diasync/db

EXPOSE 15080

ENTRYPOINT ["java",
  "-Xms192m",
  "-Xmx384m",
  "-XX:MaxMetaspaceSize=128m",
  "-XX:MaxDirectMemorySize=128m",
  "-Xlog:gc*,safepoint:stdout:time,uptime,level,tags",
  "-jar",
  "/diasync/diasync.jar"]