package me.aydgn.MorseMate.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import me.aydgn.MorseMate.dto.request.CreateCategoryRequest;
import me.aydgn.MorseMate.dto.request.UpdateCategoryRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Category DTO Validation Tests")
class CategoryDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ================ CreateCategoryRequest Tests ================

    @Test
    @DisplayName("CreateCategoryRequest: Valid request should pass validation")
    void createRequest_Valid() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Category")
                .description("Valid description")
                .displayOrder(1)
                .iconUrl("https://example.com/icon.png")
                .isActive(true)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Minimal valid request should pass")
    void createRequest_Minimal() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Null name should fail validation")
    void createRequest_NullName() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name(null)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Empty name should fail validation")
    void createRequest_EmptyName() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Blank name should fail validation")
    void createRequest_BlankName() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("   ")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name is required");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Name exceeding 100 chars should fail")
    void createRequest_NameTooLong() {
        // Given
        String longName = "A".repeat(101);
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name(longName)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name must not exceed 100 characters");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Name with exactly 100 chars should pass")
    void createRequest_NameExactly100Chars() {
        // Given
        String name = "A".repeat(100);
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name(name)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Description exceeding 1000 chars should fail")
    void createRequest_DescriptionTooLong() {
        // Given
        String longDescription = "A".repeat(1001);
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .description(longDescription)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description must not exceed 1000 characters");
    }

    @Test
    @DisplayName("CreateCategoryRequest: IconUrl exceeding 255 chars should fail")
    void createRequest_IconUrlTooLong() {
        // Given
        String longUrl = "https://example.com/" + "A".repeat(256);
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl(longUrl)
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Icon URL must not exceed 255 characters");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Valid HTTPS URL should pass")
    void createRequest_ValidHttpsUrl() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl("https://example.com/icon.png")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Valid HTTP URL should pass")
    void createRequest_ValidHttpUrl() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl("http://example.com/icon.png")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Empty URL should pass (optional field)")
    void createRequest_EmptyUrl() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl("")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateCategoryRequest: Invalid URL protocol should fail")
    void createRequest_InvalidUrlProtocol() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Valid Name")
                .iconUrl("ftp://example.com/icon.png")
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Icon URL must be a valid HTTP or HTTPS URL");
    }

    @Test
    @DisplayName("CreateCategoryRequest: Multiple validation errors")
    void createRequest_MultipleErrors() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("") // blank
                .description("A".repeat(1001)) // too long
                .iconUrl("ftp://invalid.com") // invalid protocol
                .build();

        // When
        Set<ConstraintViolation<CreateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
    }

    // ================ UpdateCategoryRequest Tests ================

    @Test
    @DisplayName("UpdateCategoryRequest: Valid request should pass validation")
    void updateRequest_Valid() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Category")
                .description("Updated description")
                .displayOrder(5)
                .iconUrl("https://example.com/updated.png")
                .isActive(false)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Empty request should pass (all fields optional)")
    void updateRequest_Empty() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Partial update should pass")
    void updateRequest_Partial() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .description("Only description")
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Name exceeding 100 chars should fail")
    void updateRequest_NameTooLong() {
        // Given
        String longName = "A".repeat(101);
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name(longName)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Category name must not exceed 100 characters");
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Description exceeding 1000 chars should fail")
    void updateRequest_DescriptionTooLong() {
        // Given
        String longDescription = "A".repeat(1001);
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .description(longDescription)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description must not exceed 1000 characters");
    }

    @Test
    @DisplayName("UpdateCategoryRequest: IconUrl exceeding 255 chars should fail")
    void updateRequest_IconUrlTooLong() {
        // Given
        String longUrl = "https://example.com/" + "A".repeat(256);
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .iconUrl(longUrl)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Icon URL must not exceed 255 characters");
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Invalid URL protocol should fail")
    void updateRequest_InvalidUrlProtocol() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .iconUrl("ftp://example.com/icon.png")
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Icon URL must be a valid HTTP or HTTPS URL");
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Valid HTTPS URL should pass")
    void updateRequest_ValidHttpsUrl() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .iconUrl("https://cdn.example.com/images/category/icon.svg")
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Empty string fields should pass")
    void updateRequest_EmptyStrings() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("")
                .description("")
                .iconUrl("")
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty(); // No @NotBlank on update request
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Setting isActive to false should pass")
    void updateRequest_IsActiveFalse() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .isActive(false)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateCategoryRequest: Negative displayOrder should pass (business logic validates)")
    void updateRequest_NegativeDisplayOrder() {
        // Given
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .displayOrder(-1)
                .build();

        // When
        Set<ConstraintViolation<UpdateCategoryRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty(); // No @Min constraint
    }
}
