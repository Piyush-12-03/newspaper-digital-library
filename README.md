# Newspaper Digital Library

A production-quality backend system for managing newspaper editions and their daily PDF issues.

## 📋 Purpose

This backend system manages newspaper editions (e.g., Bhopal City, Indore City) and their daily issues. It supports admin operations including PDF upload/download, edition management, and date-based querying of newspaper archives.

### Domain Model

```
Edition (e.g., "Bhopal City")
│
├── Issue - 12 Aug 2026
├── Issue - 13 Aug 2026
└── Issue - 14 Aug 2026
```

An **Edition** represents a newspaper edition (reusable across dates).  
An **Issue** represents a specific edition published on a specific date.

## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **Maven**
- **Spring Web**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Security**
- **JWT (io.jsonwebtoken)**
- **Jakarta Validation**
- **Lombok**
- **Spring Boot Actuator**
- **Spring Boot DevTools**
- **Springdoc OpenAPI / Swagger**
- **AWS SDK for S3**

## 🏗️ Architecture Overview

This project follows a **layered architecture** with clear separation of concerns:

- **Controller Layer**: Thin REST controllers handling HTTP requests
- **Service Layer**: Business logic implementation
- **Repository Layer**: Data persistence using Spring Data JPA
- **Entity Layer**: JPA entities representing domain models
- **DTO Layer**: Data Transfer Objects for API communication
- **Security Layer**: JWT authentication and authorization
- **Exception Layer**: Centralized exception handling
- **Configuration Layer**: Application configuration and beans

### Design Principles

1. Controllers remain thin - only handle HTTP concerns
2. Business logic resides in services
3. DTOs are separate from entities
4. Storage is abstracted behind `FileStorageService` interface
5. Constructor injection via Lombok `@RequiredArgsConstructor`
6. No field injection
7. Global exception handling through `@RestControllerAdvice`

## 📁 Package Structure

```
src/main/java/com/newspaper/library/
├── config/
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   └── StorageConfig.java
│
├── controller/
│   ├── AuthController.java
│   └── EditionController.java
│
├── service/
│   ├── AuthService.java
│   ├── EditionService.java
│   └── FileStorageService.java
│
├── service/impl/
│   ├── AuthServiceImpl.java
│   ├── EditionServiceImpl.java
│   └── S3FileStorageServiceImpl.java
│
├── repository/
│   ├── AdminUserRepository.java
│   ├── EditionRepository.java
│   └── IssueRepository.java
│
├── entity/
│   ├── AdminUser.java
│   ├── Edition.java
│   └── Issue.java
│
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   └── edition/
│       ├── EditionResponse.java
│       ├── IssueResponse.java
│       └── EditionListResponse.java
│
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   └── CustomUserDetailsService.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── InvalidFileException.java
│   └── StorageException.java
│
├── mapper/
│   ├── EditionMapper.java
│   └── IssueMapper.java
│
├── constants/
│   └── SecurityConstants.java
│
├── enums/
│   ├── Role.java
│   └── EditionType.java
│
└── NewspaperDigitalLibraryApplication.java
```

## 🔧 Local Setup

### Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 14+
- Docker & Docker Compose (optional)

### Environment Variables

Copy `.env.example` to `.env` and configure the following variables:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/newspaper_library
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# AWS S3
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_S3_BUCKET=newspaper-library-pdfs

# Profile
SPRING_PROFILES_ACTIVE=dev
```

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE newspaper_library;
```

2. The application will manage schema through JPA/Hibernate

## 🚀 Running the Application

### With Maven

```bash
# Development mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### With Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Building JAR

```bash
# Clean and package
mvn clean package

# Run JAR
java -jar target/newspaper-digital-library-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health

### Planned API Endpoints

- `POST /api/v1/auth/login` - Admin authentication
- `GET /api/v1/editions` - List all editions
- `POST /api/v1/editions` - Create new edition
- `GET /api/v1/editions/{id}` - Get edition details
- `GET /api/v1/editions/{id}/issues` - List issues for an edition
- `POST /api/v1/editions/{id}/issues` - Upload new issue
- `GET /api/v1/issues/{id}/download` - Download PDF
- `GET /api/v1/issues?date={date}` - Get issues by publication date
- `GET /api/v1/issues?startDate={start}&endDate={end}` - Get issues by date range

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## 📊 Current Status

This is the **foundation setup** of the project. The following have been created:

✅ Complete package structure  
✅ All entity classes (Edition, Issue, AdminUser)  
✅ Repository interfaces  
✅ Service interfaces and skeleton implementations  
✅ Controller skeletons  
✅ DTO classes  
✅ Exception handling structure  
✅ Security configuration (permits all for now)  
✅ OpenAPI/Swagger configuration  
✅ Application configuration files (dev/prod profiles)  
✅ Docker setup  
✅ Maven dependencies  

### Not Yet Implemented

⏳ Authentication logic (JWT generation/validation)  
⏳ Business logic for edition/issue management  
⏳ File upload/download endpoints  
⏳ S3 integration logic  
⏳ API endpoints implementation  
⏳ Database schema initialization  
⏳ Unit and integration tests  

## 🔐 Security Notes

- All endpoints currently permit access (configured for initial setup)
- JWT authentication will be implemented in next phase
- Passwords will be encrypted with BCrypt
- Security filter chain configured for stateless sessions

## 📝 Notes

- The project compiles successfully and is ready for business logic implementation
- Database schema will be created automatically through JPA entities
- S3 configuration is prepared but requires valid AWS credentials
- Swagger documentation is accessible without authentication

## 👥 Development

Built for a 7-day assignment timeline with focus on:
- Clean architecture
- Production-ready patterns
- Scalability
- Maintainability
- Security best practices

---

**License**: Apache 2.0
