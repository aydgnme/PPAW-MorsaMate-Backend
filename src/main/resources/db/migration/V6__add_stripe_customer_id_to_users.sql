-- Add stripe_customer_id to users table to store the Stripe customer ID
ALTER TABLE users
    ADD COLUMN stripe_customer_id VARCHAR(255);

-- Add a unique constraint to ensure no two users share the same Stripe customer ID
ALTER TABLE users
    ADD CONSTRAINT uk_stripe_customer_id UNIQUE (stripe_customer_id);
