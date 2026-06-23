import os
import time
import uuid
from contextlib import asynccontextmanager

import bcrypt
import jwt
from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, PlainTextResponse
from prometheus_fastapi_instrumentator import Instrumentator
from pydantic import BaseModel, ConfigDict
from sqlalchemy import Column, ForeignKey, Integer, String, create_engine
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, declarative_base, relationship, sessionmaker

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "DDMDemoDatabase")
DB_USERNAME = os.getenv("DB_USERNAME", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    f"postgresql+psycopg2://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}",
)

JWT_PRIVATE_KEY_PATH = os.getenv("JWT_PRIVATE_KEY_PATH", "/app/certs/private-key.pem")
JWT_PUBLIC_KEY_PATH = os.getenv("JWT_PUBLIC_KEY_PATH", "/app/certs/public-key.pem")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "RS256")
JWT_TTL_SECONDS = int(os.getenv("JWT_TTL_SECONDS", "36000"))

CORS_ORIGINS = [
    origin.strip()
    for origin in os.getenv("CORS_ORIGINS", "").split(",")
    if origin.strip()
]

ROLE_USER = "ROLE_USER"
ROLE_ADMIN = "ROLE_ADMIN"

engine = create_engine(DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)
Base = declarative_base()


class Role(Base):
    __tablename__ = "roles"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String, nullable=False, unique=True)


class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    username = Column(String, nullable=False, unique=True)
    first_name = Column("first_name", String, nullable=False)
    last_name = Column("last_name", String, nullable=False)
    password = Column(String, nullable=False)
    role_id = Column(Integer, ForeignKey("roles.id"), nullable=False)
    role = relationship("Role", lazy="joined")


class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    message: str
    token: str | None


class RegistrationRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")

    username: str
    firstName: str
    lastName: str
    password: str
    confirmPassword: str | None = None


def load_private_key() -> str:
    with open(JWT_PRIVATE_KEY_PATH, "r", encoding="utf-8") as key_file:
        return key_file.read()


def load_public_key() -> str:
    with open(JWT_PUBLIC_KEY_PATH, "r", encoding="utf-8") as key_file:
        return key_file.read()


def hash_password(raw_password: str) -> str:
    return bcrypt.hashpw(raw_password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def verify_password(raw_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(raw_password.encode("utf-8"), hashed_password.encode("utf-8"))


def generate_token(user: User) -> str:
    now = int(time.time())
    payload = {
        "iss": "self",
        "iat": now,
        "exp": now + JWT_TTL_SECONDS,
        "sub": user.username,
        "scope": user.role.name,
    }
    return jwt.encode(payload, load_private_key(), algorithm=JWT_ALGORITHM)


def ensure_seed_data() -> None:
    Base.metadata.create_all(bind=engine)

    with SessionLocal() as session:
        for role_name in (ROLE_USER, ROLE_ADMIN):
            exists = session.query(Role).filter(Role.name == role_name).first()
            if exists is None:
                session.add(Role(name=role_name))
        session.commit()


@asynccontextmanager
async def lifespan(_: FastAPI):
    ensure_seed_data()
    yield


app = FastAPI(title="DDM Auth Service", version="1.0.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type"],
)
Instrumentator().instrument(app).expose(app, endpoint="/metrics")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/api/auth/login", response_model=LoginResponse)
def login(req: LoginRequest):
    with SessionLocal() as session:
        user = session.query(User).filter(User.username == req.username).first()

        if user is None or not verify_password(req.password, user.password):
            return JSONResponse(
                status_code=400,
                content=LoginResponse(message="Login error", token=None).model_dump(),
            )

        return LoginResponse(
            message="User logged in successfully",
            token=generate_token(user),
        )


@app.post("/api/auth/register")
def register(req: RegistrationRequest):
    if req.confirmPassword is not None and req.password != req.confirmPassword:
        return PlainTextResponse("Passwords do not match", status_code=400)

    with SessionLocal() as session:
        existing_user = session.query(User).filter(User.username == req.username).first()
        if existing_user is not None:
            return PlainTextResponse(
                "User with this credentials already exists",
                status_code=400,
            )

        role = session.query(Role).filter(Role.name == ROLE_USER).first()
        if role is None:
            raise HTTPException(status_code=500, detail="Default role is missing")

        user = User(
            username=req.username,
            first_name=req.firstName,
            last_name=req.lastName,
            password=hash_password(req.password),
            role=role,
        )
        session.add(user)

        try:
            session.commit()
        except IntegrityError:
            session.rollback()
            return PlainTextResponse(
                "User with this credentials already exists",
                status_code=400,
            )

    return PlainTextResponse("User registered successfully", status_code=201)


@app.get("/api/auth/verify")
def verify(authorization: str = Header(default="")):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="missing bearer token")

    token = authorization.removeprefix("Bearer ").strip()

    try:
        payload = jwt.decode(token, load_public_key(), algorithms=[JWT_ALGORITHM])
    except jwt.PyJWTError as exc:
        raise HTTPException(status_code=401, detail=f"invalid token: {exc}") from exc

    return {"valid": True, "subject": payload.get("sub"), "scope": payload.get("scope")}
