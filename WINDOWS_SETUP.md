# Windows Setup Guide

Complete guide for setting up and running the Platform on Windows with Docker Desktop.

## Prerequisites Installation

### 1. Install Docker Desktop for Windows

1. Download from: https://www.docker.com/products/docker-desktop/
2. Run the installer
3. Follow installation wizard
4. Restart your computer when prompted
5. Launch Docker Desktop
6. Wait for "Docker Desktop is running" in system tray

**Verify Installation:**
```powershell
docker --version
docker-compose --version
```

Expected output:
```
Docker version 24.x.x
docker-compose version 1.29.x (or 2.x.x)
```

### 2. Install Git for Windows (if not installed)

1. Download from: https://git-scm.com/download/win
2. Run installer with default options
3. Restart PowerShell

**Verify Installation:**
```powershell
git --version
```

### 3. Optional: Install Windows Terminal

Download from Microsoft Store for better terminal experience.

## Setup Steps

### Step 1: Clone the Repository

Open PowerShell:

```powershell
cd C:\Projects  # Or your preferred directory
git clone <repository-url>
cd deploy
```

### Step 2: Verify Docker Desktop is Running

In PowerShell:

```powershell
docker ps
```

If you see a table (even if empty), Docker is running.

If you see an error, start Docker Desktop from Start Menu.

### Step 3: Build and Start Services

```powershell
docker-compose up -d
```

**What happens:**
- Downloads required images (first time only)
- Builds the Java application
- Starts PostgreSQL, Redis, and the Platform API
- May take 5-10 minutes on first run

### Step 4: Monitor Progress

**Watch logs:**
```powershell
docker-compose logs -f platform-api
```

**Wait for:**
```
Started PlatformApplication in XX seconds
```

Press `Ctrl+C` to stop watching logs (services keep running).

### Step 5: Verify Services

**Check all containers are running:**
```powershell
docker-compose ps
```

You should see 4 services running (UP):
- platform-postgres
- platform-redis
- platform-api
- platform-pgadmin

**Test the API:**
```powershell
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

## Testing the Platform

### Method 1: PowerShell Script (Easiest)

We've included a test script:

```powershell
.\test-api.ps1
```

This will:
- Check health
- Create test environment
- Create test application
- Create test deployment
- Show you all the data

### Method 2: Swagger UI (Interactive)

1. Open browser: http://localhost:8080/swagger-ui.html
2. Expand any endpoint
3. Click "Try it out"
4. Fill in the example data
5. Click "Execute"
6. See the response

### Method 3: Manual PowerShell Commands

**Create an environment:**
```powershell
$body = @{
    name = "development"
    description = "Dev environment"
    namespace = "dev"
    autoDeployEnabled = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/environments" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

**List environments:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/environments" -Method GET
```

## Accessing Services

### Platform API
- **URL**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Health**: http://localhost:8080/actuator/health

### pgAdmin (Database UI)
- **URL**: http://localhost:5050
- **Email**: admin@platform.com
- **Password**: admin123

**Connect to PostgreSQL:**
1. Login to pgAdmin
2. Right-click "Servers" → "Register" → "Server"
3. General tab: Name = "Platform DB"
4. Connection tab:
   - Host: postgres
   - Port: 5432
   - Database: platformdb
   - Username: platform
   - Password: platform123
5. Click "Save"

### PostgreSQL (Direct)
- **Host**: localhost
- **Port**: 5432
- **Database**: platformdb
- **Username**: platform
- **Password**: platform123

### Redis
- **Host**: localhost
- **Port**: 6379

## Common Operations

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

### Restart Service
```powershell
docker-compose restart platform-api
```

### Stop Everything
```powershell
docker-compose down
```

### Stop and Delete All Data
```powershell
docker-compose down -v
```

### Rebuild After Code Changes
```powershell
docker-compose up -d --build
```

### View Container Status
```powershell
docker-compose ps
```

### Execute Command in Container
```powershell
# Access PostgreSQL
docker exec -it platform-postgres psql -U platform -d platformdb

# Access application shell
docker exec -it platform-api /bin/sh
```

## Troubleshooting

### Issue: Port 8080 Already in Use

**Find what's using it:**
```powershell
netstat -ano | findstr :8080
```

**Option 1:** Stop the other application

**Option 2:** Change port in docker-compose.yml:
```yaml
platform-api:
  ports:
    - "8081:8080"  # Change 8081 to any free port
```

Then use http://localhost:8081

### Issue: Docker Desktop Not Running

**Fix:**
1. Open Start Menu
2. Search "Docker Desktop"
3. Launch the application
4. Wait for "Docker Desktop is running"

### Issue: Container Exits Immediately

**Check logs:**
```powershell
docker-compose logs platform-api
```

**Common causes:**
- Database not ready (wait 30s more)
- Build error (check logs)
- Port conflict (see above)

**Fix:**
```powershell
docker-compose down
docker-compose up -d
```

### Issue: Database Connection Failed

**Verify PostgreSQL is healthy:**
```powershell
docker-compose ps postgres
```

Should show "Up" and "healthy"

**If not healthy:**
```powershell
docker-compose restart postgres
Start-Sleep -Seconds 10
docker-compose restart platform-api
```

### Issue: Slow Build

First build downloads ~500MB of dependencies.

**Speed up:**
- Ensure good internet connection
- Don't stop build once started
- Subsequent builds use cache and are much faster

### Issue: "No such file or directory"

Ensure you're in the project directory:
```powershell
cd C:\Path\To\deploy
pwd  # Should show: C:\Path\To\deploy
```

## Development Workflow

### Make Code Changes

1. Edit files in your IDE (VS Code, IntelliJ, etc.)
2. Rebuild and restart:
   ```powershell
   docker-compose up -d --build
   ```
3. Wait for rebuild (faster than first build)
4. Test your changes

### View Application Logs

```powershell
docker-compose logs -f platform-api
```

### Database Changes

1. Access pgAdmin: http://localhost:5050
2. Run queries
3. View tables and data

### Reset Everything

```powershell
# Stop and remove containers + volumes
docker-compose down -v

# Start fresh
docker-compose up -d
```

## Performance Tips

### WSL 2 Backend (Recommended)

Docker Desktop can use WSL 2 for better performance:

1. Open Docker Desktop
2. Settings → General
3. Enable "Use WSL 2 based engine"
4. Click "Apply & Restart"

### Resource Allocation

Increase if builds are slow:

1. Open Docker Desktop
2. Settings → Resources
3. Increase CPU and Memory
4. Click "Apply & Restart"

Recommended:
- CPUs: 4
- Memory: 4 GB

## Next Steps

1. ✅ Services running
2. ✅ Health check passing
3. ✅ Can access Swagger UI
4. ✅ Test script works

**Now try:**
- Explore the Swagger UI
- Create environments and applications
- View data in pgAdmin
- Read API examples in `api-examples.http`
- Modify code and rebuild

## Getting Help

### Check Status
```powershell
docker-compose ps
docker-compose logs
```

### Service URLs
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050

### Documentation
- README.md - Full documentation
- QUICKSTART.md - Quick reference
- api-examples.http - API examples

### Common Commands
```powershell
docker-compose up -d          # Start
docker-compose down           # Stop
docker-compose logs -f        # View logs
docker-compose ps             # Check status
docker-compose restart <svc>  # Restart service
```
