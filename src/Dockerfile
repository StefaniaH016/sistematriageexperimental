# ---------- ETAPA 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copiar solo lo necesario para cachear dependencias
COPY pom.xml mvnw* ./
COPY .mvn .mvn
RUN mvn -B dependency:go-offline

# Copiar código fuente y compilar
COPY src ./src
ARG MAVEN_OPTS="-Xmx1024m"
RUN mvn -B -DskipTests package

# ---------- ETAPA 2: RUNTIME ----------
# Usar imagen basada en Debian/Ubuntu para máxima compatibilidad con binarios nativos
FROM eclipse-temurin:17-jre-jammy AS runtime
LABEL maintainer="UniQuindío"
LABEL description="Backend del Sistema de Triage y Gestión de Solicitudes Académicas"

# Crear usuario no-root
RUN addgroup --system triage && adduser --system --ingroup triage triage

WORKDIR /app

# Nombre del JAR: por defecto toma el primer JAR en target; puedes pasar ARG JAR_FILE si lo prefieres
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
RUN chown triage:triage app.jar && chmod 500 app.jar

# Variables por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Exponer puerto por claridad; Spring debe leer PORT en runtime (ver nota)
EXPOSE 8080

USER triage

# Healthcheck opcional (requiere que /actuator/health esté habilitado)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- --timeout=2 http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
