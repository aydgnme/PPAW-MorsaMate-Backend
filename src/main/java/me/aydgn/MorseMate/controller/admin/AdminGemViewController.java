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
 * Admin controller for gem transaction viewing.
 * Read-only view of gem transactions and statistics.
 */
@Controller
@RequestMapping("/admin/gems")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminGemViewController extends AbstractAdminPageController {

    @GetMapping
    public String viewGemTransactions(@RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "50") int size,
                                      Model model) {
        log.debug("Admin viewing gem transactions (page={}, size={})", page, size);

        // TODO: Implement actual gem transaction fetching
        // For now, return empty data
        model.addAttribute("transactions", Collections.emptyList());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", 0);

        // Mock statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGemsInCirculation", 0);
        stats.put("totalEarned", 0);
        stats.put("totalSpent", 0);
        stats.put("transactionCount", 0);
        model.addAttribute("stats", stats);

        return render(model, AdminPage.GEMS);
    }
}
