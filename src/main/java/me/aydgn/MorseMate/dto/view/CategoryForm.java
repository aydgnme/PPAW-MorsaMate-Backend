package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import me.aydgn.MorseMate.dto.response.CategoryResponse;

/**
 * Form backing bean for admin category management views.
 * Bridges Thymeleaf forms and service layer DTOs.
 */
@Getter
@Setter
@NoArgsConstructor
public class CategoryForm {

    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Integer displayOrder;

    @Size(max = 255, message = "Icon URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://.*)?$", message = "Icon URL must be a valid HTTP or HTTPS URL")
    private String iconUrl;

    private Boolean isActive = true;

    public CreateCategoryRequest toCreateRequest() {
        return CreateCategoryRequest.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .iconUrl(iconUrl)
                .isActive(isActive != null ? isActive : Boolean.TRUE)
                .build();
    }

    public UpdateCategoryRequest toUpdateRequest() {
        return UpdateCategoryRequest.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .iconUrl(iconUrl)
                .isActive(isActive)
                .build();
    }

    public static CategoryForm from(CategoryResponse category) {
        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setDisplayOrder(category.getDisplayOrder());
        form.setIconUrl(category.getIconUrl());
        form.setIsActive(category.getIsActive() != null ? category.getIsActive() : Boolean.TRUE);
        return form;
    }
}
