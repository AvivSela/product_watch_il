# Retail File Service API Documentation

## Overview

The Retail File Service is a Spring Boot microservice designed to manage retail file uploads and their metadata. It provides REST endpoints for creating, reading, updating, and deleting retail file records.

**Base URL:** `http://localhost:8080`
**API Version:** v1
**Service Port:** 8080

## Authentication

Currently, the service runs without authentication for development purposes.

## API Endpoints

### 1. Create Retail File

Creates a new retail file record in the system.

**Endpoint:** `POST /v1/retail-files`

**Request Body:**
```json
{
  "chain_id": "string",       // Required, max 100 chars
  "store_id": 123,           // Optional
  "file_name": "string",      // Required, max 255 chars
  "file_url": "string",       // Required, max 500 chars
  "file_size": 1024,         // Optional, in bytes
  "upload_date": "2024-01-15T10:30:00", // Optional, defaults to now
  "is_processed": false      // Optional, defaults to false
}
```

**Response:**
- **201 Created** - File record created successfully
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:30:00",
  "chain_id": "CHAIN001",
  "store_id": 123,
  "file_name": "sales_data.csv",
  "file_url": "https://storage.example.com/files/sales_data.csv",
  "file_size": 1024,
  "upload_date": "2024-01-15T10:30:00",
  "is_processed": false
}
```

- **400 Bad Request** - Invalid input data
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "chain_id": "Chain ID is required",
    "file_name": "File name is required"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. Get Retail File by ID

Retrieves a specific retail file record by its ID.

**Endpoint:** `GET /v1/retail-files/{id}`

**Path Parameters:**
- `id` (UUID, required) - The unique identifier of the retail file

**Response:**
- **200 OK** - File found
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:30:00",
  "chain_id": "CHAIN001",
  "store_id": 123,
  "file_name": "sales_data.csv",
  "file_url": "https://storage.example.com/files/sales_data.csv",
  "file_size": 1024,
  "upload_date": "2024-01-15T10:30:00",
  "is_processed": false
}
```

- **404 Not Found** - File not found
```json
{
  "code": "RETAIL_FILE_NOT_FOUND",
  "message": "Retail file not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. List Retail Files

Retrieves a paginated list of retail files with optional filtering.

**Endpoint:** `GET /v1/retail-files`

**Query Parameters:**
- `chain_id` (string, optional) - Filter by chain ID
- `store_id` (integer, optional) - Filter by store ID
- `is_processed` (boolean, optional) - Filter by processing status
- `page` (integer, optional, default: 1) - Page number (minimum: 1)
- `limit` (integer, optional, default: 20) - Items per page (minimum: 1, maximum: 100)

**Response:**
- **200 OK** - List retrieved successfully
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "created_at": "2024-01-15T10:30:00",
      "updated_at": "2024-01-15T10:30:00",
      "chain_id": "CHAIN001",
      "store_id": 123,
      "file_name": "sales_data.csv",
      "file_url": "https://storage.example.com/files/sales_data.csv",
      "file_size": 1024,
      "upload_date": "2024-01-15T10:30:00",
      "is_processed": false
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 50,
    "pages": 3
  }
}
```

### 4. Update Retail File

Updates an existing retail file record.

**Endpoint:** `PUT /v1/retail-files/{id}`

**Path Parameters:**
- `id` (UUID, required) - The unique identifier of the retail file

**Request Body:**
```json
{
  "chain_id": "string",       // Optional, max 100 chars
  "store_id": 123,           // Optional
  "file_name": "string",      // Optional, max 255 chars
  "file_url": "string",       // Optional, max 500 chars
  "file_size": 1024,         // Optional, in bytes
  "upload_date": "2024-01-15T10:30:00", // Optional
  "is_processed": true       // Optional
}
```

**Response:**
- **200 OK** - File updated successfully
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T11:00:00",
  "chain_id": "CHAIN001",
  "store_id": 123,
  "file_name": "updated_sales_data.csv",
  "file_url": "https://storage.example.com/files/updated_sales_data.csv",
  "file_size": 2048,
  "upload_date": "2024-01-15T10:30:00",
  "is_processed": true
}
```

- **404 Not Found** - File not found
```json
{
  "code": "RETAIL_FILE_NOT_FOUND",
  "message": "Retail file not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00"
}
```

- **400 Bad Request** - Invalid input data
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "file_name": "File name cannot exceed 255 characters"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 5. Delete Retail File

Deletes a retail file record from the system.

**Endpoint:** `DELETE /v1/retail-files/{id}`

**Path Parameters:**
- `id` (UUID, required) - The unique identifier of the retail file

**Response:**
- **204 No Content** - File deleted successfully
- **404 Not Found** - File not found
```json
{
  "code": "RETAIL_FILE_NOT_FOUND",
  "message": "Retail file not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 6. Mark File as Processed

Marks a retail file as processed.

**Endpoint:** `PATCH /v1/retail-files/{id}/process`

**Path Parameters:**
- `id` (UUID, required) - The unique identifier of the retail file

**Response:**
- **200 OK** - File marked as processed successfully
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T11:15:00",
  "chain_id": "CHAIN001",
  "store_id": 123,
  "file_name": "sales_data.csv",
  "file_url": "https://storage.example.com/files/sales_data.csv",
  "file_size": 1024,
  "upload_date": "2024-01-15T10:30:00",
  "is_processed": true
}
```

- **404 Not Found** - File not found
```json
{
  "code": "RETAIL_FILE_NOT_FOUND",
  "message": "Retail file not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Data Models

### RetailFile Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `id` | UUID | Unique identifier | Auto-generated |
| `created_at` | LocalDateTime | Creation timestamp | Auto-generated |
| `updated_at` | LocalDateTime | Last update timestamp | Auto-updated |
| `chain_id` | String | Chain identifier | Required, max 100 chars |
| `store_id` | Integer | Store identifier | Optional |
| `file_name` | String | Original file name | Required, max 255 chars |
| `file_url` | String | URL where file is stored | Required, max 500 chars |
| `file_size` | Long | File size in bytes | Optional |
| `upload_date` | LocalDateTime | When file was uploaded | Required |
| `is_processed` | Boolean | Processing status | Required, defaults to false |

### CreateRetailFileRequest DTO

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `chain_id` | String | Chain identifier | Required, max 100 chars |
| `store_id` | Integer | Store identifier | Optional |
| `file_name` | String | Original file name | Required, max 255 chars |
| `file_url` | String | URL where file is stored | Required, max 500 chars |
| `file_size` | Long | File size in bytes | Optional |
| `upload_date` | LocalDateTime | When file was uploaded | Optional, defaults to now |
| `is_processed` | Boolean | Processing status | Optional, defaults to false |

### UpdateRetailFileRequest DTO

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `chain_id` | String | Chain identifier | Optional, max 100 chars |
| `store_id` | Integer | Store identifier | Optional |
| `file_name` | String | Original file name | Optional, max 255 chars |
| `file_url` | String | URL where file is stored | Optional, max 500 chars |
| `file_size` | Long | File size in bytes | Optional |
| `upload_date` | LocalDateTime | When file was uploaded | Optional |
| `is_processed` | Boolean | Processing status | Optional |

### ErrorResponse DTO

| Field | Type | Description |
|-------|------|-------------|
| `code` | String | Error code |
| `message` | String | Error message |
| `details` | Map<String, String> | Additional error details |
| `timestamp` | LocalDateTime | When error occurred |

## Error Codes

| Code | Description |
|------|-------------|
| `RETAIL_FILE_NOT_FOUND` | Requested retail file was not found |
| `VALIDATION_ERROR` | Request validation failed |
| `INTERNAL_SERVER_ERROR` | Internal server error occurred |

## Example Usage

### Create a new retail file
```bash
curl -X POST http://localhost:8080/v1/retail-files \
  -H "Content-Type: application/json" \
  -d '{
    "chain_id": "CHAIN001",
    "store_id": 123,
    "file_name": "sales_data.csv",
    "file_url": "https://storage.example.com/files/sales_data.csv",
    "file_size": 1024
  }'
```

### Get all files for a specific chain
```bash
curl "http://localhost:8080/v1/retail-files?chain_id=CHAIN001&page=1&limit=10"
```

### Mark a file as processed
```bash
curl -X PATCH http://localhost:8080/v1/retail-files/550e8400-e29b-41d4-a716-446655440000/process
```

### Update a file
```bash
curl -X PUT http://localhost:8080/v1/retail-files/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "file_name": "updated_sales_data.csv",
    "is_processed": true
  }'
```

## Monitoring Endpoints

The service exposes several monitoring endpoints via Spring Boot Actuator:

- **Health Check:** `GET /actuator/health`
- **Metrics:** `GET /actuator/metrics`
- **Prometheus Metrics:** `GET /actuator/prometheus`
- **Application Info:** `GET /actuator/info`
- **Environment Info:** `GET /actuator/env`
- **Loggers:** `GET /actuator/loggers`

## Database

**Development Environment:**
- **Database:** H2 in-memory database
- **Console Access:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:retailfiledb`
- **Username:** `sa`
- **Password:** (empty)

## OpenAPI/Swagger Documentation

The service includes SpringDoc OpenAPI integration. When the service is running, you can access:

- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Swagger UI:** http://localhost:8080/swagger-ui.html

## CORS Configuration

The service is configured with CORS enabled for all origins (`*`) for development purposes.

## Notes

- All timestamps are in ISO 8601 format
- The service uses snake_case for JSON property naming (configured via Jackson SNAKE_CASE)
- File size is measured in bytes
- UUIDs are version 4 (random)
- Pagination starts from page 1
- Maximum page size is limited to 100 items