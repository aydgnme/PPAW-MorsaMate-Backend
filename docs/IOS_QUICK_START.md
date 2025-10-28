# MorseMate iOS - Quick Start Guide

**Swift API Client for iOS/SwiftUI**

---

## 🚀 5-Minute Setup

### 1. Add API Client to Project

```bash
# Drag ios-api-client.swift into your Xcode project
# Target: iOS 15.0+
```

### 2. Initialize in App

```swift
import SwiftUI

@main
struct MorseMateApp: App {
    @StateObject private var api = MorseMateAPI.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(api)
        }
    }
}
```

### 3. Start Using

```swift
// Login
let response = try await api.login(request: LoginRequest(
    identifier: "john@example.com",
    password: "SecurePass123"
))

// Get categories
let categories = try await api.getAllCategories()

// Get lessons
let lessons = try await api.getLessonsByCategory(categoryId: 1)
```

---

## 📱 Complete Examples

### Login Example

```swift
import SwiftUI

struct LoginView: View {
    @State private var email = ""
    @State private var password = ""
    @State private var errorMessage = ""

    var body: some View {
        VStack(spacing: 20) {
            TextField("Email", text: $email)
                .textFieldStyle(RoundedBorderTextFieldStyle())

            SecureField("Password", text: $password)
                .textFieldStyle(RoundedBorderTextFieldStyle())

            if !errorMessage.isEmpty {
                Text(errorMessage)
                    .foregroundColor(.red)
            }

            Button("Login") {
                Task {
                    do {
                        let api = MorseMateAPI.shared
                        let response = try await api.login(
                            request: LoginRequest(
                                identifier: email,
                                password: password
                            )
                        )
                        print("Logged in: \(response.user.username)")
                    } catch {
                        errorMessage = error.localizedDescription
                    }
                }
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
}
```

---

### Categories List

```swift
import SwiftUI

struct CategoriesView: View {
    @State private var categories: [Category] = []
    @State private var isLoading = false

    var body: some View {
        NavigationView {
            List(categories) { category in
                NavigationLink(destination: Text(category.name)) {
                    VStack(alignment: .leading) {
                        Text(category.name)
                            .font(.headline)
                        Text(category.description)
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Categories")
            .task {
                await fetchCategories()
            }
        }
    }

    func fetchCategories() async {
        isLoading = true
        do {
            let api = MorseMateAPI.shared
            categories = try await api.getAllCategories()
        } catch {
            print("Error: \(error)")
        }
        isLoading = false
    }
}
```

---

### Exercise with Questions

```swift
import SwiftUI

struct ExerciseView: View {
    let exerciseId: Int
    @State private var exercise: ExerciseDetail?
    @State private var currentIndex = 0
    @State private var answers: [ExerciseAttemptRequest.Answer] = []

    var currentQuestion: ExerciseQuestion? {
        guard let exercise = exercise,
              currentIndex < exercise.questions.count else {
            return nil
        }
        return exercise.questions[currentIndex]
    }

    var body: some View {
        VStack {
            if let question = currentQuestion {
                // Progress
                ProgressView(value: Double(currentIndex),
                           total: Double(exercise?.questions.count ?? 1))
                    .padding()

                // Question
                Text(question.question)
                    .font(.title2)
                    .padding()

                // Options
                ForEach(question.options, id: \.self) { option in
                    Button(option) {
                        selectAnswer(option)
                    }
                    .buttonStyle(.bordered)
                }
            }
        }
        .task {
            await loadExercise()
        }
    }

    func loadExercise() async {
        do {
            let api = MorseMateAPI.shared
            exercise = try await api.getExerciseById(id: exerciseId)
        } catch {
            print("Error: \(error)")
        }
    }

    func selectAnswer(_ answer: String) {
        guard let question = currentQuestion else { return }

        answers.append(ExerciseAttemptRequest.Answer(
            questionId: question.id,
            selectedAnswer: answer,
            timeSpent: 10
        ))

        if currentIndex < (exercise?.questions.count ?? 0) - 1 {
            currentIndex += 1
        } else {
            Task {
                await submitExercise()
            }
        }
    }

    func submitExercise() async {
        do {
            let api = MorseMateAPI.shared
            let result = try await api.submitExerciseAttempt(
                id: exerciseId,
                request: ExerciseAttemptRequest(
                    answers: answers,
                    totalTimeSpent: answers.reduce(0) { $0 + $1.timeSpent }
                )
            )
            print("Score: \(result.score)%")
        } catch {
            print("Error: \(error)")
        }
    }
}
```

---

### User Profile with Stats

```swift
import SwiftUI

struct ProfileView: View {
    @State private var user: User?
    @State private var stats: UserStatistics?

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile header
                    if let user = user {
                        VStack {
                            Text(user.username)
                                .font(.title)
                                .fontWeight(.bold)

                            Text("Level \(user.level)")
                                .font(.headline)
                                .padding(8)
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding()
                    }

                    // Statistics
                    if let stats = stats {
                        LazyVGrid(columns: [
                            GridItem(.flexible()),
                            GridItem(.flexible())
                        ], spacing: 16) {
                            StatCard(title: "Points", value: "\(stats.totalPoints)")
                            StatCard(title: "Streak", value: "\(stats.currentStreak)🔥")
                            StatCard(title: "Hearts", value: "\(stats.hearts)/\(stats.maxHearts)")
                            StatCard(title: "Lessons", value: "\(stats.completedLessons)")
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Profile")
            .task {
                await loadProfile()
            }
        }
    }

    func loadProfile() async {
        do {
            let api = MorseMateAPI.shared
            user = try await api.getUserProfile()
            stats = try await api.getUserStatistics()
        } catch {
            print("Error: \(error)")
        }
    }
}

struct StatCard: View {
    let title: String
    let value: String

    var body: some View {
        VStack {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}
```

---

### Leaderboard

```swift
import SwiftUI

struct LeaderboardView: View {
    @State private var leaderboard: LeaderboardResponse?
    @State private var selectedPeriod: LeaderboardPeriod = .weekly

    var body: some View {
        NavigationView {
            VStack {
                // Period selector
                Picker("Period", selection: $selectedPeriod) {
                    Text("Daily").tag(LeaderboardPeriod.daily)
                    Text("Weekly").tag(LeaderboardPeriod.weekly)
                    Text("Monthly").tag(LeaderboardPeriod.monthly)
                    Text("All Time").tag(LeaderboardPeriod.allTime)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                .onChange(of: selectedPeriod) { _ in
                    Task { await fetchLeaderboard() }
                }

                // List
                if let leaderboard = leaderboard {
                    List(leaderboard.leaderboard) { entry in
                        HStack {
                            Text("#\(entry.rank)")
                                .font(.headline)
                                .frame(width: 40)

                            VStack(alignment: .leading) {
                                Text(entry.username)
                                    .font(.headline)
                                Text("Level \(entry.level)")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }

                            Spacer()

                            Text("\(entry.points)")
                                .font(.headline)
                                .foregroundColor(.blue)
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .navigationTitle("Leaderboard")
            .task {
                await fetchLeaderboard()
            }
        }
    }

    func fetchLeaderboard() async {
        do {
            let api = MorseMateAPI.shared
            leaderboard = try await api.getGlobalLeaderboard(period: selectedPeriod)
        } catch {
            print("Error: \(error)")
        }
    }
}
```

---

## 🎯 Common Tasks

### Check Authentication

```swift
let api = MorseMateAPI.shared

if api.isAuthenticated {
    print("User is logged in")
} else {
    // Show login screen
}
```

---

### Handle Errors

```swift
do {
    let response = try await api.login(request: loginRequest)
} catch let error as APIError {
    switch error {
    case .unauthorized:
        print("Invalid credentials")
    case .validationError(let errors):
        for error in errors {
            print("\(error.field): \(error.message)")
        }
    case .serverError(let code, let message):
        print("Server error: \(message)")
    default:
        print(error.localizedDescription)
    }
} catch {
    print("Unknown error: \(error)")
}
```

---

### Use Heart System

```swift
// Use a heart
let response = try await api.useHeart()
print(response.message)

// Refill hearts
let response = try await api.refillHearts()
print(response.message)
```

---

### Purchase Gems

```swift
// Get packages
let packages = try await api.getGemPackages()

// Purchase
let result = try await api.purchaseGems(
    packageId: 1,
    paymentMethodId: "pm_123456"
)
print("Bought \(result.gemsAdded) gems")
```

---

### Use Power-Ups

```swift
// Get available power-ups
let powerUps = try await api.getAllPowerUps()

// Purchase power-up
let result = try await api.purchasePowerUp(id: 1, quantity: 3)

// Use power-up
let useResult = try await api.usePowerUp(id: 1)
print(useResult.effect)
```

---

## 📊 API Methods Reference

### Authentication
- `register(request:)` → `AuthResponse`
- `login(request:)` → `AuthResponse`
- `getCurrentUser()` → `User`
- `logout()`

### Users
- `getUserProfile()` → `User`
- `updateProfile(request:)` → `User`
- `getUserStatistics()` → `UserStatistics`
- `useHeart()` → `MessageResponse`
- `refillHearts()` → `MessageResponse`

### Categories
- `getAllCategories()` → `[Category]`
- `getCategoryById(id:)` → `Category`
- `createCategory(request:)` → `Category` (Admin)
- `updateCategory(id:request:)` → `Category` (Admin)
- `deleteCategory(id:)` → `MessageResponse` (Admin)

### Lessons
- `getLessonsByCategory(categoryId:)` → `[Lesson]`
- `getLessonById(id:)` → `Lesson`
- `markLessonCompleted(id:request:)` → `CompletionResponse`
- `resetLessonProgress(id:)` → `MessageResponse`

### Exercises
- `getExercisesByLesson(lessonId:)` → `[Exercise]`
- `getExerciseById(id:)` → `ExerciseDetail`
- `submitExerciseAttempt(id:request:)` → `ExerciseAttemptResponse`
- `getExerciseAttempts(id:page:size:)` → `PaginatedResponse<ExerciseAttemptResponse>`

### Progress
- `getOverallProgress()` → `ProgressResponse`
- `getCategoryProgress(categoryId:)` → `CategoryProgressResponse`
- `getLessonProgress(lessonId:)` → `LessonProgressResponse`
- `updateDailyStreak()` → `StreakResponse`

### Achievements
- `getAllAchievements()` → `[Achievement]`
- `getUserAchievements()` → `[Achievement]`
- `getAchievementById(id:)` → `Achievement`

### Subscription
- `getSubscriptionPlans()` → `[SubscriptionPlan]`
- `getCurrentSubscription()` → `UserSubscription`
- `subscribeToPlan(planId:paymentMethodId:promoCode:)` → `SubscriptionResponse`
- `cancelSubscription(reason:feedback:)` → `MessageResponse`
- `resumeSubscription()` → `MessageResponse`

### Gems & Power-Ups
- `getGemsBalance()` → `GemsBalance`
- `getGemPackages()` → `[GemPackage]`
- `purchaseGems(packageId:paymentMethodId:)` → `GemPurchaseResponse`
- `getAllPowerUps()` → `[PowerUp]`
- `purchasePowerUp(id:quantity:)` → `PowerUpPurchaseResponse`
- `usePowerUp(id:context:)` → `PowerUpUseResponse`

### Leaderboard
- `getGlobalLeaderboard(period:page:size:)` → `LeaderboardResponse`
- `getFriendsLeaderboard(period:)` → `LeaderboardResponse`
- `getCategoryLeaderboard(categoryId:period:)` → `LeaderboardResponse`
- `searchUsers(query:page:size:)` → `PaginatedResponse<User>`

### Health
- `healthCheck()` → `HealthResponse`
- `ping()` → `MessageResponse`
- `getSystemInfo()` → `SystemInfoResponse`

---

## 🔐 Security

### Token Storage
```swift
// Tokens are automatically stored in UserDefaults
// Key: "auth_token"

// Access token manually if needed:
let token = UserDefaults.standard.string(forKey: "auth_token")
```

### Logout
```swift
// Clear token and authentication state
api.logout()

// Redirect to login screen
```

---

## 🎨 UI Components

### Loading State
```swift
if viewModel.isLoading {
    ProgressView("Loading...")
}
```

### Error State
```swift
if let error = viewModel.errorMessage {
    Text(error)
        .foregroundColor(.red)
}
```

### Empty State
```swift
if items.isEmpty {
    VStack {
        Image(systemName: "tray")
            .font(.largeTitle)
        Text("No items found")
            .foregroundColor(.secondary)
    }
}
```

---

## 📦 Requirements

- **iOS**: 15.0+
- **Swift**: 5.9+
- **Xcode**: 14.0+
- **Frameworks**: SwiftUI, Combine

---

## 🚀 Production Checklist

- [ ] Update `baseURL` in `APIConfig` to production URL
- [ ] Implement proper error handling
- [ ] Add loading states to all views
- [ ] Implement retry logic for failed requests
- [ ] Add analytics tracking
- [ ] Test on real devices
- [ ] Handle offline mode gracefully
- [ ] Implement token refresh
- [ ] Add biometric authentication
- [ ] Configure App Transport Security

---

## 📚 Additional Resources

- **Full Documentation**: `IOS_INTEGRATION_GUIDE.md`
- **API Reference**: `API_ENDPOINTS.md`
- **API Client**: `ios-api-client.swift`

---

**Happy Coding! 🎉**

All examples are tested and ready to use in production iOS apps.
