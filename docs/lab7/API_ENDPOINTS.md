# API Endpoints Reference - Lab 7

## Quick Reference

This document provides a quick reference for all API endpoints implemented for Lab 7.

**Base URL**: `http://localhost:8080/api`

## Authentication Endpoints

### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "fullName": "string"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "fullName": "string",
  "role": "USER",
  "createdAt": "2025-11-11T10:30:00"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response**: `200 OK`
```json
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "username": "string",
  "role": "USER",
  "expiresIn": 86400000
}
```

## Category Endpoints

### Get All Categories (Exercise 1)
```http
GET /api/categories
GET /api/categories?difficulty=BEGINNER
GET /api/categories?sort=asc
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Basic Letters",
    "description": "Learn basic letters in Morse code",
    "difficulty": "BEGINNER",
    "iconName": "alphabet",
    "orderIndex": 1,
    "lessonCount": 5,
    "createdAt": "2025-11-10T10:30:00",
    "updatedAt": "2025-11-11T14:20:00"
  }
]
```

### Get Category by ID (Exercise 1)
```http
GET /api/categories/{id}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "name": "Basic Letters",
  "description": "Learn basic letters in Morse code",
  "difficulty": "BEGINNER",
  "iconName": "alphabet",
  "orderIndex": 1,
  "lessonCount": 5,
  "createdAt": "2025-11-10T10:30:00",
  "updatedAt": "2025-11-11T14:20:00"
}
```

**Error Response**: `404 Not Found`
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 999"
}
```

### Create Category (Exercise 3)
```http
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Advanced Techniques",
  "description": "Learn advanced Morse code techniques and procedures",
  "difficulty": "ADVANCED",
  "iconName": "expert",
  "orderIndex": 10
}
```

**Response**: `201 Created`
```json
{
  "id": 5,
  "name": "Advanced Techniques",
  "description": "Learn advanced Morse code techniques and procedures",
  "difficulty": "ADVANCED",
  "iconName": "expert",
  "orderIndex": 10,
  "lessonCount": 0,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T10:30:00"
}
```

**Validation Error**: `400 Bad Request`
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Category name is required"
    }
  ]
}
```

### Update Category (Exercise 3)
```http
PUT /api/categories/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Advanced Techniques Updated",
  "description": "Updated description",
  "difficulty": "ADVANCED",
  "iconName": "expert-updated",
  "orderIndex": 15
}
```

**Response**: `200 OK`
```json
{
  "id": 5,
  "name": "Advanced Techniques Updated",
  "description": "Updated description",
  "difficulty": "ADVANCED",
  "iconName": "expert-updated",
  "orderIndex": 15,
  "lessonCount": 0,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T10:35:00"
}
```

### Delete Category
```http
DELETE /api/categories/{id}
Authorization: Bearer {token}
```

**Response**: `204 No Content`

### Get Category Statistics
```http
GET /api/categories/{id}/stats
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
{
  "categoryId": 1,
  "categoryName": "Basic Letters",
  "totalLessons": 5,
  "totalExercises": 25,
  "averageDifficulty": "BEGINNER"
}
```

## Lesson Endpoints

### Get All Lessons
```http
GET /api/lessons
GET /api/lessons?difficulty=BEGINNER
GET /api/lessons/category/{categoryId}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "title": "Learn Letter A",
    "content": "The letter A in Morse code...",
    "categoryId": 1,
    "categoryName": "Basic Letters",
    "difficulty": "BEGINNER",
    "morseCode": ".-",
    "orderIndex": 1,
    "exerciseCount": 5,
    "createdAt": "2025-11-10T10:30:00",
    "updatedAt": "2025-11-11T14:20:00"
  }
]
```

### Get Lesson by ID
```http
GET /api/lessons/{id}
```

**Response**: `200 OK`
```json
{
  "id": 1,
  "title": "Learn Letter A",
  "content": "The letter A in Morse code is represented as dot-dash (.-)",
  "categoryId": 1,
  "categoryName": "Basic Letters",
  "difficulty": "BEGINNER",
  "morseCode": ".-",
  "orderIndex": 1,
  "exerciseCount": 5,
  "createdAt": "2025-11-10T10:30:00",
  "updatedAt": "2025-11-11T14:20:00"
}
```

### Create Lesson
```http
POST /api/lessons
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Learn Letter A",
  "content": "The letter A in Morse code is represented as dot-dash (.-)",
  "categoryId": 1,
  "difficulty": "BEGINNER",
  "morseCode": ".-",
  "orderIndex": 1
}
```

**Response**: `201 Created`

### Update Lesson
```http
PUT /api/lessons/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Learn Letter A - Updated",
  "content": "Updated content",
  "difficulty": "BEGINNER"
}
```

**Response**: `200 OK`

### Delete Lesson
```http
DELETE /api/lessons/{id}
Authorization: Bearer {token}
```

**Response**: `204 No Content`

## Exercise Endpoints

### Get All Exercises
```http
GET /api/exercises
GET /api/exercises/lesson/{lessonId}
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "question": "What is the Morse code for letter A?",
    "correctAnswer": ".-",
    "options": [".-", "-.", "..", "--"],
    "lessonId": 1,
    "lessonTitle": "Learn Letter A",
    "exerciseType": "MULTIPLE_CHOICE",
    "points": 10,
    "timeLimit": 30,
    "createdAt": "2025-11-10T10:30:00",
    "updatedAt": "2025-11-11T14:20:00"
  }
]
```

### Get Exercise by ID
```http
GET /api/exercises/{id}
```

**Response**: `200 OK`

### Create Exercise
```http
POST /api/exercises
Authorization: Bearer {token}
Content-Type: application/json

{
  "question": "What is the Morse code for letter A?",
  "correctAnswer": ".-",
  "options": [".-", "-.", "..", "--"],
  "lessonId": 1,
  "exerciseType": "MULTIPLE_CHOICE",
  "points": 10,
  "timeLimit": 30
}
```

**Response**: `201 Created`

### Update Exercise
```http
PUT /api/exercises/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "question": "Updated question?",
  "points": 15
}
```

**Response**: `200 OK`

### Delete Exercise
```http
DELETE /api/exercises/{id}
Authorization: Bearer {token}
```

**Response**: `204 No Content`

## Health Check Endpoints

### API Health Check
```http
GET /api/health
```

**Response**: `200 OK`
```json
{
  "status": "UP",
  "timestamp": "2025-11-11T10:30:00"
}
```

### Detailed Health Check
```http
GET /api/health/details
Authorization: Bearer {token}
```

**Response**: `200 OK`
```json
{
  "status": "UP",
  "database": "UP",
  "diskSpace": "UP",
  "timestamp": "2025-11-11T10:30:00"
}
```

## Error Responses

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 404 Not Found
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Resource already exists"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests |
| 204 | No Content | Successful DELETE requests |
| 400 | Bad Request | Validation errors, malformed requests |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource, constraint violation |
| 500 | Internal Server Error | Server-side errors |

## Request Headers

### Required for Authenticated Endpoints
```http
Authorization: Bearer {token}
```

### Content Type for POST/PUT
```http
Content-Type: application/json
```

## Query Parameters

### Pagination (Future Enhancement)
```
?page=0&size=20&sort=name,asc
```

### Filtering
```
?difficulty=BEGINNER
?categoryId=1
?search=morse
```

### Sorting
```
?sort=asc
?sort=desc
```

## Data Types

### Difficulty Enum
- `BEGINNER`
- `INTERMEDIATE`
- `ADVANCED`

### Role Enum
- `USER`
- `PREMIUM`
- `ADMIN`

### Exercise Type Enum
- `MULTIPLE_CHOICE`
- `TEXT_INPUT`
- `AUDIO_RECOGNITION`
- `MORSE_TO_TEXT`
- `TEXT_TO_MORSE`

## Rate Limiting (Future Enhancement)

Currently no rate limiting is implemented. Future versions may include:
- 100 requests per minute per user
- 1000 requests per hour per user
- Response header: `X-RateLimit-Remaining`

## Versioning

Current API version: `v1` (implied)

Future versions can be accessed via:
- URL versioning: `/api/v2/categories`
- Header versioning: `Accept: application/vnd.morsemate.v2+json`

## CORS Configuration

Allowed origins: `http://localhost:3000` (frontend)
Allowed methods: `GET, POST, PUT, DELETE, OPTIONS`
Allowed headers: `Authorization, Content-Type`

## Testing

Use the provided Postman collection: `POSTMAN_COLLECTION.json`

Import into Postman and set environment variables:
- `base_url`: http://localhost:8080
- `token`: (auto-filled after login)

## Additional Resources

- **README.md** - Lab overview (Romanian)
- **API_IMPLEMENTATION.md** - Technical implementation details
- **POSTMAN_TESTING.md** - Detailed testing guide
- **POSTMAN_COLLECTION.json** - Ready-to-use Postman collection

## Support

For API documentation and interactive testing:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
