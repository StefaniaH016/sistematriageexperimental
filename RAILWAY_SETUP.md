# Configuración de Railway para Base de Datos MariaDB

## ✅ Lo que ya está configurado en el código:

1. **application-prod.properties**: Configurado para usar variables de entorno de Railway
   - `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`
   - Driver: `org.mariadb.jdbc.Driver`

2. **CD Workflow (cd.yml)**: Actualizado para:
   - Usar `railway deploy` en lugar de `railway up`
   - Establecer automáticamente `SPRING_PROFILES_ACTIVE=prod`

3. **vercel.json**: Corregido para SPA routing correcto

4. **railway.toml**: Creado para configuración de Railway

## 🔧 Lo que DEBES hacer en Railway Dashboard:

### Paso 1: Verificar el Plugin MySQL/MariaDB
1. Ve a tu proyecto en Railway
2. Abre el servicio **MySQL** (o MariaDB)
3. Verifica que esté activo y corriendo
4. Copia las variables de entorno que se muestran:
   - `MYSQLHOST`
   - `MYSQLPORT`
   - `MYSQLDATABASE`
   - `MYSQLUSER`
   - `MYSQLPASSWORD`

### Paso 2: Configurar el Backend
1. Ve al servicio **sistematriageexperimental** (backend)
2. Abre la pestaña **Variables**
3. Verifica que existan estas variables (deberían estar auto-vinculadas del plugin MySQL):
   - `MYSQLHOST` → host del MySQL
   - `MYSQLPORT` → puerto (normalmente 3306)
   - `MYSQLDATABASE` → nombre de la BD
   - `MYSQLUSER` → usuario
   - `MYSQLPASSWORD` → contraseña

4. **IMPORTANTE**: Agrega esta variable si no existe:
   - **Nombre**: `SPRING_PROFILES_ACTIVE`
   - **Valor**: `prod`

5. Agrega también estas variables si no existen:
   - `JWT_SECRET` → tu clave secreta JWT
   - `GEMINI_API_KEY` → tu clave de API de Gemini (si usas IA)

### Paso 3: Redeploy
1. En el servicio **sistematriageexperimental**, haz clic en **Redeploy**
2. Espera a que termine el deploy
3. Verifica los logs para confirmar que:
   - Se activa el perfil `prod`
   - Se conecta correctamente a la BD MariaDB
   - No hay errores de conexión

## 📋 Qué esperar en los logs:

**Correcto** (conexión exitosa):
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection conn0: url=jdbc:mariadb://... user=...
HikariPool-1 - HikariPool (HikariPool-1) is now active
```

**Incorrecto** (conexión fallida):
```
Connection refused
Unable to connect to database
```

## 🚀 Próximos pasos después del deploy:

1. Verifica que el backend esté corriendo en Railway
2. Prueba los endpoints de la API
3. Verifica que el frontend en Vercel se conecte correctamente al backend

## 📝 Notas importantes:

- El puerto 3307 es solo para acceso local externo a Docker
- En Railway, el MySQL usa puerto 3306 internamente
- Las variables `MYSQL*` son proporcionadas automáticamente por el plugin de Railway
- El perfil `prod` se activa automáticamente en el workflow de CD
