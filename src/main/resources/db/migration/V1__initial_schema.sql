-- V1__initial_schema.sql
-- Initial schema for MorseMate application
-- Creates all core tables: users, categories, lessons, exercises

-- ==========================================
-- 1. USERS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,

    -- Gamification fields
    level INTEGER NOT NULL DEFAULT 1,
    total_points INTEGER NOT NULL DEFAULT 0,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,

    -- Heart system
    hearts INTEGER NOT NULL DEFAULT 5,
    max_hearts INTEGER NOT NULL DEFAULT 5,
    last_heart_refill TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Profile
    profile_picture_url VARCHAR(255),

    -- Status flags
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    -- Constraints
    CONSTRAINT users_hearts_check CHECK (hearts >= 0 AND hearts <= max_hearts),
    CONSTRAINT users_level_check CHECK (level >= 1)
);

-- Indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_total_points ON users(total_points DESC);

-- ==========================================
-- 2. CATEGORIES TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER,
    icon_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Timestamps (managed by JPA auditing)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes for categories table
CREATE INDEX idx_categories_display_order ON categories(display_order);
CREATE INDEX idx_categories_is_active ON categories(is_active);
CREATE INDEX idx_categories_name ON categories(name);

-- ==========================================
-- 3. LESSONS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS lessons (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20),
    content TEXT,
    order_index INTEGER,
    points_reward INTEGER NOT NULL DEFAULT 10,
    estimated_duration INTEGER, -- in minutes

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Foreign key
    CONSTRAINT lessons_category_id_fkey FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE CASCADE,

    -- Check constraints
    CONSTRAINT lessons_difficulty_check CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT lessons_points_reward_check CHECK (points_reward >= 0)
);

-- Indexes for lessons table
CREATE INDEX idx_lessons_category_id ON lessons(category_id);
CREATE INDEX idx_lessons_order_index ON lessons(order_index);
CREATE INDEX idx_lessons_difficulty ON lessons(difficulty);
CREATE INDEX idx_lessons_title ON lessons(title);

-- ==========================================
-- 4. EXERCISES TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS exercises (
    id SERIAL PRIMARY KEY,
    lesson_id INTEGER NOT NULL,
    type VARCHAR(50),
    question TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    options JSONB, -- for multiple choice questions
    difficulty VARCHAR(20),
    points INTEGER NOT NULL DEFAULT 5,
    time_limit INTEGER, -- in seconds
    hint TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key
    CONSTRAINT exercises_lesson_id_fkey FOREIGN KEY (lesson_id)
        REFERENCES lessons(id) ON DELETE CASCADE,

    -- Check constraints
    CONSTRAINT exercises_type_check CHECK (type IN ('ENCODE', 'DECODE', 'AUDIO', 'SPEED_TEST', 'MULTI_CHOICE')),
    CONSTRAINT exercises_difficulty_check CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT exercises_points_check CHECK (points >= 0)
);

-- Indexes for exercises table
CREATE INDEX idx_exercises_lesson_id ON exercises(lesson_id);
CREATE INDEX idx_exercises_type ON exercises(type);
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty);

-- ==========================================
-- COMMENTS
-- ==========================================
COMMENT ON TABLE users IS 'Application users with gamification features';
COMMENT ON TABLE categories IS 'Learning content categories';
COMMENT ON TABLE lessons IS 'Lessons within categories';
COMMENT ON TABLE exercises IS 'Practice exercises for lessons';

COMMENT ON COLUMN users.hearts IS 'Current hearts available (lives system)';
COMMENT ON COLUMN users.max_hearts IS 'Maximum hearts capacity';
COMMENT ON COLUMN users.current_streak IS 'Current consecutive days streak';
COMMENT ON COLUMN users.longest_streak IS 'Longest streak ever achieved';

COMMENT ON COLUMN lessons.difficulty IS 'Lesson difficulty: BEGINNER, INTERMEDIATE, ADVANCED';
COMMENT ON COLUMN lessons.points_reward IS 'Points awarded for completing this lesson';
COMMENT ON COLUMN lessons.estimated_duration IS 'Estimated time to complete in minutes';

COMMENT ON COLUMN exercises.type IS 'Exercise type: ENCODE, DECODE, AUDIO, SPEED_TEST, MULTI_CHOICE';
COMMENT ON COLUMN exercises.options IS 'JSON array of multiple choice options';
COMMENT ON COLUMN exercises.difficulty IS 'Exercise difficulty: EASY, MEDIUM, HARD';
COMMENT ON COLUMN exercises.time_limit IS 'Time limit in seconds (optional)';
