# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice for retail file management, currently undergoing migration to a monorepo structure. The project uses Maven for build management and includes integrated monitoring with Prometheus and Grafana.

## Development Commands

### Build and Run
```bash
# Build the project
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
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RetailFileControllerTest

# Run integration tests
mvn test -Dtest=RetailFileIntegrationTest
```

### Monitoring Stack
```bash
# Start Prometheus and Grafana
docker-compose up -d

# Stop monitoring stack
docker-compose down
```

## Architecture

### Service Structure
- **Main Application**: `RetailFileServiceApplication.java` - Standard Spring Boot entry point
- **Controller Layer**: REST endpoints in `controller/` package
- **Service Layer**: Business logic in `service/` package
- **Repository Layer**: Data access via Spring Data JPA in `repository/`
- **Entity**: JPA entities in `entity/` package
- **DTOs**: Request/response objects in `dto/` package
- **Exception Handling**: Custom exceptions and global handler

### Key Technologies
- **Framework**: Spring Boot 3.3.6 with Java 17
- **Database**: H2 in-memory (development)
- **Monitoring**: Micrometer + Prometheus + Grafana
- **API Documentation**: SpringDoc OpenAPI
- **Testing**: JUnit with Spring Boot Test

### Configuration
- **Application**: `application.yml` contains all service configuration
- **Database**: H2 console available at `/h2-console` (development)
- **Metrics**: Available at `/actuator/prometheus`
- **Health**: Available at `/actuator/health`

## Monorepo Migration

This repository is being migrated to a monorepo structure as outlined in `MONOREPO_MIGRATION_PLAN.md`. The target structure will include:
- `services/` - Individual microservices
- `shared/` - Common libraries and DTOs
- `infrastructure/` - Monitoring, Docker, and deployment configs
- `tools/` - Build scripts and utilities

## Important Notes

### Ports
- **Application**: 8080 (Spring Boot default)
- **Prometheus**: 9090
- **Grafana**: 3000 (admin/admin)

### Package Structure
All code follows the base package `com.avivse.retailfileservice` with standard Spring Boot conventions.

### Actuator Endpoints
Management endpoints exposed: health, info, metrics, prometheus, env, loggers
Base path: `/actuator`