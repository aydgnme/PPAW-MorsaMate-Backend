# MorseMate Frontend Integration Examples

Complete examples for integrating MorseMate API with popular frontend frameworks.

---

## 📦 Installation

```bash
# Copy the API client to your project
cp frontend-api-client.ts src/services/api.ts

# Install dependencies (if not already installed)
npm install
```

---

## ⚙️ Setup

### 1. Initialize API Client

```typescript
// src/services/api.ts
import { createApiClient } from './frontend-api-client';

const api = createApiClient({
  baseUrl: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  onTokenExpired: () => {
    // Clear token and redirect to login
    localStorage.removeItem('token');
    window.location.href = '/login';
  },
  onError: (error) => {
    console.error('API Error:', error);
    // You can show a toast notification here
  },
});

// Load token from localStorage on init
const token = localStorage.getItem('token');
if (token) {
  api.setToken(token);
}

export default api;
```

---

## 🎣 React Hooks

### useAuth Hook

```typescript
// src/hooks/useAuth.ts
import { useState, useEffect, createContext, useContext, ReactNode } from 'react';
import api from '../services/api';
import { User, LoginRequest, RegisterRequest } from '../services/api';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        api.setToken(token);
        const currentUser = await api.getCurrentUser();
        setUser(currentUser);
      } catch (error) {
        console.error('Auth check failed:', error);
        localStorage.removeItem('token');
      }
    }
    setLoading(false);
  };

  const login = async (data: LoginRequest) => {
    const response = await api.login(data);
    localStorage.setItem('token', response.token);
    api.setToken(response.token);
    setUser(response.user);
  };

  const register = async (data: RegisterRequest) => {
    const response = await api.register(data);
    localStorage.setItem('token', response.token);
    api.setToken(response.token);
    setUser(response.user);
  };

  const logout = () => {
    localStorage.removeItem('token');
    api.clearToken();
    setUser(null);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
```

---

### useCategories Hook

```typescript
// src/hooks/useCategories.ts
import { useState, useEffect } from 'react';
import api from '../services/api';
import { Category } from '../services/api';

export function useCategories() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await api.getAllCategories();
      setCategories(data);
      setError(null);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  return { categories, loading, error, refetch: fetchCategories };
}
```

---

### useLessons Hook

```typescript
// src/hooks/useLessons.ts
import { useState, useEffect } from 'react';
import api from '../services/api';
import { Lesson } from '../services/api';

export function useLessons(categoryId: number) {
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (categoryId) {
      fetchLessons();
    }
  }, [categoryId]);

  const fetchLessons = async () => {
    try {
      setLoading(true);
      const data = await api.getLessonsByCategory(categoryId);
      setLessons(data);
      setError(null);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  const completeLesson = async (lessonId: number, timeSpent: number, accuracy: number) => {
    await api.markLessonCompleted(lessonId, { timeSpent, accuracy });
    await fetchLessons(); // Refresh lessons
  };

  return { lessons, loading, error, refetch: fetchLessons, completeLesson };
}
```

---

### useExercise Hook

```typescript
// src/hooks/useExercise.ts
import { useState, useEffect } from 'react';
import api from '../services/api';
import { Exercise, ExerciseQuestion, ExerciseAttemptRequest } from '../services/api';

export function useExercise(exerciseId: number) {
  const [exercise, setExercise] = useState<(Exercise & { questions: ExerciseQuestion[] }) | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (exerciseId) {
      fetchExercise();
    }
  }, [exerciseId]);

  const fetchExercise = async () => {
    try {
      setLoading(true);
      const data = await api.getExerciseById(exerciseId);
      setExercise(data);
      setError(null);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  const submitAttempt = async (attempt: ExerciseAttemptRequest) => {
    try {
      setSubmitting(true);
      const result = await api.submitExerciseAttempt(exerciseId, attempt);
      return result;
    } catch (err) {
      throw err;
    } finally {
      setSubmitting(false);
    }
  };

  return { exercise, loading, error, submitting, refetch: fetchExercise, submitAttempt };
}
```

---

### useLeaderboard Hook

```typescript
// src/hooks/useLeaderboard.ts
import { useState, useEffect } from 'react';
import api from '../services/api';
import { LeaderboardResponse } from '../services/api';

type Period = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME';

export function useLeaderboard(period: Period = 'WEEKLY') {
  const [leaderboard, setLeaderboard] = useState<LeaderboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    fetchLeaderboard();
  }, [period]);

  const fetchLeaderboard = async () => {
    try {
      setLoading(true);
      const data = await api.getGlobalLeaderboard(period);
      setLeaderboard(data);
      setError(null);
    } catch (err) {
      setError(err as Error);
    } finally {
      setLoading(false);
    }
  };

  return { leaderboard, loading, error, refetch: fetchLeaderboard };
}
```

---

## 📱 React Components Examples

### Login Component

```tsx
// src/components/Login.tsx
import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    identifier: '',
    password: '',
    rememberMe: false,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await login(formData);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h1>Login to MorseMate</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Email or Username"
          value={formData.identifier}
          onChange={(e) => setFormData({ ...formData, identifier: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
          required
        />
        <label>
          <input
            type="checkbox"
            checked={formData.rememberMe}
            onChange={(e) => setFormData({ ...formData, rememberMe: e.target.checked })}
          />
          Remember me
        </label>
        {error && <div className="error">{error}</div>}
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </div>
  );
}
```

---

### Categories List Component

```tsx
// src/components/CategoriesList.tsx
import { useCategories } from '../hooks/useCategories';
import { Link } from 'react-router-dom';

export function CategoriesList() {
  const { categories, loading, error } = useCategories();

  if (loading) return <div>Loading categories...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div className="categories-grid">
      {categories.map((category) => (
        <Link key={category.id} to={`/categories/${category.id}`} className="category-card">
          {category.iconUrl && <img src={category.iconUrl} alt={category.name} />}
          <h3>{category.name}</h3>
          <p>{category.description}</p>
          <div className="progress">
            {category.completedLessons} / {category.lessonCount} lessons completed
          </div>
        </Link>
      ))}
    </div>
  );
}
```

---

### Lessons List Component

```tsx
// src/components/LessonsList.tsx
import { useLessons } from '../hooks/useLessons';
import { useParams, Link } from 'react-router-dom';

export function LessonsList() {
  const { categoryId } = useParams<{ categoryId: string }>();
  const { lessons, loading, error } = useLessons(Number(categoryId));

  if (loading) return <div>Loading lessons...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <div className="lessons-list">
      {lessons.map((lesson) => (
        <Link
          key={lesson.id}
          to={`/lessons/${lesson.id}`}
          className={`lesson-card ${lesson.isCompleted ? 'completed' : ''} ${
            lesson.isLocked ? 'locked' : ''
          }`}
        >
          <div className="lesson-header">
            <h3>{lesson.title}</h3>
            <span className={`difficulty ${lesson.difficulty.toLowerCase()}`}>
              {lesson.difficulty}
            </span>
          </div>
          <p>{lesson.description}</p>
          <div className="lesson-stats">
            <span>🕐 {lesson.estimatedDuration}s</span>
            <span>📝 Morse: {lesson.morseCode}</span>
          </div>
          {lesson.isCompleted && <div className="completed-badge">✓ Completed</div>}
          {lesson.isLocked && <div className="locked-badge">🔒 Locked</div>}
        </Link>
      ))}
    </div>
  );
}
```

---

### Exercise Component

```tsx
// src/components/Exercise.tsx
import { useState, useEffect } from 'react';
import { useExercise } from '../hooks/useExercise';
import { useParams, useNavigate } from 'react-router-dom';

export function Exercise() {
  const { exerciseId } = useParams<{ exerciseId: string }>();
  const navigate = useNavigate();
  const { exercise, loading, submitting, submitAttempt } = useExercise(Number(exerciseId));

  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [answers, setAnswers] = useState<Array<{ questionId: number; selectedAnswer: string; timeSpent: number }>>([]);
  const [startTime, setStartTime] = useState(Date.now());
  const [totalStartTime] = useState(Date.now());

  if (loading) return <div>Loading exercise...</div>;
  if (!exercise) return <div>Exercise not found</div>;

  const currentQ = exercise.questions[currentQuestion];

  const handleAnswer = (answer: string) => {
    const timeSpent = Math.floor((Date.now() - startTime) / 1000);

    setAnswers([
      ...answers,
      {
        questionId: currentQ.id,
        selectedAnswer: answer,
        timeSpent,
      },
    ]);

    if (currentQuestion < exercise.questions.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
      setStartTime(Date.now());
    } else {
      handleSubmit([
        ...answers,
        { questionId: currentQ.id, selectedAnswer: answer, timeSpent },
      ]);
    }
  };

  const handleSubmit = async (finalAnswers: typeof answers) => {
    const totalTimeSpent = Math.floor((Date.now() - totalStartTime) / 1000);

    try {
      const result = await submitAttempt({
        answers: finalAnswers,
        totalTimeSpent,
      });

      // Navigate to results page
      navigate(`/exercises/${exerciseId}/results`, { state: { result } });
    } catch (error) {
      console.error('Submit failed:', error);
    }
  };

  return (
    <div className="exercise-container">
      <div className="exercise-header">
        <h2>{exercise.title}</h2>
        <div className="progress">
          Question {currentQuestion + 1} of {exercise.questions.length}
        </div>
      </div>

      <div className="question-card">
        <h3>{currentQ.question}</h3>
        {currentQ.audioUrl && (
          <audio controls src={currentQ.audioUrl}>
            Your browser does not support audio.
          </audio>
        )}
        {currentQ.imageUrl && <img src={currentQ.imageUrl} alt="Question" />}

        <div className="options">
          {currentQ.options.map((option) => (
            <button
              key={option}
              onClick={() => handleAnswer(option)}
              className="option-button"
              disabled={submitting}
            >
              {option}
            </button>
          ))}
        </div>
      </div>

      <div className="exercise-footer">
        <div className="timer">⏱️ Time limit: {exercise.timeLimit}s</div>
        <div className="passing-score">Passing score: {exercise.passingScore}%</div>
      </div>
    </div>
  );
}
```

---

### Leaderboard Component

```tsx
// src/components/Leaderboard.tsx
import { useState } from 'react';
import { useLeaderboard } from '../hooks/useLeaderboard';

type Period = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ALL_TIME';

export function Leaderboard() {
  const [period, setPeriod] = useState<Period>('WEEKLY');
  const { leaderboard, loading, error } = useLeaderboard(period);

  if (loading) return <div>Loading leaderboard...</div>;
  if (error) return <div>Error: {error.message}</div>;
  if (!leaderboard) return null;

  return (
    <div className="leaderboard-container">
      <h2>Leaderboard</h2>

      <div className="period-tabs">
        {(['DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME'] as Period[]).map((p) => (
          <button
            key={p}
            className={period === p ? 'active' : ''}
            onClick={() => setPeriod(p)}
          >
            {p.replace('_', ' ')}
          </button>
        ))}
      </div>

      <div className="leaderboard-list">
        {leaderboard.leaderboard.map((entry) => (
          <div
            key={entry.userId}
            className={`leaderboard-entry ${entry.isCurrentUser ? 'current-user' : ''}`}
          >
            <div className="rank">#{entry.rank}</div>
            <div className="user-info">
              {entry.profilePictureUrl && (
                <img src={entry.profilePictureUrl} alt={entry.username} />
              )}
              <div>
                <div className="username">{entry.username}</div>
                {entry.fullName && <div className="full-name">{entry.fullName}</div>}
              </div>
            </div>
            <div className="stats">
              <span>⭐ {entry.points} pts</span>
              <span>📊 Level {entry.level}</span>
              <span>🔥 {entry.streak} day streak</span>
            </div>
          </div>
        ))}
      </div>

      <div className="your-rank">
        Your rank: #{leaderboard.currentUserRank} of {leaderboard.totalPlayers} players
      </div>
    </div>
  );
}
```

---

### User Stats Component

```tsx
// src/components/UserStats.tsx
import { useState, useEffect } from 'react';
import api from '../services/api';
import { UserStatistics } from '../services/api';

export function UserStats() {
  const [stats, setStats] = useState<UserStatistics | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const data = await api.getUserStatistics();
      setStats(data);
    } catch (error) {
      console.error('Failed to fetch stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading stats...</div>;
  if (!stats) return null;

  return (
    <div className="user-stats">
      <div className="stat-card">
        <div className="stat-value">{stats.totalPoints}</div>
        <div className="stat-label">Total Points</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.level}</div>
        <div className="stat-label">Level</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.currentStreak}🔥</div>
        <div className="stat-label">Current Streak</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">
          {stats.completedLessons}/{stats.totalLessons}
        </div>
        <div className="stat-label">Lessons</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">{stats.averageAccuracy.toFixed(1)}%</div>
        <div className="stat-label">Accuracy</div>
      </div>
      <div className="stat-card">
        <div className="stat-value">
          ❤️ {stats.hearts}/{stats.maxHearts}
        </div>
        <div className="stat-label">Hearts</div>
      </div>
    </div>
  );
}
```

---

## 🔄 Vue.js Composables

### useAuth Composable

```typescript
// src/composables/useAuth.ts
import { ref, computed } from 'vue';
import api from '../services/api';
import { User, LoginRequest, RegisterRequest } from '../services/api';

const user = ref<User | null>(null);
const loading = ref(true);

export function useAuth() {
  const isAuthenticated = computed(() => !!user.value);

  const checkAuth = async () => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        api.setToken(token);
        user.value = await api.getCurrentUser();
      } catch (error) {
        console.error('Auth check failed:', error);
        localStorage.removeItem('token');
      }
    }
    loading.value = false;
  };

  const login = async (data: LoginRequest) => {
    const response = await api.login(data);
    localStorage.setItem('token', response.token);
    api.setToken(response.token);
    user.value = response.user;
  };

  const register = async (data: RegisterRequest) => {
    const response = await api.register(data);
    localStorage.setItem('token', response.token);
    api.setToken(response.token);
    user.value = response.user;
  };

  const logout = () => {
    localStorage.removeItem('token');
    api.clearToken();
    user.value = null;
    window.location.href = '/login';
  };

  return {
    user,
    loading,
    isAuthenticated,
    checkAuth,
    login,
    register,
    logout,
  };
}
```

---

### useCategories Composable

```typescript
// src/composables/useCategories.ts
import { ref, onMounted } from 'vue';
import api from '../services/api';
import { Category } from '../services/api';

export function useCategories() {
  const categories = ref<Category[]>([]);
  const loading = ref(true);
  const error = ref<Error | null>(null);

  const fetchCategories = async () => {
    try {
      loading.value = true;
      categories.value = await api.getAllCategories();
      error.value = null;
    } catch (err) {
      error.value = err as Error;
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchCategories();
  });

  return {
    categories,
    loading,
    error,
    refetch: fetchCategories,
  };
}
```

---

## 📋 App.tsx Setup

```tsx
// src/App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './hooks/useAuth';
import { Login } from './components/Login';
import { Register } from './components/Register';
import { Dashboard } from './components/Dashboard';
import { CategoriesList } from './components/CategoriesList';
import { LessonsList } from './components/LessonsList';
import { Exercise } from './components/Exercise';
import { Leaderboard } from './components/Leaderboard';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return <div>Loading...</div>;
  if (!isAuthenticated) return <Navigate to="/login" />;

  return <>{children}</>;
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/categories"
            element={
              <ProtectedRoute>
                <CategoriesList />
              </ProtectedRoute>
            }
          />

          <Route
            path="/categories/:categoryId"
            element={
              <ProtectedRoute>
                <LessonsList />
              </ProtectedRoute>
            }
          />

          <Route
            path="/exercises/:exerciseId"
            element={
              <ProtectedRoute>
                <Exercise />
              </ProtectedRoute>
            }
          />

          <Route
            path="/leaderboard"
            element={
              <ProtectedRoute>
                <Leaderboard />
              </ProtectedRoute>
            }
          />

          <Route path="/" element={<Navigate to="/dashboard" />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
```

---

## 🎨 CSS Example

```css
/* src/styles/components.css */

/* Category Card */
.category-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-card img {
  width: 60px;
  height: 60px;
  margin-bottom: 12px;
}

.category-card h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.category-card p {
  color: #666;
  font-size: 14px;
  margin: 0 0 12px 0;
}

.category-card .progress {
  font-size: 12px;
  color: #999;
}

/* Lesson Card */
.lesson-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  text-decoration: none;
  color: inherit;
  display: block;
  transition: all 0.2s;
}

.lesson-card:hover {
  border-color: #4CAF50;
}

.lesson-card.completed {
  border-color: #4CAF50;
  background-color: #f1f8f4;
}

.lesson-card.locked {
  opacity: 0.5;
  cursor: not-allowed;
}

.difficulty {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  text-transform: uppercase;
}

.difficulty.beginner {
  background-color: #4CAF50;
  color: white;
}

.difficulty.intermediate {
  background-color: #FF9800;
  color: white;
}

.difficulty.advanced {
  background-color: #F44336;
  color: white;
}

/* Leaderboard */
.leaderboard-entry {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #e0e0e0;
  gap: 16px;
}

.leaderboard-entry.current-user {
  background-color: #fff3cd;
  border: 2px solid #ffc107;
}

.leaderboard-entry .rank {
  font-size: 24px;
  font-weight: bold;
  min-width: 50px;
}

.leaderboard-entry img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

/* User Stats */
.user-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin: 20px 0;
}

.stat-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}
```

---

**Ready to use!** 🚀

All examples are production-ready and follow best practices for React/Vue.js applications.
