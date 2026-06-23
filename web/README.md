# padelcenter-web

[![Next.js](https://img.shields.io/badge/Next.js-14-black)](https://nextjs.org/)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue)](https://www.typescriptlang.org/)

Frontend de gestión de centros de pádel: reservas, pistas, centros, torneos
y usuarios. Next.js 14 (App Router) + TypeScript, sobre la API de
[backend](../backend).

## Requisitos previos

- [Docker](https://docs.docker.com/get-docker/) + Docker Compose (arranque rápido)
- Node.js 20+ y [pnpm](https://pnpm.io/) (solo si quieres ejecutar fuera de Docker)

## Arranque rápido con Docker

Este servicio forma parte del `docker-compose.yaml` en la raíz del
monorepo, junto con `postgres` y `backend`. Para levantar todo:

```bash
# desde la raíz del repo
docker compose up -d --build
```

- App: http://localhost:3000

Dentro de la red de Docker, el frontend llega al backend por nombre de
servicio (`http://backend:8080`), no por `localhost`.

## Arranque en local (sin Docker)

```bash
pnpm install
cp .env.example .env.local   # ajusta las URLs si el backend no está en :8080
pnpm dev
```

- App: http://localhost:3000

## Generar el cliente de la API

El cliente React Query se genera a partir del contrato OpenAPI del
backend con [Orval](https://orval.dev/). Requiere el backend corriendo
en `localhost:8080` (sirve el spec en `/api-docs`):

```bash
pnpm generate-api
```

**Nunca editar a mano los ficheros de `src/lib/api/generated/`.**

## Variables de entorno

| Variable | Descripción | Dónde se usa |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | URL del backend visible desde el navegador | Cliente (axios), se incrusta en el bundle al compilar |
| `API_URL` | URL del backend usada por el servidor Next.js | `next.config.mjs` (`rewrites`), solo servidor |

Ambas se resuelven en **build time** (no en runtime): si cambias el
backend de URL, hay que reconstruir la imagen.
Plantilla en [.env.example](.env.example).

## Tests y lint

```bash
pnpm lint
```

## Estructura del proyecto

```
src/
├── app/                 # Rutas (App Router)
├── lib/
│   ├── api/
│   │   ├── client.ts        # Instancia de axios (customInstance de Orval)
│   │   └── generated/       # Hooks React Query generados por Orval — no editar
│   └── ...
└── components/          # Componentes UI (shadcn/Radix)
```

## Arquitectura

El frontend consume la API de `padelcenter` vía hooks generados con
Orval a partir del contrato OpenAPI (`docs/openapi/index.yaml` en el
backend). La autenticación usa JWT en memoria (Zustand) con refresh
automático vía cookie httpOnly.
