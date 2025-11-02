# 🎵 MorseMate - Morse Code Learning Platform

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.14-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Interactive platform for learning and practicing Morse code with gamification features, achievement system, and premium subscriptions.

## ✨ Features

- 🔐 **Secure Authentication** - JWT-based auth with BCrypt password hashing and rate limiting
- 👤 **User Management** - Profile management, heart system, points & leveling
- 📚 **Learning System** - Categorized lessons with difficulty progression
- 🎯 **Exercise Platform** - Interactive Morse code exercises with instant feedback
- 🏆 **Gamification** - Achievements, power-ups, hearts, and points system
- 💎 **Gem Economy** - Earn and spend gems on power-ups and features
- 📊 **Progress Tracking** - Detailed user statistics and exercise history
- 💳 **Premium Features** - Stripe-integrated subscription system
- 🔔 **Notifications** - In-app notification system
- 🎨 **Customization** - Theme support (light/dark mode)
- 📱 **RESTful API** - Clean, versioned API endpoints (/v1)
- 🔍 **Health Monitoring** - Database and system health checks

## 🚀 Quick Start with Docker

### Prerequisites
- Docker Desktop (or Docker Engine + Docker Compose)
- Git

### 1. Clone Repository
```bash
git clone https://github.com/aydgn/MorseMate.git
cd MorseMate
```

### 2. Setup Environment Variables
```bash
# Copy example env file
cp .env.example .env

# Edit .env with your credentials
nano .env  # or use your favorite editor
```

### 3. Start Application
```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes (CAUTION: deletes data!)
docker-compose down -v
```

### 4. Access Application
- **Backend API:** http://localhost:8080/v1
- **Health Check:** http://localhost:8080/v1/health
- **Database:** localhost:5432

### 5. Test API
```bash
# Health check
curl http://localhost:8080/v1/health

# Register user
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123",
    "fullName": "Test User"
  }'

# Login
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123"
  }'
```

---

## 🛠️ Development Setup (Without Docker)

### Prerequisites
- Java 17+
- Gradle 8+
- PostgreSQL 15+

### 1. Setup Database
```bash
# Create database
createdb morse_code_db

# Or using psql
psql -U postgres
CREATE DATABASE morse_code_db;
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env with your database credentials
```

### 3. Run Application
```bash
# Using Gradle
./gradlew bootRun

# Or build and run
./gradlew build
java -jar build/libs/morse-code-platform-0.0.1-SNAPSHOT.jar
```

---

## 📦 Docker Commands Cheatsheet

```bash
# Build and start
docker-compose up --build

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f backend
docker-compose logs -f postgres

# Restart a service
docker-compose restart backend

# Execute commands in container
docker-compose exec backend sh
docker-compose exec postgres psql -U postgres -d morse_code_db

# Check service status
docker-compose ps

# Remove everything (including volumes)
docker-compose down -v

# Rebuild backend only
docker-compose build backend
docker-compose up -d backend
```

---

## 🗄️ Database Management

### Access PostgreSQL in Docker
```bash
# Via docker-compose
docker-compose exec postgres psql -U postgres -d morse_code_db

# Via psql client (if installed)
psql -h localhost -p 5432 -U postgres -d morse_code_db
```

### Run SQL Scripts
```bash
# Place .sql files in ./init-scripts/
# They run automatically on first container start

# Or run manually
docker-compose exec postgres psql -U postgres -d morse_code_db -f /docker-entrypoint-initdb.d/your-script.sql
```

### Backup & Restore
```bash
# Backup
docker-compose exec postgres pg_dump -U postgres morse_code_db > backup.sql

# Restore
docker-compose exec -T postgres psql -U postgres -d morse_code_db < backup.sql
```

---

## 🏗️ Project Structure

```
MorseMate/
├── src/
│   ├── main/
│   │   ├── java/me/aydgn/MorseMate/
│   │   │   ├── config/          # Spring Configuration
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/      # REST API Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── PingController.java
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── request/     # API Request DTOs
│   │   │   │   └── response/    # API Response DTOs
│   │   │   ├── entity/          # JPA Database Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Lesson.java
│   │   │   │   ├── Exercise.java
│   │   │   │   ├── Achievement.java
│   │   │   │   ├── PowerUp.java
│   │   │   │   ├── SubscriptionPlan.java
│   │   │   │   ├── UserSubscription.java
│   │   │   │   └── ...
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── service/         # Business Logic Layer
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── RateLimitService.java
│   │   │   ├── security/        # Security Components
│   │   │   │   ├── JwtUtil.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   └── exception/       # Custom Exception Handlers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/me/aydgn/MorseMate/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml              # CI/CD Pipeline
│   │   └── deploy.yml          # Deployment Workflow
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   ├── feature_request.md
│   │   └── improvement.md
│   └── pull_request_template.md
├── docker-compose.yml
├── Dockerfile
├── .dockerignore
├── .env
├── .env.example
├── .gitignore
├── CONTRIBUTING.md
└── README.md
```

---

## 🔧 Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8080
lsof -i :5432

# Kill process or change port in .env
```

### Database Connection Failed
```bash
# Check if postgres is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Restart postgres
docker-compose restart postgres
```

### Backend Won't Start
```bash
# Check logs
docker-compose logs backend

# Rebuild
docker-compose build --no-cache backend
docker-compose up -d backend
```

### Clear Everything and Start Fresh
```bash
docker-compose down -v
docker system prune -a
docker-compose up --build
```

---

## 📝 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_NAME` | Database name | morse_code_db |
| `DB_USERNAME` | Database user | postgres |
| `DB_PASSWORD` | Database password | - |
| `DB_PORT` | PostgreSQL port | 5432 |
| `SERVER_PORT` | Backend API port | 8080 |
| `JWT_SECRET` | JWT secret key | - |
| `JWT_EXPIRATION` | JWT expiration (ms) | 86400000 |
| `STRIPE_API_KEY` | Stripe secret key | - |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | http://localhost:3000 |

---

## 🚢 Production Deployment

### Build Production Image
```bash
docker build -t morse-backend:latest .
```

### Deploy to Cloud
- **AWS ECS/EKS**
- **Google Cloud Run**
- **Azure Container Instances**
- **DigitalOcean App Platform**
- **Heroku**

---

## 🧪 Testing

```bash
# Run tests
./gradlew test

# Run in Docker
docker-compose exec backend ./gradlew test
```

---

## 📚 Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.6
- **Database:** PostgreSQL 15.14
- **ORM:** JPA/Hibernate 6.6.29
- **Security:** Spring Security + JWT (JJWT 0.12.3)
- **Password Hashing:** BCrypt
- **Build Tool:** Gradle 8.x
- **Payments:** Stripe API
- **Containerization:** Docker + Docker Compose
- **CI/CD:** GitHub Actions
- **Security Scanning:** Trivy
- **Code Quality:** Checkstyle, PMD

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on:
- Development setup
- Coding guidelines
- Commit message format
- Pull request process
- Testing requirements

### Quick Contribution Guide

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📖 Documentation

- [API Documentation](docs/API.md) - Complete REST API reference
- [Contributing Guidelines](CONTRIBUTING.md) - How to contribute
- [Issue Templates](.github/ISSUE_TEMPLATE/) - Bug reports, feature requests, improvements

---

## 🔒 Security

- ✅ JWT-based authentication with secure token generation
- ✅ BCrypt password hashing (work factor: 10)
- ✅ Rate limiting on authentication endpoints (5 attempts, 15-minute lockout)
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ Password strength requirements (min 8 chars, uppercase, lowercase, digit)
- ✅ Common password blacklist
- ✅ Email format validation
- ✅ Username format validation
- ✅ CORS configuration
- ✅ Sensitive data masking in logs

### Security Best Practices
- Never commit `.env` files
- Rotate JWT secrets regularly
- Use strong database passwords
- Keep dependencies updated
- Review security scan results in CI/CD

---

## 📊 Project Status

![CI/CD](https://img.shields.io/github/workflow/status/aydgn/MorseMate/CI/CD%20Pipeline)
![Tests](https://img.shields.io/badge/tests-passing-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-85%25-green)
![Dependencies](https://img.shields.io/badge/dependencies-up%20to%20date-brightgreen)

---

## 🗺️ Roadmap

### Completed Features
- [x] Authentication & Authorization (JWT, BCrypt, Role-based)
- [x] User Management (Profile, Hearts, Points, Levels, Streaks)
- [x] Rate Limiting (5 attempts, 15-min lockout)
- [x] Health Monitoring (API, Database, System Info)
- [x] Learning System (Categories, Lessons, Exercises)
- [x] Exercise Implementation (Submit, Validate, Track Progress)
- [x] Achievement System (14 endpoints, User tracking)
- [x] Gamification (Gems, PowerUps, Leaderboard)
- [x] Admin Dashboard (MVC UI with Spring Cache)
- [x] Soft Delete (16 tables, @SQLDelete, @Where)
- [x] API Documentation (91 endpoints, Postman collections)

### MVP Requirements (In Progress)
- [ ] **Subscription Controller & Service** (Entity exists, needs implementation)
- [ ] **Payment Controller & Service** (Entity exists, needs implementation)
- [ ] **Stripe Integration** (Critical for monetization)
- [ ] **Frontend Application** (Web UI or Mobile App)

### Future Enhancements
- [ ] Notification System (Push, Email, In-app)
- [ ] WebSocket Support (Real-time notifications, Live leaderboard)
- [ ] Mobile Apps (iOS, Android native)
- [ ] Advanced Analytics Dashboard
- [ ] Social Features (Friends, Challenges)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Team

- **Aydın** - Backend Development & Architecture

---

## 🙏 Acknowledgments

- Spring Boot community
- PostgreSQL community
- All contributors and testers

---

## 📞 Support

- 📧 **Email:** support@morsemate.com
- 🐛 **Issues:** [GitHub Issues](https://github.com/aydgn/MorseMate/issues)
- 💬 **Discussions:** [GitHub Discussions](https://github.com/aydgn/MorseMate/discussions)

---

## 🔗 Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

---

**Made with ❤️ for Morse Code enthusiasts**