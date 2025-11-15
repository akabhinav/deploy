# Quick Start Guide - Windows with Docker Desktop

## Step-by-Step Setup

### 1. Prerequisites Check

Ensure Docker Desktop is running on Windows:
- Open Docker Desktop application
- Wait for "Docker Desktop is running" status
- Verify by opening PowerShell and running:
  ```powershell
  docker --version
  docker-compose --version
  ```

### 2. Clone and Navigate

```powershell
git clone <your-repo-url>
cd deploy
```

### 3. Start Everything

```powershell
docker-compose up -d
```

**What this does:**
- Builds your Java 21 Spring Boot application
- Starts PostgreSQL database
- Starts Redis cache
- Starts the Platform API
- Starts pgAdmin for database management

### 4. Monitor Startup

Watch the logs to see when the app is ready:

```powershell
docker-compose logs -f platform-api
```

**Look for this message:**
```
Started PlatformApplication in X.XXX seconds
```

Press `Ctrl+C` to stop following logs (containers keep running).

### 5. Verify Everything Works

Open a web browser:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

Or use PowerShell:
```powershell
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 6. Try the API

#### Create an Environment

```powershell
curl -X POST http://localhost:8080/api/v1/environments `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"development\",\"description\":\"Dev environment\",\"namespace\":\"dev\",\"autoDeployEnabled\":true}'
```

#### List Environments

```powershell
curl http://localhost:8080/api/v1/environments
```

#### Create an Application

```powershell
curl -X POST http://localhost:8080/api/v1/applications `
  -H "Content-Type: application/json" `
  -d '{\"name\":\"test-app\",\"description\":\"Test application\",\"applicationType\":\"WEB_SERVICE\",\"sourceType\":\"DOCKER_REGISTRY\",\"dockerImage\":\"nginx:latest\",\"port\":80,\"replicas\":1}'
```

## Common Commands

### View Running Containers
```powershell
docker-compose ps
```

### View Logs
```powershell
# All services
docker-compose logs

# Specific service
docker-compose logs platform-api
docker-compose logs postgres

# Follow logs (real-time)
docker-compose logs -f platform-api
```

### Restart a Service
```powershell
docker-compose restart platform-api
```

### Stop Everything
```powershell
docker-compose down
```

### Stop and Remove Data (Fresh Start)
```powershell
docker-compose down -v
```

### Rebuild After Code Changes
```powershell
docker-compose up -d --build
```

## Accessing Services

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | None |
| Swagger UI | http://localhost:8080/swagger-ui.html | None |
| Actuator | http://localhost:8080/actuator | None |
| pgAdmin | http://localhost:5050 | admin@platform.com / admin123 |
| PostgreSQL | localhost:5432 | platform / platform123 |
| Redis | localhost:6379 | None |

## Testing the Platform

### Using Swagger UI (Easiest)

1. Open http://localhost:8080/swagger-ui.html
2. Expand any endpoint (e.g., "Environments")
3. Click "Try it out"
4. Fill in the request body
5. Click "Execute"
6. See the response below

### Using PowerShell (Windows)

See examples above. Note the backtick (`) for line continuation in PowerShell.

### Using curl (Git Bash or WSL on Windows)

```bash
curl -X POST http://localhost:8080/api/v1/environments \
  -H "Content-Type: application/json" \
  -d '{"name":"production","description":"Production environment","namespace":"prod","autoDeployEnabled":false}'
```

## Troubleshooting

### "Port already in use"

If port 8080 is in use:
1. Find what's using it: `netstat -ano | findstr :8080`
2. Either stop that process or change the port in `docker-compose.yml`

### "Container exits immediately"

Check logs: `docker-compose logs platform-api`

Common issues:
- Database not ready: Wait 30 more seconds and check again
- Java compilation error: Check build logs

### "Cannot connect to Docker daemon"

- Ensure Docker Desktop is running
- Restart Docker Desktop
- In PowerShell (as Admin): `net stop com.docker.service` then `net start com.docker.service`

### Application won't start

1. Check if all containers are healthy:
   ```powershell
   docker-compose ps
   ```

2. Check database is ready:
   ```powershell
   docker-compose logs postgres | Select-String "ready"
   ```

3. Restart everything:
   ```powershell
   docker-compose down
   docker-compose up -d
   ```

## Next Steps

1. **Explore the API** using Swagger UI
2. **Create test data** using the endpoints
3. **View data** in pgAdmin (http://localhost:5050)
4. **Modify code** and rebuild with `docker-compose up -d --build`
5. **Read the full README.md** for detailed information

## Need Help?

- Check logs: `docker-compose logs -f`
- Verify health: http://localhost:8080/actuator/health
- Review README.md for detailed documentation
- Check Docker Desktop for container status
