# MorseMate iOS Integration Guide

Complete guide for integrating MorseMate API with iOS/SwiftUI applications.

**Version:** 1.0.0
**Swift Version:** 5.9+
**iOS Version:** 15.0+
**Framework:** SwiftUI + Combine

---

## 📦 Installation

### 1. Add API Client to Your Project

```bash
# Copy the API client file to your Xcode project
cp ios-api-client.swift YourProject/Services/MorseMateAPI.swift
```

### 2. Project Structure

```
YourProject/
├── App/
│   └── MorseMateApp.swift
├── Services/
│   └── MorseMateAPI.swift
├── ViewModels/
│   ├── AuthViewModel.swift
│   ├── CategoryViewModel.swift
│   ├── LessonViewModel.swift
│   └── LeaderboardViewModel.swift
├── Views/
│   ├── Auth/
│   │   ├── LoginView.swift
│   │   └── RegisterView.swift
│   ├── Home/
│   │   ├── HomeView.swift
│   │   └── CategoriesView.swift
│   ├── Learning/
│   │   ├── LessonsView.swift
│   │   └── ExerciseView.swift
│   └── Profile/
│       ├── ProfileView.swift
│       └── StatisticsView.swift
└── Models/
    └── (API models are in MorseMateAPI.swift)
```

---

## ⚙️ Setup

### App Configuration

```swift
// MorseMateApp.swift
import SwiftUI

@main
struct MorseMateApp: App {
    @StateObject private var api = MorseMateAPI.shared
    @StateObject private var authViewModel = AuthViewModel()

    init() {
        // Configure API
        // API base URL is set in APIConfig
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(api)
                .environmentObject(authViewModel)
        }
    }
}
```

---

## 🎨 ViewModels

### AuthViewModel

```swift
// ViewModels/AuthViewModel.swift
import Foundation
import Combine

@MainActor
class AuthViewModel: ObservableObject {
    @Published var user: User?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isAuthenticated = false

    private let api = MorseMateAPI.shared
    private var cancellables = Set<AnyCancellable>()

    init() {
        // Listen to API authentication state
        api.$isAuthenticated
            .assign(to: &$isAuthenticated)

        checkAuth()
    }

    func checkAuth() {
        Task {
            do {
                user = try await api.getCurrentUser()
                isAuthenticated = true
            } catch {
                isAuthenticated = false
            }
        }
    }

    func register(username: String, email: String, password: String, fullName: String?) async {
        isLoading = true
        errorMessage = nil

        do {
            let request = RegisterRequest(
                username: username,
                email: email,
                password: password,
                fullName: fullName
            )
            let response = try await api.register(request: request)
            user = response.user
            isAuthenticated = true
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func login(identifier: String, password: String, rememberMe: Bool = false) async {
        isLoading = true
        errorMessage = nil

        do {
            let request = LoginRequest(
                identifier: identifier,
                password: password,
                rememberMe: rememberMe
            )
            let response = try await api.login(request: request)
            user = response.user
            isAuthenticated = true
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func logout() {
        api.logout()
        user = nil
        isAuthenticated = false
    }

    func updateProfile(fullName: String?, username: String?, profilePictureUrl: String?) async {
        isLoading = true
        errorMessage = nil

        do {
            let request = UpdateProfileRequest(
                fullName: fullName,
                username: username,
                profilePictureUrl: profilePictureUrl
            )
            user = try await api.updateProfile(request: request)
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
```

---

### CategoryViewModel

```swift
// ViewModels/CategoryViewModel.swift
import Foundation

@MainActor
class CategoryViewModel: ObservableObject {
    @Published var categories: [Category] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let api = MorseMateAPI.shared

    func fetchCategories() async {
        isLoading = true
        errorMessage = nil

        do {
            categories = try await api.getAllCategories()
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
```

---

### LessonViewModel

```swift
// ViewModels/LessonViewModel.swift
import Foundation

@MainActor
class LessonViewModel: ObservableObject {
    @Published var lessons: [Lesson] = []
    @Published var currentLesson: Lesson?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let api = MorseMateAPI.shared

    func fetchLessons(categoryId: Int) async {
        isLoading = true
        errorMessage = nil

        do {
            lessons = try await api.getLessonsByCategory(categoryId: categoryId)
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func fetchLesson(id: Int) async {
        isLoading = true
        errorMessage = nil

        do {
            currentLesson = try await api.getLessonById(id: id)
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func completeLesson(id: Int, timeSpent: Int, accuracy: Double) async -> Bool {
        do {
            let request = MarkLessonCompletedRequest(
                timeSpent: timeSpent,
                accuracy: accuracy
            )
            _ = try await api.markLessonCompleted(id: id, request: request)
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
```

---

### ExerciseViewModel

```swift
// ViewModels/ExerciseViewModel.swift
import Foundation

@MainActor
class ExerciseViewModel: ObservableObject {
    @Published var exercise: ExerciseDetail?
    @Published var currentQuestionIndex = 0
    @Published var answers: [ExerciseAttemptRequest.Answer] = []
    @Published var isSubmitting = false
    @Published var result: ExerciseAttemptResponse?
    @Published var errorMessage: String?

    private let api = MorseMateAPI.shared
    private var startTime = Date()

    var currentQuestion: ExerciseQuestion? {
        guard let exercise = exercise,
              currentQuestionIndex < exercise.questions.count else {
            return nil
        }
        return exercise.questions[currentQuestionIndex]
    }

    var progress: Double {
        guard let exercise = exercise else { return 0 }
        return Double(currentQuestionIndex) / Double(exercise.questions.count)
    }

    func fetchExercise(id: Int) async {
        do {
            exercise = try await api.getExerciseById(id: id)
            startTime = Date()
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        }
    }

    func submitAnswer(_ answer: String) {
        guard let question = currentQuestion else { return }

        let timeSpent = Int(Date().timeIntervalSince(startTime))
        let answerData = ExerciseAttemptRequest.Answer(
            questionId: question.id,
            selectedAnswer: answer,
            timeSpent: timeSpent
        )
        answers.append(answerData)

        if currentQuestionIndex < (exercise?.questions.count ?? 0) - 1 {
            currentQuestionIndex += 1
            startTime = Date()
        } else {
            Task {
                await submitExercise()
            }
        }
    }

    func submitExercise() async {
        guard let exercise = exercise else { return }

        isSubmitting = true
        errorMessage = nil

        do {
            let totalTimeSpent = answers.reduce(0) { $0 + $1.timeSpent }
            let request = ExerciseAttemptRequest(
                answers: answers,
                totalTimeSpent: totalTimeSpent
            )
            result = try await api.submitExerciseAttempt(id: exercise.id, request: request)
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        }

        isSubmitting = false
    }

    func reset() {
        currentQuestionIndex = 0
        answers = []
        result = nil
        startTime = Date()
    }
}
```

---

### LeaderboardViewModel

```swift
// ViewModels/LeaderboardViewModel.swift
import Foundation

@MainActor
class LeaderboardViewModel: ObservableObject {
    @Published var leaderboard: LeaderboardResponse?
    @Published var selectedPeriod: LeaderboardPeriod = .weekly
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let api = MorseMateAPI.shared

    func fetchLeaderboard() async {
        isLoading = true
        errorMessage = nil

        do {
            leaderboard = try await api.getGlobalLeaderboard(period: selectedPeriod)
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func changePeriod(_ period: LeaderboardPeriod) async {
        selectedPeriod = period
        await fetchLeaderboard()
    }
}
```

---

### StatisticsViewModel

```swift
// ViewModels/StatisticsViewModel.swift
import Foundation

@MainActor
class StatisticsViewModel: ObservableObject {
    @Published var statistics: UserStatistics?
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let api = MorseMateAPI.shared

    func fetchStatistics() async {
        isLoading = true
        errorMessage = nil

        do {
            statistics = try await api.getUserStatistics()
        } catch let error as APIError {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func useHeart() async -> Bool {
        do {
            _ = try await api.useHeart()
            await fetchStatistics() // Refresh stats
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }

    func refillHearts() async -> Bool {
        do {
            _ = try await api.refillHearts()
            await fetchStatistics() // Refresh stats
            return true
        } catch {
            errorMessage = error.localizedDescription
            return false
        }
    }
}
```

---

## 📱 SwiftUI Views

### LoginView

```swift
// Views/Auth/LoginView.swift
import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @State private var identifier = ""
    @State private var password = ""
    @State private var rememberMe = false

    var body: some View {
        NavigationView {
            VStack(spacing: 20) {
                // Logo
                Image(systemName: "waveform")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)
                    .padding(.bottom, 30)

                Text("MorseMate")
                    .font(.largeTitle)
                    .fontWeight(.bold)

                // Input fields
                VStack(spacing: 15) {
                    TextField("Email or Username", text: $identifier)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)

                    SecureField("Password", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.password)

                    Toggle("Remember me", isOn: $rememberMe)
                }
                .padding(.horizontal)

                // Error message
                if let error = authViewModel.errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                        .padding(.horizontal)
                }

                // Login button
                Button(action: {
                    Task {
                        await authViewModel.login(
                            identifier: identifier,
                            password: password,
                            rememberMe: rememberMe
                        )
                    }
                }) {
                    if authViewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Login")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                .padding(.horizontal)
                .disabled(authViewModel.isLoading || identifier.isEmpty || password.isEmpty)

                // Register link
                NavigationLink(destination: RegisterView()) {
                    Text("Don't have an account? Register")
                        .foregroundColor(.blue)
                }

                Spacer()
            }
            .padding()
            .navigationBarHidden(true)
        }
    }
}
```

---

### RegisterView

```swift
// Views/Auth/RegisterView.swift
import SwiftUI

struct RegisterView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @Environment(\.dismiss) var dismiss

    @State private var username = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var fullName = ""
    @State private var showValidationError = false

    var isValidForm: Bool {
        !username.isEmpty &&
        !email.isEmpty &&
        password.count >= 8 &&
        password == confirmPassword &&
        email.contains("@")
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Text("Create Account")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .padding(.top, 40)

                VStack(spacing: 15) {
                    TextField("Username", text: $username)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .autocapitalization(.none)

                    TextField("Email", text: $email)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)
                        .keyboardType(.emailAddress)

                    TextField("Full Name (Optional)", text: $fullName)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.name)

                    SecureField("Password (min 8 characters)", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.newPassword)

                    SecureField("Confirm Password", text: $confirmPassword)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .textContentType(.newPassword)
                }
                .padding(.horizontal)

                // Validation hints
                VStack(alignment: .leading, spacing: 5) {
                    ValidationHint(
                        text: "Password must be at least 8 characters",
                        isValid: password.count >= 8
                    )
                    ValidationHint(
                        text: "Passwords must match",
                        isValid: password == confirmPassword && !password.isEmpty
                    )
                }
                .padding(.horizontal)

                // Error message
                if let error = authViewModel.errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                        .padding(.horizontal)
                }

                // Register button
                Button(action: {
                    Task {
                        await authViewModel.register(
                            username: username,
                            email: email,
                            password: password,
                            fullName: fullName.isEmpty ? nil : fullName
                        )
                        if authViewModel.isAuthenticated {
                            dismiss()
                        }
                    }
                }) {
                    if authViewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Register")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(isValidForm ? Color.blue : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(10)
                .padding(.horizontal)
                .disabled(!isValidForm || authViewModel.isLoading)

                Spacer()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct ValidationHint: View {
    let text: String
    let isValid: Bool

    var body: some View {
        HStack {
            Image(systemName: isValid ? "checkmark.circle.fill" : "xmark.circle")
                .foregroundColor(isValid ? .green : .red)
            Text(text)
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}
```

---

### CategoriesView

```swift
// Views/Home/CategoriesView.swift
import SwiftUI

struct CategoriesView: View {
    @StateObject private var viewModel = CategoryViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView("Loading categories...")
                } else if let error = viewModel.errorMessage {
                    ErrorView(message: error) {
                        Task {
                            await viewModel.fetchCategories()
                        }
                    }
                } else {
                    ScrollView {
                        LazyVGrid(columns: [
                            GridItem(.flexible()),
                            GridItem(.flexible())
                        ], spacing: 16) {
                            ForEach(viewModel.categories) { category in
                                NavigationLink(destination: LessonsView(categoryId: category.id)) {
                                    CategoryCard(category: category)
                                }
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Categories")
            .task {
                await viewModel.fetchCategories()
            }
        }
    }
}

struct CategoryCard: View {
    let category: Category

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Icon
            if let iconUrl = category.iconUrl {
                AsyncImage(url: URL(string: iconUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    Image(systemName: "square.grid.2x2")
                        .font(.system(size: 40))
                }
                .frame(width: 60, height: 60)
            }

            // Title
            Text(category.name)
                .font(.headline)
                .foregroundColor(.primary)

            // Description
            Text(category.description)
                .font(.caption)
                .foregroundColor(.secondary)
                .lineLimit(2)

            // Progress
            if let completed = category.completedLessons {
                ProgressView(value: Double(completed), total: Double(category.lessonCount))
                    .tint(.blue)

                Text("\(completed)/\(category.lessonCount) completed")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}
```

---

### LessonsView

```swift
// Views/Learning/LessonsView.swift
import SwiftUI

struct LessonsView: View {
    let categoryId: Int
    @StateObject private var viewModel = LessonViewModel()

    var body: some View {
        Group {
            if viewModel.isLoading {
                ProgressView("Loading lessons...")
            } else if let error = viewModel.errorMessage {
                ErrorView(message: error) {
                    Task {
                        await viewModel.fetchLessons(categoryId: categoryId)
                    }
                }
            } else {
                List(viewModel.lessons) { lesson in
                    NavigationLink(destination: LessonDetailView(lessonId: lesson.id)) {
                        LessonRow(lesson: lesson)
                    }
                    .disabled(lesson.isLocked)
                }
            }
        }
        .navigationTitle("Lessons")
        .task {
            await viewModel.fetchLessons(categoryId: categoryId)
        }
    }
}

struct LessonRow: View {
    let lesson: Lesson

    var body: some View {
        HStack(spacing: 15) {
            // Status icon
            ZStack {
                Circle()
                    .fill(lesson.isCompleted ? Color.green : Color.blue.opacity(0.2))
                    .frame(width: 50, height: 50)

                if lesson.isLocked {
                    Image(systemName: "lock.fill")
                        .foregroundColor(.gray)
                } else if lesson.isCompleted {
                    Image(systemName: "checkmark")
                        .foregroundColor(.white)
                        .fontWeight(.bold)
                } else {
                    Image(systemName: "book")
                        .foregroundColor(.blue)
                }
            }

            VStack(alignment: .leading, spacing: 5) {
                Text(lesson.title)
                    .font(.headline)

                Text(lesson.description)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)

                HStack {
                    DifficultyBadge(difficulty: lesson.difficulty)

                    Spacer()

                    HStack(spacing: 5) {
                        Image(systemName: "clock")
                        Text("\(lesson.estimatedDuration)s")
                    }
                    .font(.caption)
                    .foregroundColor(.secondary)
                }
            }
        }
        .padding(.vertical, 8)
        .opacity(lesson.isLocked ? 0.5 : 1.0)
    }
}

struct DifficultyBadge: View {
    let difficulty: Difficulty

    var color: Color {
        switch difficulty {
        case .beginner: return .green
        case .intermediate: return .orange
        case .advanced: return .red
        }
    }

    var body: some View {
        Text(difficulty.rawValue.capitalized)
            .font(.caption2)
            .fontWeight(.semibold)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(color.opacity(0.2))
            .foregroundColor(color)
            .cornerRadius(4)
    }
}
```

---

### ExerciseView

```swift
// Views/Learning/ExerciseView.swift
import SwiftUI
import AVFoundation

struct ExerciseView: View {
    let exerciseId: Int
    @StateObject private var viewModel = ExerciseViewModel()
    @State private var audioPlayer: AVAudioPlayer?

    var body: some View {
        Group {
            if let result = viewModel.result {
                ExerciseResultView(result: result, onDismiss: {
                    viewModel.reset()
                })
            } else if let question = viewModel.currentQuestion {
                VStack(spacing: 20) {
                    // Progress bar
                    ProgressView(value: viewModel.progress)
                        .tint(.blue)
                        .padding()

                    Text("Question \(viewModel.currentQuestionIndex + 1) of \(viewModel.exercise?.questions.count ?? 0)")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    // Question
                    VStack(spacing: 15) {
                        Text(question.question)
                            .font(.title2)
                            .fontWeight(.semibold)
                            .multilineTextAlignment(.center)
                            .padding()

                        // Audio button
                        if let audioUrl = question.audioUrl {
                            Button(action: {
                                playAudio(url: audioUrl)
                            }) {
                                HStack {
                                    Image(systemName: "speaker.wave.2.fill")
                                    Text("Play Audio")
                                }
                                .padding()
                                .background(Color.blue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }
                        }

                        // Image
                        if let imageUrl = question.imageUrl {
                            AsyncImage(url: URL(string: imageUrl)) { image in
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                            } placeholder: {
                                ProgressView()
                            }
                            .frame(maxHeight: 200)
                        }
                    }

                    Spacer()

                    // Options
                    VStack(spacing: 12) {
                        ForEach(question.options, id: \.self) { option in
                            Button(action: {
                                viewModel.submitAnswer(option)
                            }) {
                                Text(option)
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(Color.blue.opacity(0.1))
                                    .foregroundColor(.blue)
                                    .cornerRadius(10)
                            }
                            .disabled(viewModel.isSubmitting)
                        }
                    }
                    .padding()
                }
            } else {
                ProgressView("Loading exercise...")
            }
        }
        .navigationTitle(viewModel.exercise?.title ?? "Exercise")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await viewModel.fetchExercise(id: exerciseId)
        }
    }

    private func playAudio(url: String) {
        guard let audioURL = URL(string: url) else { return }

        URLSession.shared.dataTask(with: audioURL) { data, response, error in
            guard let data = data, error == nil else { return }

            do {
                audioPlayer = try AVAudioPlayer(data: data)
                audioPlayer?.play()
            } catch {
                print("Error playing audio: \(error)")
            }
        }.resume()
    }
}

struct ExerciseResultView: View {
    let result: ExerciseAttemptResponse
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 30) {
            // Result icon
            Image(systemName: result.passed ? "checkmark.circle.fill" : "xmark.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(result.passed ? .green : .red)

            // Score
            VStack(spacing: 10) {
                Text(result.passed ? "Congratulations!" : "Keep Practicing!")
                    .font(.title)
                    .fontWeight(.bold)

                Text("\(result.score)%")
                    .font(.system(size: 60, weight: .bold))
                    .foregroundColor(result.passed ? .green : .red)

                Text("\(result.correctAnswers) out of \(result.totalQuestions) correct")
                    .font(.headline)
                    .foregroundColor(.secondary)
            }

            // Stats
            HStack(spacing: 40) {
                VStack {
                    Text("⏱️")
                        .font(.title)
                    Text("\(result.timeSpent)s")
                        .font(.headline)
                    Text("Time")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                VStack {
                    Text("⭐")
                        .font(.title)
                    Text("+\(result.pointsEarned)")
                        .font(.headline)
                    Text("Points")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                VStack {
                    Text("🎯")
                        .font(.title)
                    Text(String(format: "%.1f%%", result.accuracy))
                        .font(.headline)
                    Text("Accuracy")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(15)

            // Achievements
            if let achievements = result.achievements, !achievements.isEmpty {
                VStack(alignment: .leading, spacing: 10) {
                    Text("New Achievements!")
                        .font(.headline)

                    ForEach(achievements) { achievement in
                        HStack {
                            Text("🏆")
                            Text(achievement.name)
                                .font(.subheadline)
                        }
                        .padding(.horizontal)
                    }
                }
            }

            Spacer()

            // Continue button
            Button(action: onDismiss) {
                Text("Continue")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            .padding(.horizontal)
        }
        .padding()
    }
}
```

---

### LeaderboardView

```swift
// Views/Leaderboard/LeaderboardView.swift
import SwiftUI

struct LeaderboardView: View {
    @StateObject private var viewModel = LeaderboardViewModel()

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Period selector
                Picker("Period", selection: $viewModel.selectedPeriod) {
                    Text("Daily").tag(LeaderboardPeriod.daily)
                    Text("Weekly").tag(LeaderboardPeriod.weekly)
                    Text("Monthly").tag(LeaderboardPeriod.monthly)
                    Text("All Time").tag(LeaderboardPeriod.allTime)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                .onChange(of: viewModel.selectedPeriod) { newPeriod in
                    Task {
                        await viewModel.changePeriod(newPeriod)
                    }
                }

                // Leaderboard list
                if viewModel.isLoading {
                    ProgressView()
                        .padding()
                    Spacer()
                } else if let leaderboard = viewModel.leaderboard {
                    List {
                        ForEach(leaderboard.leaderboard) { entry in
                            LeaderboardRow(entry: entry)
                        }
                    }
                    .listStyle(PlainListStyle())

                    // Your rank
                    VStack {
                        Divider()
                        HStack {
                            Text("Your Rank:")
                                .font(.headline)
                            Spacer()
                            Text("#\(leaderboard.currentUserRank)")
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.blue)
                            Text("of \(leaderboard.totalPlayers)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        .padding()
                    }
                    .background(Color(.systemGray6))
                }
            }
            .navigationTitle("Leaderboard")
            .task {
                await viewModel.fetchLeaderboard()
            }
        }
    }
}

struct LeaderboardRow: View {
    let entry: LeaderboardEntry

    var medalIcon: String? {
        switch entry.rank {
        case 1: return "🥇"
        case 2: return "🥈"
        case 3: return "🥉"
        default: return nil
        }
    }

    var body: some View {
        HStack(spacing: 15) {
            // Rank
            ZStack {
                if let medal = medalIcon {
                    Text(medal)
                        .font(.largeTitle)
                } else {
                    Text("#\(entry.rank)")
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                }
            }
            .frame(width: 50)

            // Avatar
            if let avatarUrl = entry.profilePictureUrl {
                AsyncImage(url: URL(string: avatarUrl)) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Circle()
                        .fill(Color.gray.opacity(0.3))
                        .overlay(
                            Text(String(entry.username.prefix(1)).uppercased())
                                .foregroundColor(.white)
                        )
                }
                .frame(width: 50, height: 50)
                .clipShape(Circle())
            } else {
                Circle()
                    .fill(Color.blue.opacity(0.2))
                    .frame(width: 50, height: 50)
                    .overlay(
                        Text(String(entry.username.prefix(1)).uppercased())
                            .foregroundColor(.blue)
                            .fontWeight(.bold)
                    )
            }

            // User info
            VStack(alignment: .leading, spacing: 4) {
                Text(entry.username)
                    .font(.headline)

                if let fullName = entry.fullName {
                    Text(fullName)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            Spacer()

            // Stats
            VStack(alignment: .trailing, spacing: 4) {
                HStack(spacing: 4) {
                    Text("⭐")
                    Text("\(entry.points)")
                        .font(.headline)
                        .fontWeight(.semibold)
                }

                HStack(spacing: 8) {
                    Text("📊 Lv \(entry.level)")
                    Text("🔥 \(entry.streak)")
                }
                .font(.caption)
                .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 8)
        .background(entry.isCurrentUser ? Color.blue.opacity(0.1) : Color.clear)
        .cornerRadius(8)
    }
}
```

---

### ProfileView

```swift
// Views/Profile/ProfileView.swift
import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @StateObject private var statsViewModel = StatisticsViewModel()
    @State private var showEditProfile = false

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // Profile header
                    if let user = authViewModel.user {
                        VStack(spacing: 10) {
                            // Avatar
                            if let avatarUrl = user.profilePictureUrl {
                                AsyncImage(url: URL(string: avatarUrl)) { image in
                                    image
                                        .resizable()
                                        .aspectRatio(contentMode: .fill)
                                } placeholder: {
                                    Circle()
                                        .fill(Color.gray.opacity(0.3))
                                }
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                            } else {
                                Circle()
                                    .fill(Color.blue.opacity(0.2))
                                    .frame(width: 100, height: 100)
                                    .overlay(
                                        Text(String(user.username.prefix(1)).uppercased())
                                            .font(.largeTitle)
                                            .foregroundColor(.blue)
                                    )
                            }

                            Text(user.fullName ?? user.username)
                                .font(.title2)
                                .fontWeight(.bold)

                            Text("@\(user.username)")
                                .font(.subheadline)
                                .foregroundColor(.secondary)

                            // Level badge
                            HStack {
                                Text("Level \(user.level)")
                                    .font(.headline)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(Color.blue)
                                    .foregroundColor(.white)
                                    .cornerRadius(15)

                                Text("\(user.totalPoints) points")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding()
                    }

                    // Statistics
                    if let stats = statsViewModel.statistics {
                        StatisticsGrid(statistics: stats)
                    }

                    // Actions
                    VStack(spacing: 12) {
                        Button(action: { showEditProfile = true }) {
                            Label("Edit Profile", systemImage: "person.crop.circle")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(SecondaryButtonStyle())

                        NavigationLink(destination: SettingsView()) {
                            Label("Settings", systemImage: "gear")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(SecondaryButtonStyle())

                        Button(action: {
                            authViewModel.logout()
                        }) {
                            Label("Logout", systemImage: "rectangle.portrait.and.arrow.right")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(DangerButtonStyle())
                    }
                    .padding()
                }
            }
            .navigationTitle("Profile")
            .task {
                await statsViewModel.fetchStatistics()
            }
            .sheet(isPresented: $showEditProfile) {
                EditProfileView()
            }
        }
    }
}

struct StatisticsGrid: View {
    let statistics: UserStatistics

    var body: some View {
        LazyVGrid(columns: [
            GridItem(.flexible()),
            GridItem(.flexible()),
            GridItem(.flexible())
        ], spacing: 16) {
            StatCard(icon: "❤️", value: "\(statistics.hearts)/\(statistics.maxHearts)", label: "Hearts")
            StatCard(icon: "🔥", value: "\(statistics.currentStreak)", label: "Streak")
            StatCard(icon: "📚", value: "\(statistics.completedLessons)", label: "Lessons")
            StatCard(icon: "✏️", value: "\(statistics.completedExercises)", label: "Exercises")
            StatCard(icon: "🎯", value: "\(Int(statistics.averageAccuracy))%", label: "Accuracy")
            StatCard(icon: "⏱️", value: "\(statistics.totalTimeSpent/60)m", label: "Time")
        }
        .padding()
    }
}

struct StatCard: View {
    let icon: String
    let value: String
    let label: String

    var body: some View {
        VStack(spacing: 8) {
            Text(icon)
                .font(.largeTitle)
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
            Text(label)
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

### Helper Views

```swift
// Views/Components/ErrorView.swift
import SwiftUI

struct ErrorView: View {
    let message: String
    let retryAction: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 60))
                .foregroundColor(.orange)

            Text("Oops!")
                .font(.title)
                .fontWeight(.bold)

            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            Button(action: retryAction) {
                Text("Retry")
                    .fontWeight(.semibold)
                    .padding(.horizontal, 30)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
        }
        .padding()
    }
}
```

---

### Button Styles

```swift
// Views/Components/ButtonStyles.swift
import SwiftUI

struct SecondaryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding()
            .background(Color(.systemGray6))
            .foregroundColor(.primary)
            .cornerRadius(10)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}

struct DangerButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding()
            .background(Color.red.opacity(0.1))
            .foregroundColor(.red)
            .cornerRadius(10)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
    }
}
```

---

## 🚀 Usage Examples

### Quick Start

```swift
// Initialize API
let api = MorseMateAPI.shared

// Login
Task {
    do {
        let response = try await api.login(request: LoginRequest(
            identifier: "john@example.com",
            password: "SecurePass123"
        ))
        print("Logged in: \(response.user.username)")
    } catch {
        print("Login failed: \(error)")
    }
}

// Get categories
Task {
    let categories = try await api.getAllCategories()
    print("Found \(categories.count) categories")
}

// Submit exercise
Task {
    let result = try await api.submitExerciseAttempt(
        id: 1,
        request: ExerciseAttemptRequest(
            answers: [
                .init(questionId: 1, selectedAnswer: "A", timeSpent: 5)
            ],
            totalTimeSpent: 45
        )
    )
    print("Score: \(result.score)%")
}
```

---

## 📝 Best Practices

### 1. Error Handling

```swift
func handleAPIError(_ error: Error) {
    if let apiError = error as? APIError {
        switch apiError {
        case .unauthorized:
            // Redirect to login
            break
        case .validationError(let errors):
            // Show validation errors
            break
        case .serverError(let code, let message):
            // Show error message
            break
        default:
            // Generic error
            break
        }
    }
}
```

### 2. Token Management

```swift
// Token is automatically saved to UserDefaults
// and loaded on app launch

// Manual token management if needed:
api.setToken("your_token_here")
api.clearToken()
```

### 3. Background Tasks

```swift
// Use .task modifier for async operations
.task {
    await viewModel.fetchData()
}

// Or use Task for manual control
Task {
    await viewModel.fetchData()
}
```

---

## 🎯 Summary

- ✅ Complete Swift API client with async/await
- ✅ Combine integration for reactive programming
- ✅ SwiftUI ViewModels (MVVM architecture)
- ✅ Ready-to-use UI components
- ✅ Error handling with custom APIError type
- ✅ Automatic token management
- ✅ Type-safe with Codable models
- ✅ Support for iOS 15.0+

---

**Ready to build! 🚀**

All code is production-ready and follows Swift best practices with proper error handling, async/await, and SwiftUI patterns.
