# ETAPA 1: BUILD
FROM node:18-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencia
COPY package.json package-lock.json ./

# Instalar dependencias
RUN npm install

# Copiar código fuente
COPY . .

# Argumento para la URL de la API (opcional)
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# Construir la aplicación
RUN npm run build

# ETAPA 2: RUN (Nginx)
FROM nginx:stable-alpine

# Eliminar configuración por defecto de Nginx
RUN rm -rf /etc/nginx/conf.d/*

# Copiar configuración personalizada si fuera necesario, 
# pero por ahora usaremos una configuración básica para SPA
RUN cat <<EOF > /etc/nginx/conf.d/default.conf
server {
    listen 80;
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files \$uri \$uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
EOF

# Copiar archivos estáticos desde la etapa builder
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
