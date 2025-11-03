package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaderboardEntryResponse {

    private Long userId;
    private String username;
    private String profilePictureUrl;
    private Integer totalPoints;
    private Integer level;
    private Integer currentStreak;
    private Integer longestStreak;
    private Long achievementsCount;
    private Long rank;

    public static LeaderboardEntryResponse from(User user, Long achievementsCount, Long rank) {
        if (user == null) return null;

        return LeaderboardEntryResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .profilePictureUrl(user.getProfilePictureUrl())
                .totalPoints(user.getTotalPoints())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .achievementsCount(achievementsCount)
                .rank(rank)
                .build();
    }

    public static LeaderboardEntryResponse from(User user, Long rank) {
        if (user == null) return null;

        return LeaderboardEntryResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .profilePictureUrl(user.getProfilePictureUrl())
                .totalPoints(user.getTotalPoints())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .rank(rank)
                .build();
    }
}
