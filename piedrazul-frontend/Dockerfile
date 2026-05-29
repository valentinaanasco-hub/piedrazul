# ============================================================
# Dockerfile para Frontend (React/Vite) - Producción
# Multi-stage build: Build con Node + Servir con Nginx
# ============================================================

# --- Etapa 1: Build ---
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar todas las dependencias (incluyendo devDependencies para el build)
RUN npm ci

# Copiar código fuente
COPY . .

# Construir aplicación para producción
RUN npm run build

# --- Etapa 2: Servir con Nginx ---
FROM nginx:alpine

# Copiar build al directorio de Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto 5173
EXPOSE 5173

# Nginx se inicia automáticamente
CMD ["nginx", "-g", "daemon off;"]
