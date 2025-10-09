package me.aydgn.MorseMate.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCategoryRequest {

    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Integer displayOrder;

    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    private String iconUrl;

    private Boolean isActive;
}
