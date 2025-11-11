# Postman Testing Guide - Lab 7

## Overview

This guide provides comprehensive instructions for testing the MorseMate REST API using Postman. All Lab 7 exercises (GET, GET(id), POST, PUT) are covered with detailed examples.

## Prerequisites

1. **Install Postman**: Download from https://www.postman.com/downloads/
2. **Start the Application**: Run `./gradlew bootRun`
3. **Verify Server**: Check http://localhost:8080/api/health

## Quick Start

### Import the Collection

1. Open Postman
2. Click **Import** button
3. Select `POSTMAN_COLLECTION.json` from `docs/lab7/`
4. Collection will appear in the sidebar

### Environment Setup

Create a new environment with these variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| base_url | http://localhost:8080 | |
| api_version | api | |
| token | | (auto-filled after login) |
| user_id | | (auto-filled after login) |
| category_id | | (auto-filled after creating category) |
| lesson_id | | (auto-filled after creating lesson) |

## Testing Workflow

### Step 1: Health Check

**Purpose**: Verify the API is running

```
GET {{base_url}}/api/health
```

**Expected Response** (200 OK):
```json
{
  "status": "UP",
  "timestamp": "2025-11-11T10:30:00"
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("API is UP", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.eql("UP");
});
```

### Step 2: User Registration

**Purpose**: Create a test user account

```
POST {{base_url}}/api/auth/register
```

**Request Body**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123!",
  "fullName": "Test User"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "fullName": "Test User",
  "role": "USER",
  "createdAt": "2025-11-11T10:30:00"
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("User created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.username).to.eql("testuser");
    pm.expect(jsonData.role).to.eql("USER");
    pm.environment.set("user_id", jsonData.id);
});
```

### Step 3: User Login

**Purpose**: Authenticate and get JWT token

```
POST {{base_url}}/api/auth/login
```

**Request Body**:
```json
{
  "username": "testuser",
  "password": "Test123!"
}
```

**Expected Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "role": "USER",
  "expiresIn": 86400000
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Token received", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.exist;
    pm.environment.set("token", jsonData.token);
});
```

**Authorization Setup**:
After login, all subsequent requests should include:
- **Type**: Bearer Token
- **Token**: `{{token}}`

## Exercise 1: GET Operations

### Test 1.1: Get All Categories

**Purpose**: Test GET endpoint for listing all resources

```
GET {{base_url}}/api/categories
```

**Headers**:
```
Authorization: Bearer {{token}}
```

**Expected Response** (200 OK):
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
  },
  {
    "id": 2,
    "name": "Numbers",
    "description": "Learn numbers in Morse code",
    "difficulty": "BEGINNER",
    "iconName": "numbers",
    "orderIndex": 2,
    "lessonCount": 3,
    "createdAt": "2025-11-10T10:35:00",
    "updatedAt": "2025-11-11T14:25:00"
  }
]
```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});

pm.test("Categories have required fields", function () {
    var jsonData = pm.response.json();
    if (jsonData.length > 0) {
        pm.expect(jsonData[0]).to.have.property('id');
        pm.expect(jsonData[0]).to.have.property('name');
        pm.expect(jsonData[0]).to.have.property('difficulty');
    }
});
```

### Test 1.2: Get All Categories with Filter

**Purpose**: Test filtering by difficulty

```
GET {{base_url}}/api/categories?difficulty=BEGINNER&sort=asc
```

**Query Parameters**:
- `difficulty`: BEGINNER | INTERMEDIATE | ADVANCED
- `sort`: asc | desc

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Basic Letters",
    "difficulty": "BEGINNER",
    ...
  }
]
```

**Postman Test Script**:
```javascript
pm.test("All categories have BEGINNER difficulty", function () {
    var jsonData = pm.response.json();
    jsonData.forEach(function(category) {
        pm.expect(category.difficulty).to.eql("BEGINNER");
    });
});
```

### Test 1.3: Get Category by ID

**Purpose**: Test GET endpoint for single resource

```
GET {{base_url}}/api/categories/1
```

**Expected Response** (200 OK):
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

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Category ID matches", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.eql(1);
});

pm.test("Response has all required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.all.keys(
        'id', 'name', 'description', 'difficulty',
        'iconName', 'orderIndex', 'lessonCount',
        'createdAt', 'updatedAt'
    );
});
```

### Test 1.4: Get Non-Existent Category

**Purpose**: Test error handling

```
GET {{base_url}}/api/categories/99999
```

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 99999"
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 404", function () {
    pm.response.to.have.status(404);
});

pm.test("Error message is present", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include("not found");
});
```

## Exercise 3: POST Operations

### Test 3.1: Create New Category

**Purpose**: Test POST endpoint for creating resources

```
POST {{base_url}}/api/categories
```

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Request Body**:
```json
{
  "name": "Advanced Techniques",
  "description": "Learn advanced Morse code techniques and procedures",
  "difficulty": "ADVANCED",
  "iconName": "expert",
  "orderIndex": 10
}
```

**Expected Response** (201 Created):
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

**Postman Test Script**:
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Category created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.expect(jsonData.name).to.eql("Advanced Techniques");
    pm.environment.set("category_id", jsonData.id);
});

pm.test("Created timestamp is present", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.createdAt).to.exist;
    pm.expect(jsonData.updatedAt).to.exist;
});
```

### Test 3.2: Create Category with Validation Errors

**Purpose**: Test input validation

```
POST {{base_url}}/api/categories
```

**Request Body** (Invalid - missing required fields):
```json
{
  "name": "A",
  "description": "Short"
}
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name must be between 2 and 100 characters"
    },
    {
      "field": "description",
      "message": "Description must be between 10 and 1000 characters"
    },
    {
      "field": "difficulty",
      "message": "Difficulty is required"
    }
  ]
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});

pm.test("Validation errors present", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.errors).to.be.an('array');
    pm.expect(jsonData.errors.length).to.be.above(0);
});
```

### Test 3.3: Create Duplicate Category

**Purpose**: Test duplicate prevention

```
POST {{base_url}}/api/categories
```

**Request Body** (Duplicate name):
```json
{
  "name": "Basic Letters",
  "description": "This is a duplicate",
  "difficulty": "BEGINNER"
}
```

**Expected Response** (409 Conflict):
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Category already exists with name: Basic Letters"
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 409", function () {
    pm.response.to.have.status(409);
});

pm.test("Conflict message is clear", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include("already exists");
});
```

## Exercise 3: PUT Operations

### Test 3.4: Update Category

**Purpose**: Test PUT endpoint for updating resources

```
PUT {{base_url}}/api/categories/{{category_id}}
```

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Request Body**:
```json
{
  "name": "Advanced Techniques Updated",
  "description": "Updated description with more details about advanced techniques",
  "difficulty": "ADVANCED",
  "iconName": "expert-updated",
  "orderIndex": 15
}
```

**Expected Response** (200 OK):
```json
{
  "id": 5,
  "name": "Advanced Techniques Updated",
  "description": "Updated description with more details about advanced techniques",
  "difficulty": "ADVANCED",
  "iconName": "expert-updated",
  "orderIndex": 15,
  "lessonCount": 0,
  "createdAt": "2025-11-11T10:30:00",
  "updatedAt": "2025-11-11T10:35:00"
}
```

**Postman Test Script**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Category updated successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.name).to.eql("Advanced Techniques Updated");
    pm.expect(jsonData.orderIndex).to.eql(15);
});

pm.test("Updated timestamp changed", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.updatedAt).to.not.eql(jsonData.createdAt);
});
```

### Test 3.5: Partial Update Category

**Purpose**: Test partial updates (PATCH-like behavior)

```
PUT {{base_url}}/api/categories/{{category_id}}
```

**Request Body** (Only update description):
```json
{
  "description": "New description only"
}
```

**Expected Response** (200 OK):
```json
{
  "id": 5,
  "name": "Advanced Techniques Updated",
  "description": "New description only",
  "difficulty": "ADVANCED",
  "iconName": "expert-updated",
  "orderIndex": 15,
  ...
}
```

**Postman Test Script**:
```javascript
pm.test("Only description updated", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.description).to.eql("New description only");
    pm.expect(jsonData.name).to.eql("Advanced Techniques Updated");
});
```

### Test 3.6: Update Non-Existent Category

**Purpose**: Test error handling for updates

```
PUT {{base_url}}/api/categories/99999
```

**Request Body**:
```json
{
  "name": "Updated Name"
}
```

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 99999"
}
```

## Additional Tests

### Test: Delete Category

```
DELETE {{base_url}}/api/categories/{{category_id}}
```

**Expected Response** (204 No Content):
```
(Empty body)
```

**Postman Test Script**:
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

pm.test("Response body is empty", function () {
    pm.expect(pm.response.text()).to.be.empty;
});
```

### Test: Get Category Statistics

```
GET {{base_url}}/api/categories/{{category_id}}/stats
```

**Expected Response** (200 OK):
```json
{
  "categoryId": 1,
  "categoryName": "Basic Letters",
  "totalLessons": 5,
  "totalExercises": 25,
  "averageDifficulty": "BEGINNER",
  "completionRate": 75.5
}
```

## Testing Lessons API

### Get All Lessons

```
GET {{base_url}}/api/lessons
```

### Get Lessons by Category

```
GET {{base_url}}/api/lessons/category/1
```

### Get Lesson by ID

```
GET {{base_url}}/api/lessons/1
```

### Create Lesson

```
POST {{base_url}}/api/lessons
```

**Request Body**:
```json
{
  "title": "Learn Letter A",
  "content": "The letter A in Morse code is represented as dot-dash (.-)",
  "categoryId": 1,
  "difficulty": "BEGINNER",
  "morseCode": ".-",
  "orderIndex": 1
}
```

### Update Lesson

```
PUT {{base_url}}/api/lessons/{{lesson_id}}
```

**Request Body**:
```json
{
  "title": "Learn Letter A - Updated",
  "content": "Updated content with more details"
}
```

## Testing Exercises API

### Get All Exercises

```
GET {{base_url}}/api/exercises
```

### Get Exercises by Lesson

```
GET {{base_url}}/api/exercises/lesson/1
```

### Create Exercise

```
POST {{base_url}}/api/exercises
```

**Request Body**:
```json
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

## Test Automation

### Collection Runner

1. Select the collection in Postman
2. Click **Run** button
3. Select environment
4. Choose tests to run
5. Click **Run MorseMate Lab 7**

### Pre-Request Scripts

**Global Setup**:
```javascript
// Set base URL if not exists
if (!pm.environment.get("base_url")) {
    pm.environment.set("base_url", "http://localhost:8080");
}

// Add timestamp to requests
pm.request.headers.add({
    key: "X-Request-Time",
    value: new Date().toISOString()
});
```

### Test Sequences

**Complete CRUD Test Flow**:
1. Health Check
2. Register User
3. Login User
4. Create Category (POST)
5. Get All Categories (GET)
6. Get Category by ID (GET)
7. Update Category (PUT)
8. Get Updated Category (GET)
9. Delete Category (DELETE)
10. Verify Deletion (GET - expect 404)

## Environment Variables Reference

| Variable | Usage | Example |
|----------|-------|---------|
| base_url | API base URL | http://localhost:8080 |
| token | JWT auth token | eyJhbGci... |
| user_id | Current user ID | 1 |
| category_id | Last created category | 5 |
| lesson_id | Last created lesson | 10 |
| exercise_id | Last created exercise | 25 |

## Common Issues and Solutions

### Issue: 401 Unauthorized

**Solution**: Login and ensure token is set:
```javascript
pm.environment.set("token", jsonData.token);
```

### Issue: 403 Forbidden

**Solution**: Check user role. Some endpoints require ADMIN role.

### Issue: 400 Validation Error

**Solution**: Check request body against validation rules in API_IMPLEMENTATION.md

### Issue: 409 Conflict

**Solution**: Resource already exists. Use PUT to update or choose different name.

## Best Practices

1. **Use Environment Variables**: Never hardcode IDs or tokens
2. **Add Tests**: Every request should have test scripts
3. **Chain Requests**: Use test scripts to set variables for next request
4. **Clean Up**: Delete test data after testing
5. **Document**: Add descriptions to requests and folders
6. **Version Control**: Export and commit collection JSON
7. **Organize**: Group related requests in folders
8. **Mock Data**: Use consistent test data across tests

## Next Steps

1. Import `POSTMAN_COLLECTION.json`
2. Configure environment variables
3. Run health check test
4. Execute full test suite
5. Review test results
6. Export results report

For more details on API implementation, see `API_IMPLEMENTATION.md`.
