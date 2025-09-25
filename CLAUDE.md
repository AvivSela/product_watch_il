# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **monorepo** for the Product Watch Platform containing multiple microservices. The project uses Maven for build management with a multi-module structure and includes integrated monitoring with Prometheus and Grafana.

## Monorepo Structure

```
product-watch-platform/
├── services/
│   ├── retail-file-service/          # Spring Boot microservice for retail file management
│   └── store-service/                # Spring Boot microservice for store management
├── infrastructure/
│   ├── monitoring/                   # Prometheus and Grafana configurations
│   ├── storage/                      # MinIO S3 storage configurations
│   └── docker/                       # Docker configurations
├── docs/                             # Project documentation
├── root-pom.xml                     # Root parent POM for all modules
└── docker-compose.yml               # Local development stack
```

## Development Commands

### Root Level (All Services)
```bash
# Build all modules
mvn clean compile -f root-pom.xml

# Run tests for all modules
mvn test -f root-pom.xml

# Package all modules
mvn clean package -f root-pom.xml
```

### Individual Service
```bash
# Navigate to service directory (example with retail-file-service)
cd services/retail-file-service
# OR
cd services/store-service

# Build the service
mvn clean compile

# Run tests
mvn test

# Run the application
mvn spring-boot:run

# Build JAR
mvn clean package
```

### Testing
```bash
# Run all tests (from service directory)
mvn test

# Run specific test class
mvn test -Dtest=RetailFileControllerTest

# Run integration tests
mvn test -Dtest=RetailFileIntegrationTest
```

### Monitoring Stack
```bash
# Start Prometheus and Grafana (from root)
docker-compose up -d prometheus grafana

# Stop monitoring stack
docker-compose down prometheus grafana
```

### Storage Stack (MinIO S3)
```bash
# Start MinIO S3-compatible storage
docker-compose up -d minio

# Stop MinIO
docker-compose down minio

# Start all infrastructure services
docker-compose up -d
```

## Architecture

### Current Services
- **retail-file-service**: Spring Boot microservice for managing retail file uploads
- **store-service**: Spring Boot microservice for managing store information

### Service Structure (Common Pattern)
Both services follow the standard Spring Boot layered architecture:
- **Main Application**: `*ServiceApplication.java` - Standard Spring Boot entry point
- **Controller Layer**: REST endpoints in `controller/` package
- **Service Layer**: Business logic in `service/` package
- **Repository Layer**: Data access via Spring Data JPA in `repository/`
- **Entity**: JPA entities in `entity/` package
- **DTOs**: Request/response objects in `dto/` package
- **Exception Handling**: Custom exceptions and global handler
- **Configuration**: OpenAPI config in `config/` package

### Key Technologies
- **Framework**: Spring Boot 3.3.6 with Java 17
- **Build Tool**: Maven (multi-module)
- **Database**: H2 in-memory (development)
- **Storage**: MinIO S3-compatible object storage (development)
- **Monitoring**: Micrometer + Prometheus + Grafana
- **API Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit with Spring Boot Test

### Configuration
- **Application Config**: Each service has `src/main/resources/application.yml`
- **Database**: H2 console available at `/h2-console` (development)
- **Metrics**: Available at `/actuator/prometheus`
- **Health**: Available at `/actuator/health`
- **OpenAPI/Swagger**: Available at service-specific paths:
  - retail-file-service: `/swagger-ui.html`
  - store-service: `/docs`
- **API Documentation**: Each service may have its own API documentation

## Maven Multi-Module Configuration

The project uses a parent POM (`root-pom.xml`) that manages:
- Dependency versions across all modules
- Plugin configurations
- Build profiles
- Module declarations

All services inherit from this parent POM for consistent configuration.

## Adding New Services

When adding new microservices:
1. Create directory under `services/`
2. Add module to `root-pom.xml`
3. Inherit from parent POM in service POM
4. Follow established package structure

## Important Notes

### Ports
- **retail-file-service**: 8001
- **store-service**: 8000
- **Prometheus**: 9090 (via docker-compose)
- **Grafana**: 3000 (admin/admin)
- **MinIO API**: 9000 (minioadmin/minioadmin123)
- **MinIO Console**: 9001 (minioadmin/minioadmin123)

### Package Structure
- **retail-file-service**: `com.avivse.retailfileservice` base package
- **store-service**: `com.avivse.storeservice` base package

### Actuator Endpoints
Management endpoints exposed: health, info, metrics, prometheus, env, loggers
Base path: `/actuator`

### CI/CD
- **GitHub Actions**: `.github/workflows/pr-tests.yml` - Runs tests on PR creation
- **Test Command**: `mvn test -f root-pom.xml` (runs all tests across modules)

### Docker Infrastructure
- **Compose File**: Uses `infrastructure/monitoring/` for Prometheus/Grafana configs
- **Storage**: Uses `infrastructure/storage/` for MinIO S3 configs
- **Services**:
  - Prometheus (port 9090)
  - Grafana (port 3000, admin/admin)
  - MinIO API (port 9000, minioadmin/minioadmin123)
  - MinIO Console (port 9001, minioadmin/minioadmin123)
- **Network**: Host networking for monitoring, port mapping for MinIO
- **Volumes**: Persistent data for Grafana and MinIO

### File Storage
- **MinIO S3**: S3-compatible object storage for file uploads/downloads
- **Default Bucket**: Create `retail-files` bucket for retail file service
- **Integration**: Use AWS SDK with MinIO endpoint configuration
- **Web Console**: Access MinIO web interface at http://localhost:9001