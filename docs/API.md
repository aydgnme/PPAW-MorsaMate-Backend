# MorseMate API Documentation

**Base URL:** `http://localhost:8080/v1`

**Version:** 1.0.0

## Table of Contents
- [Authentication](#authentication)
- [Users](#users)
- [Health & Monitoring](#health--monitoring)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)

---

## Authentication

All endpoints except public ones require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Register User

Create a new user account.

**Endpoint:** `POST /v1/auth/register`

**Access:** Public

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

**Validation Rules:**
- `username`: 3-20 characters, alphanumeric, underscore, and hyphen only
- `email`: Valid email format
- `password`: Min 8 characters, must contain uppercase, lowercase, and digit
- `fullName`: 2-100 characters

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "heartsRemaining": 5,
    "maxHearts": 5,
    "points": 0,
    "level": 1,
    "totalGems": 0,
    "isPremium": false,
    "preferredTheme": "LIGHT",
    "createdAt": "2025-10-08T12:00:00",
    "lastLogin": "2025-10-08T12:00:00"
  }
}
```

**Error Responses:**

`400 Bad Request` - Validation failed
```json
{
  "message": "Username already exists"
}
```

`429 Too Many Requests` - Rate limit exceeded
```json
{
  "message": "Too many registration attempts. Please try again later."
}
```

---

### Login

Authenticate and receive JWT token.

**Endpoint:** `POST /v1/auth/login`

**Access:** Public

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "heartsRemaining": 5,
    "maxHearts": 5,
    "points": 150,
    "level": 2,
    "totalGems": 50,
    "isPremium": false,
    "preferredTheme": "DARK",
    "createdAt": "2025-10-08T12:00:00",
    "lastLogin": "2025-10-08T14:30:00"
  }
}
```

**Error Responses:**

`401 Unauthorized` - Invalid credentials
```json
{
  "message": "Invalid email or password"
}
```

`429 Too Many Requests` - Rate limit exceeded
```json
{
  "message": "Too many login attempts. Please try again later."
}
```

---

### Get Current User

Get authenticated user's information.

**Endpoint:** `GET /v1/auth/me`

**Access:** Protected (Requires JWT)

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "heartsRemaining": 4,
  "maxHearts": 5,
  "points": 250,
  "level": 3,
  "totalGems": 75,
  "isPremium": true,
  "preferredTheme": "DARK",
  "createdAt": "2025-10-08T12:00:00",
  "lastLogin": "2025-10-08T16:45:00"
}
```

**Error Responses:**

`401 Unauthorized` - Invalid or missing token
```json
{
  "message": "Unauthorized"
}
```

---

## Users

User management endpoints.

### Get User Profile

Get current user's profile.

**Endpoint:** `GET /v1/users/me`

**Access:** Protected (Requires JWT)

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "heartsRemaining": 5,
  "maxHearts": 5,
  "points": 500,
  "level": 5,
  "totalGems": 150,
  "isPremium": true,
  "preferredTheme": "DARK",
  "createdAt": "2025-10-08T12:00:00",
  "lastLogin": "2025-10-08T18:00:00"
}
```

---

### Update Profile

Update user profile information.

**Endpoint:** `PUT /v1/users/me`

**Access:** Protected (Requires JWT)

**Request Body:**
```json
{
  "fullName": "John Michael Doe",
  "preferredTheme": "DARK"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Michael Doe",
  "heartsRemaining": 5,
  "maxHearts": 5,
  "points": 500,
  "level": 5,
  "totalGems": 150,
  "isPremium": true,
  "preferredTheme": "DARK",
  "createdAt": "2025-10-08T12:00:00",
  "lastLogin": "2025-10-08T18:00:00"
}
```

---

### Use Heart

Consume one heart (for starting an exercise).

**Endpoint:** `POST /v1/users/me/hearts/use`

**Access:** Protected (Requires JWT)

**Response:** `200 OK`
```json
{
  "message": "Heart used successfully"
}
```

**Error Responses:**

`400 Bad Request` - No hearts available
```json
{
  "message": "No hearts available"
}
```

---

### Refill Hearts

Manually refill hearts (premium feature or after cooldown).

**Endpoint:** `POST /v1/users/me/hearts/refill`

**Access:** Protected (Requires JWT)

**Response:** `200 OK`
```json
{
  "message": "Hearts refilled successfully"
}
```

---

### Add Points

Add points to user (internal use or achievement rewards).

**Endpoint:** `POST /v1/users/me/points`

**Access:** Protected (Requires JWT)

**Request Body:**
```json
{
  "points": 50
}
```

**Response:** `200 OK`
```json
{
  "message": "Points added successfully"
}
```

---

### Get User Statistics

Get detailed user statistics.

**Endpoint:** `GET /v1/users/me/stats`

**Access:** Protected (Requires JWT)

**Response:** `200 OK`
```json
{
  "totalExercises": 45,
  "completedExercises": 38,
  "totalAchievements": 12,
  "currentStreak": 7,
  "longestStreak": 15,
  "averageAccuracy": 87.5,
  "totalPracticeTime": 3600,
  "level": 5,
  "points": 500,
  "totalGems": 150,
  "rank": "Advanced"
}
```

---

### Activate/Deactivate Account

Activate or deactivate user account.

**Endpoint:** `PATCH /v1/users/me/status`

**Access:** Protected (Requires JWT)

**Request Body:**
```json
{
  "active": false
}
```

**Response:** `200 OK`
```json
{
  "message": "Account status updated successfully"
}
```

---

### Get All Users (Admin)

Get paginated list of all users.

**Endpoint:** `GET /v1/users`

**Access:** Protected (Requires JWT + Admin role)

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "level": 5,
      "points": 500,
      "isPremium": true,
      "createdAt": "2025-10-08T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    }
  },
  "totalPages": 5,
  "totalElements": 100,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

## Health & Monitoring

System health and monitoring endpoints.

### Basic Health Check

Simple ping endpoint.

**Endpoint:** `GET /v1/ping`

**Access:** Public

**Response:** `200 OK`
```json
{
  "message": "pong",
  "timestamp": "2025-10-08T20:15:30"
}
```

---

### Health Check

Check application and database health.

**Endpoint:** `GET /v1/health`

**Access:** Public

**Response:** `200 OK`
```json
{
  "status": "UP",
  "timestamp": "2025-10-08T20:15:30",
  "database": {
    "status": "UP",
    "responseTime": "15ms"
  },
  "memory": {
    "total": "512 MB",
    "used": "256 MB",
    "free": "256 MB",
    "max": "1 GB"
  }
}
```

**Error Response:** `503 Service Unavailable`
```json
{
  "status": "DOWN",
  "timestamp": "2025-10-08T20:15:30",
  "database": {
    "status": "DOWN",
    "error": "Connection refused"
  }
}
```

---

### Database Health Check

Detailed database connection and configuration check.

**Endpoint:** `GET /v1/db-check`

**Access:** Public

**Response:** `200 OK`
```json
{
  "status": "UP",
  "timestamp": "2025-10-08T20:15:30",
  "database": {
    "connected": true,
    "url": "jdbc:postgresql://localhost:5432/morse_code_db",
    "username": "postgres",
    "driverVersion": "42.6.0",
    "databaseProductName": "PostgreSQL",
    "databaseProductVersion": "15.14"
  }
}
```

---

### System Check

Comprehensive system health check including JVM, database, memory, and threads.

**Endpoint:** `GET /v1/system-check`

**Access:** Public

**Response:** `200 OK`
```json
{
  "status": "HEALTHY",
  "timestamp": "2025-10-08T20:15:30",
  "checks": {
    "jvm": {
      "status": "UP",
      "version": "17.0.8",
      "vendor": "Oracle Corporation",
      "uptime": "2h 15m"
    },
    "database": {
      "status": "UP",
      "url": "jdbc:postgresql://localhost:5432/morse_code_db",
      "version": "PostgreSQL 15.14"
    },
    "memory": {
      "total": "512 MB",
      "used": "256 MB",
      "free": "256 MB",
      "max": "1 GB",
      "usagePercent": 50.0
    },
    "threads": {
      "active": 15,
      "peak": 25,
      "daemon": 8
    }
  }
}
```

---

## Error Handling

All errors follow a consistent format:

### Standard Error Response

```json
{
  "message": "Error description",
  "timestamp": "2025-10-08T20:15:30",
  "status": 400,
  "error": "Bad Request",
  "path": "/v1/auth/register"
}
```

### HTTP Status Codes

- `200 OK` - Request succeeded
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required or failed
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Service temporarily unavailable

### Validation Errors

When validation fails, the response includes detailed field errors:

```json
{
  "message": "Validation failed",
  "timestamp": "2025-10-08T20:15:30",
  "status": 400,
  "errors": [
    {
      "field": "password",
      "message": "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    },
    {
      "field": "email",
      "message": "Email must be a valid email address"
    }
  ]
}
```

---

## Rate Limiting

Rate limiting is applied to sensitive endpoints to prevent abuse.

### Protected Endpoints

- `/v1/auth/register` - 5 attempts per 15 minutes per email
- `/v1/auth/login` - 5 attempts per 15 minutes per email

### Rate Limit Headers

When rate limited, the response includes:

**Status:** `429 Too Many Requests`

**Body:**
```json
{
  "message": "Too many login attempts. Please try again later.",
  "retryAfter": "14m 35s",
  "timestamp": "2025-10-08T20:15:30"
}
```

---

## Authentication Flow

### Registration Flow

1. Client sends registration request to `/v1/auth/register`
2. Server validates input (username, email, password format)
3. Server checks rate limiting
4. Server hashes password with BCrypt
5. Server creates user account
6. Server generates JWT token
7. Server returns token and user data

### Login Flow

1. Client sends login request to `/v1/auth/login`
2. Server checks rate limiting
3. Server validates email and password
4. Server generates new JWT token
5. Server updates last login timestamp
6. Server returns token and user data

### Protected Request Flow

1. Client includes JWT token in Authorization header
2. Server validates token signature and expiration
3. Server extracts user ID from token
4. Server processes request with authenticated user context
5. Server returns response

---

## Best Practices

### Token Management

- Store tokens securely (e.g., httpOnly cookies or secure storage)
- Include token in Authorization header: `Bearer <token>`
- Handle token expiration gracefully (implement refresh logic)
- Never expose tokens in URLs or logs

### Security

- Always use HTTPS in production
- Never store passwords in plain text
- Validate all input on client and server
- Implement proper CORS configuration
- Use strong, unique passwords
- Enable 2FA when available

### Performance

- Use pagination for large datasets
- Cache user data when appropriate
- Minimize unnecessary API calls
- Implement proper error handling
- Use connection pooling for database

---

## Examples

### cURL Examples

**Register:**
```bash
curl -X POST http://localhost:8080/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Get Profile (with JWT):**
```bash
curl -X GET http://localhost:8080/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Update Profile:**
```bash
curl -X PUT http://localhost:8080/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Michael Doe",
    "preferredTheme": "DARK"
  }'
```

**Use Heart:**
```bash
curl -X POST http://localhost:8080/v1/users/me/hearts/use \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Health Check:**
```bash
curl -X GET http://localhost:8080/v1/health
```

---

### JavaScript (Fetch API) Examples

**Register:**
```javascript
const register = async (username, email, password, fullName) => {
  const response = await fetch('http://localhost:8080/v1/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, email, password, fullName })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  const data = await response.json();
  localStorage.setItem('token', data.accessToken);
  return data;
};
```

**Login:**
```javascript
const login = async (email, password) => {
  const response = await fetch('http://localhost:8080/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email, password })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  const data = await response.json();
  localStorage.setItem('token', data.accessToken);
  return data;
};
```

**Get Profile:**
```javascript
const getProfile = async () => {
  const token = localStorage.getItem('token');

  const response = await fetch('http://localhost:8080/v1/users/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    if (response.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    throw new Error('Failed to fetch profile');
  }

  return await response.json();
};
```

---

## Changelog

### Version 1.0.0 (2025-10-08)
- Initial API release
- Authentication endpoints (register, login, me)
- User management endpoints
- Health monitoring endpoints
- Rate limiting implementation
- JWT authentication
- BCrypt password hashing

---

**API Version:** 1.0.0
**Last Updated:** October 8, 2025
**Contact:** support@morsemate.com
