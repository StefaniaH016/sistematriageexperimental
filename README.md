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
4. **XAMPP / WampServer o MariaDB Server**: Para levantar el motor de base de datos relacional.

---

## ⚙️ Instrucciones Detalladas de Ejecución desde Cero

Sigue **exactamente** este orden para garantizar que el ecosistema completo se levante sin errores.

### PASO 1: Preparación de la Base de Datos (MariaDB)
El proyecto utiliza MariaDB en lugar de una base en memoria para garantizar persistencia profesional.
1. Abre el panel de control de tu gestor (ej. XAMPP) y haz clic en **Start** en el módulo de *MySQL/MariaDB*.
2. Entra a tu administrador de bases de datos preferido (phpMyAdmin en `http://localhost/phpmyadmin`, DBeaver, o HeidiSQL).
3. Ejecuta el siguiente comando SQL para crear el esquema vacío:
   ```sql
   CREATE DATABASE solicitudesdb;
   ```
4. **Configuración de Credenciales**: Por defecto, el proyecto intentará conectarse usando el usuario `root` y la contraseña `root`. 
   - **¡IMPORTANTE!** Si usas XAMPP estándar, normalmente el usuario es `root` pero la **contraseña es vacía**.
   - Para corregir esto, abre el archivo en el proyecto: `src/main/resources/application.properties`
   - Modifica la línea de la contraseña para que quede vacía, de esta forma:
     ```properties
     spring.datasource.username=root
     spring.datasource.password=
     ```

### PASO 2: Configuración de Inteligencia Artificial (Gemini)
El sistema se conecta a la red de IA de Google para sugerir clasificaciones y borradores.

**Recomendación de Seguridad**: Para evitar subir tu clave al repositorio, el proyecto está configurado para leer la clave desde una **variable de entorno**.

1. Obtén tu clave en [Google AI Studio](https://aistudio.google.com/).
2. **Configuración en Windows (PowerShell)**:
   ```powershell
   $env:GEMINI_API_KEY="tu_clave_aqui"
   ```
3. **Configuración en VS Code (`launch.json`)**:
   Agrega la clave en la sección `env`:
   ```json
   "env": {
     "GEMINI_API_KEY": "tu_clave_aqui"
   }
   ```
4. Si prefieres no usar variables de entorno, puedes ponerla directamente en `src/main/resources/application.properties` en la línea `gemini.api.key`, pero **ten cuidado de no hacer push** con el archivo modificado.

### PASO 3: Despliegue del Backend (Spring Boot)
1. Abre una terminal (Símbolo del sistema, PowerShell o la terminal de tu IDE) ubicada en la **carpeta principal** del repositorio (donde está el `pom.xml`).
2. Limpia, descarga dependencias y compila el proyecto ejecutando:
   ```bash
   mvn clean install -DskipTests
   ```
3. Una vez termine exitosamente, inicia el servidor ejecutando:
   ```bash
   mvn spring-boot:run
   ```
4. Espera a que la consola muestre el mensaje de que Tomcat ha iniciado en el puerto `8080`.
   *(Nota: En su primer arranque, Hibernate creará todas las tablas en MariaDB automáticamente gracias a `ddl-auto=update`, y el sistema inyectará unos usuarios de prueba).*

### PASO 4: Despliegue del Frontend Visual (Angular)
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

Verás en pantalla el **Sistema de Triage Académico** con su diseño premium oscuro y animación de entrada.

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
