#!/bin/bash
set -e

echo "=== Compilando con Maven ==="
mvn clean package -DskipTests

echo "=== Verificando JAR ==="
ls -lah target/sistematriageexperimental-0.0.1-SNAPSHOT.jar

echo "=== Build completado ==="
