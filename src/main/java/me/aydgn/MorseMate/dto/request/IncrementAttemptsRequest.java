package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class IncrementAttemptsRequest {

    @NotNull
    private Long userProgressId;
}