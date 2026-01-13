-- V9__create_achievements_tables.sql
-- Create tables for the achievement system

-- ==========================================
-- 1. ACHIEVEMENTS TABLE
-- ==========================================
CREATE TABLE IF NOT EXISTS achievements (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    category VARCHAR(50) NOT NULL, -- e.g., 'PROGRESS', 'STREAK', 'SOCIAL'
    rarity VARCHAR(20) DEFAULT 'COMMON', -- 'COMMON', 'RARE', 'EPIC', 'LEGENDARY'
    points INTEGER DEFAULT 10,
    
    -- Logic for unlocking
    criteria_type VARCHAR(50) NOT NULL, -- e.g., 'LESSONS_COMPLETED', 'STREAK_DAYS'
    criteria_target INTEGER NOT NULL DEFAULT 1,
    criteria_metadata JSONB, -- Additional flexible config
    
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_achievements_category ON achievements(category);
CREATE INDEX idx_achievements_is_active ON achievements(is_active);

-- ==========================================
-- 2. USER ACHIEVEMENTS TABLE (Progress/Unlock)
-- ==========================================
CREATE TABLE IF NOT EXISTS user_achievements (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    achievement_id INTEGER NOT NULL,
    
    is_unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP,
    
    current_progress INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT user_achievements_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT user_achievements_achievement_id_fkey FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE,
    
    -- Unique constraint (one record per user per achievement)
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id)
);

-- Indexes
CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_is_unlocked ON user_achievements(is_unlocked);

-- ==========================================
-- 3. INITIAL SEED DATA
-- ==========================================
INSERT INTO achievements (name, description, category, rarity, points, criteria_type, criteria_target) VALUES
('First Steps', 'Complete your first lesson', 'PROGRESS', 'COMMON', 10, 'LESSONS_COMPLETED', 1),
('Determined Learner', 'Complete 5 lessons', 'PROGRESS', 'COMMON', 25, 'LESSONS_COMPLETED', 5),
('Active Soul', 'Login for 3 consecutive days', 'STREAK', 'RARE', 50, 'STREAK_DAYS', 3),
('Knowledge Master', 'Score 100% on a quiz', 'PERFORMANCE', 'EPIC', 100, 'PERFECT_SCORE', 1);
