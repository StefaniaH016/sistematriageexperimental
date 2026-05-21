# ============================================================
# Dockerfile para Frontend (Angular + Nginx)
# ============================================================

# ETAPA 1: BUILD
FROM node:22-alpine AS build

# Instalar Angular CLI globalmente para compilar
RUN npm install -g @angular/cli@21.1.4

WORKDIR /app

# Copiar archivos de dependencias e instalarlas
COPY package.json package-lock.json ./
RUN npm ci

# Copiar código fuente y compilar para producción
COPY . .
RUN ng build --configuration production

# ETAPA 2: RUNTIME
FROM nginx:1.27-alpine AS runtime
LABEL maintainer="UniQuindío"
LABEL description="Frontend del Sistema de Triage y Gestión de Solicitudes Académicas"

# Eliminar configuración por defecto
RUN rm /etc/nginx/conf.d/default.conf

# Copiar nuestra configuración personalizada
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar los compilados de Angular de la etapa build
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
