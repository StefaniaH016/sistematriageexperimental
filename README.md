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
3. Ejecuta el siguiente comando SQL para crear el esquema vacío:
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
5. Espera a que el compilador termine. Abre tu navegador web favorito y entra a:
   👉 **[http://localhost:4200](http://localhost:4200)**

Verás en pantalla el **Sistema de Triage Académico** con su diseño premium oscuro y animación de entrada, una belleza

---

## 🔐 Ingreso al Sistema y Pruebas (Usuarios Demo)
Al levantar el sistema por primera vez, el Backend insertó usuarios en MariaDB con las contraseñas encriptadas para que puedas probar la plataforma inmediatamente sin tener que hacer registros directos en base de datos.

En la pantalla de Login de Angular (`http://localhost:4200`), utiliza cualquiera de los siguientes correos. **La contraseña para todos es `123456`** *(a excepción del administrativo que es `admin123`)*.

| Usuario                     | Rol            | Contraseña | Qué puedes probar con él                         |
|-----------------------------|----------------|------------|--------------------------------------------------|
| `juan.perez@uq.edu.co`      | `ESTUDIANTE`   | `123456`   | Crear radicación de solicitudes, ver sus estados |
| `carlos.lopez@uq.edu.co`    | `RESPONSABLE`  | `123456`   | Atender solicitudes, usar IA, cambiar estados    |
| `ana.martinez@uq.edu.co`    | `ADMINISTRATIVO`| `admin123` | Asignación y gestión general                     |

### 📚 Documentación de API (Swagger) Secundaria
El proyecto cuenta con interfaz gráfica completa en Angular. Sin embargo, si deseas revisar los esquemas de la API o probar peticiones crudas:
1. Entra a: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
2. Para hacer pruebas desde allí, primero deberás hacer un POST a `/api/auth/login` con uno de los correos de arriba, copiar el `token` devuelto y pegarlo en el botón **Authorize** ubicado arriba a la derecha.
