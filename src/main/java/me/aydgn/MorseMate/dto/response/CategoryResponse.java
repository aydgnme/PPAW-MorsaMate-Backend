package me.aydgn.MorseMate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import me.aydgn.MorseMate.entity.Category;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private String iconUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long lessonCount;

    public static CategoryResponse from(Category c) {
        if (c == null) return null;
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .displayOrder(c.getDisplayOrder())
                .iconUrl(c.getIconUrl())
                .isActive(c.getIsActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}