# MorseMate API - Complete Endpoint Reference

**Base URL:** `http://localhost:8080`
**Version:** 1.0.0
**Authentication:** JWT Bearer Token (except public endpoints)

---

## 📋 Table of Contents

1. [Authentication & Authorization](#1-authentication--authorization)
2. [User Management](#2-user-management)
3. [Categories](#3-categories)
4. [Lessons](#4-lessons)
5. [Exercises](#5-exercises)
6. [User Progress](#6-user-progress)
7. [Achievements](#7-achievements)
8. [Subscription & Payment](#8-subscription--payment)
9. [Gems & Power-Ups](#9-gems--power-ups)
10. [Promo Codes](#10-promo-codes)
11. [Leaderboard & Social](#11-leaderboard--social)
12. [Health & System](#12-health--system)

---

## 1. Authentication & Authorization

### Register New User
```http
POST /auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "level": 1,
    "hearts": 5,
    "maxHearts": 5,
    "totalPoints": 0,
    "isActive": true,
    "emailVerified": false,
    "createdAt": "2025-01-20T10:00:00"
  }
}
```

---

### Login
```http
POST /auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "identifier": "john@example.com",
  "password": "SecurePass123",
  "rememberMe": true
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400,
  "user": { /* same as register */ }
}
```

---

### Get Current User
```http
GET /auth/me
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "profilePictureUrl": null,
  "level": 1,
  "hearts": 5,
  "maxHearts": 5,
  "totalPoints": 0,
  "currentStreak": 0,
  "longestStreak": 0,
  "isActive": true,
  "emailVerified": false,
  "createdAt": "2025-01-20T10:00:00",
  "lastLogin": "2025-01-20T10:05:00"
}
```

---

### Refresh Token (Future)
```http
POST /auth/refresh
Content-Type: application/json
```

**Request Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

---

### Logout (Future)
```http
POST /auth/logout
Authorization: Bearer {token}
```

---

### Forgot Password (Future)
```http
POST /auth/forgot-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

---

### Reset Password (Future)
```http
POST /auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "NewSecurePass123"
}
```

---

### Verify Email (Future)
```http
POST /auth/verify-email
Content-Type: application/json
```

**Request Body:**
```json
{
  "token": "verification_token_from_email"
}
```

---

### Resend Verification Email (Future)
```http
POST /auth/resend-verification
Authorization: Bearer {token}
```

---

## 2. User Management

### Get Current User Profile
```http
GET /users/me
Authorization: Bearer {token}
```

---

### Update Current User Profile
```http
PUT /users/me
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "John Doe Updated",
  "username": "john_doe_new",
  "profilePictureUrl": "https://example.com/avatar.jpg"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_doe_new",
  "email": "john@example.com",
  "fullName": "John Doe Updated",
  "profilePictureUrl": "https://example.com/avatar.jpg",
  /* ... other fields */
}
```

---

### Get User By ID
```http
GET /users/{id}
Authorization: Bearer {token}
```

---

### Get User By Username
```http
GET /users/username/{username}
Authorization: Bearer {token}
```

**Example:**
```http
GET /users/username/john_doe
```

---

### Get All Users (Paginated)
```http
GET /users?page=0&size=10&sort=createdAt,desc
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    { /* user object */ }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true }
  },
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true
}
```

---

### Get User Statistics
```http
GET /users/me/statistics
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "totalPoints": 1250,
  "level": 5,
  "currentStreak": 7,
  "longestStreak": 15,
  "hearts": 5,
  "maxHearts": 5,
  "completedLessons": 23,
  "totalLessons": 50,
  "completedExercises": 150,
  "totalExercises": 300,
  "averageAccuracy": 85.5,
  "totalTimeSpent": 7200,
  "achievements": 12,
  "rank": "Gold",
  "nextLevelPoints": 1500
}
```

---

### Use Heart
```http
POST /users/me/hearts/use
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Heart used successfully",
  "remainingHearts": 4
}
```

---

### Refill Hearts
```http
POST /users/me/hearts/refill
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Hearts refilled successfully",
  "hearts": 5
}
```

---

### Add Points to User (Admin)
```http
POST /users/{id}/points?points=100
Authorization: Bearer {admin_token}
```

---

### Deactivate User Account (Admin)
```http
POST /users/{id}/deactivate
Authorization: Bearer {admin_token}
```

---

### Activate User Account (Admin)
```http
POST /users/{id}/activate
Authorization: Bearer {admin_token}
```

---

### Verify User Email (Admin)
```http
POST /users/{id}/verify-email
Authorization: Bearer {admin_token}
```

---

### Delete Current User Account
```http
DELETE /users/me
Authorization: Bearer {token}
```

---

### Upload Profile Picture (Future)
```http
POST /users/me/profile-picture
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: Image file (JPEG, PNG)

---

### Change Password (Future)
```http
PUT /users/me/password
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "currentPassword": "OldPass123",
  "newPassword": "NewSecurePass123"
}
```

---

## 3. Categories

### Get All Active Categories
```http
GET /v1/categories
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Letters",
    "description": "Learn Morse code for individual letters",
    "displayOrder": 1,
    "iconUrl": "https://example.com/icons/letters.png",
    "lessonCount": 26,
    "completedLessons": 5,
    "isActive": true
  }
]
```

---

### Get Category By ID
```http
GET /v1/categories/{id}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Letters",
  "description": "Learn Morse code for individual letters",
  "displayOrder": 1,
  "iconUrl": "https://example.com/icons/letters.png",
  "lessonCount": 26,
  "completedLessons": 5,
  "isActive": true,
  "lessons": [
    {
      "id": 1,
      "title": "Letter A",
      "description": "Learn the Morse code for letter A",
      "difficulty": "BEGINNER",
      "isCompleted": true
    }
  ]
}
```

---

### Create Category (Admin)
```http
POST /v1/categories
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Numbers",
  "description": "Learn Morse code for numbers 0-9",
  "displayOrder": 2,
  "iconUrl": "https://example.com/icons/numbers.png",
  "isActive": true
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "name": "Numbers",
  "description": "Learn Morse code for numbers 0-9",
  "displayOrder": 2,
  "iconUrl": "https://example.com/icons/numbers.png",
  "lessonCount": 0,
  "isActive": true
}
```

---

### Update Category (Admin)
```http
PUT /v1/categories/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Numbers Updated",
  "description": "Updated description",
  "displayOrder": 3,
  "isActive": true
}
```

---

### Delete Category (Admin)
```http
DELETE /v1/categories/{id}
Authorization: Bearer {admin_token}
```

---

### Get All Categories Including Inactive (Admin)
```http
GET /v1/categories/admin/all
Authorization: Bearer {admin_token}
```

---

## 4. Lessons

### Get All Lessons in Category
```http
GET /v1/categories/{categoryId}/lessons
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Letter A",
    "description": "Learn the Morse code for letter A: .-",
    "morseCode": ".-",
    "audioUrl": "https://example.com/audio/a.mp3",
    "difficulty": "BEGINNER",
    "displayOrder": 1,
    "estimatedDuration": 300,
    "isCompleted": true,
    "userProgress": {
      "completionPercentage": 100,
      "lastAttemptDate": "2025-01-20T10:00:00",
      "attempts": 3
    }
  }
]
```

---

### Get Lesson By ID
```http
GET /v1/lessons/{id}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Letters",
  "title": "Letter A",
  "description": "Learn the Morse code for letter A: .-",
  "morseCode": ".-",
  "audioUrl": "https://example.com/audio/a.mp3",
  "videoUrl": null,
  "imageUrl": "https://example.com/images/letter-a.png",
  "difficulty": "BEGINNER",
  "displayOrder": 1,
  "estimatedDuration": 300,
  "requiredPointsToUnlock": 0,
  "isLocked": false,
  "isCompleted": true,
  "exercises": [
    {
      "id": 1,
      "title": "Listening Exercise",
      "type": "LISTENING",
      "isCompleted": true
    }
  ]
}
```

---

### Create Lesson (Admin)
```http
POST /v1/lessons
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "categoryId": 1,
  "title": "Letter B",
  "description": "Learn the Morse code for letter B: -...",
  "morseCode": "-...",
  "audioUrl": "https://example.com/audio/b.mp3",
  "imageUrl": "https://example.com/images/letter-b.png",
  "difficulty": "BEGINNER",
  "displayOrder": 2,
  "estimatedDuration": 300,
  "requiredPointsToUnlock": 0,
  "isActive": true
}
```

---

### Update Lesson (Admin)
```http
PUT /v1/lessons/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

---

### Delete Lesson (Admin)
```http
DELETE /v1/lessons/{id}
Authorization: Bearer {admin_token}
```

---

### Mark Lesson as Completed
```http
POST /v1/lessons/{id}/complete
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "timeSpent": 450,
  "accuracy": 95.5
}
```

**Response (200 OK):**
```json
{
  "message": "Lesson completed successfully",
  "pointsEarned": 50,
  "newTotalPoints": 1300,
  "levelUp": false,
  "achievements": []
}
```

---

### Reset Lesson Progress
```http
POST /v1/lessons/{id}/reset
Authorization: Bearer {token}
```

---

## 5. Exercises

### Get All Exercises in Lesson
```http
GET /v1/lessons/{lessonId}/exercises
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "lessonId": 1,
    "title": "Listening Exercise",
    "description": "Listen to the Morse code and identify the letter",
    "type": "LISTENING",
    "difficulty": "BEGINNER",
    "displayOrder": 1,
    "questionCount": 10,
    "timeLimit": 60,
    "passingScore": 70,
    "isCompleted": true,
    "bestScore": 95,
    "attempts": 3
  }
]
```

---

### Get Exercise By ID
```http
GET /v1/exercises/{id}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "lessonId": 1,
  "title": "Listening Exercise",
  "description": "Listen to the Morse code and identify the letter",
  "type": "LISTENING",
  "difficulty": "BEGINNER",
  "displayOrder": 1,
  "instructions": "Listen carefully to each Morse code sequence and select the correct letter",
  "questions": [
    {
      "id": 1,
      "question": "What letter does this represent?",
      "audioUrl": "https://example.com/audio/questions/q1.mp3",
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "A",
      "explanation": "This is the Morse code for letter A: .-"
    }
  ],
  "timeLimit": 60,
  "passingScore": 70,
  "isCompleted": true,
  "bestScore": 95,
  "attempts": 3
}
```

---

### Create Exercise (Admin)
```http
POST /v1/exercises
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "lessonId": 1,
  "title": "Typing Exercise",
  "description": "Type the Morse code for the given letter",
  "type": "TYPING",
  "difficulty": "BEGINNER",
  "displayOrder": 2,
  "instructions": "Type the correct Morse code sequence",
  "timeLimit": 90,
  "passingScore": 70,
  "isActive": true
}
```

---

### Update Exercise (Admin)
```http
PUT /v1/exercises/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json
```

---

### Delete Exercise (Admin)
```http
DELETE /v1/exercises/{id}
Authorization: Bearer {admin_token}
```

---

### Submit Exercise Attempt
```http
POST /v1/exercises/{id}/attempt
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "answers": [
    {
      "questionId": 1,
      "selectedAnswer": "A",
      "timeSpent": 5
    },
    {
      "questionId": 2,
      "selectedAnswer": "B",
      "timeSpent": 4
    }
  ],
  "totalTimeSpent": 45
}
```

**Response (200 OK):**
```json
{
  "attemptId": 123,
  "score": 85,
  "passed": true,
  "correctAnswers": 17,
  "incorrectAnswers": 3,
  "totalQuestions": 20,
  "accuracy": 85.0,
  "timeSpent": 45,
  "pointsEarned": 85,
  "isNewBestScore": true,
  "achievements": [
    {
      "id": 5,
      "name": "First Perfect Score",
      "description": "Score 100% on any exercise"
    }
  ],
  "feedback": [
    {
      "questionId": 1,
      "isCorrect": true,
      "correctAnswer": "A",
      "yourAnswer": "A",
      "explanation": "Correct! This is the Morse code for letter A: .-"
    }
  ]
}
```

---

### Get Exercise Attempts History
```http
GET /v1/exercises/{id}/attempts?page=0&size=10
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 123,
      "exerciseId": 1,
      "score": 85,
      "passed": true,
      "accuracy": 85.0,
      "timeSpent": 45,
      "attemptedAt": "2025-01-20T10:30:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1
}
```

---

## 6. User Progress

### Get Overall Progress
```http
GET /v1/progress
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "totalPoints": 1250,
  "level": 5,
  "currentStreak": 7,
  "longestStreak": 15,
  "totalLessonsCompleted": 23,
  "totalExercisesCompleted": 150,
  "totalTimeSpent": 7200,
  "averageAccuracy": 85.5,
  "categories": [
    {
      "categoryId": 1,
      "categoryName": "Letters",
      "completedLessons": 15,
      "totalLessons": 26,
      "progressPercentage": 57.7,
      "lastAccessedAt": "2025-01-20T10:00:00"
    }
  ]
}
```

---

### Get Progress for Category
```http
GET /v1/progress/categories/{categoryId}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "categoryId": 1,
  "categoryName": "Letters",
  "completedLessons": 15,
  "totalLessons": 26,
  "progressPercentage": 57.7,
  "lessons": [
    {
      "lessonId": 1,
      "lessonTitle": "Letter A",
      "isCompleted": true,
      "completedAt": "2025-01-15T14:30:00",
      "timeSpent": 450,
      "attempts": 2,
      "bestScore": 95
    }
  ],
  "lastAccessedAt": "2025-01-20T10:00:00"
}
```

---

### Get Progress for Lesson
```http
GET /v1/progress/lessons/{lessonId}
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "lessonId": 1,
  "lessonTitle": "Letter A",
  "isCompleted": true,
  "completedAt": "2025-01-15T14:30:00",
  "attempts": 2,
  "totalTimeSpent": 450,
  "exercises": [
    {
      "exerciseId": 1,
      "exerciseTitle": "Listening Exercise",
      "isCompleted": true,
      "bestScore": 95,
      "attempts": 3,
      "lastAttemptAt": "2025-01-15T14:30:00"
    }
  ]
}
```

---

### Update Daily Streak
```http
POST /v1/progress/streak
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "currentStreak": 8,
  "longestStreak": 15,
  "streakBroken": false,
  "reward": {
    "points": 10,
    "message": "Keep it up! 8 days in a row!"
  }
}
```

---

## 7. Achievements

### Get All Achievements
```http
GET /v1/achievements
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "First Steps",
    "description": "Complete your first lesson",
    "iconUrl": "https://example.com/icons/first-steps.png",
    "category": "PROGRESS",
    "rarity": "COMMON",
    "points": 10,
    "isUnlocked": true,
    "unlockedAt": "2025-01-15T10:00:00",
    "progress": {
      "current": 1,
      "target": 1,
      "percentage": 100
    }
  },
  {
    "id": 2,
    "name": "Perfect Streak",
    "description": "Maintain a 7-day streak",
    "iconUrl": "https://example.com/icons/streak.png",
    "category": "STREAK",
    "rarity": "RARE",
    "points": 50,
    "isUnlocked": false,
    "unlockedAt": null,
    "progress": {
      "current": 5,
      "target": 7,
      "percentage": 71.4
    }
  }
]
```

---

### Get User Achievements
```http
GET /v1/achievements/me
Authorization: Bearer {token}
```

---

### Get Achievement By ID
```http
GET /v1/achievements/{id}
Authorization: Bearer {token}
```

---

### Create Achievement (Admin)
```http
POST /v1/achievements
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Master Learner",
  "description": "Complete all lessons in a category",
  "iconUrl": "https://example.com/icons/master.png",
  "category": "PROGRESS",
  "rarity": "LEGENDARY",
  "points": 100,
  "criteria": {
    "type": "CATEGORY_COMPLETION",
    "target": 1
  }
}
```

---

## 8. Subscription & Payment

### Get Subscription Plans
```http
GET /v1/subscription/plans
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Free",
    "description": "Basic access to all content",
    "price": 0,
    "currency": "USD",
    "billingPeriod": "MONTHLY",
    "features": [
      "5 hearts",
      "Basic exercises",
      "Limited hints"
    ],
    "isPopular": false
  },
  {
    "id": 2,
    "name": "Premium",
    "description": "Unlimited learning experience",
    "price": 9.99,
    "currency": "USD",
    "billingPeriod": "MONTHLY",
    "features": [
      "Unlimited hearts",
      "All exercises",
      "Unlimited hints",
      "Ad-free experience",
      "Priority support"
    ],
    "isPopular": true
  }
]
```

---

### Get Current Subscription
```http
GET /v1/subscription/me
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "planId": 2,
  "planName": "Premium",
  "status": "ACTIVE",
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-02-01T00:00:00",
  "autoRenew": true,
  "paymentMethod": "CARD",
  "nextBillingDate": "2025-02-01T00:00:00"
}
```

---

### Subscribe to Plan
```http
POST /v1/subscription/subscribe
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "planId": 2,
  "paymentMethodId": "pm_1234567890",
  "promoCode": "WELCOME20"
}
```

**Response (200 OK):**
```json
{
  "subscriptionId": 1,
  "status": "ACTIVE",
  "message": "Successfully subscribed to Premium plan",
  "payment": {
    "amount": 7.99,
    "currency": "USD",
    "discount": 2.00,
    "paymentId": "pay_123456"
  }
}
```

---

### Cancel Subscription
```http
POST /v1/subscription/cancel
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "reason": "Too expensive",
  "feedback": "Great app but I can't afford it right now"
}
```

---

### Resume Subscription
```http
POST /v1/subscription/resume
Authorization: Bearer {token}
```

---

### Get Payment History
```http
GET /v1/payments?page=0&size=10
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "amount": 9.99,
      "currency": "USD",
      "status": "COMPLETED",
      "paymentMethod": "CARD",
      "description": "Premium Monthly Subscription",
      "createdAt": "2025-01-01T10:00:00",
      "receiptUrl": "https://example.com/receipts/123"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

### Create Payment Intent (Stripe)
```http
POST /v1/payments/create-intent
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "planId": 2,
  "promoCode": "WELCOME20"
}
```

**Response (200 OK):**
```json
{
  "clientSecret": "pi_1234567890_secret_abcdef",
  "amount": 799,
  "currency": "usd"
}
```

---

### Webhook Handler (Stripe)
```http
POST /v1/payments/webhook
Stripe-Signature: {stripe_signature}
Content-Type: application/json
```

---

## 9. Gems & Power-Ups

### Get User Gems Balance
```http
GET /v1/gems/balance
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "totalGems": 250,
  "earnedGems": 200,
  "purchasedGems": 50,
  "spentGems": 30,
  "lifetimeGems": 280
}
```

---

### Purchase Gems
```http
POST /v1/gems/purchase
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "packageId": 1,
  "paymentMethodId": "pm_1234567890"
}
```

**Response (200 OK):**
```json
{
  "transactionId": 123,
  "gemsAdded": 100,
  "newBalance": 350,
  "payment": {
    "amount": 4.99,
    "currency": "USD",
    "paymentId": "pay_123456"
  }
}
```

---

### Get Gem Packages
```http
GET /v1/gems/packages
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Small Pack",
    "gems": 100,
    "price": 4.99,
    "currency": "USD",
    "bonus": 0,
    "isPopular": false
  },
  {
    "id": 2,
    "name": "Medium Pack",
    "gems": 500,
    "price": 19.99,
    "currency": "USD",
    "bonus": 50,
    "isPopular": true
  }
]
```

---

### Get Gem Transaction History
```http
GET /v1/gems/transactions?page=0&size=20
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "type": "EARNED",
      "amount": 10,
      "description": "Completed lesson: Letter A",
      "balanceAfter": 260,
      "createdAt": "2025-01-20T10:00:00"
    },
    {
      "id": 2,
      "type": "SPENT",
      "amount": -5,
      "description": "Used power-up: Skip Exercise",
      "balanceAfter": 255,
      "createdAt": "2025-01-20T11:00:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 2
}
```

---

### Get All Power-Ups
```http
GET /v1/power-ups
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Heart Refill",
    "description": "Instantly refill all hearts",
    "iconUrl": "https://example.com/icons/heart-refill.png",
    "cost": 10,
    "costType": "GEMS",
    "isAvailable": true,
    "ownedQuantity": 2
  },
  {
    "id": 2,
    "name": "Skip Exercise",
    "description": "Skip a difficult exercise",
    "iconUrl": "https://example.com/icons/skip.png",
    "cost": 5,
    "costType": "GEMS",
    "isAvailable": true,
    "ownedQuantity": 0
  }
]
```

---

### Purchase Power-Up
```http
POST /v1/power-ups/{id}/purchase
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "quantity": 3
}
```

**Response (200 OK):**
```json
{
  "message": "Power-up purchased successfully",
  "powerUpId": 1,
  "quantity": 3,
  "totalCost": 30,
  "remainingGems": 220,
  "newQuantity": 5
}
```

---

### Use Power-Up
```http
POST /v1/power-ups/{id}/use
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "context": {
    "exerciseId": 5
  }
}
```

**Response (200 OK):**
```json
{
  "message": "Power-up used successfully",
  "effect": "All hearts refilled to maximum",
  "remainingQuantity": 4
}
```

---

### Get User Power-Ups
```http
GET /v1/power-ups/me
Authorization: Bearer {token}
```

---

## 10. Promo Codes

### Validate Promo Code
```http
POST /v1/promo-codes/validate
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "code": "WELCOME20"
}
```

**Response (200 OK):**
```json
{
  "isValid": true,
  "code": "WELCOME20",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "description": "20% off your first subscription",
  "expiresAt": "2025-12-31T23:59:59",
  "usageLimit": 1000,
  "usedCount": 156
}
```

---

### Apply Promo Code
```http
POST /v1/promo-codes/apply
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "code": "WELCOME20",
  "planId": 2
}
```

**Response (200 OK):**
```json
{
  "originalPrice": 9.99,
  "discount": 2.00,
  "finalPrice": 7.99,
  "currency": "USD",
  "promoCodeApplied": "WELCOME20"
}
```

---

### Create Promo Code (Admin)
```http
POST /v1/promo-codes
Authorization: Bearer {admin_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "code": "SUMMER25",
  "description": "25% off summer promotion",
  "discountType": "PERCENTAGE",
  "discountValue": 25,
  "usageLimit": 500,
  "startsAt": "2025-06-01T00:00:00",
  "expiresAt": "2025-08-31T23:59:59",
  "isActive": true
}
```

---

### Get All Promo Codes (Admin)
```http
GET /v1/promo-codes?page=0&size=20
Authorization: Bearer {admin_token}
```

---

### Deactivate Promo Code (Admin)
```http
POST /v1/promo-codes/{id}/deactivate
Authorization: Bearer {admin_token}
```

---

## 11. Leaderboard & Social

### Get Global Leaderboard
```http
GET /v1/leaderboard?period=WEEKLY&page=0&size=50
Authorization: Bearer {token}
```

**Query Parameters:**
- `period`: `DAILY`, `WEEKLY`, `MONTHLY`, `ALL_TIME`
- `page`: Page number (default: 0)
- `size`: Page size (default: 50)

**Response (200 OK):**
```json
{
  "period": "WEEKLY",
  "currentUserRank": 45,
  "leaderboard": [
    {
      "rank": 1,
      "userId": 25,
      "username": "morse_master",
      "fullName": "John Doe",
      "profilePictureUrl": "https://example.com/avatars/25.jpg",
      "points": 5420,
      "level": 15,
      "streak": 30,
      "isCurrentUser": false
    },
    {
      "rank": 2,
      "userId": 87,
      "username": "code_ninja",
      "fullName": "Jane Smith",
      "profilePictureUrl": null,
      "points": 4850,
      "level": 14,
      "streak": 21,
      "isCurrentUser": false
    }
  ],
  "totalPlayers": 1250
}
```

---

### Get Friends Leaderboard
```http
GET /v1/leaderboard/friends?period=WEEKLY
Authorization: Bearer {token}
```

---

### Get Category Leaderboard
```http
GET /v1/leaderboard/categories/{categoryId}?period=WEEKLY
Authorization: Bearer {token}
```

---

### Search Users
```http
GET /v1/users/search?q=john&page=0&size=20
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "username": "john_doe",
      "fullName": "John Doe",
      "profilePictureUrl": "https://example.com/avatars/1.jpg",
      "level": 5,
      "totalPoints": 1250,
      "isFriend": false
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

---

### Send Friend Request (Future)
```http
POST /v1/friends/request
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": 25
}
```

---

### Accept Friend Request (Future)
```http
POST /v1/friends/accept/{requestId}
Authorization: Bearer {token}
```

---

### Get Friends List (Future)
```http
GET /v1/friends?page=0&size=20
Authorization: Bearer {token}
```

---

### Remove Friend (Future)
```http
DELETE /v1/friends/{userId}
Authorization: Bearer {token}
```

---

## 12. Health & System

### API Health Check
```http
GET /api/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2025-01-20T10:00:00",
  "service": "MorseMate API"
}
```

---

### Ping
```http
GET /api/ping
```

**Response (200 OK):**
```json
{
  "message": "pong"
}
```

---

### System Information
```http
GET /api/info
```

**Response (200 OK):**
```json
{
  "applicationName": "MorseMate",
  "version": "1.0.0",
  "environment": "production",
  "serverTime": "2025-01-20T10:00:00",
  "javaVersion": "21.0.8",
  "springBootVersion": "3.5.6"
}
```

---

### API Root
```http
GET /
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "message": "Welcome to MorseMate API - Morse Code Learning Platform",
  "version": "1.0.0",
  "endpoints": {
    "health": "/api/health",
    "auth-register": "/auth/register",
    "auth-login": "/auth/login",
    "categories": "/v1/categories",
    /* ... other endpoints */
  }
}
```

---

### API Documentation (JSON)
```http
GET /api-docs
Accept: application/json
```

---

### API Documentation (HTML)
```http
GET /api-docs
Accept: text/html
```

---

### Swagger UI
```http
GET /swagger-ui.html
```

---

## 📝 Common Response Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 204 | No Content | Successful request with no content |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Authentication required or failed |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate) |
| 422 | Unprocessable Entity | Validation error |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Service temporarily unavailable |

---

## 🔐 Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

Token expires after 24 hours (configurable). Use refresh token endpoint to get a new token.

---

## 📊 Pagination

Paginated endpoints support these query parameters:

- `page`: Page number (0-indexed)
- `size`: Number of items per page
- `sort`: Sort field and direction (e.g., `createdAt,desc`)

**Example:**
```http
GET /users?page=0&size=10&sort=createdAt,desc
```

---

## 🔍 Filtering & Search

Some endpoints support filtering:

- `q`: Search query
- `status`: Filter by status
- `category`: Filter by category
- `difficulty`: Filter by difficulty level

**Example:**
```http
GET /v1/lessons?category=1&difficulty=BEGINNER&q=letter
```

---

## 🌐 CORS

CORS is enabled for specified origins (configured in `application.properties`):

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
```

---

## 📱 Rate Limiting (Future)

Rate limiting will be implemented:

- **Unauthenticated**: 20 requests/minute
- **Authenticated Free**: 100 requests/minute
- **Authenticated Premium**: 500 requests/minute

Rate limit info in response headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642684800
```

---

## 🐛 Error Response Format

All errors return consistent JSON format:

```json
{
  "timestamp": "2025-01-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/auth/register",
  "errors": [
    {
      "field": "password",
      "message": "Password must contain at least one uppercase letter"
    }
  ]
}
```

---

## 🚀 Coming Soon

Features planned for future releases:

- ✅ Lessons Management (In Progress)
- ✅ Exercises System (In Progress)
- ⏳ Social Features (Friends, Chat)
- ⏳ Notifications System
- ⏳ Daily Challenges
- ⏳ Community Lessons
- ⏳ Offline Mode Support
- ⏳ Multi-language Support
- ⏳ Voice Commands
- ⏳ AR Visualization

---

**Last Updated:** January 20, 2025
**API Version:** 1.0.0
**Documentation Version:** 1.0

For more information, visit: https://docs.morsemate.com
