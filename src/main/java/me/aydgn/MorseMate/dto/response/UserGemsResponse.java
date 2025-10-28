package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.UserGems;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserGemsResponse {

    private Long id;
    private Long userId;
    private Integer balance;
    private Integer totalEarned;
    private Integer totalSpent;
    private LocalDateTime lastUpdated;

    public static UserGemsResponse from(UserGems userGems) {
        if (userGems == null) return null;

        return UserGemsResponse.builder()
                .id(userGems.getId())
                .userId(userGems.getUser() != null ? userGems.getUser().getId() : null)
                .balance(userGems.getBalance())
                .totalEarned(userGems.getTotalEarned())
                .totalSpent(userGems.getTotalSpent())
                .lastUpdated(userGems.getLastUpdated())
                .build();
    }
}
