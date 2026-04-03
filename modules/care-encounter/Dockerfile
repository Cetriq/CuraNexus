# Care Encounter Service Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Copy all POM files (required for multi-module build)
COPY pom.xml .
COPY modules/events/pom.xml modules/events/pom.xml
COPY modules/patient/pom.xml modules/patient/pom.xml
COPY modules/care-encounter/pom.xml modules/care-encounter/pom.xml
COPY modules/journal/pom.xml modules/journal/pom.xml
COPY modules/task/pom.xml modules/task/pom.xml
COPY modules/authorization/pom.xml modules/authorization/pom.xml
COPY modules/audit/pom.xml modules/audit/pom.xml
COPY modules/triage/pom.xml modules/triage/pom.xml
COPY modules/integration/pom.xml modules/integration/pom.xml
COPY modules/notification/pom.xml modules/notification/pom.xml
COPY modules/booking/pom.xml modules/booking/pom.xml
COPY modules/medication/pom.xml modules/medication/pom.xml
COPY modules/referral/pom.xml modules/referral/pom.xml
COPY modules/lab/pom.xml modules/lab/pom.xml

RUN chmod +x mvnw && ./mvnw dependency:go-offline -pl modules/events,modules/care-encounter -am -q

# Copy only the source for this module and its dependencies
COPY modules/events/src modules/events/src
COPY modules/care-encounter/src modules/care-encounter/src

RUN ./mvnw package -pl modules/events,modules/care-encounter -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S curanexus && adduser -u 1001 -S curanexus -G curanexus

COPY --from=builder /build/modules/care-encounter/target/*.jar app.jar

RUN chown -R curanexus:curanexus /app

USER curanexus

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
