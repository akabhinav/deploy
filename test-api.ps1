# PowerShell script to test the Platform API
# Run this on Windows after starting docker-compose

$baseUrl = "http://localhost:8080"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "Platform API Test Script" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# Function to make HTTP requests
function Invoke-ApiRequest {
    param (
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null
    )

    $url = "$baseUrl$Endpoint"
    $headers = @{
        "Content-Type" = "application/json"
    }

    try {
        if ($Body) {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $headers -Body $Body
        } else {
            $response = Invoke-RestMethod -Uri $url -Method $Method -Headers $headers
        }
        return $response
    } catch {
        Write-Host "Error: $_" -ForegroundColor Red
        return $null
    }
}

# Test 1: Health Check
Write-Host "1. Testing Health Check..." -ForegroundColor Yellow
$health = Invoke-ApiRequest -Method "GET" -Endpoint "/actuator/health"
if ($health.status -eq "UP") {
    Write-Host "   ✓ Application is healthy!" -ForegroundColor Green
} else {
    Write-Host "   ✗ Application is not healthy!" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Create Environment
Write-Host "2. Creating Development Environment..." -ForegroundColor Yellow
$envBody = @{
    name = "development"
    description = "Development environment"
    namespace = "dev"
    clusterId = "local-k8s"
    autoDeployEnabled = $true
    environmentVariables = @{
        LOG_LEVEL = "DEBUG"
        DEBUG = "true"
    }
} | ConvertTo-Json

$env = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/environments" -Body $envBody
if ($env) {
    Write-Host "   ✓ Environment created with ID: $($env.id)" -ForegroundColor Green
    $envId = $env.id
} else {
    Write-Host "   ! Environment may already exist, continuing..." -ForegroundColor Yellow
    $envId = 1
}
Write-Host ""

# Test 3: List Environments
Write-Host "3. Listing All Environments..." -ForegroundColor Yellow
$environments = Invoke-ApiRequest -Method "GET" -Endpoint "/api/v1/environments"
if ($environments) {
    Write-Host "   ✓ Found $($environments.Count) environment(s)" -ForegroundColor Green
    foreach ($e in $environments) {
        Write-Host "     - $($e.name) (Namespace: $($e.namespace))" -ForegroundColor Cyan
    }
}
Write-Host ""

# Test 4: Create Application
Write-Host "4. Creating Web Application..." -ForegroundColor Yellow
$appBody = @{
    name = "test-web-app"
    description = "Test web application"
    applicationType = "WEB_SERVICE"
    sourceType = "DOCKER_REGISTRY"
    dockerImage = "nginx:latest"
    port = 80
    replicas = 1
    environmentId = "$envId"
    resources = @{
        cpuRequest = "100m"
        cpuLimit = "500m"
        memoryRequest = "128Mi"
        memoryLimit = "512Mi"
    }
    healthCheck = @{
        path = "/health"
        port = 80
        initialDelaySeconds = 30
        periodSeconds = 10
    }
} | ConvertTo-Json -Depth 10

$app = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/applications" -Body $appBody
if ($app) {
    Write-Host "   ✓ Application created with ID: $($app.id)" -ForegroundColor Green
    $appId = $app.id
} else {
    Write-Host "   ! Application may already exist, continuing..." -ForegroundColor Yellow
    $appId = 1
}
Write-Host ""

# Test 5: List Applications
Write-Host "5. Listing All Applications..." -ForegroundColor Yellow
$applications = Invoke-ApiRequest -Method "GET" -Endpoint "/api/v1/applications"
if ($applications) {
    Write-Host "   ✓ Found $($applications.Count) application(s)" -ForegroundColor Green
    foreach ($a in $applications) {
        Write-Host "     - $($a.name) (Type: $($a.applicationType), Status: $($a.status))" -ForegroundColor Cyan
    }
}
Write-Host ""

# Test 6: Create Deployment
Write-Host "6. Creating Deployment..." -ForegroundColor Yellow
$deployBody = @{
    applicationId = $appId
    version = "1.0.0"
    gitCommitSha = "abc123"
    deploymentStrategy = "ROLLING"
    triggeredBy = "test-script"
    metadata = @{
        source = "PowerShell Test Script"
        timestamp = (Get-Date).ToString()
    }
} | ConvertTo-Json -Depth 10

$deployment = Invoke-ApiRequest -Method "POST" -Endpoint "/api/v1/deployments" -Body $deployBody
if ($deployment) {
    Write-Host "   ✓ Deployment created with ID: $($deployment.id)" -ForegroundColor Green
    $deployId = $deployment.id
}
Write-Host ""

# Test 7: Update Deployment Status
if ($deployId) {
    Write-Host "7. Updating Deployment Status..." -ForegroundColor Yellow
    $updateDeploy = Invoke-ApiRequest -Method "PATCH" -Endpoint "/api/v1/deployments/$deployId/status?status=SUCCESS"
    if ($updateDeploy -and $updateDeploy.status -eq "SUCCESS") {
        Write-Host "   ✓ Deployment status updated to SUCCESS" -ForegroundColor Green
    }
    Write-Host ""
}

# Test 8: List Deployments
Write-Host "8. Listing All Deployments..." -ForegroundColor Yellow
$deployments = Invoke-ApiRequest -Method "GET" -Endpoint "/api/v1/deployments"
if ($deployments) {
    Write-Host "   ✓ Found $($deployments.Count) deployment(s)" -ForegroundColor Green
    foreach ($d in $deployments) {
        Write-Host "     - Deployment #$($d.id) for App #$($d.applicationId) - Status: $($d.status)" -ForegroundColor Cyan
    }
}
Write-Host ""

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "All Tests Completed!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "You can now:" -ForegroundColor Yellow
Write-Host "  - Open Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  - View API Docs: http://localhost:8080/api-docs" -ForegroundColor Cyan
Write-Host "  - Access pgAdmin: http://localhost:5050" -ForegroundColor Cyan
Write-Host ""
