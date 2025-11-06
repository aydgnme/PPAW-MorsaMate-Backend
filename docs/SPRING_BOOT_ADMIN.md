# Spring Boot Admin Panel

Django-style admin panel for MorseMate using Spring Boot Admin.

## 🎯 Overview

Spring Boot Admin provides a web UI for managing and monitoring Spring Boot applications. It offers:
- **Application Health Monitoring** - Real-time health status
- **Metrics Visualization** - JVM, HTTP, database metrics
- **Log Viewing** - Live application logs
- **Environment Properties** - View and manage configuration
- **Thread Dumps** - Debug performance issues
- **HTTP Traces** - Monitor HTTP requests

## 🚀 Access

**Admin Panel URL**: http://localhost:8080/admin

**Features Available:**
- ✅ Application overview and health
- ✅ Metrics and performance monitoring
- ✅ Environment and configuration
- ✅ Logger management
- ✅ Thread dumps and heap dumps
- ✅ HTTP traces
- ✅ Actuator endpoints access

## 📊 Dashboard Features

### 1. **Application Overview**
- Application status (UP/DOWN)
- Build information
- JVM details
- Process info

### 2. **Health & Metrics**
- Database connection status
- Disk space
- Custom health indicators
- Memory usage (heap, non-heap)
- CPU usage
- Thread count
- HTTP request metrics

### 3. **Logging**
- View and change log levels dynamically
- Filter logs by logger name
- Real-time log streaming

### 4. **Environment**
- All application properties
- System properties
- Environment variables

### 5. **Threads**
- Thread dump
- Thread states visualization
- Blocked threads detection

## 🛠️ Configuration

Configuration in `application.properties`:

```properties
# Spring Boot Admin Server
spring.boot.admin.server.enabled=true
spring.boot.admin.client.url=http://localhost:8080
spring.boot.admin.client.instance.name=${spring.application.name}

# Actuator Endpoints (required for full functionality)
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

## 🔐 Security

Currently configured to allow public access. In production:

1. Enable Spring Security for admin endpoints
2. Configure user authentication
3. Use HTTPS
4. Restrict access by IP if needed

Example secured configuration:
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/actuator/**").hasRole("ADMIN")
```

## 📝 CRUD Management

For Django-style CRUD operations, use REST API endpoints with admin UI:

### Available CRUD Operations:
- **Categories**: `/v1/categories`
- **Lessons**: `/v1/lessons`
- **Exercises**: `/v1/exercises`
- **Users**: `/v1/users`
- **Achievements**: `/v1/achievements`

Access via Swagger UI for testing: http://localhost:8080/swagger-ui/index.html

## 🎨 Customization

Spring Boot Admin can be customized with:
- Custom branding and colors
- Additional health indicators
- Custom metrics
- Notification systems (email, Slack, etc.)

## 📖 Additional Resources

- [Spring Boot Admin Documentation](https://codecentric.github.io/spring-boot-admin/current/)
- [Spring Boot Actuator Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Custom Health Indicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.writing-custom-health-indicators)
