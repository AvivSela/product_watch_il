# MinIO Storage Configuration

This directory contains configuration files for MinIO S3-compatible object storage.

## MinIO Setup

MinIO is configured in the docker-compose.yml file and provides:

- **API Endpoint**: http://localhost:9000
- **Web Console**: http://localhost:9001
- **Access Key**: minioadmin
- **Secret Key**: minioadmin123

## Usage

### Start MinIO
```bash
docker-compose up -d minio
```

### Access Web Console
Open http://localhost:9001 in your browser and login with:
- Username: minioadmin
- Password: minioadmin123

### Create Default Bucket
You can create buckets through the web console or via API:

```bash
# Using AWS CLI (after configuring credentials)
aws --endpoint-url http://localhost:9000 s3 mb s3://retail-files

# Using curl
curl -X PUT http://localhost:9000/retail-files \
  -H "Authorization: AWS minioadmin:minioadmin123"
```

### Integration with Services

Your Spring Boot services can integrate with MinIO using the AWS SDK:

1. Add AWS SDK dependency to pom.xml
2. Configure AWS properties in application.yml
3. Use S3 client to upload/download files

Example configuration:
```yaml
aws:
  s3:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin123
    bucket: retail-files
    region: us-east-1
```