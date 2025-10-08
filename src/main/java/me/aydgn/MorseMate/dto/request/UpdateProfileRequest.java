package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 255)
    private String profilePictureUrl;


    @Size(min = 3, max = 50)
    private String username;
}