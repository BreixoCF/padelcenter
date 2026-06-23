# PadelCenter — TFG

Trabajo Fin de Grado: sistema de gestión de reservas de pistas de pádel y torneos.
Monorepo con los tres componentes del proyecto, cada uno conservando su historial
de commits original (importado vía `git subtree`).

| Carpeta | Contenido | Stack |
|---|---|---|
| [`backend/`](backend/) | API REST, lógica de negocio | Java 21 + Spring Boot 3.4 + PostgreSQL |
| [`web/`](web/) | Aplicación web | Next.js 14 (App Router) + TypeScript |
| [`memoria/`](memoria/) | Memoria del TFG | LaTeX |

## Arranque rápido

```bash
# 1. Backend — base de datos + API
cd backend/tools/docker
docker compose up -d --build

# 2. Web — frontend (requiere el backend corriendo en :8080)
cd ../../../web
docker compose up -d --build
```

- API: http://localhost:8080 — Swagger UI en `/swagger-ui/index.html`
- Web: http://localhost:3000

Instrucciones detalladas, variables de entorno y arranque sin Docker en el
README de cada subproyecto: [backend/README.md](backend/README.md),
[web/README.md](web/README.md).

## Memoria

Fuente LaTeX en `memoria/`, PDF compilado en
[`memoria/memoria_tfg.pdf`](memoria/memoria_tfg.pdf).

```bash
cd memoria
latexmk -xelatex memoria_tfg.tex
```
