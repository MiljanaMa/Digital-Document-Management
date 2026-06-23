# DDM Auth

Standalone authentication microservice for the DDM stack. It owns:

- user registration
- username/password login
- JWT signing compatible with `ddm-backend`
- user/role persistence in PostgreSQL

## Endpoints

| Method | Path               | Description |
|--------|--------------------|-------------|
| GET    | `/health`          | Liveness/readiness probe |
| POST   | `/api/auth/login`  | `{username, password}` -> `{message, token}` |
| POST   | `/api/auth/register` | Registers a new user in PostgreSQL |
| GET    | `/api/auth/verify` | Validates `Authorization: Bearer <token>` |

## Config

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `DATABASE_URL` optional full SQLAlchemy connection string override
- `JWT_PRIVATE_KEY_PATH` path to the RSA private key used for signing
- `JWT_PUBLIC_KEY_PATH` path to the RSA public key used for verification
- `JWT_ALGORITHM` default `RS256`
- `JWT_TTL_SECONDS` default `36000`
- `CORS_ORIGINS` comma-separated allowed browser origins
- `PORT` default `8090`

The service seeds `ROLE_USER` and `ROLE_ADMIN` into the shared `roles` table on startup.

## Run locally

```bash
pip install -r requirements.txt
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=DDMDemoDatabase
export DB_USERNAME=postgres
export DB_PASSWORD='<postgres-password>'
export JWT_PRIVATE_KEY_PATH=/absolute/path/to/private-key.pem
export JWT_PUBLIC_KEY_PATH=/absolute/path/to/public-key.pem
uvicorn main:app --reload --host 0.0.0.0 --port 8090
```
