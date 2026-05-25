# Guía de Despliegue - Sistema de Triage

## 🚀 URLs de Acceso

### Frontend (Vercel)
**URL**: https://sistematriageexperimental.vercel.app

Este es el único lugar donde debes acceder. El frontend está alojado en Vercel.

### Backend (Railway)
**URL**: https://sistematriageexperimental-production.up.railway.app

El backend solo sirve la API REST. **NO accedas directamente a esta URL en el navegador** - recibirás 403 Forbidden.

### API REST
**Base URL**: https://sistematriageexperimental-production.up.railway.app/api/v1

Endpoints disponibles:
- `POST /auth/login` - Iniciar sesión
- `POST /auth/registro` - Registrarse
- `GET /solicitudes` - Listar solicitudes
- `POST /solicitudes` - Crear solicitud
- etc.

### Swagger/OpenAPI
**URL**: https://sistematriageexperimental-production.up.railway.app/swagger-ui.html

Documentación interactiva de la API.

---

## 🔐 Credenciales de Prueba

### Administrativo
- **Email**: `ana.martinez@uq.edu.co`
- **Contraseña**: `admin123`

### Responsable
- **Email**: `carlos.lopez@uq.edu.co`
- **Contraseña**: `123456`

### Estudiante
- **Email**: `juan.perez@uq.edu.co`
- **Contraseña**: `123456`

---

## 📋 Flujo de Acceso Correcto

1. **Abre el navegador** y ve a: https://sistematriageexperimental.vercel.app
2. **Verás la página de login** del frontend
3. **Ingresa las credenciales** (ej: ana.martinez@uq.edu.co / admin123)
4. **El frontend hace una llamada** a: https://sistematriageexperimental-production.up.railway.app/api/v1/auth/login
5. **El backend responde** con un token JWT
6. **El frontend guarda el token** en localStorage
7. **Accedes al dashboard** con tu sesión autenticada

---

## ⚠️ Errores Comunes

### Error: "Se denegó el acceso" (403 Forbidden)
**Causa**: Estás intentando acceder directamente a la URL de Railway en el navegador.
**Solución**: Accede a https://sistematriageexperimental.vercel.app en su lugar.

### Error: "401 Unauthorized" en login
**Causa**: Las credenciales son incorrectas o el usuario no existe.
**Solución**: Verifica que uses exactamente:
- Email: `ana.martinez@uq.edu.co` (con minúsculas)
- Contraseña: `admin123`

### Error: "No se puede conectar al servidor"
**Causa**: El backend en Railway no está corriendo.
**Solución**: Verifica que Railway esté activo y que la base de datos PostgreSQL esté conectada.

---

## 🔄 Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────────────┐
│                    Usuario en Navegador                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
        ┌────────────────────────────────┐
        │   Frontend (Vercel)            │
        │ https://sistematriageexperimental.vercel.app
        │                                │
        │ - Angular 18                   │
        │ - Componentes UI               │
        │ - Autenticación JWT            │
        └────────────┬───────────────────┘
                     │
                     │ Llamadas API
                     │ (HTTPS)
                     ▼
        ┌────────────────────────────────┐
        │   Backend (Railway)            │
        │ https://sistematriageexperimental-production.up.railway.app
        │                                │
        │ - Spring Boot 3.2.3            │
        │ - API REST                     │
        │ - Autenticación JWT            │
        └────────────┬───────────────────┘
                     │
                     │ Conexión JDBC
                     │
                     ▼
        ┌────────────────────────────────┐
        │   Base de Datos (Railway)      │
        │ PostgreSQL 16                  │
        │                                │
        │ - Usuarios                     │
        │ - Solicitudes                  │
        │ - Historial                    │
        └────────────────────────────────┘
```

---

## 🛠️ Tecnologías

- **Frontend**: Angular 18, TypeScript, Vercel
- **Backend**: Spring Boot 3.2.3, Java 17, Railway
- **Base de Datos**: PostgreSQL 16, Railway
- **Autenticación**: JWT (JSON Web Tokens)
- **API**: REST, OpenAPI/Swagger

---

## 📞 Soporte

Si tienes problemas:
1. Verifica que estés usando la URL correcta de Vercel
2. Comprueba que las credenciales sean exactas
3. Revisa los logs del backend en Railway
4. Verifica que la base de datos PostgreSQL esté conectada
