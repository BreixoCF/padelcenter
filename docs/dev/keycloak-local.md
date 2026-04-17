# Keycloak local setup

## Arquitectura

El backend es un **resource server puro** — no emite tokens, solo los valida.
Keycloak actúa como proveedor de identidad (IdP): emite JWTs firmados con RS256
que el backend verifica mediante el JWK Set público del realm.

```
Frontend → Keycloak (login) → JWT
Frontend → Backend (JWT en Authorization header) → Spring valida contra Keycloak JWK Set
```

## Arranque

```bash
cd tools/docker
docker compose up -d postgres keycloak

# Espera ~30 segundos a que Keycloak importe el realm
# Admin console: http://localhost:8180/admin  (admin / admin)
# Realm configurado: padelcenter-dev
```

El realm `padelcenter-dev` se importa automáticamente desde
`tools/keycloak/padelcenter-dev-realm.json` al arrancar el contenedor.

## Obtener token para Postman / curl

```bash
curl -s -X POST \
  http://localhost:8180/realms/padelcenter-dev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=padelcenter-web" \
  -d "grant_type=password" \
  -d "username=admin@padelcenter.com" \
  -d "password=admin123" | jq .access_token
```

Incluye el token en las peticiones al backend:

```
Authorization: Bearer <access_token>
```

## Usuarios de desarrollo

| Email | Password | Roles |
|-------|----------|-------|
| admin@padelcenter.com | admin123 | ADMIN |
| user@padelcenter.com | user123 | USER |

## Sincronización de usuario (primer login)

Tras el primer login con Keycloak, el frontend debe llamar a:

```bash
POST /api/v1/auth/sync
Authorization: Bearer <access_token>
```

Este endpoint crea (o recupera si ya existe) el registro `User` local
mapeando el `sub` del JWT como `keycloak_id`. El `passwordHash` queda vacío
porque la autenticación es responsabilidad de Keycloak.

## Flujo de autorización

El claim `roles` del JWT (configurado en el realm como `oidc-usermodel-realm-role-mapper`)
se extrae como `ROLE_ADMIN` / `ROLE_USER` por `SecurityConfig.jwtAuthenticationConverter()`.
Los `@PreAuthorize("hasRole('ADMIN')")` y los evaluadores de centro funcionan igual que antes.

## Variables de entorno (producción)

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `KEYCLOAK_ISSUER_URI` | Issuer URI del realm | `https://auth.padelcenter.com/realms/padelcenter` |
| `KEYCLOAK_JWK_SET_URI` | JWK Set URI para validar firmas | `https://auth.padelcenter.com/realms/padelcenter/protocol/openid-connect/certs` |

## Verificación paso a paso

```bash
# 1. Levantar servicios
cd tools/docker && docker compose up -d postgres keycloak

# 2. Esperar 30s y verificar que el realm existe
open http://localhost:8180/admin

# 3. Obtener token
TOKEN=$(curl -s -X POST \
  http://localhost:8180/realms/padelcenter-dev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=padelcenter-web&grant_type=password&username=admin@padelcenter.com&password=admin123" \
  | jq -r .access_token)

# 4. Arrancar la app
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 5. Sincronizar usuario local
curl -s http://localhost:8080/api/v1/auth/sync \
  -H "Authorization: Bearer $TOKEN" | jq .

# 6. Llamar a un endpoint protegido
curl -s http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" | jq .
```

## Troubleshooting

**`401 Unable to find X509 certificate`** — Keycloak aún no ha arrancado del todo.
Espera más tiempo o comprueba los logs con `docker compose logs keycloak`.

**`403 Forbidden` en endpoints de ADMIN** — El usuario de Keycloak no tiene el rol `ADMIN`
asignado en el realm. Asígnalo desde la admin console o usa `admin@padelcenter.com`.

**Realm no importado** — Verifica que `tools/keycloak/padelcenter-dev-realm.json` existe
y que el volumen en `docker-compose.yaml` apunta al directorio correcto.
