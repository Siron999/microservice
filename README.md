# Spring Microservices Architecture Project

A robust microservices architecture implementing secure authentication, API gateway, and monitoring capabilities using Spring Boot and Cloud Gateway.

## 📋 Overview

This project demonstrates a production-ready microservices setup with:
- Authentication service with JWT security
- API Gateway for routing and rate limiting
- Redis caching for performance optimization
- ELK stack for centralized logging
- Docker containerization

## 🏗 Architecture

```
├── APIGateway/             # API Gateway Service
├── authService/            # Authentication Service
├── authservicedocker/     # Auth Service Docker config  
├── databasedocker/        # Database Docker configs
├── gatewaydocker/         # Gateway Docker config
├── searchdocker/          # ELK Stack Docker config
└── logs/                  # Log files
```

## 🚀 Tech Stack

- **Spring Boot 3.4.3**
- **Spring Cloud Gateway**  
- **Spring WebFlux**
- **MongoDB**
- **Redis**
- **Postgres**
- **Docker & Docker Compose**
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Maven**
- **Java 23**

## 🛠 Prerequisites

- Docker Desktop for Mac
- Java 23
- Maven
- Git

## ⚙️ Setup & Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd microservices-auth
```

2. **Configure Environment Variables**
Create `.env` files in:
- authservicedocker/
- databasedocker/
- gatewaydocker/
- searchdocker/

3. **Start Services**
```bash
# Start databases
cd databasedocker && docker-compose up -d

# Start auth service
cd ../authservicedocker && docker-compose up -d

# Start API gateway
cd ../gatewaydocker && docker-compose up -d

# Start ELK stack
cd ../searchdocker && docker-compose up -d
```

## 📡 API Documentation

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "string",
    "password": "string"
}
```

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
    "username": "string",
    "email": "string",
    "password": "string",
    "fullName": "string",
    "role": "string",
    "phoneNumber": "string",
    "address": "string"
}
```

#### Authenticate Token
```http
GET /api/auth/authenticate
Authorization: Bearer <token>
```

## 🔒 Security Features

- JWT authentication
- Password encryption (BCrypt)
- Redis token blacklisting
- API rate limiting
- CORS configuration
- Input validation
- Global error handling

## 🚦 Rate Limiting

- 5 requests/second replenish rate
- 10 requests burst capacity
- Per service and client IP limits

## 📊 Monitoring

### Logging (ELK Stack)
- Filebeat collects service logs
- Elasticsearch for storage
- Kibana dashboards at `http://localhost:5601`

### Metrics
- API Gateway metrics
- Authentication service logs
- Error tracking
- Performance monitoring

## 🧪 Development

### Building Services
```bash
# Build Auth Service
cd authService && ./mvnw clean package

# Build API Gateway  
cd ../APIGateway && ./mvnw clean package
```

### Testing
```bash
# Unit tests
./mvnw test

# Integration tests  
./mvnw verify

# Load tests
k6 run loadTest.js
```

### Docker Commands
```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 🐛 Error Handling

Standard error response format:
```json
{
    "status": "error",
    "message": "Error description",
    "data": null
}
```

