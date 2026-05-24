# ============================================================
# Dockerfile para Backend (Spring Boot) - Java 17
# Multi-stage build: compila con Maven, ejecuta con JRE ligero
# ============================================================

# ETAPA 1: BUILD
# Usamos la imagen oficial de Maven con Java 17 sobre Alpine
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Optimización de caché de dependencias Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src .
RUN mvn clean package -DskipTests -B

# ETAPA 2: RUNTIME
# Imagen mínima con JRE de Java 17 sobre Alpine Linux
FROM eclipse-temurin:17-jre-alpine AS runtime
LABEL maintainer="UniQuindío"
LABEL description="Backend del Sistema de Triage y Gestión de Solicitudes Académicas"

# Crear usuario no-root por seguridad
RUN addgroup -S triage && adduser -S triage -G triage
WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar
RUN chown triage:triage app.jar

USER triage
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
