# Environment Variables Guide

**Last Updated:** November 2, 2025
**Application:** MorseMate API
**Version:** 1.0.0

---

## Overview

This document describes all environment variables used by MorseMate. These variables are configured in the `.env` file for local development and should be set in your deployment environment for production.

---

## 🚀 Quick Start

### 1. Copy Example File
```bash
cp .env.example .env
```

### 2. Edit Variables
```bash
nano .env  # or your preferred editor
```

### 3. Required Changes for Production
⚠️ **CRITICAL:** Change these values before deploying to production:
- `JWT_SECRET` - Generate a strong 256-bit secret
- `DB_PASSWORD` - Use a secure database password
- `STRIPE_API_KEY` - Use production Stripe key
- `STRIPE_WEBHOOK_SECRET` - Use production webhook secret

---

## 📋 Environment Variables Reference

### Database Configuration

#### `DB_URL`
**Required:** ✅ Yes
**Default:** `jdbc:postgresql://localhost:5432/morse_code_db`
**Description:** PostgreSQL database connection URL

**Format:**
```
jdbc:postgresql://<host>:<port>/<database_name>
```

**Examples:**
```bash
# Local development
DB_URL=jdbc:postgresql://localhost:5432/morse_code_db

# Docker container
DB_URL=jdbc:postgresql://postgres:5432/morse_code_db

# Remote database
DB_URL=jdbc:postgresql://db.example.com:5432/morsemate_prod
```

---

#### `DB_USERNAME`
**Required:** ✅ Yes
**Default:** `postgres`
**Description:** Database username

**Example:**
```bash
DB_USERNAME=postgres
```

**Security Notes:**
- Use least privilege principle
- Create dedicated application user
- Don't use superuser in production

---

#### `DB_PASSWORD`
**Required:** ✅ Yes
**Default:** `postgres`
**Description:** Database password

**Example:**
```bash
DB_PASSWORD=your_secure_password_here
```

**Security Notes:**
- ⚠️ **NEVER commit this to version control**
- Use strong passwords (16+ characters)
- Rotate passwords regularly
- Use different passwords per environment

---

### JWT Configuration

#### `JWT_SECRET`
**Required:** ✅ Yes (Production)
**Default:** `MorseMate-Super-Secret-Key-For-Development-Only-Change-In-Production-2025`
**Description:** Secret key for signing JWT tokens

**Requirements:**
- Minimum 256 bits (32 characters)
- Use cryptographically random string
- Must be same across all app instances

**Generate Strong Secret:**
```bash
# Using OpenSSL
openssl rand -base64 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

# Example output
C4v70Mjy7KZrOjtG5YeDIXD/hGWTGTcfUqguf7cao7zU0V78tzZq7zN3lX+W7wSM
```

**Example:**
```bash
JWT_SECRET=C4v70Mjy7KZrOjtG5YeDIXD/hGWTGTcfUqguf7cao7zU0V78tzZq7zN3lX+W7wSM
```

**Security Notes:**
- ⚠️ **CRITICAL:** Change default value in production
- Never expose in logs or error messages
- Store securely (use secrets management)
- Rotate periodically

---

#### `JWT_EXPIRATION`
**Required:** ❌ No
**Default:** `86400000` (24 hours)
**Description:** JWT token expiration time in milliseconds

**Common Values:**
```bash
# 1 hour
JWT_EXPIRATION=3600000

# 24 hours (default)
JWT_EXPIRATION=86400000

# 7 days
JWT_EXPIRATION=604800000

# 30 days
JWT_EXPIRATION=2592000000
```

**Recommendations:**
- **Development:** 24 hours
- **Production:** 1-24 hours
- **Mobile apps:** 7-30 days (with refresh token)

---

### Stripe Configuration

#### `STRIPE_API_KEY`
**Required:** ✅ Yes (for payments)
**Default:** `sk_test_your_stripe_secret_key_here`
**Description:** Stripe secret API key

**Format:**
```bash
# Test key (development)
STRIPE_API_KEY=sk_test_xxxxxxxxxxxxx

# Production key
STRIPE_API_KEY=sk_live_xxxxxxxxxxxxx
```

**Get Your Keys:**
1. Go to https://dashboard.stripe.com/apikeys
2. Copy "Secret key"
3. Use test key for development
4. Use live key for production

**Security Notes:**
- ⚠️ **NEVER commit to version control**
- Never expose in frontend
- Use test keys in development
- Rotate if compromised

---

#### `STRIPE_WEBHOOK_SECRET`
**Required:** ✅ Yes (for webhooks)
**Default:** `whsec_your_webhook_secret_here`
**Description:** Stripe webhook signing secret

**Format:**
```bash
STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx
```

**Get Webhook Secret:**
1. Go to https://dashboard.stripe.com/webhooks
2. Add endpoint: `https://your-domain.com/v1/webhooks/stripe`
3. Copy "Signing secret"

**Security Notes:**
- Validates webhook authenticity
- Different per endpoint
- Rotate if compromised

---

### Application Configuration

#### `SERVER_PORT`
**Required:** ❌ No
**Default:** `8080`
**Description:** HTTP server port

**Example:**
```bash
SERVER_PORT=8080
```

**Common Ports:**
- `8080` - Development default
- `8000` - Alternative dev port
- `80` - Production HTTP (requires root/capabilities)
- `443` - Production HTTPS (requires root/capabilities)

**Note:** Cloud platforms may override this

---

#### `CORS_ALLOWED_ORIGINS`
**Required:** ❌ No
**Default:** `http://localhost:3000`
**Description:** Comma-separated list of allowed CORS origins

**Format:**
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

**Examples:**
```bash
# Development (React + Vite)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Production
CORS_ALLOWED_ORIGINS=https://morsemate.com,https://www.morsemate.com

# Multiple environments
CORS_ALLOWED_ORIGINS=https://app.morsemate.com,https://staging.morsemate.com
```

**Security Notes:**
- Be specific (avoid wildcards `*`)
- Use HTTPS in production
- Include all frontend domains
- Don't include trailing slashes

---

#### `API_VERSION`
**Required:** ❌ No
**Default:** `v1`
**Description:** API version prefix

**Example:**
```bash
API_VERSION=v1
```

**Usage:**
- Endpoints: `/v1/categories`, `/v1/auth/login`
- Allows API versioning
- Future: `v2`, `v3`, etc.

---

## 📝 Complete `.env` Template

```bash
# ========================================
# MorseMate Environment Configuration
# ========================================

# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/morse_code_db
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# JWT Configuration
JWT_SECRET=your_256bit_secret_key_here
JWT_EXPIRATION=86400000

# Stripe Configuration (Optional - for payments)
STRIPE_API_KEY=sk_test_your_stripe_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Application Configuration
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
API_VERSION=v1
```

---

## 🌍 Environment-Specific Configurations

### Development Environment

```bash
# .env.development
DB_URL=jdbc:postgresql://localhost:5432/morse_code_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=dev-secret-key-change-in-production-2025
JWT_EXPIRATION=86400000
STRIPE_API_KEY=sk_test_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_test_xxxxx
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Production Environment

```bash
# .env.production (use secrets management)
DB_URL=jdbc:postgresql://prod-db.example.com:5432/morsemate
DB_USERNAME=morsemate_app
DB_PASSWORD=${SECURE_DB_PASSWORD}  # From secrets manager
JWT_SECRET=${SECURE_JWT_SECRET}     # From secrets manager
JWT_EXPIRATION=3600000
STRIPE_API_KEY=sk_live_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_live_xxxxx
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://morsemate.com,https://www.morsemate.com
```

---

## 🔒 Security Best Practices

### 1. Never Commit Secrets
```bash
# Add to .gitignore
.env
.env.local
.env.*.local
```

### 2. Use Secrets Management
- **AWS:** AWS Secrets Manager, Parameter Store
- **GCP:** Secret Manager
- **Azure:** Key Vault
- **Kubernetes:** Secrets
- **Docker:** Docker Secrets

### 3. Rotate Credentials Regularly
- JWT secrets: Every 90 days
- Database passwords: Every 90 days
- API keys: When compromised

### 4. Principle of Least Privilege
- Database users: Only required permissions
- API keys: Minimum required scope
- Service accounts: Restricted access

### 5. Monitor Access
- Log environment variable access
- Alert on suspicious activity
- Audit secrets usage

---

## 🚀 Deployment Examples

### Docker
```dockerfile
# Dockerfile
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
```

### Docker Compose
```yaml
# docker-compose.yml
services:
  backend:
    environment:
      - DB_URL=${DB_URL}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
```

### Kubernetes
```yaml
# deployment.yaml
env:
  - name: DB_URL
    valueFrom:
      secretKeyRef:
        name: morsemate-secrets
        key: db-url
  - name: JWT_SECRET
    valueFrom:
      secretKeyRef:
        name: morsemate-secrets
        key: jwt-secret
```

### Heroku
```bash
heroku config:set DB_URL="jdbc:postgresql://..."
heroku config:set JWT_SECRET="your_secret"
heroku config:set STRIPE_API_KEY="sk_live_xxxxx"
```

---

## 🧪 Testing Configuration

### Unit Tests
```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
jwt.secret=test-secret-key-for-unit-tests
```

### Integration Tests
```bash
# .env.test
DB_URL=jdbc:postgresql://localhost:5433/morse_code_test
DB_USERNAME=test_user
DB_PASSWORD=test_password
JWT_SECRET=test-jwt-secret
STRIPE_API_KEY=sk_test_xxxxx
```

---

## 📞 Support

If you have questions about environment configuration:

1. Check this documentation
2. Review `application.properties`
3. See `.env.example`
4. Open GitHub issue

---

**Remember:**
- ⚠️ Never commit `.env` files
- 🔒 Use strong, unique secrets
- 🔄 Rotate credentials regularly
- 📝 Document custom variables
- 🛡️ Use secrets management in production
