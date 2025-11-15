# Porter-like Platform-as-a-Service (PaaS)

A developer-first Platform-as-a-Service built with Java 21 and Spring Boot that abstracts away infrastructure complexity, allowing developers to deploy applications with zero DevOps knowledge.

## ⭐ Core Features

- **Multi-Source Deployment**: Git, Docker Registry, Dockerfile, Buildpacks
- **Application Management**: Web Services, Workers, Cron Jobs, Static Sites
- **Environment Management**: Development, Staging, Production environments
- **Deployment Strategies**: Rolling, Blue-Green, Canary deployments
- **Database & Data Services**: PostgreSQL, Redis, and more
- **Monitoring & Observability**: Built-in metrics, logging, and health checks
- **REST API**: Complete platform control via RESTful API
- **OpenAPI Documentation**: Interactive API docs with Swagger UI

## 🚀 Advanced Features (NEW!)

This platform now includes enterprise-grade advanced features:

- **✅ Kubernetes Integration** - Real deployments to K8s clusters with HPA
- **✅ Docker Build Service** - Build images from Git repositories
- **✅ Multi-Tenancy & RBAC** - Organizations, projects, users with role-based access
- **✅ Auto-Scaling** - Horizontal Pod Autoscaling with CPU/Memory metrics
- **✅ Real-time Logging** - WebSocket-based log streaming
- **✅ Managed Databases** - PostgreSQL, MySQL, MongoDB, Redis provisioning
- **✅ Audit Logging** - Complete audit trail for compliance
- **✅ Notification System** - Slack, Email, Webhooks for alerts
- **✅ JWT Authentication** - Secure API access with token-based auth
- **✅ Prometheus & Grafana** - Advanced metrics and monitoring

**📖 See [ADVANCED_FEATURES.md](ADVANCED_FEATURES.md) for detailed documentation on all advanced features.**

## Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.2.0** - Modern Java framework
- **PostgreSQL 16** - Primary database
- **Redis 7** - Caching and queues
- **Docker** - Containerization
- **Maven** - Build tool
- **MapStruct** - Object mapping
- **SpringDoc OpenAPI** - API documentation

## Project Structure

```
.
├── platform-common/          # Common DTOs and utilities
│   └── src/main/java/com/paas/common/
│       ├── dto/             # Data Transfer Objects
│       └── enums/           # Enumerations
├── platform-core/           # Core business logic
│   └── src/main/java/com/paas/core/
│       ├── entity/          # JPA entities
│       ├── repository/      # Data repositories
│       ├── service/         # Business services
│       └── mapper/          # Entity-DTO mappers
├── platform-api/            # REST API layer
│   └── src/main/java/com/paas/api/
│       ├── controller/      # REST controllers
│       └── exception/       # Exception handlers
├── Dockerfile              # Multi-stage Docker build
└── docker-compose.yml      # Local development setup
```

## Prerequisites

- **Docker Desktop** for Windows (latest version)
- **Java 21** (optional, for local development without Docker)
- **Maven 3.9+** (optional, for local development without Docker)

## Quick Start with Docker Desktop

### 1. Clone the Repository

```bash
git clone <repository-url>
cd deploy
```

### 2. Start the Application

```bash
docker-compose up -d
```

This command will:
- Build the Spring Boot application
- Start PostgreSQL database
- Start Redis cache
- Start the Platform API
- Start pgAdmin (database management UI)

### 3. Wait for Services to Start

The application takes about 60-90 seconds to fully start. Monitor the logs:

```bash
docker-compose logs -f platform-api
```

Wait for the message: "Started PlatformApplication in X seconds"

### 4. Verify the Application

Check health status:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Access the Application

Once running, you can access:

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **pgAdmin**: http://localhost:5050 (admin@platform.com / admin123)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin / admin)

## API Endpoints

### Applications

- `GET /api/v1/applications` - Get all applications
- `GET /api/v1/applications/{id}` - Get application by ID
- `GET /api/v1/applications/name/{name}` - Get application by name
- `POST /api/v1/applications` - Create new application
- `PUT /api/v1/applications/{id}` - Update application
- `DELETE /api/v1/applications/{id}` - Delete application

### Deployments

- `GET /api/v1/deployments` - Get all deployments
- `GET /api/v1/deployments/{id}` - Get deployment by ID
- `GET /api/v1/deployments/application/{applicationId}` - Get deployments by application
- `POST /api/v1/deployments` - Create new deployment
- `PATCH /api/v1/deployments/{id}/status` - Update deployment status

### Environments

- `GET /api/v1/environments` - Get all environments
- `GET /api/v1/environments/{id}` - Get environment by ID
- `GET /api/v1/environments/name/{name}` - Get environment by name
- `POST /api/v1/environments` - Create new environment
- `PUT /api/v1/environments/{id}` - Update environment
- `DELETE /api/v1/environments/{id}` - Delete environment

## Example API Usage

### Create an Environment

```bash
curl -X POST http://localhost:8080/api/v1/environments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "development",
    "description": "Development environment",
    "namespace": "dev",
    "clusterId": "local-cluster",
    "autoDeployEnabled": true
  }'
```

### Create an Application

```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-web-app",
    "description": "My awesome web application",
    "applicationType": "WEB_SERVICE",
    "sourceType": "DOCKER_REGISTRY",
    "dockerImage": "nginx:latest",
    "port": 80,
    "replicas": 2,
    "resources": {
      "cpuRequest": "100m",
      "cpuLimit": "500m",
      "memoryRequest": "128Mi",
      "memoryLimit": "512Mi"
    },
    "healthCheck": {
      "path": "/health",
      "port": 80,
      "initialDelaySeconds": 30,
      "periodSeconds": 10
    }
  }'
```

### Create a Deployment

```bash
curl -X POST http://localhost:8080/api/v1/deployments \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 1,
    "version": "1.0.0",
    "deploymentStrategy": "ROLLING",
    "triggeredBy": "user@example.com"
  }'
```

## Docker Commands

### View Running Containers

```bash
docker-compose ps
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f platform-api
docker-compose logs -f postgres
```

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Slate)

```bash
docker-compose down -v
```

### Rebuild After Code Changes

```bash
docker-compose up -d --build
```

## Database Management

### Using pgAdmin

1. Open http://localhost:5050
2. Login with: admin@platform.com / admin123
3. Add new server:
   - Host: postgres
   - Port: 5432
   - Database: platformdb
   - Username: platform
   - Password: platform123

### Using psql (if PostgreSQL client installed)

```bash
docker exec -it platform-postgres psql -U platform -d platformdb
```

## Development

### Local Development Without Docker

If you prefer to run locally:

1. **Start PostgreSQL and Redis**:
```bash
docker-compose up -d postgres redis
```

2. **Build the project**:
```bash
mvn clean install
```

3. **Run the application**:
```bash
cd platform-api
mvn spring-boot:run
```

### Build Only

```bash
mvn clean package -DskipTests
```

### Run Tests

```bash
mvn test
```

## Configuration

Configuration is managed through environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| DB_HOST | localhost | PostgreSQL host |
| DB_PORT | 5432 | PostgreSQL port |
| DB_NAME | platformdb | Database name |
| DB_USERNAME | platform | Database username |
| DB_PASSWORD | platform123 | Database password |
| SPRING_PROFILES_ACTIVE | default | Spring profile |

## Troubleshooting

### Application Won't Start

1. Check if ports are available:
```bash
netstat -an | findstr "8080"
netstat -an | findstr "5432"
```

2. Check Docker Desktop is running

3. Check logs for errors:
```bash
docker-compose logs platform-api
```

### Database Connection Issues

1. Ensure PostgreSQL is healthy:
```bash
docker-compose ps postgres
```

2. Check PostgreSQL logs:
```bash
docker-compose logs postgres
```

### Build Fails

1. Clear Maven cache:
```bash
docker-compose down
docker-compose build --no-cache
```

2. Ensure you have enough disk space

### Port Already in Use

If ports are already in use, modify `docker-compose.yml`:

```yaml
services:
  platform-api:
    ports:
      - "8081:8080"  # Change 8081 to any available port
```

## Next Steps

This is an initial build with core functionality. Future enhancements:

- [ ] Kubernetes integration for actual deployments
- [ ] Docker build and deployment pipeline
- [ ] Git integration for source code deployment
- [ ] Buildpack support for auto-detection
- [ ] Monitoring dashboard
- [ ] User authentication and authorization
- [ ] Multi-tenancy support
- [ ] CLI tool for easier interaction
- [ ] WebSocket support for real-time logs
- [ ] Database backup and restore
- [ ] Auto-scaling configuration

## Contributing

Contributions are welcome! Please submit pull requests or open issues.

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please open a GitHub issue.
