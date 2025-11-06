package me.aydgn.MorseMate.dto.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.aydgn.MorseMate.dto.request.CreateLessonRequest;
import me.aydgn.MorseMate.dto.request.UpdateLessonRequest;
import me.aydgn.MorseMate.dto.response.LessonResponse;

/**
 * Form backing bean for admin lesson management views.
 */
@Getter
@Setter
@NoArgsConstructor
public class LessonForm {

    private Long id;

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED

    private Integer orderIndex;

    private Integer pointsReward = 10;

    private Integer estimatedDuration;

    private String content; // JSON string

    public CreateLessonRequest toCreateRequest() {
        return CreateLessonRequest.builder()
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .difficulty(difficulty)
                .orderIndex(orderIndex)
                .pointsReward(pointsReward)
                .estimatedDuration(estimatedDuration)
                .content(content)
                .build();
    }

    public UpdateLessonRequest toUpdateRequest() {
        return UpdateLessonRequest.builder()
                .title(title)
                .description(description)
                .categoryId(categoryId)
                .difficulty(difficulty)
                .orderIndex(orderIndex)
                .pointsReward(pointsReward)
                .estimatedDuration(estimatedDuration)
                .content(content)
                .build();
    }

    public static LessonForm from(LessonResponse lesson) {
        LessonForm form = new LessonForm();
        form.setId(lesson.getId());
        form.setTitle(lesson.getTitle());
        form.setDescription(lesson.getDescription());
        form.setCategoryId(lesson.getCategoryId());
        form.setDifficulty(lesson.getDifficulty());
        form.setOrderIndex(lesson.getOrderIndex());
        form.setPointsReward(lesson.getPointsReward());
        form.setEstimatedDuration(lesson.getEstimatedDuration());
        form.setContent(lesson.getContent());
        return form;
    }
}
