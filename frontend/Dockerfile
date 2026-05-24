# ---------- ETAPA 1: BUILD ----------
FROM node:22-alpine AS build

# Variables opcionales
ARG BASE_HREF=/
ENV CI=true

WORKDIR /app

# Copiar package files primero para aprovechar cache de dependencias
COPY package.json package-lock.json ./
RUN npm ci --silent

# Copiar el resto del código y compilar
COPY . .
# Si usas Angular CLI local, usa npx; si está global, ajusta
RUN npx ng build --configuration production --base-href=${BASE_HREF}

# ---------- ETAPA 2: RUNTIME (Nginx) ----------
FROM nginx:stable-alpine AS runtime
LABEL maintainer="StefaniaH016"

# El directorio donde Nginx sirve los archivos
ENV NGINX_HTML_DIR=/usr/share/nginx/html

# Limpia contenido por defecto y copia build
RUN rm -rf ${NGINX_HTML_DIR}/*
COPY --from=build /app/dist/frontend/browser ${NGINX_HTML_DIR}

# Copiar configuración personalizada de nginx si existe (opcional)
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto (documental)
EXPOSE 80

# Healthcheck simple (requiere wget en la imagen; en alpine nginx suele tener wget)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- --timeout=2 http://localhost/ || exit 1

# Ejecutar nginx en primer plano
CMD ["nginx", "-g", "daemon off;"]
