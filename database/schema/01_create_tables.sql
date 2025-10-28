-- MorseMate Database Schema
-- PostgreSQL DDL Script
-- Created: 2025-10-08

-- ============================================================================
-- DROP EXISTING TABLES (for clean setup)
-- ============================================================================
DROP TABLE IF EXISTS exercise_attempts CASCADE;
DROP TABLE IF EXISTS user_progress CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS lessons CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS user_achievements CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS gem_transactions CASCADE;
DROP TABLE IF EXISTS user_inventory CASCADE;
DROP TABLE IF EXISTS power_ups CASCADE;
DROP TABLE IF EXISTS leaderboard CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    total_gems INTEGER DEFAULT 0,
    total_xp INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    role VARCHAR(20) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    CONSTRAINT check_gems_positive CHECK (total_gems >= 0),
    CONSTRAINT check_xp_positive CHECK (total_xp >= 0)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- ============================================================================
-- CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    difficulty_level VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_difficulty CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX idx_categories_display_order ON categories(display_order);

-- ============================================================================
-- LESSONS TABLE
-- ============================================================================
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    content TEXT,
    display_order INTEGER DEFAULT 0,
    duration_minutes INTEGER,
    difficulty_level VARCHAR(20),
    xp_reward INTEGER DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT check_lesson_difficulty CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX idx_lessons_category ON lessons(category_id);
CREATE INDEX idx_lessons_display_order ON lessons(display_order);

-- ============================================================================
-- EXERCISES TABLE
-- ============================================================================
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    exercise_type VARCHAR(50) NOT NULL,
    question_text TEXT,
    correct_answer TEXT,
    morse_pattern VARCHAR(500),
    audio_url VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    difficulty_level VARCHAR(20),
    xp_reward INTEGER DEFAULT 5,
    time_limit_seconds INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT check_exercise_type CHECK (exercise_type IN ('TEXT_TO_MORSE', 'MORSE_TO_TEXT', 'AUDIO_RECOGNITION', 'PATTERN_MATCHING')),
    CONSTRAINT check_exercise_difficulty CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
CREATE INDEX idx_exercises_type ON exercises(exercise_type);
CREATE INDEX idx_exercises_display_order ON exercises(display_order);

-- ============================================================================
-- USER_PROGRESS TABLE
-- ============================================================================
CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'NOT_STARTED',
    completion_percentage INTEGER DEFAULT 0,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    time_spent_minutes INTEGER DEFAULT 0,
    attempts_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    UNIQUE(user_id, lesson_id),
    CONSTRAINT check_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT check_percentage CHECK (completion_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_user_progress_user ON user_progress(user_id);
CREATE INDEX idx_user_progress_lesson ON user_progress(lesson_id);
CREATE INDEX idx_user_progress_status ON user_progress(status);

-- ============================================================================
-- EXERCISE_ATTEMPTS TABLE
-- ============================================================================
CREATE TABLE exercise_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    user_answer TEXT,
    is_correct BOOLEAN NOT NULL,
    score INTEGER DEFAULT 0,
    time_taken_seconds INTEGER,
    hints_used INTEGER DEFAULT 0,
    attempt_number INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    CONSTRAINT check_score CHECK (score >= 0)
);

CREATE INDEX idx_exercise_attempts_user ON exercise_attempts(user_id);
CREATE INDEX idx_exercise_attempts_exercise ON exercise_attempts(exercise_id);
CREATE INDEX idx_exercise_attempts_created ON exercise_attempts(created_at);

-- ============================================================================
-- ACHIEVEMENTS TABLE
-- ============================================================================
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    achievement_type VARCHAR(50) NOT NULL,
    criteria_type VARCHAR(50) NOT NULL,
    criteria_value INTEGER NOT NULL,
    xp_reward INTEGER DEFAULT 50,
    gem_reward INTEGER DEFAULT 10,
    rarity VARCHAR(20) DEFAULT 'COMMON',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_achievement_type CHECK (achievement_type IN ('LESSON_COMPLETION', 'EXERCISE_MASTERY', 'STREAK', 'SPEED', 'PERFECT_SCORE')),
    CONSTRAINT check_rarity CHECK (rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY'))
);

CREATE INDEX idx_achievements_type ON achievements(achievement_type);
CREATE INDEX idx_achievements_rarity ON achievements(rarity);

-- ============================================================================
-- USER_ACHIEVEMENTS TABLE
-- ============================================================================
CREATE TABLE user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    UNIQUE(user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_earned ON user_achievements(earned_at);

-- ============================================================================
-- GEM_TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE gem_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount INTEGER NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    description TEXT,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT check_transaction_type CHECK (transaction_type IN ('EARNED', 'SPENT', 'BONUS', 'REFUND'))
);

CREATE INDEX idx_gem_transactions_user ON gem_transactions(user_id);
CREATE INDEX idx_gem_transactions_type ON gem_transactions(transaction_type);
CREATE INDEX idx_gem_transactions_created ON gem_transactions(created_at);

-- ============================================================================
-- POWER_UPS TABLE
-- ============================================================================
CREATE TABLE power_ups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    power_up_type VARCHAR(50) NOT NULL,
    effect_value INTEGER,
    duration_minutes INTEGER,
    gem_cost INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_power_up_type CHECK (power_up_type IN ('HINT_BOOST', 'TIME_EXTENSION', 'XP_MULTIPLIER', 'STREAK_FREEZE'))
);

CREATE INDEX idx_power_ups_type ON power_ups(power_up_type);

-- ============================================================================
-- USER_INVENTORY TABLE
-- ============================================================================
CREATE TABLE user_inventory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    power_up_id BIGINT NOT NULL,
    quantity INTEGER DEFAULT 1,
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (power_up_id) REFERENCES power_ups(id) ON DELETE CASCADE,
    CONSTRAINT check_quantity CHECK (quantity >= 0)
);

CREATE INDEX idx_user_inventory_user ON user_inventory(user_id);
CREATE INDEX idx_user_inventory_power_up ON user_inventory(power_up_id);

-- ============================================================================
-- LEADERBOARD TABLE
-- ============================================================================
CREATE TABLE leaderboard (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_xp INTEGER DEFAULT 0,
    total_lessons_completed INTEGER DEFAULT 0,
    total_exercises_completed INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    rank_position INTEGER,
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id)
);

CREATE INDEX idx_leaderboard_xp ON leaderboard(total_xp DESC);
CREATE INDEX idx_leaderboard_rank ON leaderboard(rank_position);
CREATE INDEX idx_leaderboard_streak ON leaderboard(current_streak DESC);

-- ============================================================================
-- TRIGGERS FOR UPDATED_AT
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lessons_updated_at BEFORE UPDATE ON lessons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_exercises_updated_at BEFORE UPDATE ON exercises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_progress_updated_at BEFORE UPDATE ON user_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
