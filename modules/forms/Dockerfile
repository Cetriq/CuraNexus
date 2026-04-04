FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8093

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
