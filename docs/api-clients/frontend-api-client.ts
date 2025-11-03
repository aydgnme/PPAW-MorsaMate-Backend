/**
 * MorseMate API Client
 * TypeScript/JavaScript API client for frontend integration
 *
 * @version 1.0.0
 * @author MorseMate Team
 */

// ==================== TYPES & INTERFACES ====================

export interface ApiConfig {
  baseUrl: string;
  token?: string;
  onTokenExpired?: () => void;
  onError?: (error: ApiError) => void;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: Array<{
    field: string;
    message: string;
  }>;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: { sorted: boolean };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

// Auth Types
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
  rememberMe?: boolean;
}

export interface AuthResponse {
  token: string;
  expiresIn: number;
  user: User;
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  profilePictureUrl?: string;
  level: number;
  hearts: number;
  maxHearts: number;
  totalPoints: number;
  currentStreak: number;
  longestStreak: number;
  isActive: boolean;
  emailVerified: boolean;
  createdAt: string;
  lastLogin?: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  username?: string;
  profilePictureUrl?: string;
}

export interface UserStatistics {
  totalPoints: number;
  level: number;
  currentStreak: number;
  longestStreak: number;
  hearts: number;
  maxHearts: number;
  completedLessons: number;
  totalLessons: number;
  completedExercises: number;
  totalExercises: number;
  averageAccuracy: number;
  totalTimeSpent: number;
  achievements: number;
  rank: string;
  nextLevelPoints: number;
}

// Category Types
export interface Category {
  id: number;
  name: string;
  description: string;
  displayOrder: number;
  iconUrl?: string;
  lessonCount: number;
  completedLessons?: number;
  isActive: boolean;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
  displayOrder?: number;
  iconUrl?: string;
  isActive?: boolean;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  displayOrder?: number;
  iconUrl?: string;
  isActive?: boolean;
}

// Lesson Types
export interface Lesson {
  id: number;
  categoryId: number;
  categoryName?: string;
  title: string;
  description: string;
  morseCode: string;
  audioUrl?: string;
  videoUrl?: string;
  imageUrl?: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  displayOrder: number;
  estimatedDuration: number;
  requiredPointsToUnlock: number;
  isLocked: boolean;
  isCompleted: boolean;
  exercises?: Exercise[];
  userProgress?: {
    completionPercentage: number;
    lastAttemptDate: string;
    attempts: number;
  };
}

export interface CreateLessonRequest {
  categoryId: number;
  title: string;
  description: string;
  morseCode: string;
  audioUrl?: string;
  videoUrl?: string;
  imageUrl?: string;
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  displayOrder: number;
  estimatedDuration: number;
  requiredPointsToUnlock?: number;
  isActive?: boolean;
}

export interface MarkLessonCompletedRequest {
  timeSpent: number;
  accuracy: number;
}

// Exercise Types
export interface Exercise {
  id: number;
  lessonId: number;
  title: string;
  description: string;
  type: 'LISTENING' | 'TYPING' | 'MULTIPLE_CHOICE' | 'MATCHING';
  difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  displayOrder: number;
  questionCount?: number;
  timeLimit: number;
  passingScore: number;
  isCompleted: boolean;
  bestScore?: number;
  attempts?: number;
}

export interface ExerciseQuestion {
  id: number;
  question: string;
  audioUrl?: string;
  imageUrl?: string;
  options: string[];
  correctAnswer: string;
  explanation?: string;
}

export interface ExerciseAttemptRequest {
  answers: Array<{
    questionId: number;
    selectedAnswer: string;
    timeSpent: number;
  }>;
  totalTimeSpent: number;
}

export interface ExerciseAttemptResponse {
  attemptId: number;
  score: number;
  passed: boolean;
  correctAnswers: number;
  incorrectAnswers: number;
  totalQuestions: number;
  accuracy: number;
  timeSpent: number;
  pointsEarned: number;
  isNewBestScore: boolean;
  achievements?: Achievement[];
  feedback: Array<{
    questionId: number;
    isCorrect: boolean;
    correctAnswer: string;
    yourAnswer: string;
    explanation?: string;
  }>;
}

// Achievement Types
export interface Achievement {
  id: number;
  name: string;
  description: string;
  iconUrl?: string;
  category: 'PROGRESS' | 'STREAK' | 'SOCIAL' | 'SPECIAL';
  rarity: 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
  points: number;
  isUnlocked: boolean;
  unlockedAt?: string;
  progress?: {
    current: number;
    target: number;
    percentage: number;
  };
}

// Subscription Types
export interface SubscriptionPlan {
  id: number;
  name: string;
  description: string;
  price: number;
  currency: string;
  billingPeriod: 'MONTHLY' | 'YEARLY';
  features: string[];
  isPopular: boolean;
}

export interface UserSubscription {
  id: number;
  userId: number;
  planId: number;
  planName: string;
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startDate: string;
  endDate: string;
  autoRenew: boolean;
  paymentMethod: string;
  nextBillingDate?: string;
}

// Gems & Power-ups Types
export interface GemsBalance {
  userId: number;
  totalGems: number;
  earnedGems: number;
  purchasedGems: number;
  spentGems: number;
  lifetimeGems: number;
}

export interface GemPackage {
  id: number;
  name: string;
  gems: number;
  price: number;
  currency: string;
  bonus: number;
  isPopular: boolean;
}

export interface PowerUp {
  id: number;
  name: string;
  description: string;
  iconUrl?: string;
  cost: number;
  costType: 'GEMS' | 'PREMIUM';
  isAvailable: boolean;
  ownedQuantity?: number;
}

// Leaderboard Types
export interface LeaderboardEntry {
  rank: number;
  userId: number;
  username: string;
  fullName?: string;
  profilePictureUrl?: string;
  points: number;
  level: number;
  streak: number;
  isCurrentUser: boolean;
}

export interface LeaderboardResponse {
  period: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME';
  currentUserRank: number;
  leaderboard: LeaderboardEntry[];
  totalPlayers: number;
}

// ==================== API CLIENT ====================

export class MorseMateApi {
  private config: ApiConfig;

  constructor(config: ApiConfig) {
    this.config = config;
  }

  // ==================== PRIVATE METHODS ====================

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.config.baseUrl}${endpoint}`;

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (this.config.token) {
      headers['Authorization'] = `Bearer ${this.config.token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (!response.ok) {
        const error: ApiError = await response.json().catch(() => ({
          timestamp: new Date().toISOString(),
          status: response.status,
          error: response.statusText,
          message: 'An error occurred',
          path: endpoint,
        }));

        if (response.status === 401 && this.config.onTokenExpired) {
          this.config.onTokenExpired();
        }

        if (this.config.onError) {
          this.config.onError(error);
        }

        throw error;
      }

      // Handle 204 No Content
      if (response.status === 204) {
        return {} as T;
      }

      return await response.json();
    } catch (error) {
      if (error instanceof Error) {
        const apiError: ApiError = {
          timestamp: new Date().toISOString(),
          status: 0,
          error: 'Network Error',
          message: error.message,
          path: endpoint,
        };

        if (this.config.onError) {
          this.config.onError(apiError);
        }

        throw apiError;
      }
      throw error;
    }
  }

  private get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  private post<T>(endpoint: string, body?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  private put<T>(endpoint: string, body?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  }

  private delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }

  // ==================== PUBLIC METHODS ====================

  // Set authentication token
  setToken(token: string): void {
    this.config.token = token;
  }

  // Clear authentication token
  clearToken(): void {
    this.config.token = undefined;
  }

  // ==================== AUTHENTICATION ====================

  async register(data: RegisterRequest): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/register', data);
  }

  async login(data: LoginRequest): Promise<AuthResponse> {
    return this.post<AuthResponse>('/auth/login', data);
  }

  async getCurrentUser(): Promise<User> {
    return this.get<User>('/auth/me');
  }

  async logout(): Promise<void> {
    return this.post<void>('/auth/logout');
  }

  // ==================== USER MANAGEMENT ====================

  async getUserProfile(): Promise<User> {
    return this.get<User>('/users/me');
  }

  async updateProfile(data: UpdateProfileRequest): Promise<User> {
    return this.put<User>('/users/me', data);
  }

  async getUserById(id: number): Promise<User> {
    return this.get<User>(`/users/${id}`);
  }

  async getUserByUsername(username: string): Promise<User> {
    return this.get<User>(`/users/username/${username}`);
  }

  async getAllUsers(page = 0, size = 10, sort = 'createdAt,desc'): Promise<PaginatedResponse<User>> {
    return this.get<PaginatedResponse<User>>(`/users?page=${page}&size=${size}&sort=${sort}`);
  }

  async getUserStatistics(): Promise<UserStatistics> {
    return this.get<UserStatistics>('/users/me/statistics');
  }

  async useHeart(): Promise<{ message: string; remainingHearts: number }> {
    return this.post('/users/me/hearts/use');
  }

  async refillHearts(): Promise<{ message: string; hearts: number }> {
    return this.post('/users/me/hearts/refill');
  }

  async deleteAccount(): Promise<{ message: string }> {
    return this.delete('/users/me');
  }

  // ==================== CATEGORIES ====================

  async getAllCategories(): Promise<Category[]> {
    return this.get<Category[]>('/v1/categories');
  }

  async getCategoryById(id: number): Promise<Category> {
    return this.get<Category>(`/v1/categories/${id}`);
  }

  async createCategory(data: CreateCategoryRequest): Promise<Category> {
    return this.post<Category>('/v1/categories', data);
  }

  async updateCategory(id: number, data: UpdateCategoryRequest): Promise<Category> {
    return this.put<Category>(`/v1/categories/${id}`, data);
  }

  async deleteCategory(id: number): Promise<{ message: string }> {
    return this.delete(`/v1/categories/${id}`);
  }

  async getAllCategoriesAdmin(): Promise<Category[]> {
    return this.get<Category[]>('/v1/categories/admin/all');
  }

  // ==================== LESSONS ====================

  async getLessonsByCategory(categoryId: number): Promise<Lesson[]> {
    return this.get<Lesson[]>(`/v1/categories/${categoryId}/lessons`);
  }

  async getLessonById(id: number): Promise<Lesson> {
    return this.get<Lesson>(`/v1/lessons/${id}`);
  }

  async createLesson(data: CreateLessonRequest): Promise<Lesson> {
    return this.post<Lesson>('/v1/lessons', data);
  }

  async updateLesson(id: number, data: Partial<CreateLessonRequest>): Promise<Lesson> {
    return this.put<Lesson>(`/v1/lessons/${id}`, data);
  }

  async deleteLesson(id: number): Promise<{ message: string }> {
    return this.delete(`/v1/lessons/${id}`);
  }

  async markLessonCompleted(id: number, data: MarkLessonCompletedRequest): Promise<any> {
    return this.post(`/v1/lessons/${id}/complete`, data);
  }

  async resetLessonProgress(id: number): Promise<{ message: string }> {
    return this.post(`/v1/lessons/${id}/reset`);
  }

  // ==================== EXERCISES ====================

  async getExercisesByLesson(lessonId: number): Promise<Exercise[]> {
    return this.get<Exercise[]>(`/v1/lessons/${lessonId}/exercises`);
  }

  async getExerciseById(id: number): Promise<Exercise & { questions: ExerciseQuestion[] }> {
    return this.get(`/v1/exercises/${id}`);
  }

  async submitExerciseAttempt(id: number, data: ExerciseAttemptRequest): Promise<ExerciseAttemptResponse> {
    return this.post(`/v1/exercises/${id}/attempt`, data);
  }

  async getExerciseAttempts(id: number, page = 0, size = 10): Promise<PaginatedResponse<any>> {
    return this.get(`/v1/exercises/${id}/attempts?page=${page}&size=${size}`);
  }

  // ==================== PROGRESS ====================

  async getOverallProgress(): Promise<any> {
    return this.get('/v1/progress');
  }

  async getCategoryProgress(categoryId: number): Promise<any> {
    return this.get(`/v1/progress/categories/${categoryId}`);
  }

  async getLessonProgress(lessonId: number): Promise<any> {
    return this.get(`/v1/progress/lessons/${lessonId}`);
  }

  async updateDailyStreak(): Promise<any> {
    return this.post('/v1/progress/streak');
  }

  // ==================== ACHIEVEMENTS ====================

  async getAllAchievements(): Promise<Achievement[]> {
    return this.get<Achievement[]>('/v1/achievements');
  }

  async getUserAchievements(): Promise<Achievement[]> {
    return this.get<Achievement[]>('/v1/achievements/me');
  }

  async getAchievementById(id: number): Promise<Achievement> {
    return this.get<Achievement>(`/v1/achievements/${id}`);
  }

  // ==================== SUBSCRIPTION ====================

  async getSubscriptionPlans(): Promise<SubscriptionPlan[]> {
    return this.get<SubscriptionPlan[]>('/v1/subscription/plans');
  }

  async getCurrentSubscription(): Promise<UserSubscription> {
    return this.get<UserSubscription>('/v1/subscription/me');
  }

  async subscribeToPlan(planId: number, paymentMethodId: string, promoCode?: string): Promise<any> {
    return this.post('/v1/subscription/subscribe', { planId, paymentMethodId, promoCode });
  }

  async cancelSubscription(reason?: string, feedback?: string): Promise<{ message: string }> {
    return this.post('/v1/subscription/cancel', { reason, feedback });
  }

  async resumeSubscription(): Promise<{ message: string }> {
    return this.post('/v1/subscription/resume');
  }

  // ==================== GEMS & POWER-UPS ====================

  async getGemsBalance(): Promise<GemsBalance> {
    return this.get<GemsBalance>('/v1/gems/balance');
  }

  async getGemPackages(): Promise<GemPackage[]> {
    return this.get<GemPackage[]>('/v1/gems/packages');
  }

  async purchaseGems(packageId: number, paymentMethodId: string): Promise<any> {
    return this.post('/v1/gems/purchase', { packageId, paymentMethodId });
  }

  async getGemTransactions(page = 0, size = 20): Promise<PaginatedResponse<any>> {
    return this.get(`/v1/gems/transactions?page=${page}&size=${size}`);
  }

  async getAllPowerUps(): Promise<PowerUp[]> {
    return this.get<PowerUp[]>('/v1/power-ups');
  }

  async getUserPowerUps(): Promise<PowerUp[]> {
    return this.get<PowerUp[]>('/v1/power-ups/me');
  }

  async purchasePowerUp(id: number, quantity: number): Promise<any> {
    return this.post(`/v1/power-ups/${id}/purchase`, { quantity });
  }

  async usePowerUp(id: number, context?: any): Promise<any> {
    return this.post(`/v1/power-ups/${id}/use`, { context });
  }

  // ==================== LEADERBOARD ====================

  async getGlobalLeaderboard(
    period: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME' = 'WEEKLY',
    page = 0,
    size = 50
  ): Promise<LeaderboardResponse> {
    return this.get(`/v1/leaderboard?period=${period}&page=${page}&size=${size}`);
  }

  async getFriendsLeaderboard(period: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME' = 'WEEKLY'): Promise<LeaderboardResponse> {
    return this.get(`/v1/leaderboard/friends?period=${period}`);
  }

  async getCategoryLeaderboard(
    categoryId: number,
    period: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME' = 'WEEKLY'
  ): Promise<LeaderboardResponse> {
    return this.get(`/v1/leaderboard/categories/${categoryId}?period=${period}`);
  }

  async searchUsers(query: string, page = 0, size = 20): Promise<PaginatedResponse<User>> {
    return this.get(`/v1/users/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  }

  // ==================== HEALTH & SYSTEM ====================

  async healthCheck(): Promise<{ status: string; timestamp: string; service: string }> {
    return this.get('/api/health');
  }

  async ping(): Promise<{ message: string }> {
    return this.get('/api/ping');
  }

  async getSystemInfo(): Promise<any> {
    return this.get('/api/info');
  }

  async getApiRoot(): Promise<any> {
    return this.get('/');
  }
}

// ==================== SINGLETON INSTANCE ====================

let apiInstance: MorseMateApi | null = null;

export function createApiClient(config: ApiConfig): MorseMateApi {
  apiInstance = new MorseMateApi(config);
  return apiInstance;
}

export function getApiClient(): MorseMateApi {
  if (!apiInstance) {
    throw new Error('API client not initialized. Call createApiClient first.');
  }
  return apiInstance;
}

// ==================== USAGE EXAMPLE ====================

/*
// Initialize the API client
const api = createApiClient({
  baseUrl: 'http://localhost:8080',
  onTokenExpired: () => {
    console.log('Token expired, redirecting to login...');
    // Redirect to login page
  },
  onError: (error) => {
    console.error('API Error:', error);
    // Show error notification
  },
});

// Register
try {
  const response = await api.register({
    username: 'john_doe',
    email: 'john@example.com',
    password: 'SecurePass123',
    fullName: 'John Doe',
  });

  // Save token
  api.setToken(response.token);
  localStorage.setItem('token', response.token);

  console.log('User registered:', response.user);
} catch (error) {
  console.error('Registration failed:', error);
}

// Login
try {
  const response = await api.login({
    identifier: 'john@example.com',
    password: 'SecurePass123',
  });

  api.setToken(response.token);
  localStorage.setItem('token', response.token);
} catch (error) {
  console.error('Login failed:', error);
}

// Get categories
const categories = await api.getAllCategories();

// Get lessons for a category
const lessons = await api.getLessonsByCategory(1);

// Submit exercise attempt
const result = await api.submitExerciseAttempt(1, {
  answers: [
    { questionId: 1, selectedAnswer: 'A', timeSpent: 5 },
    { questionId: 2, selectedAnswer: 'B', timeSpent: 4 },
  ],
  totalTimeSpent: 45,
});

// Get leaderboard
const leaderboard = await api.getGlobalLeaderboard('WEEKLY');

// Use heart
await api.useHeart();

// Get user statistics
const stats = await api.getUserStatistics();
*/

export default MorseMateApi;
