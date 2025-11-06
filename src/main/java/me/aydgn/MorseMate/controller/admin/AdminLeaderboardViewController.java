package me.aydgn.MorseMate.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for leaderboard viewing.
 * Read-only view of user rankings.
 */
@Controller
@RequestMapping("/admin/leaderboard")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeaderboardViewController extends AbstractAdminPageController {

    @GetMapping
    public String viewLeaderboard(@RequestParam(value = "type", defaultValue = "xp") String type,
                                  Model model) {
        log.debug("Admin viewing leaderboard (type={})", type);

        model.addAttribute("leaderboard", Collections.emptyList());
        model.addAttribute("leaderboardType", type);

        // Set title based on type
        String title = switch (type) {
            case "gems" -> "By Gems";
            case "achievements" -> "By Achievements";
            case "streak" -> "By Streak";
            default -> "By XP";
        };
        model.addAttribute("leaderboardTitle", title);

        // Mock statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalXP", 0);
        stats.put("totalGems", 0);
        stats.put("totalAchievements", 0);
        stats.put("longestStreak", 0);
        model.addAttribute("stats", stats);

        return render(model, AdminPage.LEADERBOARD);
    }
}
