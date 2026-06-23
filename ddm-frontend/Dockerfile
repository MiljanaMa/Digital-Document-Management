# syntax=docker/dockerfile:1

# ---- build stage ----
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
# React 19 + react-scripts 5 emit peer-dep warnings; don't let them fail the build.
RUN npm ci --legacy-peer-deps

COPY . .

# Baked in at build time. Defaults to a relative /api/ so the app talks to the
# backend through the same-origin Ingress (host/api -> ddm-backend).
ARG REACT_APP_API_BASE_URL=/api/
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL
ARG REACT_APP_AUTH_BASE_URL=/api/auth/
ENV REACT_APP_AUTH_BASE_URL=$REACT_APP_AUTH_BASE_URL
# CRA treats lint warnings as errors when CI=true; disable for container builds.
ENV CI=false
RUN npm run build

# ---- runtime stage ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
