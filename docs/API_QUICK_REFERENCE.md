# MorseMate API - Quick Reference

**Base URL:** `http://localhost:8080`

---

## 🔐 Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | ❌ | Register new user |
| POST | `/auth/login` | ❌ | Login user |
| GET | `/auth/me` | ✅ | Get current user |
| POST | `/auth/logout` | ✅ | Logout user |
| POST | `/auth/refresh` | ❌ | Refresh token |
| POST | `/auth/forgot-password` | ❌ | Request password reset |
| POST | `/auth/reset-password` | ❌ | Reset password |
| POST | `/auth/verify-email` | ❌ | Verify email |

---

## 👤 User Management

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/users/me` | ✅ | Get profile |
| PUT | `/users/me` | ✅ | Update profile |
| DELETE | `/users/me` | ✅ | Delete account |
| GET | `/users/{id}` | ✅ | Get user by ID |
| GET | `/users/username/{username}` | ✅ | Get user by username |
| GET | `/users` | ✅ | Get all users (paginated) |
| GET | `/users/me/statistics` | ✅ | Get user stats |
| POST | `/users/me/hearts/use` | ✅ | Use a heart |
| POST | `/users/me/hearts/refill` | ✅ | Refill hearts |
| POST | `/users/{id}/points` | 🔒 | Add points (Admin) |
| POST | `/users/{id}/deactivate` | 🔒 | Deactivate user (Admin) |
| POST | `/users/{id}/activate` | 🔒 | Activate user (Admin) |
| POST | `/users/{id}/verify-email` | 🔒 | Verify email (Admin) |

---

## 📚 Categories

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/categories` | ❌ | Get all active categories |
| GET | `/v1/categories/{id}` | ❌ | Get category by ID |
| POST | `/v1/categories` | 🔒 | Create category (Admin) |
| PUT | `/v1/categories/{id}` | 🔒 | Update category (Admin) |
| DELETE | `/v1/categories/{id}` | 🔒 | Delete category (Admin) |
| GET | `/v1/categories/admin/all` | 🔒 | Get all categories (Admin) |

---

## 📖 Lessons

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/categories/{categoryId}/lessons` | ✅ | Get lessons by category |
| GET | `/v1/lessons/{id}` | ✅ | Get lesson by ID |
| POST | `/v1/lessons` | 🔒 | Create lesson (Admin) |
| PUT | `/v1/lessons/{id}` | 🔒 | Update lesson (Admin) |
| DELETE | `/v1/lessons/{id}` | 🔒 | Delete lesson (Admin) |
| POST | `/v1/lessons/{id}/complete` | ✅ | Mark lesson as completed |
| POST | `/v1/lessons/{id}/reset` | ✅ | Reset lesson progress |

---

## ✏️ Exercises

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/lessons/{lessonId}/exercises` | ✅ | Get exercises by lesson |
| GET | `/v1/exercises/{id}` | ✅ | Get exercise by ID |
| POST | `/v1/exercises` | 🔒 | Create exercise (Admin) |
| PUT | `/v1/exercises/{id}` | 🔒 | Update exercise (Admin) |
| DELETE | `/v1/exercises/{id}` | 🔒 | Delete exercise (Admin) |
| POST | `/v1/exercises/{id}/attempt` | ✅ | Submit exercise attempt |
| GET | `/v1/exercises/{id}/attempts` | ✅ | Get attempt history |

---

## 📊 Progress

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/progress` | ✅ | Get overall progress |
| GET | `/v1/progress/categories/{id}` | ✅ | Get category progress |
| GET | `/v1/progress/lessons/{id}` | ✅ | Get lesson progress |
| POST | `/v1/progress/streak` | ✅ | Update daily streak |

---

## 🏆 Achievements

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/achievements` | ✅ | Get all achievements |
| GET | `/v1/achievements/me` | ✅ | Get user achievements |
| GET | `/v1/achievements/{id}` | ✅ | Get achievement by ID |
| POST | `/v1/achievements` | 🔒 | Create achievement (Admin) |

---

## 💳 Subscription & Payments

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/subscription/plans` | ❌ | Get subscription plans |
| GET | `/v1/subscription/me` | ✅ | Get current subscription |
| POST | `/v1/subscription/subscribe` | ✅ | Subscribe to plan |
| POST | `/v1/subscription/cancel` | ✅ | Cancel subscription |
| POST | `/v1/subscription/resume` | ✅ | Resume subscription |
| GET | `/v1/payments` | ✅ | Get payment history |
| POST | `/v1/payments/create-intent` | ✅ | Create payment intent |
| POST | `/v1/payments/webhook` | ❌ | Stripe webhook |

---

## 💎 Gems & Power-Ups

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/gems/balance` | ✅ | Get gems balance |
| GET | `/v1/gems/packages` | ❌ | Get gem packages |
| POST | `/v1/gems/purchase` | ✅ | Purchase gems |
| GET | `/v1/gems/transactions` | ✅ | Get gem transactions |
| GET | `/v1/power-ups` | ✅ | Get all power-ups |
| GET | `/v1/power-ups/me` | ✅ | Get user power-ups |
| POST | `/v1/power-ups/{id}/purchase` | ✅ | Purchase power-up |
| POST | `/v1/power-ups/{id}/use` | ✅ | Use power-up |

---

## 🎁 Promo Codes

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/v1/promo-codes/validate` | ✅ | Validate promo code |
| POST | `/v1/promo-codes/apply` | ✅ | Apply promo code |
| POST | `/v1/promo-codes` | 🔒 | Create promo code (Admin) |
| GET | `/v1/promo-codes` | 🔒 | Get all promo codes (Admin) |
| POST | `/v1/promo-codes/{id}/deactivate` | 🔒 | Deactivate promo code (Admin) |

---

## 🏅 Leaderboard & Social

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/v1/leaderboard` | ✅ | Get global leaderboard |
| GET | `/v1/leaderboard/friends` | ✅ | Get friends leaderboard |
| GET | `/v1/leaderboard/categories/{id}` | ✅ | Get category leaderboard |
| GET | `/v1/users/search` | ✅ | Search users |
| POST | `/v1/friends/request` | ✅ | Send friend request |
| POST | `/v1/friends/accept/{id}` | ✅ | Accept friend request |
| GET | `/v1/friends` | ✅ | Get friends list |
| DELETE | `/v1/friends/{id}` | ✅ | Remove friend |

---

## 🏥 Health & System

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/health` | ❌ | API health check |
| GET | `/api/ping` | ❌ | Simple ping |
| GET | `/api/info` | ❌ | System information |
| GET | `/` | ✅ | API root |
| GET | `/api-docs` | ❌ | API documentation (JSON) |
| GET | `/api-docs` | ❌ | API documentation (HTML) |
| GET | `/swagger-ui.html` | ❌ | Swagger UI |

---

## 📋 Legend

- ❌ = No authentication required (Public)
- ✅ = Authentication required (JWT Bearer Token)
- 🔒 = Admin authentication required

---

## 🔑 Authentication Header

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## 📄 Common Query Parameters

### Pagination
- `page` - Page number (0-indexed)
- `size` - Items per page
- `sort` - Sort field and direction (e.g., `createdAt,desc`)

### Filtering
- `q` - Search query
- `status` - Filter by status
- `category` - Filter by category ID
- `difficulty` - Filter by difficulty level
- `period` - Time period (`DAILY`, `WEEKLY`, `MONTHLY`, `ALL_TIME`)

---

## 📊 Response Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Success |
| 201 | Created | Resource created |
| 204 | No Content | Success with no content |
| 400 | Bad Request | Invalid parameters |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict |
| 422 | Unprocessable Entity | Validation error |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

---

## 🚀 Quick Start Examples

### Register & Login
```typescript
// Register
const response = await api.register({
  username: 'john_doe',
  email: 'john@example.com',
  password: 'SecurePass123',
  fullName: 'John Doe'
});

// Login
const response = await api.login({
  identifier: 'john@example.com',
  password: 'SecurePass123'
});

// Save token
localStorage.setItem('token', response.token);
api.setToken(response.token);
```

### Get Categories & Lessons
```typescript
// Get all categories
const categories = await api.getAllCategories();

// Get lessons for a category
const lessons = await api.getLessonsByCategory(1);

// Get lesson details
const lesson = await api.getLessonById(1);
```

### Submit Exercise
```typescript
const result = await api.submitExerciseAttempt(1, {
  answers: [
    { questionId: 1, selectedAnswer: 'A', timeSpent: 5 },
    { questionId: 2, selectedAnswer: 'B', timeSpent: 4 }
  ],
  totalTimeSpent: 45
});

console.log(`Score: ${result.score}%`);
console.log(`Passed: ${result.passed}`);
```

### Get Leaderboard
```typescript
const leaderboard = await api.getGlobalLeaderboard('WEEKLY');
console.log(`Your rank: #${leaderboard.currentUserRank}`);
```

---

## 📁 Files

- **API_ENDPOINTS.md** - Complete API documentation (200+ pages)
- **frontend-api-client.ts** - TypeScript API client
- **frontend-examples.md** - React/Vue examples with hooks
- **MorseMate_Postman_Collection.json** - Postman collection
- **MorseMate_Test_Flows.json** - Automated test flows
- **POSTMAN_GUIDE.md** - Postman usage guide

---

**Version:** 1.0.0
**Last Updated:** January 20, 2025
**Base URL:** http://localhost:8080
