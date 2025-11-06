package me.aydgn.MorseMate.dto.view;

/**
 * Presentation DTO representing a single dashboard activity line item.
 */
public record AdminRecentActivityItem(
        String icon,
        String title,
        String description,
        String timeAgo
) {
    public static AdminRecentActivityItem of(String icon, String title, String description, String timeAgo) {
        return new AdminRecentActivityItem(icon, title, description, timeAgo);
    }
}
