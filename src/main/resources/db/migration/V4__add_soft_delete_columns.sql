-- V4__add_soft_delete_columns.sql
-- Add soft delete capability to all tables extending BaseEntity
-- Adds deleted_at column for logical deletion

-- ==========================================
-- ADD deleted_at COLUMNS
-- ==========================================

-- Users table
ALTER TABLE users
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Categories table
ALTER TABLE categories
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Lessons table
ALTER TABLE lessons
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Exercises table (if exists)
ALTER TABLE IF EXISTS exercises
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Exercise attempts table (if exists)
ALTER TABLE IF EXISTS exercise_attempts
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- User progress table (if exists)
ALTER TABLE IF EXISTS user_progress
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Achievements table (if exists)
ALTER TABLE IF EXISTS achievements
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- User achievements table (if exists)
ALTER TABLE IF EXISTS user_achievements
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Subscription plans table (if exists)
ALTER TABLE IF EXISTS subscription_plans
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- User subscriptions table (if exists)
ALTER TABLE IF EXISTS user_subscriptions
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Power ups table (if exists)
ALTER TABLE IF EXISTS power_ups
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- User power ups table (if exists)
ALTER TABLE IF EXISTS user_power_ups
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Gem transactions table (if exists)
ALTER TABLE IF EXISTS gem_transactions
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- User gems table (if exists)
ALTER TABLE IF EXISTS user_gems
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Payments table (if exists)
ALTER TABLE IF EXISTS payments
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- Promo codes table (if exists)
ALTER TABLE IF EXISTS promo_codes
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP DEFAULT NULL;

-- ==========================================
-- CREATE INDEXES FOR SOFT DELETE
-- ==========================================

-- Index for filtering active (non-deleted) records
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_categories_deleted_at ON categories(deleted_at);
CREATE INDEX IF NOT EXISTS idx_lessons_deleted_at ON lessons(deleted_at);
CREATE INDEX IF NOT EXISTS idx_exercises_deleted_at ON exercises(deleted_at);
CREATE INDEX IF NOT EXISTS idx_exercise_attempts_deleted_at ON exercise_attempts(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_progress_deleted_at ON user_progress(deleted_at);
CREATE INDEX IF NOT EXISTS idx_achievements_deleted_at ON achievements(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_achievements_deleted_at ON user_achievements(deleted_at);
CREATE INDEX IF NOT EXISTS idx_subscription_plans_deleted_at ON subscription_plans(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_subscriptions_deleted_at ON user_subscriptions(deleted_at);
CREATE INDEX IF NOT EXISTS idx_power_ups_deleted_at ON power_ups(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_power_ups_deleted_at ON user_power_ups(deleted_at);
CREATE INDEX IF NOT EXISTS idx_gem_transactions_deleted_at ON gem_transactions(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_gems_deleted_at ON user_gems(deleted_at);
CREATE INDEX IF NOT EXISTS idx_payments_deleted_at ON payments(deleted_at);
CREATE INDEX IF NOT EXISTS idx_promo_codes_deleted_at ON promo_codes(deleted_at);

-- ==========================================
-- COMMENTS
-- ==========================================

COMMENT ON COLUMN users.deleted_at IS 'Timestamp when user was soft deleted. NULL means active.';
COMMENT ON COLUMN categories.deleted_at IS 'Timestamp when category was soft deleted. NULL means active.';
COMMENT ON COLUMN lessons.deleted_at IS 'Timestamp when lesson was soft deleted. NULL means active.';
