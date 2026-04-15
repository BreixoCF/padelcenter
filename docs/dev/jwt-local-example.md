# Local JWT Authentication

When running with the `local` profile, the application accepts JWTs signed with
HMAC-SHA256 using the secret configured in `application-local.yaml`.

Default secret: `local-dev-secret-32-chars-min!!`

## JWT Payload Example

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "admin@padelcenter.com",
  "roles": ["ADMIN"],
  "iat": 1713168000,
  "exp": 1745290000
}
```

- `sub` — User UUID (maps to `AuthenticatedUser.userId`)
- `roles` — Extracted as `ROLE_*` granted authorities
- `iat` / `exp` — Standard issued-at and expiration timestamps

## Generate a Token

### Using jwt.io

1. Go to [jwt.io](https://jwt.io)
2. Select algorithm: **HS256**
3. Paste the payload above
4. Set the secret to: `local-dev-secret-32-chars-min!!`
5. Copy the encoded token

### Using the command line (requires `jq` and `openssl`)

```bash
HEADER=$(echo -n '{"alg":"HS256","typ":"JWT"}' | base64 -w0 | tr '+/' '-_' | tr -d '=')
PAYLOAD=$(echo -n '{"sub":"550e8400-e29b-41d4-a716-446655440000","email":"admin@padelcenter.com","roles":["ADMIN"],"iat":1713168000,"exp":1745290000}' | base64 -w0 | tr '+/' '-_' | tr -d '=')
SECRET="local-dev-secret-32-chars-min!!"
SIGNATURE=$(echo -n "${HEADER}.${PAYLOAD}" | openssl dgst -sha256 -hmac "${SECRET}" -binary | base64 -w0 | tr '+/' '-_' | tr -d '=')
TOKEN="${HEADER}.${PAYLOAD}.${SIGNATURE}"
echo $TOKEN
```

## Authenticated curl Example

```bash
# Generate or paste your token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...."

# List users (authenticated)
curl -s http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer ${TOKEN}" | jq .

# Create a center (authenticated)
curl -s -X POST http://localhost:8080/api/v1/centers \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Padel Barcelona","address":"Calle Principal 1","city":"Barcelona","phoneNumber":"933000000","email":"info@padelbcn.com"}' | jq .
```

## Switching to a Real Provider

When connecting Keycloak, Auth0, or any OIDC provider, update `application.yaml`
(or set environment variables) with the provider's URIs:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-provider.example.com/realms/padelcenter
          jwk-set-uri: https://your-provider.example.com/realms/padelcenter/protocol/openid-connect/certs
```

Remove the `local` profile and the `jwt.secret` property — the application will
validate tokens using the provider's public keys automatically.
