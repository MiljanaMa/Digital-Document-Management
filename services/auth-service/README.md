# DDM Auth Service

A minimal standalone microservice (Python / FastAPI) used to demonstrate
managing multiple independent microservices with Argo CD.

## Endpoints

| Method | Path           | Description                                  |
|--------|----------------|----------------------------------------------|
| GET    | `/health`      | Liveness/readiness probe                     |
| POST   | `/auth/login`  | `{username, password}` → JWT (demo: any non-empty creds) |
| GET    | `/auth/verify` | Validates `Authorization: Bearer <token>`    |

## Config (env)

- `JWT_SECRET` — signing key (set via Secret in the chart)
- `JWT_ALGORITHM` — default `HS256`
- `JWT_TTL_SECONDS` — token lifetime, default `3600`
- `PORT` — listen port, default `8090`

## Run locally

```bash
pip install -r requirements.txt
uvicorn main:app --reload --port 8090
```

Deployed via `Gitops/charts/auth-service` (Argo CD app `apps/auth-service.yaml`).
Image: `miljanama/ddm-auth-service`, built by `.github/workflows/build-images.yml`.