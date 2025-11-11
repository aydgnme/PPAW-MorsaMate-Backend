# API Implementation Guide - Lab 7

## Overview

This document provides detailed technical information about the REST API implementation for the MorseMate application. The API follows RESTful principles and implements all CRUD operations as required by Lab 7.

## Architecture

### Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Gradle 8.x
- **Database**: PostgreSQL 15
- **ORM**: Hibernate (via Spring Data JPA)
- **Security**: Spring Security + JWT
- **Validation**: Jakarta Bean Validation (JSR 380)
- **Documentation**: Swagger/OpenAPI 3.0

### Project Structure

```
src/main/java/me/aydgn/MorseMate/
├── config/                      # Configuration classes
│   ├── SecurityConfig.java      # Security configuration
│   ├── JwtAuthenticationFilter.java
│   └── JpaConfig.java           # JPA auditing configuration
├── controller/                  # REST Controllers
│   ├── CategoryController.java
│   ├── LessonController.java
│   ├── ExerciseController.java
│   ├── AuthController.java
│   └── UserProgressController.java
├── service/                     # Business logic layer
│   ├── CategoryService.java
│   ├── LessonService.java
│   ├── ExerciseService.java
│   └── AuthService.java
├── repository/                  # Data access layer
│   ├── CategoryRepository.java
│   ├── LessonRepository.java
│   ├── ExerciseRepository.java
│   └── UserRepository.java
├── entity/                      # Database entities
│   ├── Category.java
│   ├── Lesson.java
│   ├── Exercise.java
│   └── User.java
├── dto/                         # Data Transfer Objects
│   ├── request/                 # Request DTOs
│   │   ├── CreateCategoryRequest.java
│   │   ├── UpdateCategoryRequest.java
│   │   └── ...
│   └── response/                # Response DTOs
│       ├── CategoryResponse.java
│       ├── LessonResponse.java
│       └── ...
├── enums/                       # Enumerations
│   ├── Difficulty.java
│   └── Role.java
├── exception/                   # Custom exceptions
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
└── security/                    # Security components
    └── JwtUtil.java
```

## Exercise 1: GET and GET(id) Implementation

### CategoryController Example

```java
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories - Get all categories
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "asc") String sort) {

        List<CategoryResponse> categories = categoryService.getAllCategories(difficulty, sort);
        return ResponseEntity.ok(categories);
    }

    // GET /api/categories/{id} - Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
}
```

### Service Layer

```java
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories(Difficulty difficulty, String sort) {
        List<Category> categories;

        if (difficulty != null) {
            categories = categoryRepository.findByDifficulty(difficulty);
        } else {
            categories = categoryRepository.findAll();
        }

        // Sort categories
        if ("desc".equalsIgnoreCase(sort)) {
            categories.sort(Comparator.comparing(Category::getName).reversed());
        } else {
            categories.sort(Comparator.comparing(Category::getName));
        }

        // Convert entities to DTOs
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return convertToResponse(category);
    }

    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setDifficulty(category.getDifficulty());
        response.setIconName(category.getIconName());
        response.setOrderIndex(category.getOrderIndex());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
```

### Repository Layer

```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByDifficulty(Difficulty difficulty);

    List<Category> findAllByOrderByOrderIndexAsc();

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.lessons WHERE c.id = :id")
    Optional<Category> findByIdWithLessons(@Param("id") Long id);
}
```

## Exercise 2: DTO Models and Automatic Mapping

### Entity Model (Database)

```java
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "order_index")
    private Integer orderIndex;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and setters...
}
```

### DTO Model (API Response)

```java
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;
    private String iconName;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional: Include related data
    private Integer lessonCount;
    private List<LessonSummary> lessons;

    // Getters and setters...
}
```

### Request DTOs

```java
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    private String iconName;

    private Integer orderIndex;

    // Getters and setters...
}
```

```java
public class UpdateCategoryRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private Difficulty difficulty;
    private String iconName;
    private Integer orderIndex;

    // Getters and setters...
}
```

### Mapping Strategy

**Manual Mapping** (currently implemented):
```java
private CategoryResponse convertToResponse(Category category) {
    CategoryResponse response = new CategoryResponse();
    response.setId(category.getId());
    response.setName(category.getName());
    response.setDescription(category.getDescription());
    response.setDifficulty(category.getDifficulty());
    response.setIconName(category.getIconName());
    response.setOrderIndex(category.getOrderIndex());
    response.setCreatedAt(category.getCreatedAt());
    response.setUpdatedAt(category.getUpdatedAt());

    // Add lesson count
    response.setLessonCount(category.getLessons().size());

    return response;
}
```

**Alternative: ModelMapper** (can be added):
```java
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
        return mapper;
    }
}
```

```java
// Usage in service
private final ModelMapper modelMapper;

private CategoryResponse convertToResponse(Category category) {
    return modelMapper.map(category, CategoryResponse.class);
}
```

## Exercise 3: POST and PUT Implementation

### POST - Create New Resource

```java
@PostMapping
public ResponseEntity<CategoryResponse> createCategory(
        @Valid @RequestBody CreateCategoryRequest request) {

    CategoryResponse created = categoryService.createCategory(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
}
```

Service implementation:
```java
public CategoryResponse createCategory(CreateCategoryRequest request) {
    // Check if category already exists
    if (categoryRepository.findByName(request.getName()).isPresent()) {
        throw new DuplicateResourceException("Category already exists with name: " + request.getName());
    }

    // Create new entity
    Category category = new Category();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    category.setDifficulty(request.getDifficulty());
    category.setIconName(request.getIconName());
    category.setOrderIndex(request.getOrderIndex());

    // Save to database
    Category saved = categoryRepository.save(category);

    // Convert to response DTO
    return convertToResponse(saved);
}
```

### PUT - Update Existing Resource

```java
@PutMapping("/{id}")
public ResponseEntity<CategoryResponse> updateCategory(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCategoryRequest request) {

    CategoryResponse updated = categoryService.updateCategory(id, request);

    return ResponseEntity.ok(updated);
}
```

Service implementation:
```java
public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
    // Find existing category
    Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    // Update fields (only if provided)
    if (request.getName() != null) {
        // Check for duplicate name
        categoryRepository.findByName(request.getName())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Category already exists with name: " + request.getName());
                });
        category.setName(request.getName());
    }

    if (request.getDescription() != null) {
        category.setDescription(request.getDescription());
    }

    if (request.getDifficulty() != null) {
        category.setDifficulty(request.getDifficulty());
    }

    if (request.getIconName() != null) {
        category.setIconName(request.getIconName());
    }

    if (request.getOrderIndex() != null) {
        category.setOrderIndex(request.getOrderIndex());
    }

    // Save changes
    Category updated = categoryRepository.save(category);

    // Convert to response DTO
    return convertToResponse(updated);
}
```

### DELETE - Remove Resource

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
}
```

Service implementation:
```java
public void deleteCategory(Long id) {
    Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

    categoryRepository.delete(category);
}
```

## Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed",
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
```

## Security Configuration

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## Validation

### Bean Validation Examples

```java
public class CreateLessonRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(value = 0, message = "Order index must be non-negative")
    private Integer orderIndex;

    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid format")
    private String slug;
}
```

## API Response Formats

### Success Response

```json
{
  "id": 1,
  "name": "Basic Letters",
  "description": "Learn basic letters in Morse code",
  "difficulty": "BEGINNER",
  "iconName": "alphabet",
  "orderIndex": 1,
  "lessonCount": 5,
  "createdAt": "2025-11-10T10:30:00",
  "updatedAt": "2025-11-11T14:20:00"
}
```

### Error Response

```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 999"
}
```

### Validation Error Response

```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Category name is required"
    },
    {
      "field": "difficulty",
      "message": "Difficulty is required"
    }
  ]
}
```

## Database Configuration

### application.properties

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/morse_code_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JWT Configuration
jwt.secret=${JWT_SECRET:MorseMate-Secret-Key}
jwt.expiration=86400000
```

## Testing with Postman

See `POSTMAN_TESTING.md` for detailed testing guide and `POSTMAN_COLLECTION.json` for ready-to-use collection.

## API Documentation

Interactive API documentation available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Best Practices Implemented

1. **RESTful Design**: Proper HTTP methods and status codes
2. **DTO Pattern**: Separation between entity and API models
3. **Validation**: Input validation on all endpoints
4. **Error Handling**: Centralized exception handling
5. **Security**: JWT authentication and role-based access
6. **Documentation**: Swagger/OpenAPI documentation
7. **Layered Architecture**: Controller → Service → Repository
8. **Transaction Management**: @Transactional on service methods
9. **Query Optimization**: Fetch strategies and query methods
10. **API Versioning**: Ready for /api/v1, /api/v2

## Code Quality

- **SOLID Principles**: Applied throughout
- **DRY**: Reusable components and utilities
- **Clean Code**: Meaningful names, small methods
- **Testing**: Unit and integration tests
- **Documentation**: JavaDoc and inline comments
