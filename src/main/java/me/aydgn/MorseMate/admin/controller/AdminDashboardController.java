package me.aydgn.MorseMate.admin.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.admin.service.EntityMetadataService;
import me.aydgn.MorseMate.entity.*;
import me.aydgn.MorseMate.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Main admin dashboard controller.
 * Registers all entities and shows admin home page with statistics.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final EntityMetadataService metadataService;

    // Repositories for statistics
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Register all entities on application startup.
     */
    @PostConstruct
    public void registerEntities() {
        log.info("Registering entities for Django-style admin panel...");

        // Register User
        metadataService.registerEntity(User.class, UserRepository.class);

        // Register Category
        metadataService.registerEntity(Category.class, CategoryRepository.class);

        // Register Lesson
        metadataService.registerEntity(Lesson.class, LessonRepository.class);

        // Register Exercise
        metadataService.registerEntity(Exercise.class, ExerciseRepository.class);

        // Register UserSubscription
        metadataService.registerEntity(UserSubscription.class, UserSubscriptionRepository.class);

        // Register Achievement
        metadataService.registerEntity(Achievement.class, AchievementRepository.class);

        log.info("Successfully registered {} entities", metadataService.getAllEntities().size());
    }

    /**
     * Admin dashboard home page.
     * URL: /admin
     */
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        log.info("Loading admin dashboard");

        // Gather statistics
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalLessons", lessonRepository.count());
        stats.put("totalExercises", exerciseRepository.count());
        stats.put("totalSubscriptions", userSubscriptionRepository.count());
        stats.put("totalAchievements", achievementRepository.count());

        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("currentUser", "Administrator");

        return "admin/dashboard";
    }
}
