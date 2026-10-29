FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system releaseflow && useradd --system --gid releaseflow releaseflow \
    && mkdir -p /app/data /app/logs && chown -R releaseflow:releaseflow /app
COPY --from=build /build/target/releaseflow-0.0.1-SNAPSHOT.jar /app/releaseflow.jar
USER releaseflow
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/releaseflow.jar"]
