package me.aydgn.MorseMate.dto.view;

/**
 * Immutable projection for the admin dashboard statistics cards.
 */
public record AdminDashboardStats(
        long totalCategories,
        long totalLessons,
        long totalExercises,
        long totalUsers,
        long totalAchievements
) {
    public static AdminDashboardStats empty() {
        return new AdminDashboardStats(0, 0, 0, 0, 0);
    }
}
