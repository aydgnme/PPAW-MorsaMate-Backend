-- V8__fix_subscription_status_constraint.sql
-- Fix user_subscriptions status check constraint to include CANCELED status

-- Drop the existing constraint
ALTER TABLE user_subscriptions
    DROP CONSTRAINT IF EXISTS user_subscriptions_status_check;

-- Add the updated constraint with all enum values
ALTER TABLE user_subscriptions
    ADD CONSTRAINT user_subscriptions_status_check
    CHECK (status IN ('ACTIVE', 'CANCELED', 'INCOMPLETE', 'PAST_DUE'));
