-- Migration: Add role column to users table
-- Description: Adds role-based access control to user accounts
-- Version: V2
-- Author: MorseMate Team
-- Date: 2025-01-20

-- Add role column with default value USER
ALTER TABLE users
ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Add check constraint to ensure only valid roles
ALTER TABLE users
ADD CONSTRAINT check_user_role CHECK (role IN ('USER', 'ADMIN'));

-- Create index on role column for performance
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Update existing users to have USER role (if any)
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- Optional: Create an admin user for testing (uncomment if needed)
-- UPDATE users SET role = 'ADMIN' WHERE username = 'admin';

-- Add comment to role column
COMMENT ON COLUMN users.role IS 'User role for access control: USER or ADMIN';
