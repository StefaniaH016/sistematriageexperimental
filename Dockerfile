# ---------- ETAPA: RUNTIME ----------
# Este Dockerfile usa el JAR precompilado en lugar de compilar Maven
# Esto es más rápido y confiable en Railway

FROM eclipse-temurin:17-jre-jammy AS runtime
LABEL maintainer="UniQuindío"
LABEL description="Backend del Sistema de Triage y Gestión de Solicitudes Académicas"

# Crear usuario no-root
RUN addgroup --system triage && adduser --system --ingroup triage triage

WORKDIR /app

# Copiar el JAR precompilado desde el repositorio
COPY target/sistematriageexperimental-0.0.1-SNAPSHOT.jar app.jar
RUN chown triage:triage app.jar && chmod 500 app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

USER triage
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
