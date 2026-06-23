"""Minimal standalone auth microservice for the DDM stack.

Demonstrates an independent (Python/FastAPI) microservice managed by Argo CD,
alongside the Java backend and React frontend. Issues and verifies JWTs.
"""
import os
import time

import jwt
from fastapi import FastAPI, Header, HTTPException
from prometheus_fastapi_instrumentator import Instrumentator
from pydantic import BaseModel

# Config via env (wired through the Helm chart's ConfigMap/Secret).
JWT_SECRET = os.getenv("JWT_SECRET", "dev-secret-change-me")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
JWT_TTL_SECONDS = int(os.getenv("JWT_TTL_SECONDS", "3600"))

app = FastAPI(title="DDM Auth Service", version="0.1.0")

# Expose Prometheus metrics at /metrics (scraped via the chart's ServiceMonitor).
Instrumentator().instrument(app).expose(app, endpoint="/metrics")


class LoginRequest(BaseModel):
    username: str
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "Bearer"
    expires_in: int


@app.get("/health")
def health():
    """Liveness/readiness probe target."""
    return {"status": "ok"}


@app.post("/auth/login", response_model=TokenResponse)
def login(req: LoginRequest):
    # Demo only: accept any non-empty credentials and mint a JWT.
    if not req.username or not req.password:
        raise HTTPException(status_code=400, detail="username and password are required")
    now = int(time.time())
    payload = {"sub": req.username, "iat": now, "exp": now + JWT_TTL_SECONDS}
    token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)
    return TokenResponse(access_token=token, expires_in=JWT_TTL_SECONDS)


@app.get("/auth/verify")
def verify(authorization: str = Header(default="")):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="missing bearer token")
    token = authorization.removeprefix("Bearer ").strip()
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except jwt.PyJWTError as exc:
        raise HTTPException(status_code=401, detail=f"invalid token: {exc}") from exc
    return {"valid": True, "subject": payload.get("sub")}