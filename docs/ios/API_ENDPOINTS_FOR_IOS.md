# MorseMate API Endpoints - iOS Implementation Guide

**Base URL:** `http://localhost:8080`
**Production URL:** `https://api.morsemate.com` (To be configured)

**Authentication:** JWT Bearer Token
**Content-Type:** `application/json`
**iOS Minimum:** iOS 15.0+

---

## 📋 Table of Contents

1. [Current Implementation Status](#current-implementation-status)
2. [Authentication Endpoints](#1-authentication-endpoints)
3. [User Management Endpoints](#2-user-management-endpoints)
4. [Category Endpoints](#3-category-endpoints)
5. [Lesson Endpoints](#4-lesson-endpoints-coming-soon)
6. [Exercise Endpoints](#5-exercise-endpoints-coming-soon)
7. [Progress Tracking Endpoints](#6-progress-tracking-endpoints-coming-soon)
8. [Achievement Endpoints](#7-achievement-endpoints-coming-soon)
9. [Subscription & Payment Endpoints](#8-subscription--payment-endpoints-coming-soon)
10. [Gems & Power-Ups Endpoints](#9-gems--power-ups-endpoints-coming-soon)
11. [Promo Code Endpoints](#10-promo-code-endpoints-coming-soon)
12. [Leaderboard & Social Endpoints](#11-leaderboard--social-endpoints-coming-soon)
13. [Health & System Endpoints](#12-health--system-endpoints)
14. [Error Codes & Handling](#error-codes--handling)
15. [iOS Implementation Notes](#ios-implementation-notes)

---

## Current Implementation Status

### ✅ Ready to Use (28 endpoints)
- Authentication (4 endpoints)
- User Management (14 endpoints)
- Categories (6 endpoints)
- Health & System (4 endpoints)

### ⏳ Coming Soon (50+ endpoints)
- Lessons (7 endpoints)
- Exercises (7 endpoints)
- Progress Tracking (4 endpoints)
- Achievements (4 endpoints)
- Subscription & Payments (8 endpoints)
- Gems & Power-Ups (8 endpoints)
- Promo Codes (5 endpoints)
- Leaderboard & Social (8+ endpoints)

---

## 1. Authentication Endpoints

### 1.1 Register New User ✅

**Endpoint:** `POST /auth/register`
**Authentication:** None
**Status:** ✅ Implemented

**Swift Request:**
```swift
struct RegisterRequest: Codable {
    let username: String        // 3-50 chars, alphanumeric + _ -
    let email: String          // Valid email format
    let password: String       // Min 8 chars, 1 uppercase, 1 lowercase, 1 number
    let fullName: String?      // Optional, max 100 chars
}

let request = RegisterRequest(
    username: "john_doe",
    email: "john@example.com",
    password: "SecurePass123",
    fullName: "John Doe"
)

let response = try await api.register(request: request)
```

**Response:**
```swift
struct AuthResponse: Codable {
    let token: String           // JWT token
    let expiresIn: Int         // Expiration in seconds (86400 = 24h)
    let user: User
}
```

**Success Response (201 Created):**
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
    "currentStreak": 0,
    "longestStreak": 0,
    "isActive": true,
    "emailVerified": false,
    "createdAt": "2025-01-20T10:00:00"
  }
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors
- `409 Conflict` - Username or email already exists

**iOS Notes:**
- Save token to Keychain (recommended) or UserDefaults
- Token auto-saved by MorseMateAPI.shared
- Validate password on client side before sending

---

### 1.2 Login ✅

**Endpoint:** `POST /auth/login`
**Authentication:** None
**Status:** ✅ Implemented

**Swift Request:**
```swift
struct LoginRequest: Codable {
    let identifier: String     // Email or username
    let password: String
    let rememberMe: Bool?      // Optional, default false
}

let request = LoginRequest(
    identifier: "john@example.com",
    password: "SecurePass123",
    rememberMe: true
)

let response = try await api.login(request: request)
```

**Response:** Same as Register (AuthResponse)

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400,
  "user": { /* User object */ }
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `400 Bad Request` - Missing required fields

**iOS Notes:**
- Handle biometric authentication (Face ID/Touch ID) after successful login
- Store rememberMe preference for auto-login
- Update last login timestamp

---

### 1.3 Get Current User ✅

**Endpoint:** `GET /auth/me`
**Authentication:** Required (Bearer Token)
**Status:** ✅ Implemented

**Swift Request:**
```swift
let user = try await api.getCurrentUser()
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "profilePictureUrl": null,
  "level": 5,
  "hearts": 5,
  "maxHearts": 5,
  "totalPoints": 1250,
  "currentStreak": 7,
  "longestStreak": 15,
  "isActive": true,
  "emailVerified": true,
  "createdAt": "2025-01-15T10:00:00",
  "lastLogin": "2025-01-20T10:00:00"
}
```

**Error Responses:**
- `401 Unauthorized` - Token expired or invalid

**iOS Notes:**
- Call this on app launch to validate token
- Update local user state in ViewModel
- Handle token expiration gracefully

---

### 1.4 Logout ⏳

**Endpoint:** `POST /auth/logout`
**Authentication:** Required
**Status:** ⏳ Planned

**Swift Request:**
```swift
try await api.logout()
// Clear local token
api.clearToken()
```

**iOS Notes:**
- Clear Keychain/UserDefaults
- Reset app state
- Navigate to login screen

---

### 1.5 Refresh Token ⏳

**Endpoint:** `POST /auth/refresh`
**Authentication:** Refresh token required
**Status:** ⏳ Planned

**Swift Request:**
```swift
struct RefreshTokenRequest: Codable {
    let refreshToken: String
}
```

---

### 1.6 Forgot Password ⏳

**Endpoint:** `POST /auth/forgot-password`
**Status:** ⏳ Planned

**Swift Request:**
```swift
struct ForgotPasswordRequest: Codable {
    let email: String
}
```

---

### 1.7 Reset Password ⏳

**Endpoint:** `POST /auth/reset-password`
**Status:** ⏳ Planned

**Swift Request:**
```swift
struct ResetPasswordRequest: Codable {
    let token: String          // From email
    let newPassword: String
}
```

---

### 1.8 Verify Email ⏳

**Endpoint:** `POST /auth/verify-email`
**Status:** ⏳ Planned

---

## 2. User Management Endpoints

### 2.1 Get User Profile ✅

**Endpoint:** `GET /users/me`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let user = try await api.getUserProfile()
```

**Response:** User object (same as auth endpoints)

---

### 2.2 Update Profile ✅

**Endpoint:** `PUT /users/me`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
struct UpdateProfileRequest: Codable {
    let fullName: String?
    let username: String?           // 3-50 chars
    let profilePictureUrl: String?  // Max 255 chars
}

let request = UpdateProfileRequest(
    fullName: "John Doe Updated",
    username: "john_doe_new",
    profilePictureUrl: "https://example.com/avatar.jpg"
)

let user = try await api.updateProfile(request: request)
```

**Success Response (200 OK):** User object

**Error Responses:**
- `400 Bad Request` - Validation errors
- `409 Conflict` - Username already taken

---

### 2.3 Get User By ID ✅

**Endpoint:** `GET /users/{id}`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let user = try await api.getUserById(id: 1)
```

---

### 2.4 Get User By Username ✅

**Endpoint:** `GET /users/username/{username}`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let user = try await api.getUserByUsername(username: "john_doe")
```

---

### 2.5 Get All Users (Paginated) ✅

**Endpoint:** `GET /users?page={page}&size={size}&sort={sort}`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let users = try await api.getAllUsers(
    page: 0,
    size: 10,
    sort: "createdAt,desc"
)
```

**Response:**
```swift
struct PaginatedResponse<T: Codable>: Codable {
    let content: [T]
    let pageable: Pageable
    let totalElements: Int
    let totalPages: Int
    let last: Bool
    let first: Bool
}
```

---

### 2.6 Get User Statistics ✅

**Endpoint:** `GET /users/me/statistics`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let stats = try await api.getUserStatistics()
```

**Success Response (200 OK):**
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

**iOS Notes:**
- Update UI with charts/graphs
- Cache statistics locally
- Refresh periodically

---

### 2.7 Use Heart ✅

**Endpoint:** `POST /users/me/hearts/use`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let response = try await api.useHeart()
```

**Success Response (200 OK):**
```json
{
  "message": "Heart used successfully",
  "remainingHearts": 4
}
```

**Error Responses:**
- `400 Bad Request` - No hearts available

**iOS Notes:**
- Update hearts count in UI immediately
- Show animation for heart usage
- Offer heart refill options when 0

---

### 2.8 Refill Hearts ✅

**Endpoint:** `POST /users/me/hearts/refill`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let response = try await api.refillHearts()
```

**Success Response (200 OK):**
```json
{
  "message": "Hearts refilled successfully",
  "hearts": 5
}
```

**iOS Notes:**
- Hearts refill automatically after time
- Premium users may have unlimited hearts
- Show countdown timer for next refill

---

### 2.9 Add Points to User (Admin) ✅

**Endpoint:** `POST /users/{id}/points?points={points}`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

**Swift Request:**
```swift
// Admin only
let response = try await api.addPoints(userId: 1, points: 100)
```

---

### 2.10 Deactivate User (Admin) ✅

**Endpoint:** `POST /users/{id}/deactivate`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

### 2.11 Activate User (Admin) ✅

**Endpoint:** `POST /users/{id}/activate`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

### 2.12 Verify Email (Admin) ✅

**Endpoint:** `POST /users/{id}/verify-email`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

### 2.13 Delete Account ✅

**Endpoint:** `DELETE /users/me`
**Authentication:** Required
**Status:** ✅ Implemented

**Swift Request:**
```swift
let response = try await api.deleteAccount()
```

**iOS Notes:**
- Show confirmation alert
- Clear all local data
- Navigate to welcome screen

---

### 2.14 Upload Profile Picture ⏳

**Endpoint:** `POST /users/me/profile-picture`
**Authentication:** Required
**Status:** ⏳ Planned
**Content-Type:** `multipart/form-data`

**iOS Implementation:**
```swift
func uploadProfilePicture(image: UIImage) async throws -> URL {
    guard let imageData = image.jpegData(compressionQuality: 0.8) else {
        throw APIError.invalidData
    }

    // Implementation will be provided when endpoint is ready
}
```

---

### 2.15 Change Password ⏳

**Endpoint:** `PUT /users/me/password`
**Authentication:** Required
**Status:** ⏳ Planned

**Swift Request:**
```swift
struct ChangePasswordRequest: Codable {
    let currentPassword: String
    let newPassword: String
}
```

---

## 3. Category Endpoints

### 3.1 Get All Active Categories ✅

**Endpoint:** `GET /v1/categories`
**Authentication:** None (Public)
**Status:** ✅ Implemented

**Swift Request:**
```swift
let categories = try await api.getAllCategories()
```

**Success Response (200 OK):**
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
  },
  {
    "id": 2,
    "name": "Numbers",
    "description": "Learn Morse code for numbers 0-9",
    "displayOrder": 2,
    "iconUrl": "https://example.com/icons/numbers.png",
    "lessonCount": 10,
    "completedLessons": 0,
    "isActive": true
  }
]
```

**Swift Model:**
```swift
struct Category: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let displayOrder: Int
    let iconUrl: String?
    let lessonCount: Int
    let completedLessons: Int?
    let isActive: Bool
}
```

**iOS Notes:**
- Cache categories locally for offline access
- Use LazyVGrid for category display
- Load icons asynchronously with AsyncImage
- Sort by displayOrder

---

### 3.2 Get Category By ID ✅

**Endpoint:** `GET /v1/categories/{id}`
**Authentication:** None (Public)
**Status:** ✅ Implemented

**Swift Request:**
```swift
let category = try await api.getCategoryById(id: 1)
```

**iOS Notes:**
- Pre-fetch category details when user taps
- Show loading state while fetching

---

### 3.3 Create Category (Admin) ✅

**Endpoint:** `POST /v1/categories`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

**Swift Request:**
```swift
struct CreateCategoryRequest: Codable {
    let name: String
    let description: String?
    let displayOrder: Int?
    let iconUrl: String?
    let isActive: Bool?
}

let request = CreateCategoryRequest(
    name: "Punctuation",
    description: "Learn Morse code for punctuation marks",
    displayOrder: 3,
    iconUrl: "https://example.com/icons/punctuation.png",
    isActive: true
)

let category = try await api.createCategory(request: request)
```

---

### 3.4 Update Category (Admin) ✅

**Endpoint:** `PUT /v1/categories/{id}`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

### 3.5 Delete Category (Admin) ✅

**Endpoint:** `DELETE /v1/categories/{id}`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

### 3.6 Get All Categories Including Inactive (Admin) ✅

**Endpoint:** `GET /v1/categories/admin/all`
**Authentication:** Required (Admin)
**Status:** ✅ Implemented

---

## 4. Lesson Endpoints (Coming Soon)

### 4.1 Get Lessons By Category ⏳

**Endpoint:** `GET /v1/categories/{categoryId}/lessons`
**Authentication:** Required
**Status:** ⏳ Coming Soon (Next Release)

**Swift Request:**
```swift
let lessons = try await api.getLessonsByCategory(categoryId: 1)
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "categoryId": 1,
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
    "userProgress": {
      "completionPercentage": 100,
      "lastAttemptDate": "2025-01-20T10:00:00",
      "attempts": 3
    }
  }
]
```

**Swift Model:**
```swift
enum Difficulty: String, Codable {
    case beginner = "BEGINNER"
    case intermediate = "INTERMEDIATE"
    case advanced = "ADVANCED"
}

struct Lesson: Codable, Identifiable {
    let id: Int
    let categoryId: Int
    let categoryName: String?
    let title: String
    let description: String
    let morseCode: String
    let audioUrl: String?
    let videoUrl: String?
    let imageUrl: String?
    let difficulty: Difficulty
    let displayOrder: Int
    let estimatedDuration: Int
    let requiredPointsToUnlock: Int
    let isLocked: Bool
    let isCompleted: Bool
    let userProgress: UserProgress?
}

struct UserProgress: Codable {
    let completionPercentage: Int
    let lastAttemptDate: String
    let attempts: Int
}
```

**iOS Notes:**
- Show locked lessons with lock icon
- Display progress bar for each lesson
- Play audio with AVPlayer
- Cache lesson data

---

### 4.2 Get Lesson By ID ⏳

**Endpoint:** `GET /v1/lessons/{id}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
let lesson = try await api.getLessonById(id: 1)
```

**iOS Notes:**
- Pre-load next lesson for smooth navigation
- Cache audio files for offline use

---

### 4.3 Create Lesson (Admin) ⏳

**Endpoint:** `POST /v1/lessons`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

### 4.4 Update Lesson (Admin) ⏳

**Endpoint:** `PUT /v1/lessons/{id}`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

### 4.5 Delete Lesson (Admin) ⏳

**Endpoint:** `DELETE /v1/lessons/{id}`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

### 4.6 Mark Lesson as Completed ⏳

**Endpoint:** `POST /v1/lessons/{id}/complete`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
struct MarkLessonCompletedRequest: Codable {
    let timeSpent: Int        // Seconds
    let accuracy: Double      // Percentage
}

let request = MarkLessonCompletedRequest(
    timeSpent: 450,
    accuracy: 95.5
)

let response = try await api.markLessonCompleted(id: 1, request: request)
```

**Expected Response:**
```json
{
  "message": "Lesson completed successfully",
  "pointsEarned": 50,
  "newTotalPoints": 1300,
  "levelUp": false,
  "achievements": []
}
```

**iOS Notes:**
- Track time automatically in background
- Calculate accuracy from exercise results
- Show celebration animation on completion
- Update points with animation

---

### 4.7 Reset Lesson Progress ⏳

**Endpoint:** `POST /v1/lessons/{id}/reset`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

## 5. Exercise Endpoints (Coming Soon)

### 5.1 Get Exercises By Lesson ⏳

**Endpoint:** `GET /v1/lessons/{lessonId}/exercises`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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

**Swift Model:**
```swift
enum ExerciseType: String, Codable {
    case listening = "LISTENING"
    case typing = "TYPING"
    case multipleChoice = "MULTIPLE_CHOICE"
    case matching = "MATCHING"
}

struct Exercise: Codable, Identifiable {
    let id: Int
    let lessonId: Int
    let title: String
    let description: String
    let type: ExerciseType
    let difficulty: Difficulty
    let displayOrder: Int
    let questionCount: Int?
    let timeLimit: Int
    let passingScore: Int
    let isCompleted: Bool
    let bestScore: Int?
    let attempts: Int?
}
```

---

### 5.2 Get Exercise By ID ⏳

**Endpoint:** `GET /v1/exercises/{id}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
```json
{
  "id": 1,
  "lessonId": 1,
  "title": "Listening Exercise",
  "description": "Listen to the Morse code and identify the letter",
  "type": "LISTENING",
  "difficulty": "BEGINNER",
  "displayOrder": 1,
  "instructions": "Listen carefully to each Morse code sequence",
  "questions": [
    {
      "id": 1,
      "question": "What letter does this represent?",
      "audioUrl": "https://example.com/audio/q1.mp3",
      "imageUrl": null,
      "options": ["A", "B", "C", "D"],
      "correctAnswer": "A",
      "explanation": "This is the Morse code for letter A: .-"
    }
  ],
  "timeLimit": 60,
  "passingScore": 70
}
```

**Swift Model:**
```swift
struct ExerciseDetail: Codable {
    let id: Int
    let lessonId: Int
    let title: String
    let description: String
    let type: ExerciseType
    let difficulty: Difficulty
    let displayOrder: Int
    let instructions: String
    let questions: [ExerciseQuestion]
    let timeLimit: Int
    let passingScore: Int
    let isCompleted: Bool
    let bestScore: Int?
    let attempts: Int?
}

struct ExerciseQuestion: Codable, Identifiable {
    let id: Int
    let question: String
    let audioUrl: String?
    let imageUrl: String?
    let options: [String]
    let correctAnswer: String
    let explanation: String?
}
```

---

### 5.3 Submit Exercise Attempt ⏳

**Endpoint:** `POST /v1/exercises/{id}/attempt`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
struct ExerciseAttemptRequest: Codable {
    let answers: [Answer]
    let totalTimeSpent: Int

    struct Answer: Codable {
        let questionId: Int
        let selectedAnswer: String
        let timeSpent: Int
    }
}

let request = ExerciseAttemptRequest(
    answers: [
        .init(questionId: 1, selectedAnswer: "A", timeSpent: 5),
        .init(questionId: 2, selectedAnswer: "B", timeSpent: 4)
    ],
    totalTimeSpent: 45
)

let result = try await api.submitExerciseAttempt(id: 1, request: request)
```

**Expected Response:**
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
      "explanation": "Correct! This is letter A: .-"
    }
  ]
}
```

**iOS Notes:**
- Implement timer in ViewModel
- Track time per question
- Auto-submit when time expires
- Show results with animations
- Display achievements earned

---

### 5.4 Get Exercise Attempts History ⏳

**Endpoint:** `GET /v1/exercises/{id}/attempts?page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

## 6. Progress Tracking Endpoints (Coming Soon)

### 6.1 Get Overall Progress ⏳

**Endpoint:** `GET /v1/progress`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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

**iOS Notes:**
- Display with charts (SwiftUI Charts)
- Show progress bars
- Update in real-time

---

### 6.2 Get Category Progress ⏳

**Endpoint:** `GET /v1/progress/categories/{categoryId}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 6.3 Get Lesson Progress ⏳

**Endpoint:** `GET /v1/progress/lessons/{lessonId}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 6.4 Update Daily Streak ⏳

**Endpoint:** `POST /v1/progress/streak`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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

**iOS Notes:**
- Update streak daily at midnight
- Send local notification for streak reminders
- Show flame emoji animation

---

## 7. Achievement Endpoints (Coming Soon)

### 7.1 Get All Achievements ⏳

**Endpoint:** `GET /v1/achievements`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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
  }
]
```

**Swift Model:**
```swift
enum AchievementCategory: String, Codable {
    case progress = "PROGRESS"
    case streak = "STREAK"
    case social = "SOCIAL"
    case special = "SPECIAL"
}

enum Rarity: String, Codable {
    case common = "COMMON"
    case rare = "RARE"
    case epic = "EPIC"
    case legendary = "LEGENDARY"
}

struct Achievement: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let iconUrl: String?
    let category: AchievementCategory
    let rarity: Rarity
    let points: Int
    let isUnlocked: Bool
    let unlockedAt: String?
    let progress: Progress?

    struct Progress: Codable {
        let current: Int
        let target: Int
        let percentage: Double
    }
}
```

**iOS Notes:**
- Show achievements grid
- Animate unlocked achievements
- Display progress for locked achievements
- Use different colors for rarity levels

---

### 7.2 Get User Achievements ⏳

**Endpoint:** `GET /v1/achievements/me`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 7.3 Get Achievement By ID ⏳

**Endpoint:** `GET /v1/achievements/{id}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 7.4 Create Achievement (Admin) ⏳

**Endpoint:** `POST /v1/achievements`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

## 8. Subscription & Payment Endpoints (Coming Soon)

### 8.1 Get Subscription Plans ⏳

**Endpoint:** `GET /v1/subscription/plans`
**Authentication:** None (Public)
**Status:** ⏳ Coming Soon

**Expected Response:**
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

**iOS Notes:**
- Integrate with StoreKit 2
- Handle in-app purchases
- Restore purchases
- Validate receipts

---

### 8.2 Get Current Subscription ⏳

**Endpoint:** `GET /v1/subscription/me`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 8.3 Subscribe to Plan ⏳

**Endpoint:** `POST /v1/subscription/subscribe`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
struct SubscribeRequest: Codable {
    let planId: Int
    let paymentMethodId: String    // From StoreKit
    let promoCode: String?
}
```

---

### 8.4 Cancel Subscription ⏳

**Endpoint:** `POST /v1/subscription/cancel`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 8.5 Resume Subscription ⏳

**Endpoint:** `POST /v1/subscription/resume`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 8.6 Get Payment History ⏳

**Endpoint:** `GET /v1/payments?page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 8.7 Create Payment Intent ⏳

**Endpoint:** `POST /v1/payments/create-intent`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 8.8 Webhook Handler ⏳

**Endpoint:** `POST /v1/payments/webhook`
**Authentication:** Stripe signature
**Status:** ⏳ Coming Soon
**Note:** Backend only, not used directly from iOS

---

## 9. Gems & Power-Ups Endpoints (Coming Soon)

### 9.1 Get Gems Balance ⏳

**Endpoint:** `GET /v1/gems/balance`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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

### 9.2 Get Gem Packages ⏳

**Endpoint:** `GET /v1/gems/packages`
**Authentication:** None (Public)
**Status:** ⏳ Coming Soon

**Expected Response:**
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

**iOS Notes:**
- Integrate with StoreKit
- Show best value badge
- Highlight bonus gems

---

### 9.3 Purchase Gems ⏳

**Endpoint:** `POST /v1/gems/purchase`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 9.4 Get Gem Transaction History ⏳

**Endpoint:** `GET /v1/gems/transactions?page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 9.5 Get All Power-Ups ⏳

**Endpoint:** `GET /v1/power-ups`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Expected Response:**
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
  }
]
```

---

### 9.6 Get User Power-Ups ⏳

**Endpoint:** `GET /v1/power-ups/me`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 9.7 Purchase Power-Up ⏳

**Endpoint:** `POST /v1/power-ups/{id}/purchase`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 9.8 Use Power-Up ⏳

**Endpoint:** `POST /v1/power-ups/{id}/use`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

## 10. Promo Code Endpoints (Coming Soon)

### 10.1 Validate Promo Code ⏳

**Endpoint:** `POST /v1/promo-codes/validate`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
struct ValidatePromoCodeRequest: Codable {
    let code: String
}
```

**Expected Response:**
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

### 10.2 Apply Promo Code ⏳

**Endpoint:** `POST /v1/promo-codes/apply`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 10.3 Create Promo Code (Admin) ⏳

**Endpoint:** `POST /v1/promo-codes`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

### 10.4 Get All Promo Codes (Admin) ⏳

**Endpoint:** `GET /v1/promo-codes`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

### 10.5 Deactivate Promo Code (Admin) ⏳

**Endpoint:** `POST /v1/promo-codes/{id}/deactivate`
**Authentication:** Required (Admin)
**Status:** ⏳ Coming Soon

---

## 11. Leaderboard & Social Endpoints (Coming Soon)

### 11.1 Get Global Leaderboard ⏳

**Endpoint:** `GET /v1/leaderboard?period={period}&page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Query Parameters:**
- `period`: `DAILY`, `WEEKLY`, `MONTHLY`, `ALL_TIME`
- `page`: Page number (default: 0)
- `size`: Items per page (default: 50)

**Expected Response:**
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
    }
  ],
  "totalPlayers": 1250
}
```

**Swift Model:**
```swift
enum LeaderboardPeriod: String {
    case daily = "DAILY"
    case weekly = "WEEKLY"
    case monthly = "MONTHLY"
    case allTime = "ALL_TIME"
}

struct LeaderboardResponse: Codable {
    let period: String
    let currentUserRank: Int
    let leaderboard: [LeaderboardEntry]
    let totalPlayers: Int
}

struct LeaderboardEntry: Codable, Identifiable {
    let rank: Int
    let userId: Int
    let username: String
    let fullName: String?
    let profilePictureUrl: String?
    let points: Int
    let level: Int
    let streak: Int
    let isCurrentUser: Bool

    var id: Int { userId }
}
```

**iOS Notes:**
- Use List with LazyVStack for performance
- Highlight current user
- Add pull-to-refresh
- Cache leaderboard data

---

### 11.2 Get Friends Leaderboard ⏳

**Endpoint:** `GET /v1/leaderboard/friends?period={period}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 11.3 Get Category Leaderboard ⏳

**Endpoint:** `GET /v1/leaderboard/categories/{categoryId}?period={period}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 11.4 Search Users ⏳

**Endpoint:** `GET /v1/users/search?q={query}&page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

**Swift Request:**
```swift
let users = try await api.searchUsers(
    query: "john",
    page: 0,
    size: 20
)
```

---

### 11.5 Send Friend Request ⏳

**Endpoint:** `POST /v1/friends/request`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 11.6 Accept Friend Request ⏳

**Endpoint:** `POST /v1/friends/accept/{requestId}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 11.7 Get Friends List ⏳

**Endpoint:** `GET /v1/friends?page={page}&size={size}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

### 11.8 Remove Friend ⏳

**Endpoint:** `DELETE /v1/friends/{userId}`
**Authentication:** Required
**Status:** ⏳ Coming Soon

---

## 12. Health & System Endpoints

### 12.1 API Health Check ✅

**Endpoint:** `GET /api/health`
**Authentication:** None
**Status:** ✅ Implemented

**Swift Request:**
```swift
let health = try await api.healthCheck()
```

**Success Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2025-01-20T10:00:00",
  "service": "MorseMate API"
}
```

**iOS Notes:**
- Call on app launch to check connectivity
- Show offline banner if fails

---

### 12.2 Ping ✅

**Endpoint:** `GET /api/ping`
**Authentication:** None
**Status:** ✅ Implemented

**Success Response (200 OK):**
```json
{
  "message": "pong"
}
```

---

### 12.3 System Information ✅

**Endpoint:** `GET /api/info`
**Authentication:** None
**Status:** ✅ Implemented

**Success Response (200 OK):**
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

### 12.4 API Root ✅

**Endpoint:** `GET /`
**Authentication:** Required (redirects if not authenticated)
**Status:** ✅ Implemented

**Success Response (200 OK):**
```json
{
  "message": "Welcome to MorseMate API",
  "version": "1.0.0",
  "endpoints": {
    "health": "/api/health",
    "auth-register": "/auth/register",
    "auth-login": "/auth/login",
    "categories": "/v1/categories"
  }
}
```

---

## Error Codes & Handling

### HTTP Status Codes

| Code | Status | Description | iOS Action |
|------|--------|-------------|------------|
| 200 | OK | Success | Process response |
| 201 | Created | Resource created | Process response |
| 204 | No Content | Success with no content | Show success message |
| 400 | Bad Request | Invalid parameters | Show validation errors |
| 401 | Unauthorized | Authentication required | Redirect to login |
| 403 | Forbidden | Insufficient permissions | Show permission error |
| 404 | Not Found | Resource not found | Show not found message |
| 409 | Conflict | Resource conflict | Show conflict error |
| 422 | Unprocessable | Validation error | Show validation errors |
| 429 | Too Many Requests | Rate limit exceeded | Show retry later |
| 500 | Server Error | Internal server error | Show generic error |
| 503 | Service Unavailable | Service down | Show maintenance message |

---

### Error Response Format

```json
{
  "timestamp": "2025-01-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/auth/register",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

---

### iOS Error Handling

```swift
do {
    let response = try await api.login(request: loginRequest)
} catch let error as APIError {
    switch error {
    case .unauthorized:
        // Redirect to login
        showLoginScreen()

    case .validationError(let errors):
        // Show validation errors
        for error in errors {
            showAlert(title: error.field, message: error.message)
        }

    case .serverError(let code, let message):
        // Show error message
        showAlert(title: "Error", message: message)

    case .networkError(let error):
        // Show network error
        showAlert(title: "Network Error", message: error.localizedDescription)

    default:
        // Generic error
        showAlert(title: "Error", message: "Something went wrong")
    }
}
```

---

## iOS Implementation Notes

### 1. Authentication

```swift
// Save token to Keychain (recommended)
import Security

func saveToken(_ token: String) {
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: "jwt_token",
        kSecValueData as String: token.data(using: .utf8)!
    ]

    SecItemDelete(query as CFDictionary)
    SecItemAdd(query as CFDictionary, nil)
}

// Or use UserDefaults (less secure)
UserDefaults.standard.set(token, forKey: "jwt_token")
```

---

### 2. Token Management

```swift
// Token is automatically managed by MorseMateAPI
let api = MorseMateAPI.shared

// Token is auto-loaded on init
// Token is auto-saved on login/register
// Token is auto-cleared on logout

// Manual token access if needed
api.setToken("your_token")
api.clearToken()
```

---

### 3. Offline Support

```swift
// Cache data locally
class CacheManager {
    static let shared = CacheManager()

    func cacheCategories(_ categories: [Category]) {
        // Save to UserDefaults or CoreData
    }

    func getCachedCategories() -> [Category]? {
        // Load from cache
    }
}

// Use cached data when offline
Task {
    do {
        let categories = try await api.getAllCategories()
        CacheManager.shared.cacheCategories(categories)
    } catch {
        // Use cached data if available
        if let cached = CacheManager.shared.getCachedCategories() {
            self.categories = cached
        }
    }
}
```

---

### 4. Audio Playback

```swift
import AVFoundation

class AudioPlayer {
    private var player: AVAudioPlayer?

    func playMorseCode(url: String) async {
        guard let audioURL = URL(string: url) else { return }

        do {
            let (data, _) = try await URLSession.shared.data(from: audioURL)
            player = try AVAudioPlayer(data: data)
            player?.play()
        } catch {
            print("Audio playback failed: \(error)")
        }
    }
}
```

---

### 5. Image Loading

```swift
// Use AsyncImage (iOS 15+)
AsyncImage(url: URL(string: category.iconUrl ?? "")) { image in
    image
        .resizable()
        .aspectRatio(contentMode: .fit)
} placeholder: {
    ProgressView()
}
.frame(width: 60, height: 60)

// Or use SDWebImage for better caching
```

---

### 6. Progress Tracking

```swift
class ExerciseTimer: ObservableObject {
    @Published var timeElapsed: Int = 0
    private var timer: Timer?

    func start() {
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            self.timeElapsed += 1
        }
    }

    func stop() {
        timer?.invalidate()
        timer = nil
    }
}
```

---

### 7. Network Monitoring

```swift
import Network

class NetworkMonitor: ObservableObject {
    @Published var isConnected = true
    private let monitor = NWPathMonitor()

    init() {
        monitor.pathUpdateHandler = { path in
            DispatchQueue.main.async {
                self.isConnected = path.status == .satisfied
            }
        }
        monitor.start(queue: DispatchQueue.global())
    }
}
```

---

### 8. Push Notifications

```swift
// Request permission
import UserNotifications

func requestNotificationPermission() {
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
        if granted {
            DispatchQueue.main.async {
                UIApplication.shared.registerForRemoteNotifications()
            }
        }
    }
}

// Handle notifications for:
// - Streak reminders
// - Heart refill
// - Friend requests
// - Achievement unlocked
```

---

### 9. StoreKit Integration

```swift
import StoreKit

class SubscriptionManager: ObservableObject {
    @Published var subscriptionStatus: SubscriptionStatus = .free

    func purchase(planId: String) async {
        // StoreKit 2 implementation
        // Verify receipt with backend
    }
}
```

---

### 10. Error Logging

```swift
// Use OSLog for error logging
import os.log

extension OSLog {
    static let api = OSLog(subsystem: "com.morsemate.app", category: "API")
}

// Log errors
os_log("API Error: %{public}@", log: .api, type: .error, error.localizedDescription)

// Or use Firebase Crashlytics
// Crashlytics.crashlytics().record(error: error)
```

---

## Summary for iOS Team

### ✅ Ready Now (28 endpoints)
Start implementing with these endpoints:
1. **Authentication** - Login, Register, Get Current User
2. **User Profile** - Get, Update, Statistics
3. **Categories** - List, Details
4. **Hearts System** - Use, Refill

### ⏳ Coming Next Release (7 endpoints)
Prepare UI for these features:
1. **Lessons** - List, Details, Complete
2. **Basic Exercises** - List, Attempt

### ⏳ Coming Later (50+ endpoints)
Plan UI/UX for these features:
1. **Progress Tracking**
2. **Achievements**
3. **Subscription & Payments**
4. **Leaderboard**
5. **Social Features**

---

## Contact & Support

**Questions about endpoints?**
- Contact: Backend Team
- Slack: #api-support
- Email: backend@morsemate.com

**Documentation updates?**
- This document will be updated as new endpoints are released
- Check version number at top of document
- Subscribe to #api-updates channel

---

**Document Version:** 1.0.0
**Last Updated:** January 20, 2025
**Next Update:** February 2025 (Lessons & Exercises)

**Total Endpoints:** 78 (28 live, 50 planned)

---

**Happy Coding! 🚀**
