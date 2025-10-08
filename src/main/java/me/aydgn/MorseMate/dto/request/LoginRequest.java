package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    /**
     * Can be either email or username. Keep one flexible field to simplify client UX.
     * On server side:
     *  - if contains '@' -> treat as email
     *  - else -> treat as username
     */
    @NotBlank
    @Size(min = 3, max = 100) // usernames up to 50, emails up to 100 (safe upper bound)
    private String identifier;

    @NotBlank
    @Size(min = 8, max = 255)
    private String password;

    // Optional: remember me -> longer refresh token TTL, etc.
    private Boolean rememberMe;
}