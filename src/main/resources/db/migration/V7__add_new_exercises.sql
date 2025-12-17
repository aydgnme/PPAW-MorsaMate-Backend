-- V7__add_new_exercises.sql
-- In this file, you can add new exercises to the database.
-- The application will automatically run this script when it starts.

-- =============================================================================================
-- HOW TO USE THIS FILE:
-- 1. Find the `lesson_id` you want to add an exercise to. You can find these in the `lessons` table.
--    For example, let's say "Introduction to Alphabets" is lesson with id = 1.
--
-- 2. Choose the exercise `type`. The available types are:
--    'ENCODE': User converts text to Morse code.
--    'DECODE': User converts Morse code to text.
--    'AUDIO': User listens to Morse code audio and writes the text.
--    'MULTI_CHOICE': User selects the correct answer from a list of options.
--
-- 3. Write your INSERT statement. See the examples below.
-- =============================================================================================

-- EXAMPLE 1: A multiple-choice question for lesson_id = 1
-- The 'options' field is a JSON array of strings.
INSERT INTO exercises (lesson_id, type, question, correct_answer, options, difficulty, points)
VALUES (
    1,
    'MULTI_CHOICE',
    'What is the Morse code for the letter "S"?',
    '...',
    '["--", "...", "-.-.", ".-"]',
    'EASY',
    10
);

-- EXAMPLE 2: A text-input question for lesson_id = 1 (decode)
-- For non-multiple-choice questions, the 'options' field can be NULL.
INSERT INTO exercises (lesson_id, type, question, correct_answer, options, difficulty, points)
VALUES (
    1,
    'DECODE',
    'Translate the following Morse code to a letter: --.',
    'M',
    NULL,
    'EASY',
    10
);

-- EXAMPLE 3: Another text-input question for lesson_id = 2 (encode)
INSERT INTO exercises (lesson_id, type, question, correct_answer, options, difficulty, points)
VALUES (
    2,
    'ENCODE',
    'How do you write the word "IT" in Morse code? (Use a space between letters)',
    '.. -',
    NULL,
    'MEDIUM',
    15
);

-- ==> ADD YOUR OWN EXERCISES BELOW THIS LINE <==
