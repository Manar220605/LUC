# Lebanese University Connect (LUC)

A Reddit-style Q&A platform connecting Lebanese University students with alumni.

## How to run

> Docker Compose setup is added in Step 1. Once available:

```bash
cp .env.example .env
# Edit .env and set secrets (KEYCLOAK_ADMIN_CLIENT_SECRET, NEXTAUTH_SECRET)

docker compose up --build
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost:3000      |
| Backend  | http://localhost:8080      |
| Keycloak | http://localhost:8081      |
| Postgres | localhost:5432             |

See [IMPLEMENTATION.md](./IMPLEMENTATION.md) for the full build plan.
