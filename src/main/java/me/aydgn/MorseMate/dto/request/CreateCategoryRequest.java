package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Integer displayOrder;

    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Icon URL must be a valid HTTP or HTTPS URL")
    private String iconUrl;

    @Builder.Default
    private Boolean isActive = true;
}
