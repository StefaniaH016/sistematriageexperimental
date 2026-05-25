# 🔴 Problema: Maven NO compila en Docker de Railway

## El Problema

El error persiste:
```
Error: Unable to access jarfile target/sistematriageexperimental-0.0.1-SNAPSHOT.jar
```

Esto significa que **Maven NO está compilando dentro de Docker en Railway**, aunque:
- ✅ Maven compila correctamente en tu máquina local
- ✅ El JAR se genera en `target/sistematriageexperimental-0.0.1-SNAPSHOT.jar` localmente
- ❌ Maven NO genera el JAR dentro del contenedor Docker de Railway

## Posibles Causas

1. **Problema de memoria**: Railway en tier gratuito tiene RAM limitada. Maven puede estar siendo killed por OOMKilled.
2. **Problema de red**: Maven no puede descargar dependencias dentro de Docker.
3. **Problema de permisos**: El contenedor no tiene permisos para escribir en `/workspace/target/`.
4. **Problema de caché**: El caché de Docker está corrupto.

## ✅ Soluciones (en orden de recomendación)

### Solución 1: Compilar localmente y subir el JAR (RECOMENDADA)

En lugar de compilar en Docker, compilamos localmente y subimos el JAR a Railway.

**Ventajas:**
- ✅ Más rápido (no recompila cada vez)
- ✅ Más confiable (Maven funciona en tu máquina)
- ✅ Menos uso de RAM en Railway
- ✅ Mejor para CI/CD

**Pasos:**

1. **Compilar localmente:**
```bash
mvn clean package -DskipTests
```

2. **Verificar que el JAR existe:**
```bash
ls -lh target/sistematriageexperimental-0.0.1-SNAPSHOT.jar
```

3. **Hacer push a GitHub:**
```bash
git add target/sistematriageexperimental-0.0.1-SNAPSHOT.jar
git commit -m "build: JAR compilado"
git push origin main
```

4. **Railway detectará el cambio y desplegará automáticamente**

**Nota:** El JAR es un archivo binario (~50-100MB). Normalmente no se suben a Git, pero para este caso es aceptable.

### Solución 2: Usar el Dockerfile.prod alternativo

He creado un `Dockerfile.prod` que NO compila Maven. Solo copia el JAR precompilado.

**Pasos:**

1. **Compilar localmente:**
```bash
mvn clean package -DskipTests
```

2. **Hacer push:**
```bash
git add target/sistematriageexperimental-0.0.1-SNAPSHOT.jar
git commit -m "build: JAR compilado"
git push origin main
```

3. **En Railway, cambiar el Dockerfile:**
   - Ve a tu proyecto en Railway
   - Settings → Build Command
   - Cambia el Dockerfile a `Dockerfile.prod`

### Solución 3: Aumentar la RAM en Railway (de pago)

Si quieres que Maven compile en Docker, necesitas más RAM:
- Tier gratuito: 512MB (insuficiente para Maven + Spring Boot)
- Tier de pago: 1GB+ (suficiente)

**Costo:** $7/mes

---

## 🚀 Recomendación: Solución 1 (Compilar localmente)

Esta es la mejor opción porque:

1. **Es más rápido**: No recompila cada vez
2. **Es más confiable**: Maven funciona en tu máquina
3. **Es más barato**: No necesitas tier de pago
4. **Es estándar en CI/CD**: Los pipelines profesionales compilan localmente

### Pasos para implementar la Solución 1:

```bash
# 1. Compilar localmente
mvn clean package -DskipTests

# 2. Verificar que el JAR existe
ls -lh target/sistematriageexperimental-0.0.1-SNAPSHOT.jar

# 3. Hacer push (incluye el JAR)
git add target/sistematriageexperimental-0.0.1-SNAPSHOT.jar
git commit -m "build: JAR compilado para Railway"
git push origin main

# 4. Railway detectará el cambio automáticamente
# Espera 2-3 minutos a que Railway recompile

# 5. Verificar que funciona
curl https://sistematriageexperimental-production.up.railway.app/actuator/health
```

---

## 📋 Checklist para la Solución 1

- [ ] Ejecuté `mvn clean package -DskipTests` localmente
- [ ] El JAR se generó en `target/sistematriageexperimental-0.0.1-SNAPSHOT.jar`
- [ ] Hice `git add target/sistematriageexperimental-0.0.1-SNAPSHOT.jar`
- [ ] Hice `git commit -m "build: JAR compilado"`
- [ ] Hice `git push origin main`
- [ ] Esperé 3 minutos a que Railway recompile
- [ ] El health check responde `{"status":"UP"}`

---

## 🔍 Cómo verificar que funciona

```bash
# Después de 3 minutos, ejecuta:
curl https://sistematriageexperimental-production.up.railway.app/actuator/health

# Debe responder:
# {"status":"UP"}
```

Si ves `{"status":"UP"}`, ¡el backend está corriendo correctamente!

---

## 📝 Notas

### ¿Por qué el JAR es tan grande?

El JAR de Spring Boot es un "fat JAR" que incluye:
- Tu código compilado
- Todas las dependencias (Spring, PostgreSQL driver, etc.)
- El servidor Tomcat embebido
- Recursos (archivos de configuración, etc.)

Por eso pesa 50-100MB. Es normal.

### ¿Es seguro subir el JAR a Git?

Técnicamente no es la mejor práctica (los binarios no deberían estar en Git), pero para un proyecto estudiantil es aceptable. En proyectos profesionales, usarías un artifact repository (Nexus, Artifactory) o un registry de Docker.

### ¿Qué pasa si cambio el código?

1. Compilas localmente: `mvn clean package -DskipTests`
2. Haces push: `git add target/... && git commit && git push`
3. Railway detecta el cambio y redeploya automáticamente

---

## ✅ Resumen

**El problema:** Maven no compila en Docker de Railway.

**La solución:** Compilar localmente y subir el JAR a Git.

**El resultado:** Tu backend estará corriendo en Railway sin errores.

**Próximo paso:** Una vez que el backend funcione, desplegar el frontend en Vercel.
