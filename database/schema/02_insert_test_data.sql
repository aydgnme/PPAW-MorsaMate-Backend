-- MorseMate Test Data
-- PostgreSQL Insert Script
-- Created: 2025-10-08

-- ============================================================================
-- TEST USERS
-- ============================================================================
INSERT INTO users (username, email, password_hash, full_name, total_gems, total_xp, current_streak, role) VALUES
('admin', 'admin@morsemate.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGx', 'Admin User', 1000, 5000, 10, 'ADMIN'),
('john_doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGx', 'John Doe', 150, 500, 3, 'USER'),
('jane_smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGx', 'Jane Smith', 200, 750, 5, 'USER'),
('test_user', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGx', 'Test User', 50, 100, 1, 'USER');

-- ============================================================================
-- CATEGORIES
-- ============================================================================
INSERT INTO categories (name, description, icon_url, display_order, difficulty_level) VALUES
('Morse Code Basics', 'Learn the fundamentals of Morse code', '/icons/basics.png', 1, 'BEGINNER'),
('Letters A-M', 'Master the first half of the alphabet in Morse code', '/icons/letters-am.png', 2, 'BEGINNER'),
('Letters N-Z', 'Complete the alphabet with letters N through Z', '/icons/letters-nz.png', 3, 'BEGINNER'),
('Numbers 0-9', 'Learn to encode and decode numbers in Morse', '/icons/numbers.png', 4, 'INTERMEDIATE'),
('Special Characters', 'Punctuation and special symbols in Morse code', '/icons/special.png', 5, 'INTERMEDIATE'),
('Speed Training', 'Improve your Morse code speed and accuracy', '/icons/speed.png', 6, 'ADVANCED');

-- ============================================================================
-- LESSONS
-- ============================================================================
INSERT INTO lessons (category_id, title, description, content, display_order, duration_minutes, difficulty_level, xp_reward) VALUES
-- Category 1: Morse Code Basics
(1, 'Introduction to Morse Code', 'History and basics of Morse code communication', 'Morse code is a method of transmitting text information as a series of on-off tones, lights, or clicks.', 1, 15, 'BEGINNER', 10),
(1, 'Dots and Dashes', 'Understanding the fundamental units of Morse code', 'Every letter in Morse code is made up of dots (.) and dashes (-). A dash is three times longer than a dot.', 2, 10, 'BEGINNER', 10),
(1, 'Timing Rules', 'Learn the timing conventions in Morse code', 'The space between dots and dashes within a letter equals one dot. Between letters, it equals three dots.', 3, 12, 'BEGINNER', 15),

-- Category 2: Letters A-M
(2, 'Letters A-E', 'First five letters of the alphabet', 'A: .- B: -... C: -.-. D: -.. E: .', 1, 20, 'BEGINNER', 20),
(2, 'Letters F-J', 'Letters F through J in Morse code', 'F: ..-. G: --. H: .... I: .. J: .---', 2, 20, 'BEGINNER', 20),
(2, 'Letters K-M', 'Complete the first half of the alphabet', 'K: -.- L: .-.. M: --', 3, 20, 'BEGINNER', 20),

-- Category 3: Letters N-Z
(3, 'Letters N-R', 'Letters N through R', 'N: -. O: --- P: .--. Q: --.- R: .-.', 1, 20, 'BEGINNER', 20),
(3, 'Letters S-V', 'Letters S through V', 'S: ... T: - U: ..- V: ...-', 2, 20, 'BEGINNER', 20),
(3, 'Letters W-Z', 'Final letters of the alphabet', 'W: .-- X: -..- Y: -.-- Z: --..', 3, 20, 'BEGINNER', 20),

-- Category 4: Numbers 0-9
(4, 'Numbers 1-5', 'First five digits in Morse code', '1: .---- 2: ..--- 3: ...-- 4: ....- 5: .....', 1, 25, 'INTERMEDIATE', 30),
(4, 'Numbers 6-0', 'Complete the number system', '6: -.... 7: --... 8: ---.. 9: ----. 0: -----', 2, 25, 'INTERMEDIATE', 30);

-- ============================================================================
-- EXERCISES
-- ============================================================================
INSERT INTO exercises (lesson_id, title, description, exercise_type, question_text, correct_answer, morse_pattern, display_order, difficulty_level, xp_reward, time_limit_seconds) VALUES
-- Lesson 1: Introduction to Morse Code
(1, 'What is Morse Code?', 'Basic knowledge check', 'TEXT_TO_MORSE', 'Convert the letter "E" to Morse code', '.', '.', 1, 'BEGINNER', 5, 30),
(1, 'Identify the Dash', 'Recognize Morse symbols', 'MORSE_TO_TEXT', 'What letter does "-" represent?', 'T', '-', 2, 'BEGINNER', 5, 30),

-- Lesson 2: Dots and Dashes
(2, 'Convert A', 'Convert letter to Morse', 'TEXT_TO_MORSE', 'Convert the letter "A" to Morse code', '.-', '.-', 1, 'BEGINNER', 5, 30),
(2, 'Decode Morse', 'Decode Morse to letter', 'MORSE_TO_TEXT', 'What letter is "..."?', 'S', '...', 2, 'BEGINNER', 5, 30),

-- Lesson 4: Letters A-E
(4, 'Letter A Practice', 'Practice letter A', 'TEXT_TO_MORSE', 'Convert "A" to Morse', '.-', '.-', 1, 'BEGINNER', 5, 45),
(4, 'Letter B Practice', 'Practice letter B', 'TEXT_TO_MORSE', 'Convert "B" to Morse', '-...', '-...', 2, 'BEGINNER', 5, 45),
(4, 'Letter C Practice', 'Practice letter C', 'TEXT_TO_MORSE', 'Convert "C" to Morse', '-.-.', '-.-.', 3, 'BEGINNER', 5, 45),
(4, 'Letter D Practice', 'Practice letter D', 'TEXT_TO_MORSE', 'Convert "D" to Morse', '-..', '-..', 4, 'BEGINNER', 5, 45),
(4, 'Letter E Practice', 'Practice letter E', 'TEXT_TO_MORSE', 'Convert "E" to Morse', '.', '.', 5, 'BEGINNER', 5, 45),

-- Lesson 5: Letters F-J
(5, 'Decode F', 'Decode letter F', 'MORSE_TO_TEXT', 'What letter is "..-."?', 'F', '..-.', 1, 'BEGINNER', 5, 45),
(5, 'Decode G', 'Decode letter G', 'MORSE_TO_TEXT', 'What letter is "--."?', 'G', '--.', 2, 'BEGINNER', 5, 45);

-- ============================================================================
-- USER PROGRESS
-- ============================================================================
INSERT INTO user_progress (user_id, lesson_id, status, completion_percentage, started_at, completed_at, time_spent_minutes, attempts_count) VALUES
(2, 1, 'COMPLETED', 100, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days', 15, 1),
(2, 2, 'COMPLETED', 100, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', 12, 1),
(2, 4, 'IN_PROGRESS', 60, NOW() - INTERVAL '2 days', NULL, 25, 2),
(3, 1, 'COMPLETED', 100, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days', 18, 1),
(3, 2, 'COMPLETED', 100, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', 15, 1),
(3, 3, 'IN_PROGRESS', 80, NOW() - INTERVAL '1 day', NULL, 20, 1);

-- ============================================================================
-- EXERCISE ATTEMPTS
-- ============================================================================
INSERT INTO exercise_attempts (user_id, exercise_id, user_answer, is_correct, score, time_taken_seconds, hints_used, attempt_number) VALUES
(2, 1, '.', true, 5, 15, 0, 1),
(2, 2, 'T', true, 5, 12, 0, 1),
(2, 3, '.-', true, 5, 20, 0, 1),
(2, 4, '...', true, 5, 18, 1, 1),
(3, 1, '.', true, 5, 10, 0, 1),
(3, 2, 'T', true, 5, 8, 0, 1),
(3, 3, '.-', true, 5, 15, 0, 1),
(3, 4, '.', false, 0, 25, 2, 1),
(3, 4, '...', true, 5, 20, 0, 2);

-- ============================================================================
-- ACHIEVEMENTS
-- ============================================================================
INSERT INTO achievements (name, description, icon_url, achievement_type, criteria_type, criteria_value, xp_reward, gem_reward, rarity) VALUES
('First Steps', 'Complete your first lesson', '/icons/first-steps.png', 'LESSON_COMPLETION', 'LESSON_COUNT', 1, 50, 10, 'COMMON'),
('Quick Learner', 'Complete a lesson in under 10 minutes', '/icons/quick-learner.png', 'SPEED', 'TIME_SECONDS', 600, 75, 15, 'RARE'),
('Morse Master', 'Complete 10 lessons', '/icons/morse-master.png', 'LESSON_COMPLETION', 'LESSON_COUNT', 10, 200, 50, 'EPIC'),
('Perfect Score', 'Get 100% on 5 exercises', '/icons/perfect.png', 'PERFECT_SCORE', 'PERFECT_COUNT', 5, 100, 25, 'RARE'),
('Week Warrior', 'Maintain a 7-day learning streak', '/icons/streak.png', 'STREAK', 'STREAK_DAYS', 7, 150, 30, 'EPIC'),
('Speed Demon', 'Complete 20 exercises in under 30 seconds each', '/icons/speed.png', 'SPEED', 'FAST_COUNT', 20, 250, 60, 'LEGENDARY');

-- ============================================================================
-- USER ACHIEVEMENTS
-- ============================================================================
INSERT INTO user_achievements (user_id, achievement_id, progress) VALUES
(2, 1, 100),
(3, 1, 100),
(3, 4, 60);

-- ============================================================================
-- GEM TRANSACTIONS
-- ============================================================================
INSERT INTO gem_transactions (user_id, amount, transaction_type, description, reference_id, reference_type) VALUES
(2, 10, 'EARNED', 'Achievement: First Steps', 1, 'ACHIEVEMENT'),
(2, 20, 'EARNED', 'Completed Lesson 1', 1, 'LESSON'),
(2, 20, 'EARNED', 'Completed Lesson 2', 2, 'LESSON'),
(2, -30, 'SPENT', 'Purchased Hint Boost', 1, 'POWER_UP'),
(3, 10, 'EARNED', 'Achievement: First Steps', 1, 'ACHIEVEMENT'),
(3, 50, 'BONUS', 'Welcome bonus', NULL, NULL);

-- ============================================================================
-- POWER UPS
-- ============================================================================
INSERT INTO power_ups (name, description, icon_url, power_up_type, effect_value, duration_minutes, gem_cost) VALUES
('Hint Boost', 'Get an extra hint for exercises', '/icons/hint-boost.png', 'HINT_BOOST', 1, 60, 30),
('Time Extension', 'Add 30 seconds to exercise timer', '/icons/time-extend.png', 'TIME_EXTENSION', 30, 60, 40),
('2x XP Multiplier', 'Double XP for 1 hour', '/icons/xp-mult.png', 'XP_MULTIPLIER', 2, 60, 50),
('Streak Freeze', 'Protect your streak for 1 day', '/icons/freeze.png', 'STREAK_FREEZE', 1, 1440, 60);

-- ============================================================================
-- USER INVENTORY
-- ============================================================================
INSERT INTO user_inventory (user_id, power_up_id, quantity, purchased_at) VALUES
(2, 1, 2, NOW() - INTERVAL '2 days'),
(3, 2, 1, NOW() - INTERVAL '1 day'),
(3, 3, 1, NOW() - INTERVAL '1 day');

-- ============================================================================
-- LEADERBOARD
-- ============================================================================
INSERT INTO leaderboard (user_id, total_xp, total_lessons_completed, total_exercises_completed, current_streak, rank_position) VALUES
(3, 750, 2, 8, 5, 1),
(2, 500, 2, 6, 3, 2),
(4, 100, 0, 2, 1, 3);
