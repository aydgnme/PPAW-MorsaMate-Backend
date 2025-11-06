package me.aydgn.MorseMate.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.view.AdminDashboardStats;
import me.aydgn.MorseMate.dto.view.AdminRecentActivityItem;
import me.aydgn.MorseMate.repository.AchievementRepository;
import me.aydgn.MorseMate.repository.CategoryRepository;
import me.aydgn.MorseMate.repository.ExerciseRepository;
import me.aydgn.MorseMate.repository.LessonRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;

    /**
     * Aggregate basic counts shown on the dashboard stat cards.
     */
    @Transactional(readOnly = true)
    public AdminDashboardStats getDashboardStats() {
        try {
            long totalCategories = categoryRepository.count();
            long totalLessons = lessonRepository.count();
            long totalExercises = exerciseRepository.count();
            long totalUsers = userRepository.count();
            long totalAchievements = achievementRepository.count();
            return new AdminDashboardStats(totalCategories, totalLessons, totalExercises, totalUsers, totalAchievements);
        } catch (Exception ex) {
            log.error("Failed to assemble admin dashboard stats", ex);
            return AdminDashboardStats.empty();
        }
    }

    /**
     * Placeholder for dashboard timeline items. Actual implementation can plug in audit logs later.
     */
    @Transactional(readOnly = true)
    public List<AdminRecentActivityItem> getRecentActivity() {
        return List.of();
    }
}
