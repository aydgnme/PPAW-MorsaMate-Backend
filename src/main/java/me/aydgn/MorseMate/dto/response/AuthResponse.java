package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String tokenType;     // e.g., "Bearer"
    private String accessToken;   // JWT
    private String refreshToken;  // optional, if you issue refresh tokens
    private Long   expiresIn;     // seconds until access token expiry (optional)

    private UserResponse user;    // authenticated user snapshot

    public static AuthResponse ofTokens(String accessToken,
                                        String refreshToken,
                                        Long expiresIn,
                                        UserResponse user) {
        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }
}