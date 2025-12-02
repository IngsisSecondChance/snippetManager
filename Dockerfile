FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .

ARG GPR_USER
ARG GPR_TOKEN
ENV USERNAME=$GPR_USER
ENV TOKEN=$GPR_TOKEN

# Construimos el JAR
RUN ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Copiar New Relic
COPY newrelic newrelic

EXPOSE 8081

ENTRYPOINT ["java", "-javaagent:/app/newrelic/newrelic.jar", "-jar", "/app/app.jar"]
