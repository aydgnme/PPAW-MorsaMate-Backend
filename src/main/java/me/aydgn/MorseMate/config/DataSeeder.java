package me.aydgn.MorseMate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.entity.Category;
import me.aydgn.MorseMate.entity.Exercise;
import me.aydgn.MorseMate.entity.Lesson;
import me.aydgn.MorseMate.repository.CategoryRepository;
import me.aydgn.MorseMate.repository.ExerciseRepository;
import me.aydgn.MorseMate.repository.LessonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final CategoryRepository categoryRepository;
        private final LessonRepository lessonRepository;
        private final ExerciseRepository exerciseRepository;
        private final me.aydgn.MorseMate.repository.AchievementRepository achievementRepository;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                if (categoryRepository.count() == 0) {
                        seedContent();
                } else {
                        log.info("Content (Categories/Lessons) already seeded.");
                }

                if (achievementRepository.count() == 0) {
                        seedAchievements();
                } else {
                        log.info("Achievements already seeded.");
                }
        }

        private void seedContent() {
                log.info("Seeding initial content...");

                // 1. Create Categories
                Category basicsCategory = Category.builder()
                                .name("Fundamentals")
                                .description("Learn the basic building blocks of Morse Code.")
                                .displayOrder(1)
                                .iconUrl("https://example.com/icons/basics.png")
                                .isActive(true)
                                .build();

                categoryRepository.save(basicsCategory);

                // 2. Create Lessons for "Fundamentals"

                // Lesson 1: Letters A-E
                Lesson lesson1 = Lesson.builder()
                                .category(basicsCategory)
                                .title("Letters A-E")
                                .description("Start your journey with the first 5 letters of the alphabet.")
                                .difficulty(Lesson.Difficulty.BEGINNER)
                                .content(
                                                "In this lesson, we will learn the first five letters: A, B, C, D, and E. Remember: '.' is a dot (short) and '-' is a dash (long).")
                                .orderIndex(1)
                                .pointsReward(50)
                                .estimatedDuration(10)
                                .build();

                lessonRepository.save(lesson1);

                // Exercises for Lesson 1
                createExercisesForLesson1(lesson1);

                // Lesson 2: Letters F-J
                Lesson lesson2 = Lesson.builder()
                                .category(basicsCategory)
                                .title("Letters F-J")
                                .description("Continue with the next set of letters.")
                                .difficulty(Lesson.Difficulty.BEGINNER)
                                .content("Now let's verify F, G, H, I, and J.")
                                .orderIndex(2)
                                .pointsReward(60)
                                .estimatedDuration(12)
                                .build();

                lessonRepository.save(lesson2);

                // Exercises for Lesson 2
                createExercisesForLesson2(lesson2);

                log.info("Content seeding completed successfully.");
        }

        private void seedAchievements() {
                log.info("Seeding initial achievements...");

                List<me.aydgn.MorseMate.entity.Achievement> achievements = List.of(
                                // Progress Achievements
                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("First Steps")
                                                .description("Complete your first lesson")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.PROGRESS)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.COMMON)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.LESSONS_COMPLETED)
                                                .criteriaTarget(1)
                                                .points(10)
                                                .iconUrl("https://img.icons8.com/color/96/ok--v1.png")
                                                .build(),

                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("Determined Learner")
                                                .description("Complete 5 lessons")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.PROGRESS)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.COMMON)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.LESSONS_COMPLETED)
                                                .criteriaTarget(5)
                                                .points(50)
                                                .iconUrl("https://img.icons8.com/color/96/learning.png")
                                                .build(),

                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("Morse Master")
                                                .description("Complete 20 lessons")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.PROGRESS)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.LEGENDARY)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.LESSONS_COMPLETED)
                                                .criteriaTarget(20)
                                                .points(500)
                                                .gemReward(100)
                                                .iconUrl("https://img.icons8.com/color/96/graduate.png")
                                                .build(),

                                // Streak Achievements
                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("Warming Up")
                                                .description("Reach a 3-day streak")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.STREAK)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.COMMON)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.STREAK_DAYS)
                                                .criteriaTarget(3)
                                                .points(30)
                                                .iconUrl("https://img.icons8.com/color/96/fire-element.png")
                                                .build(),

                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("On Fire")
                                                .description("Reach a 7-day streak")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.STREAK)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.RARE)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.STREAK_DAYS)
                                                .criteriaTarget(7)
                                                .points(100)
                                                .gemReward(50)
                                                .iconUrl("https://img.icons8.com/color/96/campfire.png")
                                                .build(),

                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("Unstoppable")
                                                .description("Reach a 30-day streak")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.STREAK)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.EPIC)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.STREAK_DAYS)
                                                .criteriaTarget(30)
                                                .points(500)
                                                .gemReward(250)
                                                .iconUrl("https://img.icons8.com/color/96/explosion.png")
                                                .build(),

                                // Performance Achievements
                                me.aydgn.MorseMate.entity.Achievement.builder()
                                                .name("Point Collector")
                                                .description("Earn 1000 total points")
                                                .category(me.aydgn.MorseMate.entity.Achievement.AchievementCategory.PERFORMANCE)
                                                .rarity(me.aydgn.MorseMate.entity.Achievement.AchievementRarity.RARE)
                                                .criteriaType(me.aydgn.MorseMate.entity.Achievement.CriteriaType.TOTAL_POINTS)
                                                .criteriaTarget(1000)
                                                .points(200)
                                                .iconUrl("https://img.icons8.com/color/96/coins.png")
                                                .build());

                achievementRepository.saveAll(achievements);
                log.info("Achievements seeded successfully.");
        }

        private void createExercisesForLesson1(Lesson lesson) {
                List<Exercise> exercises = List.of(
                                // A: .-
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("A")
                                                .correctAnswer(".-")
                                                .hint("Think of 'Alpha'. Dot-Dash.")
                                                .points(10)
                                                .timeLimit(30)
                                                .build(),

                                // B: -...
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.DECODE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("-...")
                                                .correctAnswer("B")
                                                .hint("Starts with a dash, followed by 3 dots.")
                                                .points(10)
                                                .timeLimit(30)
                                                .build(),

                                // C: -.-.
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.MEDIUM)
                                                .question("C")
                                                .correctAnswer("-.-.")
                                                .hint("Coca-Cola (Long-Short-Long-Short)")
                                                .points(15)
                                                .timeLimit(45)
                                                .build(),

                                // D: -..
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.MULTI_CHOICE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("Which letter is '-..' ?")
                                                .correctAnswer("D")
                                                .options(List.of("B", "D", "K", "S"))
                                                .points(10)
                                                .timeLimit(20)
                                                .build(),

                                // E: .
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("E")
                                                .correctAnswer(".")
                                                .hint("The shortest code. Just one dot.")
                                                .points(5)
                                                .timeLimit(15)
                                                .build());

                exerciseRepository.saveAll(exercises);
        }

        private void createExercisesForLesson2(Lesson lesson) {
                List<Exercise> exercises = List.of(
                                // F: ..-.
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.MEDIUM)
                                                .question("F")
                                                .correctAnswer("..-.")
                                                .points(15)
                                                .build(),

                                // G: --.
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.DECODE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("--.")
                                                .correctAnswer("G")
                                                .points(10)
                                                .build(),

                                // H: ....
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("H")
                                                .correctAnswer("....")
                                                .hint("Four dots.")
                                                .points(10)
                                                .build(),

                                // I: ..
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.MULTI_CHOICE)
                                                .difficulty(Exercise.Difficulty.EASY)
                                                .question("What is '..' ?")
                                                .correctAnswer("I")
                                                .options(List.of("I", "E", "S", "H"))
                                                .points(5)
                                                .build(),

                                // J: .---
                                Exercise.builder()
                                                .lesson(lesson)
                                                .type(Exercise.Type.ENCODE)
                                                .difficulty(Exercise.Difficulty.HARD)
                                                .question("J")
                                                .correctAnswer(".---")
                                                .hint("One dot, three dashes.")
                                                .points(20)
                                                .build());

                exerciseRepository.saveAll(exercises);
        }
}
