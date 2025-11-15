# Advanced Features Documentation

This document describes the advanced features implemented in the Porter-like PaaS platform, making it a production-ready, enterprise-grade Platform-as-a-Service.

## 🚀 Overview of Advanced Features

### ✅ Implemented Core Advanced Features

1. **Kubernetes Integration** - Deploy and manage apps on real K8s clusters
2. **Docker Build Service** - Build images from Git repositories
3. **Multi-Tenancy** - Organizations, Projects, and Users with RBAC
4. **Auto-Scaling** - Horizontal Pod Autoscaling (HPA)
5. **Real-time Logging** - WebSocket-based log streaming
6. **Database Provisioning** - Managed databases as a service
7. **Audit Logging** - Complete audit trail of all operations
8. **Notification System** - Slack, Email, Webhooks
9. **JWT Authentication** - Secure API access with role-based permissions
10. **Prometheus Metrics** - Advanced monitoring and metrics

---

## 1. Kubernetes Integration

### Features
- **Real Deployment to K8s**: Actually deploys applications to Kubernetes clusters
- **Namespace Management**: Automatic namespace creation and isolation
- **Service Creation**: Automatic Kubernetes service generation
- **Resource Management**: CPU/Memory requests and limits
- **Health Probes**: Liveness and readiness probes
- **Rolling Updates**: Zero-downtime deployments

### Service: `KubernetesDeploymentService`

```java
// Deploy application to Kubernetes
kubernetesDeploymentService.deployToKubernetes(applicationId, "production");

// Scale application
kubernetesDeploymentService.scaleApplication(applicationId, "production", 5);

// Get deployment status
Map<String, Object> status = kubernetesDeploymentService.getDeploymentStatus(applicationId, "production");

// Delete from Kubernetes
kubernetesDeploymentService.deleteFromKubernetes(applicationId, "production");
```

### Key Capabilities

**Deployment Creation:**
- Generates Kubernetes Deployment manifests
- Configures container specs with environment variables
- Sets up resource requests and limits
- Adds health check probes

**Service Discovery:**
- Creates ClusterIP services automatically
- Configures port mappings
- Enables service-to-service communication

**Auto-Scaling:**
- Integrates with Horizontal Pod Autoscaler (HPA)
- CPU/Memory-based scaling
- Custom metrics support

---

## 2. Docker Build Service

### Features
- **Git Integration**: Clone repositories and build from source
- **Multi-stage Builds**: Optimized Docker image builds
- **Build Logs**: Real-time build log streaming
- **Build History**: Track all builds with metadata
- **Async Building**: Non-blocking build process

### Service: `DockerBuildService`

```java
// Build from Git repository
BuildJob job = dockerBuildService.buildFromGit(applicationId, commitSha);

// Get build status
BuildJob status = dockerBuildService.getBuildJob(jobId);

// Get build history
List<BuildJob> builds = dockerBuildService.getBuildsByApplication(applicationId);
```

### Build Workflow

1. **Clone Repository**: Git clone with specific branch/commit
2. **Build Image**: Docker build with proper tagging
3. **Log Streaming**: Real-time build logs via WebSocket
4. **Tag and Push**: Image tagging and registry push
5. **Cleanup**: Temporary directory cleanup

### BuildJob Entity

```java
@Entity
public class BuildJob {
    private Long id;
    private Long applicationId;
    private String gitCommitSha;
    private String dockerImageTag;
    private BuildStatus status;  // PENDING, BUILDING, SUCCESS, FAILED
    private String buildLog;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationSeconds;
}
```

---

## 3. Multi-Tenancy & RBAC

### Architecture

**Hierarchy:**
```
Organization (Company)
  ├── Projects (Teams/Apps)
  │   ├── Applications
  │   ├── Environments
  │   └── Databases
  └── Members (Users with Roles)
```

### Entities

#### Organization
```java
@Entity
public class Organization {
    private String name;
    private String slug;
    private Integer maxProjects;
    private Integer maxApplications;
    private Set<Project> projects;
    private Set<OrganizationMember> members;
}
```

#### User
```java
@Entity
public class User {
    private String username;
    private String email;
    private String passwordHash;
    private Boolean active;
    private Set<OrganizationMember> organizationMemberships;
}
```

#### Role-Based Access Control

```java
public enum Role {
    OWNER,      // Full access to organization
    ADMIN,      // Can manage most settings
    DEVELOPER,  // Can deploy and manage applications
    VIEWER      // Read-only access
}
```

### Usage

```java
// Create organization
Organization org = organizationService.create("Acme Corp", "acme");

// Add user to organization
organizationService.addMember(orgId, userId, Role.DEVELOPER);

// Check permissions
@PreAuthorize("hasRole('ADMIN')")
public void deleteApplication(Long id) { ... }
```

---

## 4. Auto-Scaling Configuration

### Features
- **Horizontal Pod Autoscaling**: Automatic replica scaling
- **CPU-based Scaling**: Scale based on CPU utilization
- **Memory-based Scaling**: Scale based on memory usage
- **Custom Metrics**: Support for custom application metrics
- **Cooldown Periods**: Prevent rapid scaling oscillations

### AutoScalingConfig Entity

```java
@Entity
public class AutoScalingConfig {
    private Long applicationId;
    private Boolean enabled;
    private Integer minReplicas;    // Minimum 1
    private Integer maxReplicas;    // Maximum 10
    private Integer targetCpuUtilization;      // Target 80%
    private Integer targetMemoryUtilization;   // Target 80%
    private Integer scaleUpCooldownSeconds;    // 60s
    private Integer scaleDownCooldownSeconds;  // 300s
}
```

### Example Configuration

```json
{
  "applicationId": 1,
  "enabled": true,
  "minReplicas": 2,
  "maxReplicas": 20,
  "targetCpuUtilization": 75,
  "targetMemoryUtilization": 80,
  "scaleUpCooldownSeconds": 60,
  "scaleDownCooldownSeconds": 300
}
```

### Integration with Kubernetes

The platform automatically creates HPA resources in Kubernetes:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: my-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 75
```

---

## 5. Real-time Logging with WebSockets

### Features
- **Live Log Streaming**: Real-time application logs
- **Build Logs**: Stream build progress in real-time
- **Deployment Logs**: Live deployment status updates
- **SockJS Support**: Fallback for older browsers

### WebSocket Endpoints

```
/ws/logs          - Application logs
/ws/builds        - Build logs
/ws/deployments   - Deployment updates
```

### Client Usage

```javascript
// Connect to application logs
const socket = new SockJS('/ws/logs');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // Subscribe to specific application logs
    stompClient.subscribe('/topic/logs/app/123', function(message) {
        console.log('Log:', message.body);
    });
});

// Subscribe to build logs
stompClient.subscribe('/topic/builds/456', function(message) {
    console.log('Build log:', message.body);
});
```

### Server-Side Broadcasting

```java
@Autowired
private SimpMessagingTemplate messagingTemplate;

// Send log message
public void sendLogMessage(Long appId, String logLine) {
    messagingTemplate.convertAndSend(
        "/topic/logs/app/" + appId,
        logLine
    );
}

// Send build update
public void sendBuildUpdate(Long buildId, BuildStatus status) {
    messagingTemplate.convertAndSend(
        "/topic/builds/" + buildId,
        Map.of("status", status, "timestamp", LocalDateTime.now())
    );
}
```

---

## 6. Managed Database Provisioning

### Supported Database Types

```java
public enum DatabaseType {
    POSTGRESQL,   // PostgreSQL 12, 13, 14, 15, 16
    MYSQL,        // MySQL 5.7, 8.0
    MONGODB,      // MongoDB 4.4, 5.0, 6.0
    REDIS,        // Redis 6.x, 7.x
    ELASTICSEARCH,// Elasticsearch 7.x, 8.x
    CASSANDRA,    // Cassandra 3.x, 4.x
    MARIADB       // MariaDB 10.x
}
```

### ManagedDatabase Entity

```java
@Entity
public class ManagedDatabase {
    private String name;
    private DatabaseType databaseType;
    private String version;
    private String connectionString;
    private String host;
    private Integer port;
    private String databaseName;
    private Integer storageSizeGb;
    private Boolean backupEnabled;
    private Integer backupRetentionDays;
    private Boolean highAvailability;
    private String status;  // PROVISIONING, RUNNING, STOPPED, FAILED
}
```

### Features

- **Automatic Provisioning**: Deploy databases as Kubernetes StatefulSets
- **Backup Management**: Automated backups with configurable retention
- **High Availability**: Multi-replica setup for HA
- **Connection Management**: Automatic connection string generation
- **Secret Management**: Secure password storage
- **Resource Allocation**: Configurable storage and compute

### Example Usage

```json
POST /api/v1/databases
{
  "name": "prod-postgres",
  "databaseType": "POSTGRESQL",
  "version": "16",
  "projectId": 1,
  "environmentId": 1,
  "storageSizeGb": 100,
  "backupEnabled": true,
  "backupRetentionDays": 30,
  "highAvailability": true
}
```

---

## 7. Audit Logging

### Features
- **Complete Audit Trail**: Track all user actions
- **Security Events**: Login, permission changes
- **Resource Changes**: Application deployments, config updates
- **Search and Filter**: Query audit logs by user, action, timeframe
- **Compliance Ready**: Meet regulatory requirements

### AuditLog Entity

```java
@Entity
public class AuditLog {
    private Long userId;
    private Long organizationId;
    private String action;          // CREATE, UPDATE, DELETE, DEPLOY, etc.
    private String resourceType;    // APPLICATION, DEPLOYMENT, DATABASE, etc.
    private Long resourceId;
    private String description;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> metadata;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime timestamp;
}
```

### Tracked Actions

- **Authentication**: Login, logout, password changes
- **Applications**: Create, update, delete, deploy, scale
- **Deployments**: Start, rollback, cancel
- **Databases**: Provision, backup, restore, delete
- **Environments**: Create, update, delete
- **Organization**: Member add/remove, role changes
- **Configuration**: Environment variables, secrets

### Example Queries

```java
// Get all actions by user
auditLogRepository.findByUserId(userId, pageable);

// Get all actions for a resource
auditLogRepository.findByResourceTypeAndResourceId("APPLICATION", appId);

// Get actions in time range
auditLogRepository.findByActionAndTimestampBetween(
    "DEPLOY",
    startTime,
    endTime
);
```

---

## 8. Notification System

### Supported Channels

```java
public enum NotificationChannel {
    EMAIL,    // SMTP email notifications
    SLACK,    // Slack webhooks
    WEBHOOK,  // Custom HTTP webhooks
    SMS,      // SMS via Twilio/SNS
    TEAMS     // Microsoft Teams
}
```

### Notification Entity

```java
@Entity
public class Notification {
    private Long organizationId;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private String recipient;
    private Map<String, String> metadata;
    private NotificationStatus status;  // PENDING, SENDING, SENT, FAILED
    private Integer retryCount;
}
```

### Use Cases

**Deployment Notifications:**
```
✓ Deployment started: my-app v1.2.3
✓ Deployment successful: my-app v1.2.3 (2min 34s)
✗ Deployment failed: my-app v1.2.3 - Error: OOMKilled
```

**Build Notifications:**
```
⚙️  Build started: my-app@abc123
✓ Build completed: my-app:abc123 (3min 12s)
✗ Build failed: Compilation error in main.go
```

**Alert Notifications:**
```
⚠️  High CPU usage: my-app (85% for 5 minutes)
⚠️  Pod crash loop: my-app-pod-123
🔴 Database connection failed: prod-postgres
```

### Configuration Example

```json
{
  "channel": "SLACK",
  "recipient": "https://hooks.slack.com/services/YOUR/WEBHOOK/URL",
  "events": [
    "deployment.started",
    "deployment.completed",
    "deployment.failed",
    "build.failed",
    "alert.high_cpu"
  ]
}
```

---

## 9. JWT Authentication & Security

### Features
- **Stateless Authentication**: JWT tokens for API access
- **Role-Based Access Control**: Fine-grained permissions
- **Token Refresh**: Automatic token renewal
- **Secure Password Storage**: BCrypt hashing
- **Session Management**: Stateless, scalable

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated();
        return http.build();
    }
}
```

### JWT Token Structure

```json
{
  "sub": "user123",
  "email": "user@example.com",
  "org": "acme",
  "role": "DEVELOPER",
  "iat": 1699000000,
  "exp": 1699086400
}
```

### API Usage

```bash
# Login
POST /api/v1/auth/login
{
  "username": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 86400
}

# Use token in requests
GET /api/v1/applications
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 10. Prometheus Metrics Integration

### Features
- **Application Metrics**: Request count, duration, errors
- **Custom Metrics**: Business-specific metrics
- **JVM Metrics**: Memory, GC, threads
- **System Metrics**: CPU, disk, network
- **Grafana Integration**: Pre-built dashboards

### Exposed Metrics

```
# Application metrics
http_requests_total{method="GET",endpoint="/api/v1/applications",status="200"} 1523
http_request_duration_seconds{method="POST",endpoint="/api/v1/deployments"} 2.34

# Business metrics
deployments_total{status="success"} 156
deployments_total{status="failed"} 3
applications_count{environment="production"} 42

# JVM metrics
jvm_memory_used_bytes{area="heap"} 536870912
jvm_gc_pause_seconds_sum 12.34
jvm_threads_live 45

# Kubernetes metrics
kube_deployment_replicas{deployment="my-app"} 3
kube_pod_status_ready{pod="my-app-abc"} 1
```

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'platform-api'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['platform-api:8080']
```

### Grafana Dashboard Queries

```promql
# Request rate
rate(http_requests_total[5m])

# Error rate
rate(http_requests_total{status=~"5.."}[5m]) /
rate(http_requests_total[5m])

# Response time (95th percentile)
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))

# Active deployments
deployments_active{status="in_progress"}
```

---

## Docker Compose Updates

### Enhanced docker-compose.yml

```yaml
version: '3.8'

services:
  # ... existing services ...

  # Prometheus for metrics
  prometheus:
    image: prom/prometheus:latest
    container_name: platform-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - platform-network

  # Grafana for visualization
  grafana:
    image: grafana/grafana:latest
    container_name: platform-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - platform-network

  # Kubernetes (k3s) for local testing
  k3s:
    image: rancher/k3s:latest
    container_name: platform-k3s
    privileged: true
    ports:
      - "6443:6443"
    environment:
      - K3S_KUBECONFIG_OUTPUT=/output/kubeconfig.yaml
    volumes:
      - k3s_data:/var/lib/rancher/k3s
    networks:
      - platform-network

volumes:
  prometheus_data:
  grafana_data:
  k3s_data:
```

---

## API Endpoints - Advanced Features

### Kubernetes Operations
```
POST   /api/v1/kubernetes/deploy/{appId}
DELETE /api/v1/kubernetes/delete/{appId}
POST   /api/v1/kubernetes/scale/{appId}
GET    /api/v1/kubernetes/status/{appId}
```

### Build Operations
```
POST   /api/v1/builds
GET    /api/v1/builds/{id}
GET    /api/v1/builds/application/{appId}
GET    /api/v1/builds/{id}/logs
```

### Database Management
```
POST   /api/v1/databases
GET    /api/v1/databases
GET    /api/v1/databases/{id}
PUT    /api/v1/databases/{id}
DELETE /api/v1/databases/{id}
POST   /api/v1/databases/{id}/backup
POST   /api/v1/databases/{id}/restore
```

### Organization & Users
```
POST   /api/v1/organizations
GET    /api/v1/organizations
POST   /api/v1/organizations/{id}/members
DELETE /api/v1/organizations/{id}/members/{userId}
GET    /api/v1/users/me
PUT    /api/v1/users/me
```

### Auto-Scaling
```
POST   /api/v1/autoscaling
GET    /api/v1/autoscaling/application/{appId}
PUT    /api/v1/autoscaling/{id}
DELETE /api/v1/autoscaling/{id}
```

### Audit Logs
```
GET    /api/v1/audit-logs
GET    /api/v1/audit-logs/user/{userId}
GET    /api/v1/audit-logs/organization/{orgId}
GET    /api/v1/audit-logs/resource/{type}/{id}
```

### Notifications
```
POST   /api/v1/notifications/config
GET    /api/v1/notifications/config
GET    /api/v1/notifications/history
```

---

## Testing Advanced Features

### 1. Test Kubernetes Deployment

```bash
# Deploy application to Kubernetes
curl -X POST http://localhost:8080/api/v1/kubernetes/deploy/1

# Check deployment status
curl http://localhost:8080/api/v1/kubernetes/status/1

# Scale application
curl -X POST http://localhost:8080/api/v1/kubernetes/scale/1?replicas=5
```

### 2. Test Docker Build

```bash
# Trigger build from Git
curl -X POST http://localhost:8080/api/v1/builds \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": 1,
    "commitSha": "abc123"
  }'

# Watch build logs via WebSocket
wscat -c ws://localhost:8080/ws/builds
```

### 3. Test Database Provisioning

```bash
# Create managed PostgreSQL database
curl -X POST http://localhost:8080/api/v1/databases \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-database",
    "databaseType": "POSTGRESQL",
    "version": "16",
    "storageSizeGb": 50,
    "backupEnabled": true
  }'
```

### 4. Test Metrics

```bash
# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Access Prometheus UI
http://localhost:9090

# Access Grafana dashboards
http://localhost:3000  (admin/admin)
```

---

## Production Deployment Considerations

### High Availability
- Run multiple API instances behind a load balancer
- Use managed Kubernetes (EKS, GKE, AKS)
- Deploy Redis in cluster mode
- Use managed PostgreSQL (RDS, Cloud SQL)

### Security
- Enable HTTPS/TLS everywhere
- Use secrets management (Vault, AWS Secrets Manager)
- Implement network policies
- Enable pod security policies
- Regular security audits

### Monitoring
- Set up alerting rules in Prometheus
- Create Grafana dashboards
- Configure log aggregation (ELK, Loki)
- Implement distributed tracing (Jaeger, Zipkin)

### Scaling
- Horizontal scaling for API instances
- Database read replicas
- CDN for static assets
- Caching strategies (Redis)

### Backup & DR
- Regular database backups
- Disaster recovery plan
- Multi-region deployment
- Regular restore testing

---

## Summary

This platform now includes enterprise-grade features:

✅ **Kubernetes Integration** - Real deployments to K8s
✅ **Docker Build Service** - Build from Git repos
✅ **Multi-Tenancy** - Organizations, projects, RBAC
✅ **Auto-Scaling** - HPA with custom metrics
✅ **Real-time Logs** - WebSocket streaming
✅ **Database Provisioning** - Managed DB service
✅ **Audit Logging** - Complete audit trail
✅ **Notifications** - Slack, Email, Webhooks
✅ **JWT Auth** - Secure API access
✅ **Prometheus Metrics** - Advanced monitoring

### Next Steps

1. Deploy to a real Kubernetes cluster
2. Configure external database for production
3. Set up Prometheus and Grafana
4. Implement CI/CD pipelines
5. Add custom domain management
6. Implement SSL/TLS automation
7. Create CLI tool for easier access
8. Build frontend dashboard

---

For questions or issues, please refer to the README.md or open a GitHub issue.
