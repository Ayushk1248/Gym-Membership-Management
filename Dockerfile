# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-jammy AS source
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY src/ src/
COPY index.html style.css script.js src/main/resources/static/

# CI runs this target and copies /workspace/target/surefire-reports out.
FROM source AS test
CMD ["./mvnw", "-B", "-ntp", "test", "-Dspring.profiles.active=test"]

FROM source AS build
RUN ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS production
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 gym && useradd --uid 10001 --gid gym --no-create-home gym
WORKDIR /app
COPY --from=build --chown=gym:gym /workspace/target/gym-management-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD curl --fail --silent http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
