# ========== Build stage ==========
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos Gradle wrapper y archivos de construcción primero para cachear dependencias
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

# Aseguramos permisos de ejecución del wrapper
RUN chmod +x gradlew

# Descargamos dependencias (sin código todavía para aprovechar cache)
RUN ./gradlew --no-daemon dependencies || true

# Ahora sí copiamos el código
COPY src src

# Construimos el JAR sin correr tests (tests ya corren en CI)
RUN ./gradlew --no-daemon clean bootJar -x test

# ========== Runtime stage ==========
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# (opcional) user no-root
RUN useradd -r -s /bin/false appuser

# Copiamos el jar de la build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copiamos el agente de New Relic (se espera que ./newrelic exista en el repo de infra)
COPY ./newrelic /newrelic

# Aseguramos permisos para que el agente pueda escribir logs cuando se ejecute como appuser
RUN mkdir -p /newrelic/logs && chown -R appuser:appuser /newrelic || true

# Exponé el puerto interno de Spring
EXPOSE 8080

# Variables por defecto (se pueden overridear en compose)
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_TOOL_OPTIONS="-javaagent:/newrelic/newrelic.jar"

USER appuser

# ENTRYPOINT modificado para incluir el agente de New Relic
ENTRYPOINT ["java", "-javaagent:/newrelic/newrelic.jar", "-jar", "/app/app.jar"]
