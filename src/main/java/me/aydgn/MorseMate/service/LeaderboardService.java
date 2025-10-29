package me.aydgn.MorseMate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.aydgn.MorseMate.dto.response.LeaderboardEntryResponse;
import me.aydgn.MorseMate.entity.User;
import me.aydgn.MorseMate.repository.UserAchievementRepository;
import me.aydgn.MorseMate.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final UserRepository userRepository;
    private final UserAchievementRepository userAchievementRepository;

    /**
     * Get top users by total points
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopUsersByPoints(int limit) {
        log.debug("Fetching top {} users by points", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalPoints"));
        List<User> topUsers = userRepository.findAll(pageable).getContent();

        return buildLeaderboardEntries(topUsers);
    }

    /**
     * Get top users by level
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopUsersByLevel(int limit) {
        log.debug("Fetching top {} users by level", limit);

        Pageable pageable = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "level")
                    .and(Sort.by(Sort.Direction.DESC, "totalPoints")));
        List<User> topUsers = userRepository.findAll(pageable).getContent();

        return buildLeaderboardEntries(topUsers);
    }

    /**
     * Get top users by current streak
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopUsersByStreak(int limit) {
        log.debug("Fetching top {} users by streak", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "currentStreak"));
        List<User> topUsers = userRepository.findAll(pageable).getContent();

        return buildLeaderboardEntries(topUsers);
    }

    /**
     * Get top users by longest streak
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopUsersByLongestStreak(int limit) {
        log.debug("Fetching top {} users by longest streak", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "longestStreak"));
        List<User> topUsers = userRepository.findAll(pageable).getContent();

        return buildLeaderboardEntries(topUsers);
    }

    /**
     * Get top users by achievements count
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getTopUsersByAchievements(int limit) {
        log.debug("Fetching top {} users by achievements", limit);

        // Get all users sorted by total points first (as a tiebreaker)
        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "totalPoints"));

        // Build list with achievement counts
        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (User user : allUsers) {
            long achievementCount = userAchievementRepository.countByUserId(user.getId());
            entries.add(LeaderboardEntryResponse.from(user, achievementCount, 0L));
        }

        // Sort by achievement count descending
        entries.sort((a, b) -> {
            int countCompare = Long.compare(b.getAchievementsCount(), a.getAchievementsCount());
            if (countCompare != 0) return countCompare;
            return Integer.compare(b.getTotalPoints(), a.getTotalPoints());
        });

        // Assign ranks and limit
        for (int i = 0; i < Math.min(entries.size(), limit); i++) {
            entries.get(i).setRank((long) (i + 1));
        }

        return entries.subList(0, Math.min(entries.size(), limit));
    }

    /**
     * Get user's rank by points
     */
    @Transactional(readOnly = true)
    public Long getUserRankByPoints(Long userId) {
        log.debug("Fetching rank by points for user id: {}", userId);

        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "totalPoints"));

        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(userId)) {
                return (long) (i + 1);
            }
        }

        return null;
    }

    /**
     * Get user's rank by level
     */
    @Transactional(readOnly = true)
    public Long getUserRankByLevel(Long userId) {
        log.debug("Fetching rank by level for user id: {}", userId);

        List<User> allUsers = userRepository.findAll(
                Sort.by(Sort.Direction.DESC, "level")
                    .and(Sort.by(Sort.Direction.DESC, "totalPoints")));

        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(userId)) {
                return (long) (i + 1);
            }
        }

        return null;
    }

    /**
     * Get user's rank by current streak
     */
    @Transactional(readOnly = true)
    public Long getUserRankByStreak(Long userId) {
        log.debug("Fetching rank by streak for user id: {}", userId);

        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "currentStreak"));

        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getId().equals(userId)) {
                return (long) (i + 1);
            }
        }

        return null;
    }

    /**
     * Get user's rank by achievements
     */
    @Transactional(readOnly = true)
    public Long getUserRankByAchievements(Long userId) {
        log.debug("Fetching rank by achievements for user id: {}", userId);

        List<User> allUsers = userRepository.findAll(Sort.by(Sort.Direction.DESC, "totalPoints"));

        // Build list with achievement counts
        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (User user : allUsers) {
            long achievementCount = userAchievementRepository.countByUserId(user.getId());
            entries.add(LeaderboardEntryResponse.from(user, achievementCount, 0L));
        }

        // Sort by achievement count descending
        entries.sort((a, b) -> {
            int countCompare = Long.compare(b.getAchievementsCount(), a.getAchievementsCount());
            if (countCompare != 0) return countCompare;
            return Integer.compare(b.getTotalPoints(), a.getTotalPoints());
        });

        // Find user's rank
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getUserId().equals(userId)) {
                return (long) (i + 1);
            }
        }

        return null;
    }

    /**
     * Get user's position in leaderboard with context
     */
    @Transactional(readOnly = true)
    public LeaderboardEntryResponse getUserLeaderboardPosition(Long userId) {
        log.debug("Fetching leaderboard position for user id: {}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        long achievementCount = userAchievementRepository.countByUserId(userId);
        Long rank = getUserRankByPoints(userId);

        return LeaderboardEntryResponse.from(user, achievementCount, rank);
    }

    /**
     * Helper method to build leaderboard entries
     */
    private List<LeaderboardEntryResponse> buildLeaderboardEntries(List<User> users) {
        List<LeaderboardEntryResponse> entries = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            long achievementCount = userAchievementRepository.countByUserId(user.getId());
            long rank = i + 1;

            entries.add(LeaderboardEntryResponse.from(user, achievementCount, rank));
        }

        return entries;
    }
}
