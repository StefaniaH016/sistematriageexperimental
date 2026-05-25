# Configuración de Railway para Base de Datos PostgreSQL

## ✅ Lo que ya está configurado en el código:

1. **application-prod.properties**: Configurado para usar variables de entorno de Railway
   - `DATABASE_URL` (formato: `postgresql://user:password@host:port/database`)
   - O también: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`
   - Driver: `org.postgresql.Driver`

2. **pom.xml**: Actualizado con driver de PostgreSQL 42.7.3

3. **docker-compose.yml**: Actualizado para usar PostgreSQL 16-alpine

4. **docker-compose.dev.yml**: Actualizado con pgAdmin para administración

5. **application.properties**: Configurado para desarrollo local con PostgreSQL

6. **CD Workflow (cd.yml)**: Configurado para desplegar en Railway

7. **vercel.json**: Configurado para SPA routing correcto

## 🔧 Lo que DEBES hacer en Railway Dashboard:

### Paso 1: Agregar Plugin PostgreSQL
1. Ve a tu proyecto en Railway
2. Haz clic en **+ Add** (o **New**)
3. Busca **PostgreSQL** en el marketplace
4. Haz clic en **Add PostgreSQL**
5. Railway creará automáticamente una instancia de PostgreSQL

### Paso 2: Verificar las Variables de Entorno
1. Abre el servicio **PostgreSQL** que acabas de crear
2. Ve a la pestaña **Variables**
3. Verifica que existan estas variables (Railway las crea automáticamente):
   - `DATABASE_URL` → URL completa de conexión
   - `PGHOST` → host del PostgreSQL
   - `PGPORT` → puerto (normalmente 5432)
   - `PGDATABASE` → nombre de la BD
   - `PGUSER` → usuario
   - `PGPASSWORD` → contraseña

### Paso 3: Conectar el Backend a PostgreSQL
1. Ve al servicio **sistematriageexperimental** (backend)
2. Abre la pestaña **Variables**
3. Verifica que existan estas variables (deberían estar auto-vinculadas del plugin PostgreSQL):
   - `DATABASE_URL` (debe estar auto-vinculada)
   - `PGHOST` (debe estar auto-vinculada)
   - `PGPORT` (debe estar auto-vinculada)
   - `PGDATABASE` (debe estar auto-vinculada)
   - `PGUSER` (debe estar auto-vinculada)
   - `PGPASSWORD` (debe estar auto-vinculada)

4. **IMPORTANTE**: Agrega estas variables si no existen:
   - **Nombre**: `SPRING_PROFILES_ACTIVE`
   - **Valor**: `prod`

5. Agrega también estas variables si no existen:
   - `JWT_SECRET` → tu clave secreta JWT
   - `GEMINI_API_KEY` → tu clave de API de Gemini (si usas IA)

### Paso 4: Redeploy
1. En el servicio **sistematriageexperimental**, haz clic en **Redeploy**
2. Espera a que termine el deploy
3. Verifica los logs para confirmar que:
   - Se activa el perfil `prod`
   - Se conecta correctamente a PostgreSQL
   - No hay errores de conexión

## 📋 Qué esperar en los logs:

**Correcto** (conexión exitosa):
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection conn0: url=jdbc:postgresql://... user=...
HikariPool-1 - HikariPool (HikariPool-1) is now active
```

**Incorrecto** (conexión fallida):
```
Connection refused
Unable to connect to database
FATAL: password authentication failed
```

## 🚀 Próximos pasos después del deploy:

1. Verifica que el backend esté corriendo en Railway
2. Prueba los endpoints de la API
3. Verifica que el frontend en Vercel se conecte correctamente al backend

## 📝 Notas importantes:

- PostgreSQL usa puerto 5432 internamente
- Las variables `PG*` son proporcionadas automáticamente por el plugin de Railway
- El perfil `prod` se activa automáticamente en el workflow de CD
- El JAR se genera automáticamente en el build de Docker
- PostgreSQL es más robusto y escalable que MariaDB

## 🐛 Si hay problemas:

1. Verifica que el plugin PostgreSQL esté activo en Railway
2. Verifica que las variables de entorno estén correctamente vinculadas
3. Revisa los logs del backend para ver mensajes de error específicos
4. Asegúrate de que `SPRING_PROFILES_ACTIVE=prod` esté configurado
5. Verifica que el `application-prod.properties` esté usando las variables correctas

## 💻 Para desarrollo local:

```bash
# Inicia los servicios con Docker Compose
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# El backend estará disponible en: http://localhost:8080
# pgAdmin estará disponible en: http://localhost:5050
# PostgreSQL estará disponible en: localhost:5432

# Para acceder a pgAdmin:
# Email: admin@example.com
# Contraseña: admin
```
