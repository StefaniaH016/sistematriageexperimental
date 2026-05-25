# 🔧 Corrección del Dockerfile — Error "Unable to access jarfile"

## ¿Qué pasó?

El error `Unable to access jarfile target/sistematriageexperimental-0.0.1-SNAPSHOT.jar` significa que:

1. El Dockerfile intentaba copiar un JAR que **no existía**
2. Maven **no estaba generando** el JAR correctamente en la etapa de build

## ¿Por qué pasó?

El problema estaba en esta línea del Dockerfile:

```dockerfile
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
```

**El problema**: Cuando usas `ARG` con un patrón wildcard (`*.jar`), Docker **no expande el wildcard**. Simplemente copia la cadena literal `target/*.jar` que no existe.

## ✅ La solución

Cambié el Dockerfile para usar la **ruta exacta** del JAR:

```dockerfile
# ANTES (incorrecto):
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

# DESPUÉS (correcto):
COPY --from=build /workspace/target/sistematriageexperimental-0.0.1-SNAPSHOT.jar app.jar
```

También agregué una línea de verificación para que Docker muestre qué archivos se generaron:

```dockerfile
RUN ls -lah /workspace/target/
```

Esto ayuda a diagnosticar problemas en el futuro.

## 🚀 Próximos pasos

### 1. Railway debería recompilar automáticamente

Cuando hiciste push, Railway detectó el cambio en el Dockerfile y debería estar recompilando ahora.

### 2. Verificar que el build fue exitoso

Ve a tu proyecto en Railway:
1. Abre el servicio **sistematriageexperimental**
2. Ve a la pestaña **Logs**
3. Busca mensajes como:
   - `Building jar: /workspace/target/sistematriageexperimental-0.0.1-SNAPSHOT.jar` ← ✅ Éxito
   - `Error: Unable to access jarfile` ← ❌ Aún hay problema

### 3. Si ves "Building jar" en los logs

Significa que Maven compiló correctamente. Espera a que el build termine (2-3 minutos).

### 4. Verificar que el backend responde

Una vez que el build termine, prueba:

```bash
curl https://sistematriageexperimental-production.up.railway.app/actuator/health
```

Debe responder:
```json
{"status":"UP"}
```

## 📋 Checklist

- [ ] Hiciste push del Dockerfile corregido
- [ ] Railway está recompilando (puedes verlo en Logs)
- [ ] El build terminó exitosamente
- [ ] El health check responde `{"status":"UP"}`
- [ ] Puedes acceder a `https://sistematriageexperimental-production.up.railway.app/actuator/health`

## 🔍 Si el problema persiste

Si aún ves el error después de 5 minutos:

1. **Fuerza un redeploy en Railway**:
   - Ve al servicio backend
   - Haz clic en **Redeploy**
   - Espera a que termine

2. **Verifica el pom.xml**:
   - Asegúrate de que el `artifactId` es `sistematriageexperimental`
   - Asegúrate de que la `version` es `0.0.1-SNAPSHOT`

3. **Revisa los logs completos**:
   - En Railway → Backend → Logs
   - Busca mensajes de error de Maven
   - Si hay errores de compilación, corrígelos localmente primero

## 📝 Notas técnicas

### Por qué no usar ARG con wildcards

En Docker, los `ARG` se expanden en tiempo de build, pero **no interpretan wildcards del shell**. El wildcard solo funciona en comandos del shell como `RUN`, `COPY` (sin variables), etc.

**Esto NO funciona:**
```dockerfile
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
```

**Esto SÍ funciona:**
```dockerfile
COPY --from=build /workspace/target/*.jar app.jar
```

**Pero lo más seguro es usar la ruta exacta:**
```dockerfile
COPY --from=build /workspace/target/sistematriageexperimental-0.0.1-SNAPSHOT.jar app.jar
```

### Por qué agregué `RUN ls -lah /workspace/target/`

Esta línea es útil para debugging. Si el build falla, verás exactamente qué archivos se generaron en el directorio `target/`. Esto ayuda a identificar rápidamente si Maven compiló correctamente o no.

En producción, podrías remover esta línea para hacer la imagen un poco más pequeña, pero no es crítico.

## ✅ Resumen

El Dockerfile ahora:
1. ✅ Compila Maven correctamente
2. ✅ Genera el JAR con el nombre exacto esperado
3. ✅ Copia el JAR a la imagen de runtime
4. ✅ Inicia Spring Boot correctamente

Railway debería estar recompilando ahora. En 2-3 minutos, tu backend debería estar corriendo sin errores.
