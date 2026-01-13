-- Fix subscription status constraint
-- This script drops and recreates the constraint with all enum values

\c morse_code_db

-- Drop the existing constraint
ALTER TABLE user_subscriptions
    DROP CONSTRAINT IF EXISTS user_subscriptions_status_check;

-- Add the updated constraint with all enum values
ALTER TABLE user_subscriptions
    ADD CONSTRAINT user_subscriptions_status_check
    CHECK (status::text = ANY (ARRAY['ACTIVE'::text, 'CANCELED'::text, 'INCOMPLETE'::text, 'PAST_DUE'::text]));

-- Verify the constraint
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'user_subscriptions'::regclass AND contype = 'c';
