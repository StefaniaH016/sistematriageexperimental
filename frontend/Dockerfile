# ---------- ETAPA 1: BUILD ----------
FROM node:22-alpine AS build
ARG BASE_HREF=/
ENV CI=true
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci --silent

COPY . .
RUN npx ng build --configuration production --base-href=${BASE_HREF}

# ---------- ETAPA 2: RUNTIME (Nginx con envsubst) ----------
FROM nginx:stable-alpine AS runtime
# Necesitamos gettext para envsubst
RUN apk add --no-cache gettext

ENV NGINX_HTML_DIR=/usr/share/nginx/html
WORKDIR /app

# Limpiar html por defecto y copiar build
RUN rm -rf ${NGINX_HTML_DIR}/*
COPY --from=build /app/dist/frontend/browser ${NGINX_HTML_DIR}

# Copiar plantilla de nginx (plantilla con {{BACKEND_URL}})
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Entrypoint: sustituir variables y arrancar nginx
CMD ["/bin/sh", "-c", "envsubst '${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"]
