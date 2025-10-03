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

6. **Backend app** - Java Spring Boot (APIs, parsing, authentication, database access). 

7. **PostgreSQL** - Relational database for backend storage.

## Functionalities 
1. **Document Parsing & Indexing**  
2. **Search Features** – Full-text, approximate KNN, phrase queries, boolean semi-structured search (AND, OR, NOT), geolocation search. 
3. **Kibana Dashboards for visualisations** - Visualizations for incident trends, affected organizations, employees, and cities. 

## Running the Application 

### ELK stack 

1. Open a terminal in the project root.  
2. Run `docker-compose up -d`. 

### Frontend (React) 

1. Open the **ddm-frontend** folder in Visual Studio Code. 
2. Open a terminal and run: `npm install`. 
3. Then run: `npm start`. 
4. The app will be available at: http://localhost:3000/. 

### Backend (Spring Boot) 

1. Open IntelliJ IDEA and import the project **ddm**. 
2. In application.properties, configure PostgreSQL database credentials. 
3. Run the Application class in src/main/java. 

