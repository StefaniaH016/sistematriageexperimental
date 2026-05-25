# Configuración Final de Railway — Sistema de Triage

## ✅ Estado Actual

Tu backend está corriendo en Railway en:
```
https://sistematriageexperimental-production.up.railway.app
```

## 🔧 Lo que FALTA hacer en Railway Dashboard

### Paso 1: Verificar que PostgreSQL está conectado

1. Ve a tu proyecto en Railway
2. Abre el servicio **PostgreSQL**
3. Ve a la pestaña **Variables**
4. Copia estos valores (los necesitarás en el Paso 2):
   - `DATABASE_URL`
   - `PGHOST`
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`

### Paso 2: Configurar las variables del Backend en Railway

1. Ve al servicio **sistematriageexperimental** (backend)
2. Abre la pestaña **Variables**
3. **VERIFICA que existan estas variables** (deberían estar auto-vinculadas):
   - `DATABASE_URL` ← del PostgreSQL
   - `PGHOST` ← del PostgreSQL
   - `PGPORT` ← del PostgreSQL
   - `PGDATABASE` ← del PostgreSQL
   - `PGUSER` ← del PostgreSQL
   - `PGPASSWORD` ← del PostgreSQL

4. **AGREGA estas variables si NO existen**:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `JWT_SECRET` = `<tu-secreto-jwt-super-seguro-minimo-64-caracteres>`
   - `GEMINI_API_KEY` = `<tu-clave-de-gemini-si-la-usas>`
   - `APP_CORS_ALLOWED_ORIGIN_PATTERNS` = `https://*.vercel.app,https://*.railway.app`
   - `LOG_LEVEL` = `INFO`

### Paso 3: Redeploy del Backend

1. En el servicio **sistematriageexperimental**
2. Haz clic en **Redeploy**
3. Espera a que termine (2-3 minutos)

### Paso 4: Verificar que el Backend funciona

Abre una terminal y ejecuta:

```bash
# Verificar health check
curl https://sistematriageexperimental-production.up.railway.app/actuator/health

# Debe responder:
# {"status":"UP"}

# Probar login (reemplaza con credenciales reales)
curl -X POST https://sistematriageexperimental-production.up.railway.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@uniquindio.edu.co","password":"Admin123!"}'

# Debe responder con un token JWT
```

---

## 🌐 Configuración del Frontend en Vercel

### Paso 5: Crear/Actualizar vercel.json

✅ **YA ESTÁ HECHO** — El archivo `frontend/vercel.json` ya tiene:
- Proxy reverso a `https://sistematriageexperimental-production.up.railway.app`
- Headers de seguridad
- Configuración de build correcta

### Paso 6: Actualizar environment.prod.ts

✅ **YA ESTÁ HECHO** — El archivo `frontend/src/environments/environment.prod.ts` ya tiene:
- `apiUrl: '/api/v1'` (relativo, para que Vercel haga proxy)

### Paso 7: Desplegar en Vercel

1. Ve a https://vercel.com
2. Importa tu repositorio `sistematriageexperimental`
3. Configura:
   - **Framework**: Angular
   - **Root Directory**: `frontend/`
   - **Build Command**: `ng build --configuration production`
   - **Output Directory**: `dist/triage-frontend/browser`
4. Haz clic en **Deploy**
5. Espera a que termine (~2-3 minutos)

---

## ✅ Verificación Final

Una vez que todo esté desplegado, prueba este flujo:

### 1. Acceder al Frontend
```
https://triage-frontend.vercel.app
```

### 2. Probar el flujo completo
- [ ] Registrarse como nuevo usuario
- [ ] Hacer login
- [ ] Crear una solicitud académica
- [ ] Ver la solicitud en el listado
- [ ] (Si eres FUNCIONARIO) Clasificar y priorizar la solicitud
- [ ] Ver el historial de cambios

### 3. Verificar que NO hay errores de CORS
- Abre DevTools (F12) → Consola
- No debe haber mensajes de error sobre CORS
- Las peticiones a `/api/v1/*` deben responder correctamente

---

## 🔍 Troubleshooting

### Error: "Connection refused" en Railway
**Causa**: Las variables de entorno no están configuradas correctamente.
**Solución**: Verifica que `DATABASE_URL` y `SPRING_PROFILES_ACTIVE=prod` estén en Railway.

### Error: CORS bloqueado en Vercel
**Causa**: El `vercel.json` no está configurado correctamente.
**Solución**: Verifica que el archivo `frontend/vercel.json` existe y tiene la URL correcta de Railway.

### Error: 404 al recargar la página en Vercel
**Causa**: Angular SPA routing no está configurado.
**Solución**: El `vercel.json` ya tiene la configuración correcta. Si persiste, verifica que el archivo existe.

### Error: "Unable to access jarfile" en Railway
**Causa**: El build de Maven falló.
**Solución**: Revisa los logs en Railway Dashboard → Backend → Logs.

---

## 📝 Resumen de URLs

| Servicio | URL |
|----------|-----|
| Backend (Railway) | `https://sistematriageexperimental-production.up.railway.app` |
| Frontend (Vercel) | `https://triage-frontend.vercel.app` |
| PostgreSQL (Railway) | Interno — no accesible desde internet |

---

## 🚀 Próximos pasos (Opcional)

1. **Configurar dominio personalizado** en Vercel (ej: `triage.midominio.com`)
2. **Agregar monitoreo** con Prometheus + Grafana
3. **Configurar backups automáticos** de PostgreSQL en Railway
4. **Agregar CI/CD** con GitHub Actions para despliegues automáticos

---

## ❓ ¿Necesitas ayuda?

Si algo no funciona:
1. Revisa los logs en Railway Dashboard
2. Verifica que todas las variables de entorno están configuradas
3. Prueba el health check del backend
4. Abre DevTools en el navegador para ver errores de CORS
