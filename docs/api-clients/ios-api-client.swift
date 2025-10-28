//
//  MorseMateAPI.swift
//  MorseMate iOS Client
//
//  Created by MorseMate Team
//  Version 1.0.0
//

import Foundation
import Combine

// MARK: - API Configuration

struct APIConfig {
    let baseURL: String
    var token: String?
    let timeout: TimeInterval

    init(baseURL: String = "http://localhost:8080", token: String? = nil, timeout: TimeInterval = 30) {
        self.baseURL = baseURL
        self.token = token
        self.timeout = timeout
    }
}

// MARK: - API Error

enum APIError: Error, LocalizedError {
    case invalidURL
    case networkError(Error)
    case decodingError(Error)
    case serverError(statusCode: Int, message: String)
    case unauthorized
    case notFound
    case validationError([ValidationError])

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        case .decodingError(let error):
            return "Decoding error: \(error.localizedDescription)"
        case .serverError(_, let message):
            return message
        case .unauthorized:
            return "Unauthorized. Please login again."
        case .notFound:
            return "Resource not found"
        case .validationError(let errors):
            return errors.map { $0.message }.joined(separator: ", ")
        }
    }
}

struct ValidationError: Codable {
    let field: String
    let message: String
}

struct ErrorResponse: Codable {
    let timestamp: String
    let status: Int
    let error: String
    let message: String
    let path: String
    let errors: [ValidationError]?
}

// MARK: - Paginated Response

struct PaginatedResponse<T: Codable>: Codable {
    let content: [T]
    let pageable: Pageable
    let totalElements: Int
    let totalPages: Int
    let last: Bool
    let first: Bool
}

struct Pageable: Codable {
    let pageNumber: Int
    let pageSize: Int
    let sort: Sort
}

struct Sort: Codable {
    let sorted: Bool
}

// MARK: - Authentication Models

struct RegisterRequest: Codable {
    let username: String
    let email: String
    let password: String
    let fullName: String?
}

struct LoginRequest: Codable {
    let identifier: String
    let password: String
    let rememberMe: Bool?
}

struct AuthResponse: Codable {
    let token: String
    let expiresIn: Int
    let user: User
}

// MARK: - User Models

struct User: Codable, Identifiable {
    let id: Int
    let username: String
    let email: String
    let fullName: String?
    let profilePictureUrl: String?
    let level: Int
    let hearts: Int
    let maxHearts: Int
    let totalPoints: Int
    let currentStreak: Int
    let longestStreak: Int
    let isActive: Bool
    let emailVerified: Bool
    let createdAt: String
    let lastLogin: String?
}

struct UpdateProfileRequest: Codable {
    let fullName: String?
    let username: String?
    let profilePictureUrl: String?
}

struct UserStatistics: Codable {
    let totalPoints: Int
    let level: Int
    let currentStreak: Int
    let longestStreak: Int
    let hearts: Int
    let maxHearts: Int
    let completedLessons: Int
    let totalLessons: Int
    let completedExercises: Int
    let totalExercises: Int
    let averageAccuracy: Double
    let totalTimeSpent: Int
    let achievements: Int
    let rank: String
    let nextLevelPoints: Int
}

// MARK: - Category Models

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

struct CreateCategoryRequest: Codable {
    let name: String
    let description: String?
    let displayOrder: Int?
    let iconUrl: String?
    let isActive: Bool?
}

struct UpdateCategoryRequest: Codable {
    let name: String?
    let description: String?
    let displayOrder: Int?
    let iconUrl: String?
    let isActive: Bool?
}

// MARK: - Lesson Models

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
    let exercises: [Exercise]?
    let userProgress: UserProgress?
}

struct UserProgress: Codable {
    let completionPercentage: Int
    let lastAttemptDate: String
    let attempts: Int
}

struct CreateLessonRequest: Codable {
    let categoryId: Int
    let title: String
    let description: String
    let morseCode: String
    let audioUrl: String?
    let videoUrl: String?
    let imageUrl: String?
    let difficulty: Difficulty
    let displayOrder: Int
    let estimatedDuration: Int
    let requiredPointsToUnlock: Int?
    let isActive: Bool?
}

struct MarkLessonCompletedRequest: Codable {
    let timeSpent: Int
    let accuracy: Double
}

// MARK: - Exercise Models

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

struct ExerciseAttemptRequest: Codable {
    let answers: [Answer]
    let totalTimeSpent: Int

    struct Answer: Codable {
        let questionId: Int
        let selectedAnswer: String
        let timeSpent: Int
    }
}

struct ExerciseAttemptResponse: Codable {
    let attemptId: Int
    let score: Int
    let passed: Bool
    let correctAnswers: Int
    let incorrectAnswers: Int
    let totalQuestions: Int
    let accuracy: Double
    let timeSpent: Int
    let pointsEarned: Int
    let isNewBestScore: Bool
    let achievements: [Achievement]?
    let feedback: [Feedback]

    struct Feedback: Codable {
        let questionId: Int
        let isCorrect: Bool
        let correctAnswer: String
        let yourAnswer: String
        let explanation: String?
    }
}

// MARK: - Achievement Models

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

// MARK: - Subscription Models

enum BillingPeriod: String, Codable {
    case monthly = "MONTHLY"
    case yearly = "YEARLY"
}

struct SubscriptionPlan: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let price: Double
    let currency: String
    let billingPeriod: BillingPeriod
    let features: [String]
    let isPopular: Bool
}

enum SubscriptionStatus: String, Codable {
    case active = "ACTIVE"
    case cancelled = "CANCELLED"
    case expired = "EXPIRED"
}

struct UserSubscription: Codable, Identifiable {
    let id: Int
    let userId: Int
    let planId: Int
    let planName: String
    let status: SubscriptionStatus
    let startDate: String
    let endDate: String
    let autoRenew: Bool
    let paymentMethod: String
    let nextBillingDate: String?
}

// MARK: - Gems & Power-ups Models

struct GemsBalance: Codable {
    let userId: Int
    let totalGems: Int
    let earnedGems: Int
    let purchasedGems: Int
    let spentGems: Int
    let lifetimeGems: Int
}

struct GemPackage: Codable, Identifiable {
    let id: Int
    let name: String
    let gems: Int
    let price: Double
    let currency: String
    let bonus: Int
    let isPopular: Bool
}

struct PowerUp: Codable, Identifiable {
    let id: Int
    let name: String
    let description: String
    let iconUrl: String?
    let cost: Int
    let costType: String
    let isAvailable: Bool
    let ownedQuantity: Int?
}

// MARK: - Leaderboard Models

enum LeaderboardPeriod: String {
    case daily = "DAILY"
    case weekly = "WEEKLY"
    case monthly = "MONTHLY"
    case allTime = "ALL_TIME"
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

struct LeaderboardResponse: Codable {
    let period: String
    let currentUserRank: Int
    let leaderboard: [LeaderboardEntry]
    let totalPlayers: Int
}

// MARK: - API Client

class MorseMateAPI: ObservableObject {

    // MARK: - Properties

    @Published var isAuthenticated = false
    private var config: APIConfig
    private var cancellables = Set<AnyCancellable>()

    // MARK: - Initialization

    init(config: APIConfig = APIConfig()) {
        self.config = config
        loadToken()
    }

    // MARK: - Token Management

    func setToken(_ token: String) {
        config.token = token
        UserDefaults.standard.set(token, forKey: "auth_token")
        isAuthenticated = true
    }

    func clearToken() {
        config.token = nil
        UserDefaults.standard.removeObject(forKey: "auth_token")
        isAuthenticated = false
    }

    private func loadToken() {
        if let token = UserDefaults.standard.string(forKey: "auth_token") {
            config.token = token
            isAuthenticated = true
        }
    }

    // MARK: - Private Request Methods

    private func request<T: Decodable>(
        endpoint: String,
        method: String = "GET",
        body: Encodable? = nil,
        requiresAuth: Bool = true
    ) -> AnyPublisher<T, APIError> {

        guard let url = URL(string: config.baseURL + endpoint) else {
            return Fail(error: APIError.invalidURL).eraseToAnyPublisher()
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.timeoutInterval = config.timeout
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if requiresAuth, let token = config.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        if let body = body {
            do {
                request.httpBody = try JSONEncoder().encode(body)
            } catch {
                return Fail(error: APIError.decodingError(error)).eraseToAnyPublisher()
            }
        }

        return URLSession.shared.dataTaskPublisher(for: request)
            .tryMap { data, response -> Data in
                guard let httpResponse = response as? HTTPURLResponse else {
                    throw APIError.networkError(URLError(.badServerResponse))
                }

                switch httpResponse.statusCode {
                case 200...299:
                    return data
                case 401:
                    DispatchQueue.main.async {
                        self.clearToken()
                    }
                    throw APIError.unauthorized
                case 404:
                    throw APIError.notFound
                case 400...499:
                    if let errorResponse = try? JSONDecoder().decode(ErrorResponse.self, from: data) {
                        if let validationErrors = errorResponse.errors {
                            throw APIError.validationError(validationErrors)
                        }
                        throw APIError.serverError(statusCode: httpResponse.statusCode, message: errorResponse.message)
                    }
                    throw APIError.serverError(statusCode: httpResponse.statusCode, message: "Client error")
                case 500...599:
                    throw APIError.serverError(statusCode: httpResponse.statusCode, message: "Server error")
                default:
                    throw APIError.serverError(statusCode: httpResponse.statusCode, message: "Unknown error")
                }
            }
            .decode(type: T.self, decoder: JSONDecoder())
            .mapError { error in
                if let apiError = error as? APIError {
                    return apiError
                } else if error is DecodingError {
                    return APIError.decodingError(error)
                } else {
                    return APIError.networkError(error)
                }
            }
            .receive(on: DispatchQueue.main)
            .eraseToAnyPublisher()
    }

    // MARK: - Async/Await Wrapper

    private func performRequest<T: Decodable>(
        endpoint: String,
        method: String = "GET",
        body: Encodable? = nil,
        requiresAuth: Bool = true
    ) async throws -> T {
        try await withCheckedThrowingContinuation { continuation in
            request(endpoint: endpoint, method: method, body: body, requiresAuth: requiresAuth)
                .sink(
                    receiveCompletion: { completion in
                        if case .failure(let error) = completion {
                            continuation.resume(throwing: error)
                        }
                    },
                    receiveValue: { value in
                        continuation.resume(returning: value)
                    }
                )
                .store(in: &cancellables)
        }
    }

    // MARK: - Authentication

    func register(request: RegisterRequest) async throws -> AuthResponse {
        let response: AuthResponse = try await performRequest(
            endpoint: "/auth/register",
            method: "POST",
            body: request,
            requiresAuth: false
        )
        setToken(response.token)
        return response
    }

    func login(request: LoginRequest) async throws -> AuthResponse {
        let response: AuthResponse = try await performRequest(
            endpoint: "/auth/login",
            method: "POST",
            body: request,
            requiresAuth: false
        )
        setToken(response.token)
        return response
    }

    func getCurrentUser() async throws -> User {
        try await performRequest(endpoint: "/auth/me")
    }

    func logout() {
        clearToken()
    }

    // MARK: - User Management

    func getUserProfile() async throws -> User {
        try await performRequest(endpoint: "/users/me")
    }

    func updateProfile(request: UpdateProfileRequest) async throws -> User {
        try await performRequest(endpoint: "/users/me", method: "PUT", body: request)
    }

    func getUserById(id: Int) async throws -> User {
        try await performRequest(endpoint: "/users/\(id)")
    }

    func getUserByUsername(username: String) async throws -> User {
        try await performRequest(endpoint: "/users/username/\(username)")
    }

    func getAllUsers(page: Int = 0, size: Int = 10, sort: String = "createdAt,desc") async throws -> PaginatedResponse<User> {
        try await performRequest(endpoint: "/users?page=\(page)&size=\(size)&sort=\(sort)")
    }

    func getUserStatistics() async throws -> UserStatistics {
        try await performRequest(endpoint: "/users/me/statistics")
    }

    func useHeart() async throws -> MessageResponse {
        try await performRequest(endpoint: "/users/me/hearts/use", method: "POST")
    }

    func refillHearts() async throws -> MessageResponse {
        try await performRequest(endpoint: "/users/me/hearts/refill", method: "POST")
    }

    func deleteAccount() async throws -> MessageResponse {
        try await performRequest(endpoint: "/users/me", method: "DELETE")
    }

    // MARK: - Categories

    func getAllCategories() async throws -> [Category] {
        try await performRequest(endpoint: "/v1/categories", requiresAuth: false)
    }

    func getCategoryById(id: Int) async throws -> Category {
        try await performRequest(endpoint: "/v1/categories/\(id)", requiresAuth: false)
    }

    func createCategory(request: CreateCategoryRequest) async throws -> Category {
        try await performRequest(endpoint: "/v1/categories", method: "POST", body: request)
    }

    func updateCategory(id: Int, request: UpdateCategoryRequest) async throws -> Category {
        try await performRequest(endpoint: "/v1/categories/\(id)", method: "PUT", body: request)
    }

    func deleteCategory(id: Int) async throws -> MessageResponse {
        try await performRequest(endpoint: "/v1/categories/\(id)", method: "DELETE")
    }

    func getAllCategoriesAdmin() async throws -> [Category] {
        try await performRequest(endpoint: "/v1/categories/admin/all")
    }

    // MARK: - Lessons

    func getLessonsByCategory(categoryId: Int) async throws -> [Lesson] {
        try await performRequest(endpoint: "/v1/categories/\(categoryId)/lessons")
    }

    func getLessonById(id: Int) async throws -> Lesson {
        try await performRequest(endpoint: "/v1/lessons/\(id)")
    }

    func createLesson(request: CreateLessonRequest) async throws -> Lesson {
        try await performRequest(endpoint: "/v1/lessons", method: "POST", body: request)
    }

    func updateLesson(id: Int, request: CreateLessonRequest) async throws -> Lesson {
        try await performRequest(endpoint: "/v1/lessons/\(id)", method: "PUT", body: request)
    }

    func deleteLesson(id: Int) async throws -> MessageResponse {
        try await performRequest(endpoint: "/v1/lessons/\(id)", method: "DELETE")
    }

    func markLessonCompleted(id: Int, request: MarkLessonCompletedRequest) async throws -> CompletionResponse {
        try await performRequest(endpoint: "/v1/lessons/\(id)/complete", method: "POST", body: request)
    }

    func resetLessonProgress(id: Int) async throws -> MessageResponse {
        try await performRequest(endpoint: "/v1/lessons/\(id)/reset", method: "POST")
    }

    // MARK: - Exercises

    func getExercisesByLesson(lessonId: Int) async throws -> [Exercise] {
        try await performRequest(endpoint: "/v1/lessons/\(lessonId)/exercises")
    }

    func getExerciseById(id: Int) async throws -> ExerciseDetail {
        try await performRequest(endpoint: "/v1/exercises/\(id)")
    }

    func submitExerciseAttempt(id: Int, request: ExerciseAttemptRequest) async throws -> ExerciseAttemptResponse {
        try await performRequest(endpoint: "/v1/exercises/\(id)/attempt", method: "POST", body: request)
    }

    func getExerciseAttempts(id: Int, page: Int = 0, size: Int = 10) async throws -> PaginatedResponse<ExerciseAttemptResponse> {
        try await performRequest(endpoint: "/v1/exercises/\(id)/attempts?page=\(page)&size=\(size)")
    }

    // MARK: - Progress

    func getOverallProgress() async throws -> ProgressResponse {
        try await performRequest(endpoint: "/v1/progress")
    }

    func getCategoryProgress(categoryId: Int) async throws -> CategoryProgressResponse {
        try await performRequest(endpoint: "/v1/progress/categories/\(categoryId)")
    }

    func getLessonProgress(lessonId: Int) async throws -> LessonProgressResponse {
        try await performRequest(endpoint: "/v1/progress/lessons/\(lessonId)")
    }

    func updateDailyStreak() async throws -> StreakResponse {
        try await performRequest(endpoint: "/v1/progress/streak", method: "POST")
    }

    // MARK: - Achievements

    func getAllAchievements() async throws -> [Achievement] {
        try await performRequest(endpoint: "/v1/achievements")
    }

    func getUserAchievements() async throws -> [Achievement] {
        try await performRequest(endpoint: "/v1/achievements/me")
    }

    func getAchievementById(id: Int) async throws -> Achievement {
        try await performRequest(endpoint: "/v1/achievements/\(id)")
    }

    // MARK: - Subscription

    func getSubscriptionPlans() async throws -> [SubscriptionPlan] {
        try await performRequest(endpoint: "/v1/subscription/plans", requiresAuth: false)
    }

    func getCurrentSubscription() async throws -> UserSubscription {
        try await performRequest(endpoint: "/v1/subscription/me")
    }

    func subscribeToPlan(planId: Int, paymentMethodId: String, promoCode: String? = nil) async throws -> SubscriptionResponse {
        let body = SubscribeRequest(planId: planId, paymentMethodId: paymentMethodId, promoCode: promoCode)
        return try await performRequest(endpoint: "/v1/subscription/subscribe", method: "POST", body: body)
    }

    func cancelSubscription(reason: String? = nil, feedback: String? = nil) async throws -> MessageResponse {
        let body = CancelSubscriptionRequest(reason: reason, feedback: feedback)
        return try await performRequest(endpoint: "/v1/subscription/cancel", method: "POST", body: body)
    }

    func resumeSubscription() async throws -> MessageResponse {
        try await performRequest(endpoint: "/v1/subscription/resume", method: "POST")
    }

    // MARK: - Gems & Power-ups

    func getGemsBalance() async throws -> GemsBalance {
        try await performRequest(endpoint: "/v1/gems/balance")
    }

    func getGemPackages() async throws -> [GemPackage] {
        try await performRequest(endpoint: "/v1/gems/packages", requiresAuth: false)
    }

    func purchaseGems(packageId: Int, paymentMethodId: String) async throws -> GemPurchaseResponse {
        let body = PurchaseGemsRequest(packageId: packageId, paymentMethodId: paymentMethodId)
        return try await performRequest(endpoint: "/v1/gems/purchase", method: "POST", body: body)
    }

    func getAllPowerUps() async throws -> [PowerUp] {
        try await performRequest(endpoint: "/v1/power-ups")
    }

    func getUserPowerUps() async throws -> [PowerUp] {
        try await performRequest(endpoint: "/v1/power-ups/me")
    }

    func purchasePowerUp(id: Int, quantity: Int) async throws -> PowerUpPurchaseResponse {
        let body = PurchasePowerUpRequest(quantity: quantity)
        return try await performRequest(endpoint: "/v1/power-ups/\(id)/purchase", method: "POST", body: body)
    }

    func usePowerUp(id: Int, context: [String: Any]? = nil) async throws -> PowerUpUseResponse {
        let body = UsePowerUpRequest(context: context)
        return try await performRequest(endpoint: "/v1/power-ups/\(id)/use", method: "POST", body: body)
    }

    // MARK: - Leaderboard

    func getGlobalLeaderboard(period: LeaderboardPeriod = .weekly, page: Int = 0, size: Int = 50) async throws -> LeaderboardResponse {
        try await performRequest(endpoint: "/v1/leaderboard?period=\(period.rawValue)&page=\(page)&size=\(size)")
    }

    func getFriendsLeaderboard(period: LeaderboardPeriod = .weekly) async throws -> LeaderboardResponse {
        try await performRequest(endpoint: "/v1/leaderboard/friends?period=\(period.rawValue)")
    }

    func getCategoryLeaderboard(categoryId: Int, period: LeaderboardPeriod = .weekly) async throws -> LeaderboardResponse {
        try await performRequest(endpoint: "/v1/leaderboard/categories/\(categoryId)?period=\(period.rawValue)")
    }

    func searchUsers(query: String, page: Int = 0, size: Int = 20) async throws -> PaginatedResponse<User> {
        let encodedQuery = query.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? query
        return try await performRequest(endpoint: "/v1/users/search?q=\(encodedQuery)&page=\(page)&size=\(size)")
    }

    // MARK: - Health & System

    func healthCheck() async throws -> HealthResponse {
        try await performRequest(endpoint: "/api/health", requiresAuth: false)
    }

    func ping() async throws -> MessageResponse {
        try await performRequest(endpoint: "/api/ping", requiresAuth: false)
    }

    func getSystemInfo() async throws -> SystemInfoResponse {
        try await performRequest(endpoint: "/api/info", requiresAuth: false)
    }
}

// MARK: - Helper Response Models

struct MessageResponse: Codable {
    let message: String
}

struct CompletionResponse: Codable {
    let message: String
    let pointsEarned: Int
    let newTotalPoints: Int
    let levelUp: Bool
    let achievements: [Achievement]?
}

struct ProgressResponse: Codable {
    let userId: Int
    let totalPoints: Int
    let level: Int
    let currentStreak: Int
    let longestStreak: Int
    let totalLessonsCompleted: Int
    let totalExercisesCompleted: Int
    let totalTimeSpent: Int
    let averageAccuracy: Double
    let categories: [CategoryProgress]

    struct CategoryProgress: Codable {
        let categoryId: Int
        let categoryName: String
        let completedLessons: Int
        let totalLessons: Int
        let progressPercentage: Double
        let lastAccessedAt: String?
    }
}

struct CategoryProgressResponse: Codable {
    let categoryId: Int
    let categoryName: String
    let completedLessons: Int
    let totalLessons: Int
    let progressPercentage: Double
    let lessons: [LessonProgress]
    let lastAccessedAt: String?

    struct LessonProgress: Codable {
        let lessonId: Int
        let lessonTitle: String
        let isCompleted: Bool
        let completedAt: String?
        let timeSpent: Int
        let attempts: Int
        let bestScore: Int?
    }
}

struct LessonProgressResponse: Codable {
    let lessonId: Int
    let lessonTitle: String
    let isCompleted: Bool
    let completedAt: String?
    let attempts: Int
    let totalTimeSpent: Int
    let exercises: [ExerciseProgress]

    struct ExerciseProgress: Codable {
        let exerciseId: Int
        let exerciseTitle: String
        let isCompleted: Bool
        let bestScore: Int?
        let attempts: Int
        let lastAttemptAt: String?
    }
}

struct StreakResponse: Codable {
    let currentStreak: Int
    let longestStreak: Int
    let streakBroken: Bool
    let reward: Reward?

    struct Reward: Codable {
        let points: Int
        let message: String
    }
}

struct SubscribeRequest: Codable {
    let planId: Int
    let paymentMethodId: String
    let promoCode: String?
}

struct SubscriptionResponse: Codable {
    let subscriptionId: Int
    let status: String
    let message: String
    let payment: Payment

    struct Payment: Codable {
        let amount: Double
        let currency: String
        let discount: Double
        let paymentId: String
    }
}

struct CancelSubscriptionRequest: Codable {
    let reason: String?
    let feedback: String?
}

struct PurchaseGemsRequest: Codable {
    let packageId: Int
    let paymentMethodId: String
}

struct GemPurchaseResponse: Codable {
    let transactionId: Int
    let gemsAdded: Int
    let newBalance: Int
    let payment: Payment

    struct Payment: Codable {
        let amount: Double
        let currency: String
        let paymentId: String
    }
}

struct PurchasePowerUpRequest: Codable {
    let quantity: Int
}

struct PowerUpPurchaseResponse: Codable {
    let message: String
    let powerUpId: Int
    let quantity: Int
    let totalCost: Int
    let remainingGems: Int
    let newQuantity: Int
}

struct UsePowerUpRequest: Codable {
    let context: [String: Any]?

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        if let context = context {
            let data = try JSONSerialization.data(withJSONObject: context)
            try container.encode(data, forKey: .context)
        }
    }

    enum CodingKeys: String, CodingKey {
        case context
    }
}

struct PowerUpUseResponse: Codable {
    let message: String
    let effect: String
    let remainingQuantity: Int
}

struct HealthResponse: Codable {
    let status: String
    let timestamp: String
    let service: String
}

struct SystemInfoResponse: Codable {
    let applicationName: String
    let version: String
    let environment: String
    let serverTime: String
    let javaVersion: String
    let springBootVersion: String
}

// MARK: - Singleton

extension MorseMateAPI {
    static let shared = MorseMateAPI()
}
