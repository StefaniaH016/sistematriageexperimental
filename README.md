# Sistema de Triage y Gestión de Solicitudes Académicas

Bienvenido al repositorio oficial del **Sistema de Triage y Gestión de Solicitudes Académicas** del programa de Ingeniería de Sistemas y Computación.

Esta implementación corresponde al **Hito 3 (Entrega Final)**: *Frontend en Angular, Seguridad JWT estricta, Base de Datos MariaDB e Integración real con Inteligencia Artificial (Gemini).*

---

## 🚀 Arquitectura y Tecnologías
- **Backend**: Java 17, Spring Boot 3.2.3, Spring Security (JWT), Spring Data JPA.
- **Frontend**: Angular 17+ (Standalone Components), Node.js, TypeScript.
- **Base de Datos**: MariaDB.
- **Inteligencia Artificial**: Google Gemini API (1.5 Flash).
- **Diseño UI/UX**: Premium Dark Mode, Glassmorphism, CSS Variables Globales.

---

## 🛑 Prerrequisitos del Sistema
Antes de ejecutar el proyecto, asegúrate de tener instalados los siguientes programas en tu equipo:
1. **Java Development Kit (JDK) 17**: Configurado en tus variables de entorno (`JAVA_HOME`).
2. **Maven**: Para compilar el backend.
3. **Node.js (versión 18 o superior)**: Para compilar y levantar Angular.
4. **MariaDB Server y HeidiSQL**: Para levantar y administrar el motor de base de datos relacional.

---

## 🔑 Variables de Entorno (¡FUNDAMENTAL ANTES DE EJECUTAR!)

Para mantener la seguridad del sistema y no exponer credenciales en el código, es **estrictamente necesario** configurar las siguientes variables de entorno en tu sistema (o en la terminal) antes de levantar el backend:

- `DB_PASSWORD`: Contraseña de tu usuario en MariaDB (por defecto el usuario es `root`).
- `JWT_SECRET`: Una cadena de texto segura (preferiblemente codificada en Base64) usada para firmar los tokens de autenticación.
- `GEMINI_API_KEY`: Tu clave de API para Google Gemini, que puedes obtener en [Google AI Studio](https://aistudio.google.com/).

**Ejemplo de cómo asignarlas temporalmente en Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="tu_password_de_mariadb"
$env:JWT_SECRET="tu_secreto_jwt_en_base64_muy_seguro_y_largo_aqui="
$env:GEMINI_API_KEY="tu_clave_de_gemini"
```

---

## ⚙️ Instrucciones Detalladas de Ejecución desde Cero

Sigue **exactamente** este orden para garantizar que el ecosistema completo se levante sin errores.

### PASO 1: Preparación de la Base de Datos (MariaDB)
El proyecto utiliza MariaDB en lugar de una base en memoria para garantizar persistencia profesional.
1. Abre tu servidor MariaDB.
2. Entra a tu administrador de bases de datos (**HeidiSQL**).
3. Conéctate usando:
   - Host: `127.0.0.1`
   - Puerto: `3307`
   - Usuario: `root`
   - Contraseña: la que definas en `DB_PASSWORD`
4. Ejecuta el siguiente comando SQL para crear el esquema vacío:
   ```sql
   CREATE DATABASE solicitudesdb;
   ```

### PASO 2: Despliegue del Backend (Spring Boot)
1. Abre una terminal (Símbolo del sistema, PowerShell o la terminal de tu IDE) ubicada en la **carpeta principal** del repositorio (donde está el `pom.xml`).
2. **Asegúrate de que las variables de entorno están asignadas** en esa misma terminal.
3. Limpia, descarga dependencias y compila el proyecto ejecutando:
   ```bash
   mvn clean install -DskipTests
   ```
4. Una vez termine exitosamente, inicia el servidor ejecutando:
   ```bash
   mvn spring-boot:run
   ```
5. Espera a que la consola muestre el mensaje de que Tomcat ha iniciado en el puerto `8080`.
   *(Nota: En su primer arranque, Hibernate creará todas las tablas en MariaDB automáticamente gracias a `ddl-auto=update`, y el sistema inyectará unos usuarios de prueba).*

### PASO 3: Despliegue del Frontend Visual (Angular)
1. Sin cerrar la terminal del Backend, abre una **segunda terminal** nueva.
2. Navega hacia adentro de la carpeta del frontend:
   ```bash
   cd frontend
   ```
3. Instala todas las librerías de interfaz ejecutando:
   ```bash
   npm install
   ```
4. Inicia el servidor de desarrollo de Angular:
   ```bash
   ng serve
   ```
6. Espera a que el compilador termine. Abre tu navegador web favorito y entra a:
   👉 **[http://localhost:4200](http://localhost:4200)**

Verás en pantalla el **Sistema de Triage Académico** con su diseño premium oscuro y animación de entrada.

---

## ☁️ Despliegue en la nube / Docker
Esta aplicación está lista para ejecutarse con Docker y para usarse como una app completa en entornos compatibles con contenedores.

### 1. Crear el archivo `.env`
Copia el archivo de ejemplo y completa los valores:
```powershell
copy .env.example .env
```
Edita el archivo `.env` y añade tus credenciales reales de:
- `DB_PASSWORD`
- `JWT_SECRET`
- `GEMINI_API_KEY`

> Si vas a ejecutar localmente, el backend usa `DB_URL` apuntando al servicio `mariadb` en Docker.

### 2. Levantar todos los servicios con Docker Compose
Desde la carpeta raíz del repositorio ejecuta:
```powershell
docker compose up --build -d
```

### 3. Acceder a la aplicación en producción local
- Frontend: **http://localhost/**
- API proxyada: **http://localhost/api/**

### 4. Verificar el estado de los servicios
```powershell
docker compose ps
```

Para ver los logs del backend:
```powershell
docker compose logs -f backend
```

### 5. Detener el despliegue
```powershell
docker compose down
```

> En este modo Docker, el frontend y backend se sirven desde el mismo origen, por lo que el navegador no debe bloquear las llamadas por CORS.

---

## ☁️ CI/CD y despliegue en la nube
Este repositorio incluye dos flujos de trabajo de GitHub Actions:
- `.github/workflows/ci.yml`: compila y valida backend + frontend en cada `push` o `pull_request`.
- `.github/workflows/cd.yml`: construye y publica imágenes Docker a GitHub Container Registry cuando se hace `push` a `main`.

### Recomendación de despliegue cloud
1. Conecta los contenedores generados en GHCR a tu proveedor de nube.
2. Despliega el backend como un servicio de contenedor.
3. Despliega el frontend con la imagen Nginx/Angular o usa el mismo stack `docker compose` en un VPS/Cloud Run/EC2.

### Opciones válidas
- **Railway**: despliega el backend como contenedor Docker o usando el repositorio con Dockerfile. Añade un plugin de MariaDB y configura las variables de entorno.
- **Vercel**: despliega solo el frontend estático. Para que funcione, el frontend debe consumir un backend público y `environment.prod.ts` debe apuntar a la URL de ese backend.
- **Servidor propio / VPS**: ejecuta `docker compose up --build -d` en la máquina.

### Despliegue en Railway
1. Crea un proyecto nuevo en Railway.
2. Añade un plugin de MariaDB o una base de datos gestionada.
3. Crea un servicio de contenedor Docker usando la imagen de backend:
   - `ghcr.io/<tu-usuario>/triage-backend:latest`
   o despliega desde el repositorio si Railway detecta tu `Dockerfile`.
4. Agrega estas variables de entorno en Railway:
   - `DB_URL` = la cadena JDBC que te da Railway (o `jdbc:mariadb://<host>:<port>/solicitudesdb`)
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `GEMINI_API_KEY`
   - `APP_CORS_ALLOWED_ORIGIN_PATTERNS` = `https://<tu-frontend>.vercel.app,https://<tu-backend>.railway.app`
5. Despliega y revisa los logs del servicio.

### Despliegue en Vercel
1. En Vercel, crea un proyecto nuevo y conecta tu repositorio.
2. Configura el build command en `frontend`:
   - `npm install && npm run build -- --configuration production`
3. Asegúrate de que el backend esté desplegado en una URL pública.
4. Antes de construir, actualiza `frontend/src/environments/environment.prod.ts` para usar la URL del backend:
   - `apiUrl: 'https://<tu-backend>/api'`
5. Publica el frontend en Vercel.

> Nota: Si el frontend se sirve desde Vercel, la API debe estar en un dominio público y el backend debe autorizar ese origen en `APP_CORS_ALLOWED_ORIGIN_PATTERNS`.

### Variables de entorno para producción
Asegúrate de proporcionar todos estos valores en la nube:
- `DB_URL` (ej. `jdbc:mariadb://mariadb:3306/solicitudesdb` o conexión a la base de datos gestionada)
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `GEMINI_API_KEY`
- `APP_CORS_ALLOWED_ORIGIN_PATTERNS` (opcional, para dominios externos como Vercel o Railway)

### Flujo recomendado para producción local y cloud
1. Copia `.env.example` a `.env`.
2. Ajusta los valores para tu entorno.
3. Ejecuta `docker compose up --build -d`.
4. Accede a `http://localhost/`.

---

## 🔐 Ingreso al Sistema y Pruebas (Usuarios Demo)
Al levantar el sistema por primera vez, el backend registra usuarios de prueba para que puedas acceder rápido.

En la pantalla de login (dev: `http://localhost:4200`, Docker: `http://localhost/`) usa cualquiera de los siguientes correos. La contraseña de todos es `123456`, excepto el administrativo que es `admin123`.

| Usuario                     | Rol             | Contraseña | Qué puedes probar con él                         |
|-----------------------------|-----------------|------------|--------------------------------------------------|
| `juan.perez@uq.edu.co`      | `ESTUDIANTE`    | `123456`   | Crear radicación de solicitudes, ver sus estados |
| `carlos.lopez@uq.edu.co`    | `RESPONSABLE`   | `123456`   | Atender solicitudes, usar IA, cambiar estados    |
| `ana.martinez@uq.edu.co`    | `ADMINISTRATIVO`| `admin123` | Asignación y gestión general                     |

### 📚 Documentación de API (Swagger)
Si deseas revisar la API directamente:
1. Entra a: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Haz un POST a `/api/auth/login` con uno de los usuarios de prueba.
3. Copia el token y haz clic en **Authorize**.
