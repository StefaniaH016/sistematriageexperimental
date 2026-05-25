# Credenciales de Prueba - Sistema de Triage

## Usuarios Disponibles

### Estudiante
- **Email**: `juan.perez@uq.edu.co`
- **Contraseña**: `123456`
- **Rol**: ESTUDIANTE

### Estudiante 2
- **Email**: `maria.garcia@uq.edu.co`
- **Contraseña**: `123456`
- **Rol**: ESTUDIANTE

### Responsable
- **Email**: `carlos.lopez@uq.edu.co`
- **Contraseña**: `123456`
- **Rol**: RESPONSABLE

### Responsable 2
- **Email**: `pedro.ramirez@uq.edu.co`
- **Contraseña**: `123456`
- **Rol**: RESPONSABLE

### Administrativo
- **Email**: `ana.martinez@uq.edu.co`
- **Contraseña**: `admin123`
- **Rol**: ADMINISTRATIVO

## URLs

- **Frontend**: https://sistematriageexperimental.vercel.app
- **Backend API**: https://sistematriageexperimental-production.up.railway.app/api/v1
- **Swagger**: https://sistematriageexperimental-production.up.railway.app/swagger-ui.html

## Cómo Probar

1. Abre el frontend en Vercel
2. Intenta hacer login con cualquiera de las credenciales anteriores
3. Si recibes 401, verifica:
   - Que el email sea exacto (incluyendo mayúsculas/minúsculas)
   - Que la contraseña sea correcta
   - Que el backend esté respondiendo en `/api/v1/auth/login`

## Troubleshooting

Si recibes 401 Unauthorized:
- Verifica que el backend esté corriendo en Railway
- Comprueba que la base de datos PostgreSQL esté conectada
- Revisa los logs del backend para ver si hay errores
