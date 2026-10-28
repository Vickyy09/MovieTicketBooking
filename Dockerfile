FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S booking && adduser -S booking -G booking
WORKDIR /app
COPY --from=build --chown=booking:booking /workspace/target/Booking-0.0.1-SNAPSHOT.jar booking.jar
USER booking
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 CMD wget -q -O /dev/null http://127.0.0.1:${PORT:-8080}/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/booking.jar"]
