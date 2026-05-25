#!/bin/bash
# Script para compilar localmente y desplegar a Railway

set -e

echo "=========================================="
echo "Sistema de Triage — Deploy a Railway"
echo "=========================================="

# Paso 1: Compilar con Maven
echo ""
echo "📦 Paso 1: Compilando con Maven..."
mvn clean package -DskipTests -B

# Verificar que el JAR se generó
if [ ! -f target/sistematriageexperimental-0.0.1-SNAPSHOT.jar ]; then
  echo "❌ ERROR: JAR no encontrado en target/"
  exit 1
fi

echo "✅ JAR compilado exitosamente"
ls -lh target/sistematriageexperimental-0.0.1-SNAPSHOT.jar

# Paso 2: Hacer push a GitHub
echo ""
echo "📤 Paso 2: Haciendo push a GitHub..."
git add -A
git commit -m "build: compilación de producción" || echo "Sin cambios para commitear"
git push origin main

# Paso 3: Esperar a que Railway detecte el cambio
echo ""
echo "⏳ Paso 3: Esperando a que Railway recompile..."
echo "   Esto puede tomar 2-3 minutos"
echo "   Puedes ver el progreso en: https://railway.app"

# Paso 4: Verificar que el backend está activo
echo ""
echo "🔍 Paso 4: Verificando que el backend está activo..."
MAX_INTENTOS=30
INTENTOS=0

until curl -sf https://sistematriageexperimental-production.up.railway.app/actuator/health | grep -q '"status":"UP"'; do
  INTENTOS=$((INTENTOS + 1))
  if [ "$INTENTOS" -ge "$MAX_INTENTOS" ]; then
    echo "❌ Timeout: el backend no respondió en 5 minutos"
    echo "   Revisa los logs en Railway Dashboard"
    exit 1
  fi
  echo "   Intento $INTENTOS/$MAX_INTENTOS — esperando 10s..."
  sleep 10
done

echo "✅ Backend activo y saludable"

# Paso 5: Resumen
echo ""
echo "=========================================="
echo "✅ DEPLOY EXITOSO"
echo "=========================================="
echo ""
echo "Backend disponible en:"
echo "  https://sistematriageexperimental-production.up.railway.app"
echo ""
echo "Próximo paso: Desplegar frontend en Vercel"
echo ""
