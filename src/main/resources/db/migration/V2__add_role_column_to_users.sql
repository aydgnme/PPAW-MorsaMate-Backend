-- Migration: Add role column to users table
-- Description: Adds role-based access control to user accounts
-- Version: V2
-- Author: MorseMate Team
-- Date: 2025-01-20

-- Step 1: Add role column as NULLABLE first (to avoid constraint violation on existing rows)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20);

-- Step 2: Set default value for all existing rows
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- Step 3: Make the column NOT NULL
ALTER TABLE users ALTER COLUMN role SET NOT NULL;

-- Step 4: Set default for future inserts
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';

-- Step 5: Add check constraint to ensure only valid roles
ALTER TABLE users ADD CONSTRAINT check_user_role CHECK (role IN ('USER', 'ADMIN', 'PREMIUM'));

-- Step 6: Create index on role column for performance
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Add comment to role column
COMMENT ON COLUMN users.role IS 'User role for access control: USER, ADMIN, or PREMIUM';
