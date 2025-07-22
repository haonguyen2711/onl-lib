# Jenkins CI/CD Pipeline Configuration

## 🎯 Overview
This Jenkinsfile provides a complete CI/CD pipeline for the Online Library Encryption System with:
- Automated testing and quality checks
- Security scanning 
- Multi-environment deployment
- Rollback capabilities

## 🏗️ Pipeline Stages

### 1. **Checkout & Setup**
- Source code checkout
- Environment preparation
- Git metadata extraction

### 2. **Code Quality & Security**
- **SonarQube Analysis**: Code quality metrics
- **OWASP Dependency Check**: Security vulnerability scanning
- **SpotBugs**: Static analysis for bugs and security issues

### 3. **Build & Test**
- **Unit Tests**: Fast, isolated tests
- **Integration Tests**: Database and API tests
- **Coverage Reports**: Test coverage analysis

### 4. **Packaging & Docker**
- **Maven Package**: JAR file creation
- **Docker Build**: Container image creation
- **Multi-tagging**: latest, branch, commit-specific tags

### 5. **Security Scanning**
- **Container Scanning**: Trivy image vulnerability scan
- **SAST**: Static Application Security Testing

### 6. **Deployment**
- **Staging**: Automatic deployment for `develop` branch
- **Production**: Manual approval for `main` branch
- **Blue-Green**: Zero-downtime deployment strategy

## 🔧 Required Jenkins Plugins

```bash
# Core plugins
Pipeline
Pipeline: Stage View
Blue Ocean
Git

# Quality & Security
SonarQube Scanner
OWASP Dependency-Check
HTML Publisher

# Docker & Kubernetes
Docker Pipeline
Kubernetes CLI

# Notifications
Slack Notification
Email Extension
```

## 🛠️ Jenkins Configuration

### 1. **Global Tools**
Configure these tools in Jenkins Global Tool Configuration:

#### Maven
- **Name**: `Maven-3.9.5`
- **Version**: Maven 3.9.5
- **Install automatically**: ✅

#### JDK
- **Name**: `OpenJDK-21`
- **Version**: OpenJDK 21
- **Install automatically**: ✅

### 2. **Credentials**
Add these credentials in Jenkins Credentials:

```bash
# Database credentials
staging-db-url        (Secret text)
staging-db-user       (Username/Password)
staging-db-pass       (Secret text)
prod-db-url          (Secret text)
prod-db-user         (Username/Password)
prod-db-pass         (Secret text)

# Docker registry
docker-registry-credentials (Username/Password)

# Kubernetes
kubernetes-config     (Secret file)
```

### 3. **Environment Variables**
Set these in Jenkins Global Properties:

```bash
DOCKER_REGISTRY=your-registry.com
SLACK_CHANNEL=#onllib-ci
EMAIL_RECIPIENTS=dev@company.com
```

## 🚀 Branch Strategy

### **Main Branch** (`main`)
- **Triggers**: Push to main
- **Pipeline**: Full pipeline + Production deployment
- **Approval**: Manual approval required for production
- **Deployment**: Blue-green to production

### **Develop Branch** (`develop`) 
- **Triggers**: Push to develop
- **Pipeline**: Full pipeline + Staging deployment
- **Deployment**: Automatic to staging environment

### **Feature Branches** (`feature/*`)
- **Triggers**: Push to feature branches
- **Pipeline**: Build + Test + Quality checks
- **Deployment**: None

### **Pull Requests**
- **Triggers**: PR creation/update
- **Pipeline**: Build + Test + Security scan
- **Deployment**: None

## 📊 Quality Gates

### **Unit Tests**
- **Threshold**: 80% code coverage minimum
- **Failure**: Pipeline fails if tests fail

### **Integration Tests**
- **Scope**: API endpoints, database operations
- **Environment**: H2 in-memory database

### **Security Scans**
- **OWASP**: Fails on CVSS 7+ vulnerabilities
- **Container**: Fails on HIGH/CRITICAL vulnerabilities
- **SAST**: Warnings for potential security issues

### **SonarQube Quality Gate**
- **Coverage**: > 80%
- **Duplications**: < 3%
- **Maintainability**: A rating
- **Reliability**: A rating
- **Security**: A rating

## 🔔 Notifications

### **Slack Integration**
```yaml
Success: ✅ Green notification with build info
Failure: ❌ Red notification with failure details  
Unstable: ⚠️ Yellow notification with warnings
```

### **Email Notifications**
- **Recipients**: Development team
- **Triggers**: Build failures, first success after failure
- **Content**: Build logs, failure reasons

## 🐳 Docker & Kubernetes

### **Docker Images**
```bash
# Tagging strategy
onllib-app:latest                    # Latest stable (main branch)
onllib-app:123                       # Build number
onllib-app:main-abc1234              # Branch + commit
onllib-app:develop-def5678           # Feature branch + commit
```

### **Kubernetes Deployment**
```yaml
# Production namespace: production
# Staging namespace: staging
# Service: onllib-app-service
# Ingress: onllib-app-ingress
```

## 📈 Monitoring & Observability

### **Health Checks**
- **Endpoint**: `/actuator/health`
- **Frequency**: Every 30 seconds
- **Timeout**: 10 seconds

### **Metrics**
- **Prometheus**: `/actuator/prometheus`
- **Grafana**: Dashboard for application metrics
- **Alerts**: CPU, Memory, Disk usage

### **Logging**
- **Centralized**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Formats**: JSON for production, plain text for development
- **Retention**: 30 days

## 🔄 Rollback Strategy

### **Automatic Rollback**
- **Trigger**: Health check failures after deployment
- **Method**: Kubernetes rollout undo
- **Notification**: Immediate Slack/Email alert

### **Manual Rollback**
```bash
# Kubernetes rollback
kubectl rollout undo deployment/onllib-app --namespace=production

# Docker rollback
docker-compose -f docker-compose.yml down
docker-compose -f docker-compose.yml up -d
```

## 🧪 Testing Strategy

### **Unit Tests**
```bash
# Location: src/test/java/**/*Test.java
# Framework: JUnit 5, Mockito
# Coverage: JaCoCo
# Profile: test
```

### **Integration Tests**
```bash
# Location: src/test/java/**/*IntegrationTest.java
# Framework: Spring Boot Test, TestContainers
# Database: H2 in-memory
# Profile: integration-test
```

### **Smoke Tests**
```bash
# Location: src/test/java/**/*SmokeTest.java
# Purpose: Basic API endpoint validation
# Environment: Staging/Production
# Frequency: After deployment
```

## 🚨 Troubleshooting

### **Common Issues**

#### Build Failures
```bash
# Check logs
View Console Output in Jenkins

# Local reproduction
mvn clean package -DskipTests=false
```

#### Docker Build Issues
```bash
# Check Dockerfile
docker build -t test-image .

# Debug container
docker run -it test-image /bin/bash
```

#### Deployment Failures
```bash
# Check Kubernetes logs
kubectl logs deployment/onllib-app -n production

# Check application logs
kubectl exec -it pod-name -n production -- tail -f /app/logs/application.log
```

#### Database Connection Issues
```bash
# Test connection
kubectl exec -it pod-name -n production -- curl -f http://localhost:8080/actuator/health
```

---

**📋 For more details, see the main [README.md](../README.md) and [SETUP.md](../SETUP.md)**
