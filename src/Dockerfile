# ---------- ETAPA 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copiar pom para cachear dependencias
COPY pom.xml ./
RUN mvn -B dependency:go-offline

# Copiar código fuente y compilar
COPY src ./src
RUN mvn -B -DskipTests package

# ---------- ETAPA 2: RUNTIME ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
LABEL maintainer="UniQuindío"
LABEL description="Backend del Sistema de Triage y Gestión de Solicitudes Académicas"

# Crear usuario no-root
RUN addgroup --system triage && adduser --system --ingroup triage triage

WORKDIR /app

# Copiar el JAR compilado desde la etapa build
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
RUN chown triage:triage app.jar && chmod 500 app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

USER triage
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
