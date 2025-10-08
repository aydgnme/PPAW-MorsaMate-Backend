package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.User;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profilePictureUrl;

    // gamification
    private Integer level;
    private Integer totalPoints;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer hearts;
    private Integer maxHearts;

    // status
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Convenient factory (optional; or use MapStruct)
    public static UserResponse from(User u) {
        if (u == null) return null;
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .profilePictureUrl(u.getProfilePictureUrl())
                .level(u.getLevel())
                .totalPoints(u.getTotalPoints())
                .currentStreak(u.getCurrentStreak())
                .longestStreak(u.getLongestStreak())
                .hearts(u.getHearts())
                .maxHearts(u.getMaxHearts())
                .isActive(u.getIsActive())
                .emailVerified(u.getEmailVerified())
                .createdAt(u.getCreatedAt())
                .lastLogin(u.getLastLogin())
                .build();
    }
}