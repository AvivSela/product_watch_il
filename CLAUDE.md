# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **monorepo** for the Product Watch Platform containing multiple microservices. The project uses Maven for build management with a multi-module structure and includes integrated monitoring with Prometheus and Grafana.

## Monorepo Structure

```
product-watch-platform/
├── services/
│   └── retail-file-service/          # Spring Boot microservice for retail file management
├── infrastructure/
│   ├── monitoring/                   # Prometheus and Grafana configurations
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
# Navigate to service directory
cd services/retail-file-service

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
docker-compose up -d

# Stop monitoring stack
docker-compose down
```

## Architecture

### Current Services
- **retail-file-service**: Spring Boot microservice for managing retail file uploads

### Service Structure (retail-file-service)
- **Main Application**: `RetailFileServiceApplication.java` - Standard Spring Boot entry point
- **Controller Layer**: REST endpoints in `controller/` package
- **Service Layer**: Business logic in `service/` package
- **Repository Layer**: Data access via Spring Data JPA in `repository/`
- **Entity**: JPA entities in `entity/` package
- **DTOs**: Request/response objects in `dto/` package
- **Exception Handling**: Custom exceptions and global handler

### Key Technologies
- **Framework**: Spring Boot 3.3.6 with Java 17
- **Build Tool**: Maven (multi-module)
- **Database**: H2 in-memory (development)
- **Monitoring**: Micrometer + Prometheus + Grafana
- **API Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit with Spring Boot Test

### Configuration
- **Application**: `services/retail-file-service/src/main/resources/application.yml`
- **Database**: H2 console available at `/h2-console` (development)
- **Metrics**: Available at `/actuator/prometheus`
- **Health**: Available at `/actuator/health`
- **OpenAPI/Swagger**: Available at `/swagger-ui.html`
- **API Documentation**: `services/retail-file-service/API_DOCUMENTATION.md`

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
- **retail-file-service**: 8080
- **Prometheus**: 9090
- **Grafana**: 3000 (admin/admin)

### Package Structure
- **retail-file-service**: `com.avivse.retailfileservice` base package

### Actuator Endpoints
Management endpoints exposed: health, info, metrics, prometheus, env, loggers
Base path: `/actuator`

### CI/CD
- **GitHub Actions**: `.github/workflows/pr-tests.yml` - Runs tests on PR creation
- **Test Command**: `mvn test -f root-pom.xml` (runs all tests across modules)

### Docker Infrastructure
- **Compose File**: Uses `infrastructure/monitoring/` for Prometheus/Grafana configs
- **Services**: Prometheus (port 9090), Grafana (port 3000, admin/admin)
- **Network**: Uses host networking mode for development