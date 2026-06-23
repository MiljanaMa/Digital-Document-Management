# Digital-Document-Management 
## Overview 

This project implements a system for **monitoring**, **parsing**, **indexing**, and **searching** security incident documents. The goal is to provide an integrated solution for cybersecurity analytics, enabling efficient document management, full-text search, and visualization of security-related data. 

The solution is designed to help security teams efficiently locate incidents, analyze reports, and gain insights through interactive dashboards. 

## Technologies 

1. **MinIO** - Object storage for raw PDF documents. 

2. **Elasticsearch** - Core search engine and indexing. 

3. **Logstash** - Data ingestion and parsing with GROK filters. 

4. **Kibana** - Visualization of security incident trends. 

5. **Frontend app** - React (user interface for search and dashboards). 

6. **Backend app** - Java Spring Boot (APIs, parsing, search, database access). 

7. **DDM Auth** - Python FastAPI microservice for login, registration, and JWT issuance.

8. **PostgreSQL** - Relational database shared by the backend and `ddm-auth`.

## Functionalities 
1. **Document Parsing & Indexing**  
2. **Search Features** – Full-text, approximate KNN, phrase queries, boolean semi-structured search (AND, OR, NOT), geolocation search. 
3. **Kibana Dashboards for visualisations** - Visualizations for incident trends, affected organizations, employees, and cities. 

## Running the Application 

### Docker stack 

1. Open a terminal in the project root.  
2. Provide secrets via environment variables or an uncommitted `.env`.
3. Mount JWT key files from outside the repo and set `JWT_KEYS_DIR`.
4. Run `docker-compose up --build -d`.
5. Open `http://localhost/`.

The Nginx gateway routes:

- `/` -> React frontend
- `/api/auth/**` -> `ddm-auth`
- `/api/**` -> ddm-backend

Required local environment variables:

- `POSTGRES_PASSWORD`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `LOCATION_API_KEY`
- `JWT_KEYS_DIR`

### Frontend (React) 

1. Open the **ddm-frontend** folder in Visual Studio Code. 
2. Open a terminal and run: `npm install`. 
3. Then run: `npm start`. 
4. The app will be available at: http://localhost:3000/. 

### Backend (Spring Boot) 

1. Open IntelliJ IDEA and import the project **ddm**. 
2. In application.properties, configure PostgreSQL database credentials. 
3. Run the Application class in src/main/java. 

### DDM Auth (FastAPI)

1. Open the **ddm-auth** folder.
2. Install dependencies with `pip install -r requirements.txt`.
3. Point `JWT_PRIVATE_KEY_PATH` and `JWT_PUBLIC_KEY_PATH` at external key files.
4. Run `uvicorn main:app --reload --host 0.0.0.0 --port 8090`.

The frontend uses:

- `REACT_APP_API_BASE_URL` for backend endpoints such as search and file APIs.
- `REACT_APP_AUTH_BASE_URL` for `ddm-auth`.

When the gateway is used, both can stay same-origin:

- `REACT_APP_API_BASE_URL=/api/`
- `REACT_APP_AUTH_BASE_URL=/api/auth/`

CORS is configured via environment variables, not hardcoded in the services:

- `ddm-backend`: `CORS_ALLOWED_ORIGIN_PATTERNS`
- `ddm-auth`: `CORS_ORIGINS`
