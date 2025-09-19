# Retail File Service - Project Structure

## Overview
A Spring Boot microservice for retail file management with integrated monitoring using Prometheus and Grafana.

## Project Structure

```
├── src/main/java/com/avivse/retailfileservice/
│   ├── RetailFileServiceApplication.java     # Main Spring Boot application
│   ├── config/
│   │   ├── DatabaseHealthIndicator.java      # Custom health check for database
│   │   └── OpenApiConfig.java                # Swagger/OpenAPI configuration
│   ├── controller/
│   │   ├── RetailFileController.java         # REST API endpoints
│   │   └── GlobalExceptionHandler.java       # Centralized exception handling
│   ├── service/
│   │   └── RetailFileService.java            # Business logic layer
│   ├── repository/
│   │   └── RetailFileRepository.java         # Data access layer
│   ├── entity/
│   │   └── RetailFile.java                   # JPA entity
│   ├── dto/
│   │   ├── UpdateRetailFileRequest.java      # Request DTOs
│   │   └── ErrorResponse.java                # Response DTOs
│   └── exception/                            # Custom exceptions
├── src/main/resources/
│   └── application.yml                       # Application configuration
├── src/test/                                 # Test files
│   ├── controller/                           # Controller tests
│   ├── service/                              # Service tests
│   └── integration/                          # Integration tests
├── monitoring/
│   ├── prometheus.yml                        # Prometheus configuration
│   └── grafana/
│       ├── dashboards/
│       │   ├── dashboard.yml                 # Dashboard provisioning config
│       │   └── retail-file-service.json      # Grafana dashboard definition
│       └── datasources/
│           └── prometheus.yml                # Grafana datasource config
├── docker-compose.yml                        # Container orchestration
└── pom.xml                                   # Maven dependencies and build config
```

## Key Technologies
- **Framework**: Spring Boot 3.3.6
- **Build Tool**: Maven
- **Monitoring**: Prometheus + Grafana
- **API Documentation**: OpenAPI/Swagger
- **Testing**: JUnit (unit & integration tests)

## Services
- **Application**: Port 8080 (default Spring Boot)
- **Prometheus**: Port 9090
- **Grafana**: Port 3000

## Getting Started
```bash
# Start monitoring stack
docker-compose up -d

# Build and run application
mvn spring-boot:run
```

## Monitoring
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Metrics**: Custom retail file metrics + JVM/HTTP metrics