package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeartsResponse {
    private Integer hearts;
    private Integer maxHearts;
    private LocalDateTime lastHeartRefill;

    public static HeartsResponse of(Integer hearts, Integer maxHearts, LocalDateTime lastRefill) {
        return HeartsResponse.builder()
                .hearts(hearts)
                .maxHearts(maxHearts)
                .lastHeartRefill(lastRefill)
                .build();
    }
}