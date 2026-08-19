# Newspaper Digital Library

A production-grade backend system for managing daily newspaper edition PDFs.

## Project Overview

This application is a backend system for managing daily newspaper edition PDFs. The newspaper group publishes multiple editions such as Bhopal, Indore, Jabalpur, Gwalior, and others. Each edition publishes a PDF newspaper daily.

The system provides APIs to:
- Upload newspaper PDFs (ADMIN only)
- Store newspaper metadata in PostgreSQL
- Store PDF files securely in Amazon S3
- Retrieve available editions by publication date
- Search editions over a date range
- Download newspaper editions via presigned S3 URLs
- Authenticate administrators before allowing uploads

The goal is to replace manual PDF management with a centralized digital library that scales efficiently and follows industry best practices.

## Assignment Requirements

| Requirement | Implementation                                             | Status |
|------------|------------------------------------------------------------|--------|
| Upload API | `POST /api/v1/issues` (Admin-only, JWT)                    | Implemented |
| Store PDF | Amazon S3 with deterministic key structure                 | Implemented |
| Store metadata | PostgreSQL with Liquibase migrations                       | Implemented |
| List editions | `GET /api/v1/editions` (Public)                            | Implemented |
| List issues | `GET /api/v1/issues` with date/range filters               | Implemented |
| Date range | Supported via `?date=` or `?from=&to=`                     | Implemented |
| Download PDF | Presigned S3 URL via `GET /api/v1/issues/{id}/download-url` | Implemented |
| Admin login | JWT authentication via `POST /api/v1/auth/login`           | Implemented |
| User registration | `POST /api/v1/auth/register`                               | Implemented |
| Promote to Admin | `POST /api/v1/auth/promote` (Admin-only)                   | Implemented |
| Swagger UI | OpenAPI 3.0 documentation                                  | Implemented |
| Duplicate prevention | Database constraint + service validation                   | Implemented |
| Pagination | pagination with configurable limits                        | Implemented |
| PDF page count | Extracted via Apache PDFBox during upload                  | Implemented |

## Architecture

```
Client / Postman / Swagger UI
             |
             v
      Spring Boot REST API
             |
       +-----+-----+
       |           |
       v           v
  PostgreSQL      Amazon S3
    Metadata      PDF Files
```

### Component Responsibilities

**Spring Boot**
- REST APIs with consistent JSON responses
- JWT authentication and authorization
- Request validation
- Business logic
- Exception handling with proper HTTP status codes
- S3 integration via AWS SDK v2
- PDF processing via Apache PDFBox

**PostgreSQL**
- Stores metadata for:
  - Editions (name, slug, city, state, language, active status)
  - Issues (edition reference, publication date, S3 key, file size, page count, timestamps)
  - Admin users (username, BCrypt password hash, role)
- Enforces constraints:
  - UNIQUE (edition_id, publication_date) - prevents duplicate uploads
  - UNIQUE (username) - prevents duplicate users
  - UNIQUE (slug) - URL-friendly edition identifiers
  - UNIQUE (storage_key) - prevents S3 key conflicts

**Amazon S3**
- Stores the actual PDF files
- PDFs are never stored in PostgreSQL
- Presigned URLs allow direct client-to-S3 downloads
- Reduces backend bandwidth consumption

## S3 Storage Design

### Storage Key Structure

```
newspaper/{yyyy}/{MM}/{dd}/{edition-slug}/{filename}
```

Example:
```
newspaper/2026/08/17/bhopal-city/bhopal-city-2026-08-17.pdf
```

### Why This Structure?

- **Deterministic**: No random UUIDs, easy to understand
- **Organized by date**: Natural hierarchy for newspaper archives
- **Edition-specific**: Clear separation between editions
- **Original filename preserved**: Users download with meaningful names
- **Scalable**: Can handle thousands of editions
- **Independent from database ID**: Storage doesn't rely on auto-increment IDs

The S3 storage key is stored in the `issues` table so the application doesn't need to reconstruct or guess storage locations.

## Database vs S3 Responsibility

```
PostgreSQL stores metadata:
- edition = "Bhopal City"
- publicationDate = "2026-08-17"
- storageKey = "newspaper/2026/08/17/bhopal-city/bhopal-city-2026-08-17.pdf"
- fileSize = 2048576
- pageCount = 12
- contentType = "application/pdf"

S3 stores the actual file:
- Key: newspaper/2026/08/17/bhopal-city/bhopal-city-2026-08-17.pdf
- Size: 2048576 bytes
- Content-Type: application/pdf
```

### Why Separate?

- **Object storage is purpose-built** for large files
- **PostgreSQL queries remain fast** without binary data
- **S3 scales independently** from the database
- **Backup/restore is simpler** with separated concerns
- **Cost-effective** - S3 is cheaper than database storage for files

## API Design

All endpoints return a consistent JSON structure:

```json
{
  "status": 200,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2026-08-18T10:30:00Z"
}
```

### Authentication APIs

#### POST /api/v1/auth/register
**Public** - Register a new user (regular user role)

Request:
```json
{
  "username": "john",
  "password": "securepass123",
  "email": "john@example.com"
}
```

Response (201):
```json
{
  "status": 201,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "username": "john",
    "role": "USER",
    "message": "Registration successful"
  }
}
```

#### POST /api/v1/auth/login
**Public** - Authenticate and receive JWT token

Request:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response (200):
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "expiresIn": 3600000,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

#### POST /api/v1/auth/promote
**Admin only** - Promote a user to ADMIN role

Request:
```json
{
  "userId": 2
}
```

Response (200):
```json
{
  "status": 200,
  "message": "User promoted to ADMIN successfully",
  "data": {
    "userId": 2,
    "username": "john",
    "oldRole": "USER",
    "newRole": "ADMIN"
  }
}
```

### Edition APIs (Metadata Management)

#### POST /api/v1/editions
**Admin only** - Create a new edition

Request:
```json
{
  "name": "Bhopal City",
  "city": "Bhopal",
  "state": "Madhya Pradesh",
  "language": "Hindi",
  "description": "Daily newspaper for Bhopal city"
}
```

Response (201):
```json
{
  "status": 201,
  "message": "Edition created successfully",
  "data": {
    "id": 1,
    "name": "Bhopal City",
    "slug": "bhopal-city",
    "city": "Bhopal",
    "state": "Madhya Pradesh",
    "language": "Hindi",
    "description": "Daily newspaper for Bhopal city",
    "editionType": "CITY",
    "active": true
  }
}
```

#### GET /api/v1/editions
**Public** - List all active editions

Response (200):
```json
{
  "status": 200,
  "message": "Editions retrieved successfully",
  "data": {
    "editions": [
      {
        "id": 1,
        "name": "Bhopal City",
        "slug": "bhopal-city",
        "city": "Bhopal",
        "state": "Madhya Pradesh",
        "language": "Hindi",
        "active": true
      }
    ],
    "count": 1
  }
}
```

#### GET /api/v1/editions/{id}
**Public** - Get edition by ID

#### GET /api/v1/editions/slug/{slug}
**Public** - Get edition by slug (e.g., `/editions/slug/bhopal-city`)

#### PUT /api/v1/editions/{id}
**Admin only** - Update an edition

#### DELETE /api/v1/editions/{id}
**Admin only** - Delete or soft-delete an edition (soft-delete if issues exist)

### Issue APIs (PDF Management)

#### POST /api/v1/issues
**Admin only** - Upload newspaper PDF

Parameters:
- `editionId` (required): Edition ID
- `publicationDate` (required): Date in `yyyy-MM-dd` format
- `file` (required): PDF file (max 50MB)

Request (multipart/form-data):
```bash
curl -X POST 'http://localhost:8080/api/v1/issues?editionId=1&publicationDate=2026-08-17' \
  -H 'Authorization: Bearer <token>' \
  -F 'file=@bhopal-city-2026-08-17.pdf'
```

Response (201):
```json
{
  "status": 201,
  "message": "PDF uploaded successfully to S3",
  "data": {
    "issueId": 123,
    "editionName": "Bhopal City",
    "publicationDate": "2026-08-17",
    "fileName": "bhopal-city-2026-08-17.pdf",
    "fileSize": 2048576,
    "storagePath": "newspaper/2026/08/17/bhopal-city/bhopal-city-2026-08-17.pdf",
    "uploadedAt": "2026-08-17T10:30:00Z",
    "message": "PDF uploaded successfully to S3 (12 pages)"
  }
}
```

Errors:
- **400**: Invalid file (not PDF, empty file)
- **404**: Edition not found
- **409**: Duplicate issue (edition + date already exists)

#### GET /api/v1/issues
**Public** - List issues with optional filters

Query parameters:
- `date` (optional): Specific date (`yyyy-MM-dd`)
- `from` (optional): Start date for range
- `to` (optional): End date for range
- `editionId` (optional): Filter by edition
- `page` (optional, default=1): Page number
- `size` (optional, default=20): Items per page (max 100)
- `sort` (optional, default=`publicationDate,desc`): Sort field and direction

Examples:
```bash
# Get issues for specific date
GET /api/v1/issues?date=2026-08-17

# Get issues for date range
GET /api/v1/issues?from=2026-08-01&to=2026-08-17

# Get issues for specific edition
GET /api/v1/issues?editionId=1

# Combine filters with pagination
GET /api/v1/issues?editionId=1&from=2026-08-01&to=2026-08-17&page=1&size=10
```

Response (200):
```json
{
  "status": 200,
  "message": "Issues retrieved successfully",
  "data": {
    "content": [
      {
        "id": 123,
        "edition": {
          "id": 1,
          "name": "Bhopal City",
          "slug": "bhopal-city"
        },
        "publicationDate": "2026-08-17",
        "fileName": "bhopal-city-2026-08-17.pdf",
        "fileSize": 2048576,
        "pageCount": 12
      }
    ],
    "pagination": {
      "page": 1,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1,
      "first": true,
      "last": true,
      "hasNext": false,
      "hasPrevious": false
    }
  }
}
```

#### GET /api/v1/issues/{id}
**Public** - Get issue metadata by ID (no PDF download)

#### GET /api/v1/issues/{id}/download-url
**Public** - Get presigned S3 download URL

Response (200):
```json
{
  "status": 200,
  "message": "Download URL generated successfully",
  "data": {
    "issueId": 123,
    "fileName": "bhopal-city-2026-08-17.pdf",
    "fileSize": 2048576,
    "pageCount": 12,
    "downloadUrl": "https://s3.amazonaws.com/e-newspaper-library-local/newspaper/2026/08/17/bhopal-city/bhopal-city-2026-08-17.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
    "expiresInMinutes": 10
  }
}
```

The presigned URL:
- Expires after 10 minutes
- Allows direct download from S3
- File downloads with original filename
- No backend bandwidth consumption

## Date and Range Search

The API uses a single flexible endpoint instead of separate endpoints for different time periods (3 days, 7 days, 30 days).

**Single date**:
```
GET /api/v1/issues?date=2026-08-17
```

**Date range**:
```
GET /api/v1/issues?from=2026-08-01&to=2026-08-17
```

**Validation**:
- Cannot specify both `date` and `from/to` simultaneously
- Both `from` and `to` must be provided together
- `from` cannot be after `to`
- Maximum range: **90 days** (configurable in application.yml)

The 90-day limit prevents unnecessarily large queries and protects API performance.

## Pagination

All list endpoints support 1-based pagination:

- `page` starts at **1**
- Default page size: **20**
- Maximum page size: **100**

Configuration (application.yml):
```yaml
newspaper:
  api:
    default-page-size: 20
    max-page-size: 100
```

Why pagination?
- Prevents very large responses
- Keeps response times predictable
- Allows the API to scale as newspaper archives grow
- Protects database from expensive unbounded queries

## Authentication and Authorization

### Mechanism
- JWT (JSON Web Token) authentication
- Tokens expire after **1 hour** (3600000ms)
- Stateless - no server-side sessions

### Access Model

| Role | Capabilities |
|------|-------------|
| **PUBLIC** | List editions, List issues, Get issue details, Download PDFs |
| **USER** | Same as PUBLIC |
| **ADMIN** | Everything PUBLIC can do + Upload PDFs, Create/Update/Delete editions, Promote users |

### How to Authenticate

1. Register or login to get JWT token
2. Include token in `Authorization` header:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. Token is validated on each request to protected endpoints

### Error Responses

**401 Unauthorized** - No token or invalid token:
```json
{
  "status": 401,
  "message": "Authentication token has expired",
  "data": null,
  "timestamp": "2026-08-18T10:30:00Z"
}
```

**403 Forbidden** - Valid token but insufficient permissions:
```json
{
  "status": 403,
  "message": "Access denied. You do not have permission to access this resource. Required role: ADMIN",
  "data": null,
  "timestamp": "2026-08-18T10:30:00Z"
}
```

## Validation

The API validates all inputs before processing:

| Validation | Enforcement |
|-----------|-------------|
| PDF file type | Must be `application/pdf` |
| File size | Maximum **50MB** |
| Edition required | Must reference existing edition ID |
| Publication date required | Must be valid `yyyy-MM-dd` format |
| Date cannot be after end date | Date range validation |
| Maximum date range | **90 days** |
| Pagination limits | Page >= 1, Size <= 100 |
| Duplicate prevention | See below |

File size limit prevents unexpectedly large uploads from consuming excessive application resources or S3 storage costs.

## Duplicate Prevention

**Critical business rule**: An edition cannot have multiple issues for the same publication date.

### Database Constraint
```sql
UNIQUE (edition_id, publication_date)
```

### Application Validation
Before uploading, the service checks:
```java
if (issueRepository.existsByEditionIdAndPublicationDate(editionId, publicationDate)) {
  throw DuplicateResourceException
}
```

### Response (409 CONFLICT)
```json
{
  "status": 409,
  "message": "Issue already exists for edition 'Bhopal City' on 2026-08-17",
  "data": null,
  "timestamp": "2026-08-18T10:30:00Z"
}
```

This prevents accidental duplicate uploads. If an issue needs to be replaced, the old one must be deleted first.

## Exception Handling

Global exception handling via `@RestControllerAdvice` ensures consistent error responses:

| HTTP Status | Error Type | Example |
|------------|------------|---------|
| 400 | Bad Request | Invalid date format, file validation failed |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Edition or issue doesn't exist |
| 409 | Conflict | Duplicate edition+date |
| 500 | Internal Server Error | Unexpected errors (with trace ID for debugging) |

Error response structure:
```json
{
  "status": 400,
  "message": "Invalid date format. Expected: yyyy-MM-dd",
  "data": null,
  "timestamp": "2026-08-18T10:30:00Z"
}
```

The API never exposes stack traces or internal implementation details to clients.

## AWS S3 Configuration

### Credentials

**IMPORTANT**: AWS credentials are **never hardcoded** in:
- application.yml
- Java source code
- Git repository

The application uses `DefaultCredentialsProvider` from AWS SDK v2, which resolves credentials from the standard AWS credential provider chain:

1. Environment variables (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
2. Java system properties
3. AWS credentials file (`~/.aws/credentials`)
4. AWS configuration file (`~/.aws/config`)
5. IAM role (when deployed on AWS infrastructure)

For local development, we use **AWS CLI named profiles**.

For production deployment, credentials should be supplied through:
- Secure secret management (AWS Secrets Manager, HashiCorp Vault)
- Environment variables
- IAM roles (if hosted on AWS EC2, ECS, Lambda, etc.)

### S3 Configuration

Configuration in application.yml:
```yaml
newspaper:
  storage:
    s3:
      bucket: ${AWS_S3_BUCKET:e-newspaper-library-local}
      region: ${AWS_REGION:us-east-1}
      max-file-size-mb: 50
      download-url-expiration-minutes: 10
      connection-timeout-ms: 5000
      api-call-timeout-ms: 30000
```

Environment variables override defaults:
- `AWS_S3_BUCKET` - S3 bucket name
- `AWS_REGION` - AWS region

## Local Setup

### Prerequisites

Required:
- **Java 21** (JDK 21.0.8 or higher)
- **Maven 3.6+** (or use included Maven Wrapper)
- **PostgreSQL 14+**
- **AWS CLI** (for credential configuration)
- **AWS account** with S3 access
- **Git**

### Install AWS CLI

**Windows**: Download the MSI installer from:
https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html

**Verify installation**:
```bash
aws --version
```

Expected output:
```
aws-cli/2.x.x Python/3.x.x Windows/...
```

### AWS Profile Configuration

1. **Get AWS credentials** from your AWS account (IAM user, not root)

2. **Configure AWS CLI named profile**:
```bash
aws configure --profile newspaper-library
```

When prompted, enter:
- AWS Access Key ID: `[your-access-key]`
- AWS Secret Access Key: `[your-secret-key]`
- Default region name: `us-east-1`
- Default output format: `json`

**NEVER put real credentials in code, README, or Git.**

3. **Verify authentication**:
```bash
aws sts get-caller-identity --profile newspaper-library
```

Expected output:
```json
{
    "UserId": "AIDAI...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/newspaper-user"
}
```

4. **Verify S3 bucket access**:
```bash
aws s3 ls s3://e-newspaper-library-local --profile newspaper-library
```

If the bucket doesn't exist, create it:
```bash
aws s3 mb s3://e-newspaper-library-local --profile newspaper-library
```

### Set AWS Profile for Application

**Windows PowerShell**:
```powershell
$env:AWS_PROFILE="newspaper-library"
```

**Verify**:
```powershell
echo $env:AWS_PROFILE
```

**Verify SDK can authenticate**:
```bash
aws sts get-caller-identity
```

This environment variable tells both AWS CLI and AWS SDK to use the `newspaper-library` profile for the current terminal session.

Using named profiles is useful when working with multiple AWS accounts or projects.

### AWS Credentials Location

AWS CLI stores credentials locally:

**Windows**:
- `%USERPROFILE%\.aws\credentials` - Access keys
- `%USERPROFILE%\.aws\config` - Configuration

**IMPORTANT**:
- Never commit these files to Git
- Never put access keys in application.yml
- Never put access keys in Java source code
- Never put secrets in README
- Never commit .env files containing secrets

AWS documentation: https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html

### PostgreSQL Setup

1. **Install PostgreSQL 14+**

2. **Create database**:
```sql
CREATE DATABASE newspaper_library;
```

3. **Configure database connection**

Set environment variables:
```bash
# Windows PowerShell
$env:DB_URL="jdbc:postgresql://localhost:5432/newspaper_library"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

Or update `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/newspaper_library
    username: postgres
    password: your_password
```

4. **Database migrations run automatically** via Liquibase on application startup

### Project Setup

1. **Clone repository**:
```bash
git clone <repository-url>
cd newspaper-digital-library
```

2. **Configure environment** (see PostgreSQL and AWS sections above)

3. **Build project**:
```bash
.\mvnw.cmd clean package
```

This compiles the application, runs tests, and creates the packaged JAR.

4. **Run application**:
```bash
.\mvnw.cmd spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/newspaper-digital-library-0.0.1-SNAPSHOT.jar
```

5. **Verify application is running**:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Swagger UI

Once the application is running, access interactive API documentation:

**Swagger UI**: http://localhost:8080/swagger-ui.html

**OpenAPI JSON**: http://localhost:8080/v3/api-docs

### How to Test From Swagger

**Step 1**: Open Swagger
```
http://localhost:8080/swagger-ui.html
```

**Step 2**: Register or Login
- Use `POST /api/v1/auth/register` to create an account
- Use `POST /api/v1/auth/login` with username/password
- Copy the `token` from the response

**Step 3**: Authorize
- Click the green "Authorize" button at the top
- Enter: `Bearer <your-token>`
- Click "Authorize"

**Step 4**: Create Edition (Admin only)
- Use `POST /api/v1/editions` with edition details
- Note the returned `id`

**Step 5**: Upload PDF (Admin only)
- Use `POST /api/v1/issues`
- Provide:
  - `editionId`: ID from step 4
  - `publicationDate`: Date in `yyyy-MM-dd` format
  - `file`: Select a PDF file

**Step 6**: List Issues (Public)
- Use `GET /api/v1/issues` with date or date range
- No authentication required

**Step 7**: Get Download URL (Public)
- Use `GET /api/v1/issues/{id}/download-url`
- Copy the `downloadUrl` from response
- Paste URL in browser to download PDF

## S3 Permissions

The AWS identity used by the application should follow the **principle of least privilege**.

Required S3 permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::e-newspaper-library-local"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::e-newspaper-library-local/*"
    }
  ]
}
```

**Do NOT use**:
- `AmazonS3FullAccess` policy
- `AdministratorAccess` policy
- AWS root account credentials

These are overly permissive for normal application operation.

## Security Practices

- Credentials are never stored in source code
- Credentials are never stored in application.yml
- Secrets must not be committed to Git
- S3 bucket is configured as private (not public)
- API validates all uploaded files (type, size)
- Admin-only operations require JWT authentication
- Public users can only access intended read-only APIs
- Database credentials are supplied through environment variables
- Passwords are stored as BCrypt hashes (never plaintext)
- JWT tokens expire after 1 hour
- Production should use IAM roles where deployment platform supports it

## Design Decisions and Assumptions

### PDF Storage: Amazon S3

**Reason**: PDFs are potentially large binary files. Object storage (S3) is better suited than PostgreSQL because:
- PDFs can be multiple megabytes
- Object storage is purpose-built for files
- Application instances don't need local file storage
- Storage can scale independently from application/database
- Cost-effective (S3 is cheaper than database storage for files)
- Enables CDN integration for high-traffic scenarios

### Metadata Storage: PostgreSQL

**Reason**: Metadata needs querying, filtering, and transactional consistency:
- Date-based queries (find issues on specific dates)
- Edition-based queries (find all issues for an edition)
- Pagination support
- Database constraints prevent duplicate records
- ACID guarantees for critical business rules

### Download Approach: Presigned URLs

**Reason**: Presigned S3 URLs provide better performance than streaming through the backend:
- Reduces backend bandwidth consumption
- Allows S3 to handle file transfer directly
- Better suited for large PDF downloads
- Client-to-S3 connection is faster than client-to-backend-to-S3
- Backend remains stateless

### Authentication: Simple JWT

**Reason**: The assignment requires simple admin login, not a complete identity management platform. JWT provides:
- Stateless authentication
- Industry-standard token format
- Easy to integrate with frontend applications
- Sufficient for the current requirements

### Date Range Limit: 90 Days

**Reason**: Protects the API from expensive unbounded queries:
- Prevents accidental queries spanning decades
- Keeps database query performance predictable
- Configurable if requirements change

### Pagination: 1-Based

**Reason**: More intuitive for users:
- Page 1 is the first page (not page 0)
- Matches user expectations
- Matches most pagination UI libraries

### editionId instead of editionName

**Reason**: IDs provide better performance and reliability:
- Primary key lookup is O(1) and indexed
- Edition names can change, IDs are immutable
- No case-sensitivity or whitespace issues
- Standard REST API practice

## What Is Intentionally Not Included

This project intentionally does not introduce unnecessary infrastructure:

- **Microservices** - A modular monolith is sufficient at current scale
- **Kafka/RabbitMQ** - No event-driven requirements
- **Redis** - PostgreSQL handles query performance adequately
- **Elasticsearch** - Date/edition queries work well with PostgreSQL indexes
- **Complex RBAC** - Simple ADMIN/USER roles are sufficient
- **Multiple backend services** - Keeps deployment and maintenance simple

**Reason**: The assignment is focused on a digital newspaper library. The current architecture can handle:
- Thousands of editions
- Decades of daily issues
- Hundreds of concurrent users
- Gigabytes of PDF storage

The system can evolve to microservices or add caching if traffic grows significantly. Starting simple leaves room for future scaling while avoiding premature complexity.

## Production Considerations

These are **future improvements**, not currently implemented:

- **Private S3 bucket** with IAM policies
- **HTTPS** with SSL certificates
- **Environment-specific configuration** (dev/staging/prod)
- **Secure secret management** (AWS Secrets Manager, HashiCorp Vault)
- **IAM roles** for EC2/ECS/Lambda deployment
- **Database backups** with point-in-time recovery
- **Monitoring and logging** (CloudWatch, Datadog, ELK)
- **Rate limiting** if public traffic grows
- **CDN** (CloudFront) if PDF download traffic becomes high
- **Object lifecycle policies** if old editions need archival to Glacier
- **Health checks** and auto-scaling
- **Database read replicas** for high read traffic

## Project Structure

```
src/main/java/com/newspaper/library/
├── config/                    # Spring configuration classes
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── S3Config.java
│   └── properties/
├── controller/                # REST controllers
│   ├── AuthController.java
│   ├── EditionController.java
│   └── IssueController.java
├── service/                   # Business logic interfaces
│   ├── AuthService.java
│   ├── EditionService.java
│   ├── IssueService.java
│   └── PdfStorageService.java
├── service/impl/              # Service implementations
│   ├── AuthServiceImpl.java
│   ├── EditionServiceImpl.java
│   ├── IssueServiceImpl.java
│   └── S3FileStorageServiceImpl.java
├── repository/                # Spring Data JPA repositories
│   ├── AdminUserRepository.java
│   ├── EditionRepository.java
│   └── IssueRepository.java
├── entity/                    # JPA entities
│   ├── AdminUser.java
│   ├── Edition.java
│   └── Issue.java
├── dto/                       # Data Transfer Objects
│   ├── auth/
│   ├── edition/
│   ├── issue/
│   ├── common/
│   └── storage/
├── security/                  # Security components
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAccessDeniedHandler.java
│   └── CustomUserDetailsService.java
├── exception/                 # Exception handling
│   ├── GlobalExceptionHandler.java
│   └── [custom exceptions]
├── mapper/                    # Entity to DTO mappers
│   ├── EditionMapper.java
│   └── IssueMapper.java
├── util/                      # Utility classes
│   ├── JwtUtil.java
│   └── S3StorageKeyBuilder.java
├── enums/                     # Enumerations
│   ├── Role.java
│   └── EditionType.java
└── NewspaperDigitalLibraryApplication.java
```

## Health Check

Spring Boot Actuator provides health endpoints:

```bash
GET /actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

This verifies the application is running and database connection is healthy.

## Quick Start

```bash
# Clone repository
git clone <repository-url>
cd newspaper-digital-library

# Configure AWS profile
aws configure --profile newspaper-library

# Select AWS profile
$env:AWS_PROFILE="newspaper-library"

# Verify AWS authentication
aws sts get-caller-identity

# Verify S3 bucket access
aws s3 ls s3://e-newspaper-library

# Set database credentials (optional if using defaults)
$env:DB_URL="jdbc:postgresql://localhost:5432/newspaper_library"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"

# Build application
.\mvnw.cmd clean package

# Run application
.\mvnw.cmd spring-boot:run
```

**Swagger UI**: http://localhost:8080/api/v1/swagger-ui/index.html#/

**Health Check**: http://localhost:8080/api/v1/actuator/health

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Web** - REST APIs
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication and authorization
- **Spring Validation** - Request validation
- **Spring Actuator** - Health monitoring
- **PostgreSQL** - Metadata storage
- **Liquibase** - Database migrations
- **AWS SDK v2 (2.20.26)** - S3 integration
- **Apache PDFBox 3.0.3** - PDF processing (page count extraction)
- **JWT (0.12.3)** - Authentication tokens
- **Springdoc OpenAPI (2.7.0)** - Swagger documentation
- **Lombok** - Boilerplate reduction
- **Testcontainers (1.19.3)** - Integration testing
- **Maven** - Build tool

## License

Apache 2.0

## Author

Developed for a newspaper digital library assignment with focus on production-grade architecture, scalability, and maintainability.
