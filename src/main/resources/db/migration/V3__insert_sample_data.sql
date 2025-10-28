-- V3__insert_sample_data.sql
-- Sample content data for MorseMate Learning System
-- Creates categories, lessons, and exercises for demonstration and testing

-- ==========================================
-- 1. INSERT CATEGORIES
-- ==========================================

INSERT INTO categories (id, name, description, display_order, icon_url, is_active, created_at, updated_at) VALUES
(1, 'Getting Started', 'Learn the fundamentals of Morse code', 1, 'icons/basics.svg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Letters A-Z', 'Master the complete alphabet in Morse code', 2, 'icons/letters.svg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Numbers 0-9', 'Learn numbers in Morse code', 3, 'icons/numbers.svg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Punctuation & Symbols', 'Common punctuation marks and special characters', 4, 'icons/punctuation.svg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Advanced Techniques', 'Speed training and complex patterns', 5, 'icons/advanced.svg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 2. INSERT LESSONS
-- ==========================================

-- Category 1: Getting Started
INSERT INTO lessons (id, category_id, title, description, difficulty, content, order_index, points_reward, estimated_duration, created_at, updated_at) VALUES
(1, 1, 'Introduction to Morse Code', 'Learn what Morse code is and its basic structure', 'BEGINNER', 'Morse code is a method of encoding text characters using sequences of dots (•) and dashes (—). Each letter and number has a unique pattern. This system was invented by Samuel Morse in the 1840s.', 1, 10, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Dots and Dashes', 'Understanding the timing and rhythm of Morse code', 'BEGINNER', 'A dot is one unit of time. A dash is three units. The space between parts of the same letter is one unit, between letters is three units, and between words is seven units.', 2, 15, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'Your First Letters: E, T, I', 'Start with the simplest letters', 'BEGINNER', 'E is just one dot (•). T is one dash (—). I is two dots (••). These are the most common letters in English.', 3, 20, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Category 2: Letters A-Z
INSERT INTO lessons (id, category_id, title, description, difficulty, content, order_index, points_reward, estimated_duration, created_at, updated_at) VALUES
(4, 2, 'Letters A-E', 'First five letters of the alphabet', 'BEGINNER', 'A: •— (di-dah) | B: —••• (dah-di-di-dit) | C: —•—• (dah-di-dah-dit) | D: —•• (dah-di-dit) | E: • (dit)', 1, 25, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'Letters F-J', 'Continue with F through J', 'BEGINNER', 'F: ••—• | G: ——• | H: •••• | I: •• | J: •———', 2, 25, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, 'Letters K-O', 'Middle alphabet letters', 'INTERMEDIATE', 'K: —•— | L: •—•• | M: —— | N: —• | O: ———', 3, 30, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 2, 'Letters P-T', 'More letter patterns', 'INTERMEDIATE', 'P: •——• | Q: ——•— | R: •—• | S: ••• | T: —', 4, 30, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'Letters U-Z', 'Complete the alphabet', 'INTERMEDIATE', 'U: ••— | V: •••— | W: •—— | X: —••— | Y: —•—— | Z: ——••', 5, 35, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Category 3: Numbers 0-9
INSERT INTO lessons (id, category_id, title, description, difficulty, content, order_index, points_reward, estimated_duration, created_at, updated_at) VALUES
(9, 3, 'Numbers 1-5', 'First five numbers in Morse', 'BEGINNER', '1: •———— | 2: ••——— | 3: •••—— | 4: ••••— | 5: •••••', 1, 20, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 3, 'Numbers 6-0', 'Complete the number system', 'BEGINNER', '6: —•••• | 7: ——••• | 8: ———•• | 9: ————• | 0: —————', 2, 20, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Category 4: Punctuation & Symbols
INSERT INTO lessons (id, category_id, title, description, difficulty, content, order_index, points_reward, estimated_duration, created_at, updated_at) VALUES
(11, 4, 'Common Punctuation', 'Period, comma, question mark', 'INTERMEDIATE', 'Period (.): •—•—•— | Comma (,): ——••—— | Question (?): ••——•• | Apostrophe (''): •————•', 1, 25, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 4, 'Special Characters', 'Parentheses, colon, and more', 'ADVANCED', 'Colon (:): ———••• | Semicolon (;): —•—•—• | Slash (/): —••—• | @ symbol: •——•—•', 2, 30, 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Category 5: Advanced Techniques
INSERT INTO lessons (id, category_id, title, description, difficulty, content, order_index, points_reward, estimated_duration, created_at, updated_at) VALUES
(13, 5, 'Speed Training Basics', 'Increase your Morse code speed', 'ADVANCED', 'Learn to recognize patterns quickly. Practice common letter combinations. Build muscle memory for faster encoding.', 1, 40, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 5, 'Common Abbreviations', 'Standard Morse code abbreviations', 'ADVANCED', 'SOS: •••———••• | CQ (seeking you): —•—•——•— | TNX (thanks): ———•—••— | 73 (best regards)', 2, 45, 45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ==========================================
-- 3. INSERT EXERCISES
-- ==========================================

-- Lesson 1: Introduction to Morse Code
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(1, 1, 'MULTI_CHOICE', 'Who invented Morse code?', 'Samuel Morse', '["Samuel Morse", "Alexander Graham Bell", "Nikola Tesla", "Thomas Edison"]', 'EASY', 5, 30, 'Think about the name of the code', CURRENT_TIMESTAMP),
(2, 1, 'MULTI_CHOICE', 'Morse code uses which two symbols?', 'Dots and dashes', '["Dots and dashes", "Zeros and ones", "Letters and numbers", "Lines and circles"]', 'EASY', 5, 30, 'The basic building blocks', CURRENT_TIMESTAMP),
(3, 1, 'MULTI_CHOICE', 'When was Morse code invented?', '1840s', '["1840s", "1900s", "1920s", "1950s"]', 'EASY', 5, 30, 'In the 19th century', CURRENT_TIMESTAMP);

-- Lesson 2: Dots and Dashes
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(4, 2, 'MULTI_CHOICE', 'How many units long is a dash?', '3 units', '["1 unit", "2 units", "3 units", "4 units"]', 'EASY', 5, 30, 'Three times as long as a dot', CURRENT_TIMESTAMP),
(5, 2, 'MULTI_CHOICE', 'How many units of space between letters?', '3 units', '["1 unit", "2 units", "3 units", "7 units"]', 'MEDIUM', 5, 30, 'Same as dash length', CURRENT_TIMESTAMP),
(6, 2, 'MULTI_CHOICE', 'How many units of space between words?', '7 units', '["1 unit", "3 units", "5 units", "7 units"]', 'MEDIUM', 5, 30, 'Seven units', CURRENT_TIMESTAMP);

-- Lesson 3: Your First Letters: E, T, I
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(7, 3, 'ENCODE', 'Encode the letter E in Morse code', '.', NULL, 'EASY', 10, 30, 'E is the simplest: just one dot', CURRENT_TIMESTAMP),
(8, 3, 'ENCODE', 'Encode the letter T in Morse code', '-', NULL, 'EASY', 10, 30, 'T is one dash', CURRENT_TIMESTAMP),
(9, 3, 'ENCODE', 'Encode the letter I in Morse code', '..', NULL, 'EASY', 10, 30, 'I is two dots', CURRENT_TIMESTAMP),
(10, 3, 'DECODE', 'Decode: .', 'E', NULL, 'EASY', 10, 30, 'One dot represents E', CURRENT_TIMESTAMP),
(11, 3, 'DECODE', 'Decode: -', 'T', NULL, 'EASY', 10, 30, 'One dash represents T', CURRENT_TIMESTAMP),
(12, 3, 'DECODE', 'Decode: ..', 'I', NULL, 'EASY', 10, 30, 'Two dots represent I', CURRENT_TIMESTAMP);

-- Lesson 4: Letters A-E
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(13, 4, 'ENCODE', 'Encode the letter A', '.-', NULL, 'EASY', 10, 30, 'A is dot-dash', CURRENT_TIMESTAMP),
(14, 4, 'ENCODE', 'Encode the letter B', '-...', NULL, 'EASY', 10, 30, 'B is dash-dot-dot-dot', CURRENT_TIMESTAMP),
(15, 4, 'ENCODE', 'Encode the letter C', '-.-.', NULL, 'MEDIUM', 10, 30, 'C is dash-dot-dash-dot', CURRENT_TIMESTAMP),
(16, 4, 'ENCODE', 'Encode the letter D', '-..', NULL, 'EASY', 10, 30, 'D is dash-dot-dot', CURRENT_TIMESTAMP),
(17, 4, 'DECODE', 'Decode: .-', 'A', NULL, 'EASY', 10, 30, 'Dot-dash is A', CURRENT_TIMESTAMP),
(18, 4, 'DECODE', 'Decode: -...', 'B', NULL, 'EASY', 10, 30, 'Dash-three dots is B', CURRENT_TIMESTAMP),
(19, 4, 'DECODE', 'Decode: -.-.', 'C', NULL, 'MEDIUM', 10, 30, 'Alternating dash-dot pattern', CURRENT_TIMESTAMP),
(20, 4, 'DECODE', 'Decode: -..', 'D', NULL, 'EASY', 10, 30, 'D for Dog', CURRENT_TIMESTAMP);

-- Lesson 5: Letters F-J
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(21, 5, 'ENCODE', 'Encode the letter F', '..-.', NULL, 'MEDIUM', 10, 30, 'F is two dots, dash, dot', CURRENT_TIMESTAMP),
(22, 5, 'ENCODE', 'Encode the letter G', '--.', NULL, 'EASY', 10, 30, 'G is two dashes and a dot', CURRENT_TIMESTAMP),
(23, 5, 'ENCODE', 'Encode the letter H', '....', NULL, 'EASY', 10, 30, 'H is four dots', CURRENT_TIMESTAMP),
(24, 5, 'DECODE', 'Decode: .---', 'J', NULL, 'MEDIUM', 10, 30, 'Dot and three dashes', CURRENT_TIMESTAMP),
(25, 5, 'MULTI_CHOICE', 'Which letter is represented by: ..-.', 'F', '["E", "F", "G", "H"]', 'MEDIUM', 5, 30, 'Two dots, dash, dot', CURRENT_TIMESTAMP);

-- Lesson 6: Letters K-O
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(26, 6, 'ENCODE', 'Encode the letter K', '-.-', NULL, 'MEDIUM', 10, 40, 'K is dash-dot-dash', CURRENT_TIMESTAMP),
(27, 6, 'ENCODE', 'Encode the letter M', '--', NULL, 'EASY', 10, 30, 'M is two dashes', CURRENT_TIMESTAMP),
(28, 6, 'ENCODE', 'Encode the letter N', '-.', NULL, 'EASY', 10, 30, 'N is dash-dot', CURRENT_TIMESTAMP),
(29, 6, 'ENCODE', 'Encode the letter O', '---', NULL, 'EASY', 10, 30, 'O is three dashes', CURRENT_TIMESTAMP),
(30, 6, 'DECODE', 'Decode: .-..', 'L', NULL, 'MEDIUM', 10, 40, 'Dot-dash-dot-dot', CURRENT_TIMESTAMP);

-- Lesson 7: Letters P-T
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(31, 7, 'ENCODE', 'Encode the letter P', '.--.', NULL, 'MEDIUM', 10, 40, 'P is dot-dash-dash-dot', CURRENT_TIMESTAMP),
(32, 7, 'ENCODE', 'Encode the letter R', '.-.', NULL, 'MEDIUM', 10, 40, 'R is dot-dash-dot', CURRENT_TIMESTAMP),
(33, 7, 'ENCODE', 'Encode the letter S', '...', NULL, 'EASY', 10, 30, 'S is three dots', CURRENT_TIMESTAMP),
(34, 7, 'DECODE', 'Decode: --.-', 'Q', NULL, 'HARD', 15, 40, 'Two dashes, dot, dash', CURRENT_TIMESTAMP),
(35, 7, 'SPEED_TEST', 'Quick! Decode: ...', 'S', NULL, 'EASY', 15, 15, 'Three dots - think SOS', CURRENT_TIMESTAMP);

-- Lesson 8: Letters U-Z
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(36, 8, 'ENCODE', 'Encode the letter U', '..-', NULL, 'MEDIUM', 10, 40, 'U is two dots and a dash', CURRENT_TIMESTAMP),
(37, 8, 'ENCODE', 'Encode the letter W', '.--', NULL, 'MEDIUM', 10, 40, 'W is dot-dash-dash', CURRENT_TIMESTAMP),
(38, 8, 'ENCODE', 'Encode the letter Z', '--..', NULL, 'HARD', 15, 40, 'Z is two dashes, two dots', CURRENT_TIMESTAMP),
(39, 8, 'DECODE', 'Decode: ...-', 'V', NULL, 'MEDIUM', 10, 40, 'Three dots and a dash', CURRENT_TIMESTAMP),
(40, 8, 'DECODE', 'Decode: -.--', 'Y', NULL, 'HARD', 15, 40, 'Dash-dot-dash-dash', CURRENT_TIMESTAMP);

-- Lesson 9: Numbers 1-5
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(41, 9, 'ENCODE', 'Encode the number 1', '.----', NULL, 'EASY', 10, 30, 'One dot, four dashes', CURRENT_TIMESTAMP),
(42, 9, 'ENCODE', 'Encode the number 2', '..---', NULL, 'EASY', 10, 30, 'Two dots, three dashes', CURRENT_TIMESTAMP),
(43, 9, 'ENCODE', 'Encode the number 3', '...--', NULL, 'EASY', 10, 30, 'Three dots, two dashes', CURRENT_TIMESTAMP),
(44, 9, 'ENCODE', 'Encode the number 4', '....-', NULL, 'EASY', 10, 30, 'Four dots, one dash', CURRENT_TIMESTAMP),
(45, 9, 'ENCODE', 'Encode the number 5', '.....', NULL, 'EASY', 10, 30, 'Five dots', CURRENT_TIMESTAMP);

-- Lesson 10: Numbers 6-0
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(46, 10, 'ENCODE', 'Encode the number 6', '-....', NULL, 'EASY', 10, 30, 'One dash, four dots', CURRENT_TIMESTAMP),
(47, 10, 'ENCODE', 'Encode the number 7', '--...', NULL, 'EASY', 10, 30, 'Two dashes, three dots', CURRENT_TIMESTAMP),
(48, 10, 'ENCODE', 'Encode the number 8', '---..', NULL, 'EASY', 10, 30, 'Three dashes, two dots', CURRENT_TIMESTAMP),
(49, 10, 'ENCODE', 'Encode the number 9', '----.', NULL, 'EASY', 10, 30, 'Four dashes, one dot', CURRENT_TIMESTAMP),
(50, 10, 'ENCODE', 'Encode the number 0', '-----', NULL, 'EASY', 10, 30, 'Five dashes', CURRENT_TIMESTAMP);

-- Lesson 11: Common Punctuation
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(51, 11, 'ENCODE', 'Encode a period (.)', '.-.-.-', NULL, 'MEDIUM', 15, 40, 'Dot-dash pattern repeated', CURRENT_TIMESTAMP),
(52, 11, 'ENCODE', 'Encode a comma (,)', '--..--', NULL, 'MEDIUM', 15, 40, 'Two dashes, two dots, two dashes', CURRENT_TIMESTAMP),
(53, 11, 'ENCODE', 'Encode a question mark (?)', '..--..', NULL, 'HARD', 15, 40, 'Two dots, two dashes, two dots', CURRENT_TIMESTAMP),
(54, 11, 'DECODE', 'Decode: .-----.', 'APOSTROPHE', NULL, 'HARD', 15, 40, 'Dot-dash pattern with extra dash', CURRENT_TIMESTAMP);

-- Lesson 12: Special Characters
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(55, 12, 'ENCODE', 'Encode a colon (:)', '---...', NULL, 'HARD', 20, 50, 'Three dashes, three dots', CURRENT_TIMESTAMP),
(56, 12, 'ENCODE', 'Encode a slash (/)', '-..-.', NULL, 'HARD', 20, 50, 'Dash-dot-dot-dash-dot', CURRENT_TIMESTAMP),
(57, 12, 'MULTI_CHOICE', 'Which symbol is: .--.-', '@ symbol', '["@ symbol", "& symbol", "# symbol", "$ symbol"]', 'HARD', 15, 40, 'The at symbol', CURRENT_TIMESTAMP);

-- Lesson 13: Speed Training Basics
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(58, 13, 'SPEED_TEST', 'Quick! Decode: .-', 'A', NULL, 'MEDIUM', 20, 10, 'Don''t think, just recognize', CURRENT_TIMESTAMP),
(59, 13, 'SPEED_TEST', 'Quick! Decode: -...', 'B', NULL, 'MEDIUM', 20, 10, 'Fast pattern recognition', CURRENT_TIMESTAMP),
(60, 13, 'SPEED_TEST', 'Quick! Encode the letter M', '--', NULL, 'MEDIUM', 20, 10, 'Two dashes', CURRENT_TIMESTAMP);

-- Lesson 14: Common Abbreviations
INSERT INTO exercises (id, lesson_id, type, question, correct_answer, options, difficulty, points, time_limit, hint, created_at) VALUES
(61, 14, 'DECODE', 'Decode the famous distress signal: ...---...', 'SOS', NULL, 'EASY', 15, 30, 'Save Our Ship', CURRENT_TIMESTAMP),
(62, 14, 'MULTI_CHOICE', 'What does CQ mean in Morse code?', 'Seeking you / calling any station', '["Seeking you / calling any station", "See you later", "Call quickly", "Come quick"]', 'MEDIUM', 15, 40, 'General call', CURRENT_TIMESTAMP),
(63, 14, 'MULTI_CHOICE', 'What does 73 mean?', 'Best regards', '["Best regards", "Goodbye", "Thank you", "Hello"]', 'MEDIUM', 15, 40, 'Common amateur radio sign-off', CURRENT_TIMESTAMP);

-- Reset sequence counters
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('lessons_id_seq', (SELECT MAX(id) FROM lessons));
SELECT setval('exercises_id_seq', (SELECT MAX(id) FROM exercises));

-- ==========================================
-- VERIFICATION
-- ==========================================
-- After running this script, you should have:
-- - 5 categories
-- - 14 lessons
-- - 63 exercises

-- Summary by category:
-- Getting Started: 3 lessons, 12 exercises
-- Letters A-Z: 5 lessons, 28 exercises
-- Numbers 0-9: 2 lessons, 10 exercises
-- Punctuation & Symbols: 2 lessons, 7 exercises
-- Advanced Techniques: 2 lessons, 6 exercises
