package me.aydgn.MorseMate.service;

import me.aydgn.MorseMate.dto.response.ApiDocumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ApiDocumentationService {

    @Value("${app.version:1.0.0}")
    private String version;

    public ApiDocumentation getApiDocumentation() {
        return ApiDocumentation.builder()
                .title("MorseMate API")
                .version(version)
                .description("Interactive API for learning Morse code - A comprehensive platform for Morse code education")
                .baseUrl("")
                .endpointGroups(Arrays.asList(
                        buildSystemGroup(),
                        buildAuthGroup(),
                        buildCategoryGroup(),
                        buildLessonGroup(),
                        buildAchievementGroup(),
                        buildPowerUpGroup(),
                        buildGemGroup(),
                        buildLeaderboardGroup()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildSystemGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("System")
                .description("System health and information endpoints")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/")
                                .summary("API Root")
                                .description("Get API overview and available endpoints")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Successful response")
                                                .example("{\n  \"message\": \"Welcome to MorseMate API\",\n  \"version\": \"1.0.0\",\n  \"endpoints\": {\n    \"health\": \"/api/health\",\n    \"ping\": \"/api/ping\"\n  }\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/api/health")
                                .summary("Health Check")
                                .description("Check if the API is running and healthy")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("API is healthy")
                                                .example("{\n  \"status\": \"UP\",\n  \"timestamp\": \"2025-10-09T12:00:00\",\n  \"service\": \"MorseMate API\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/api/ping")
                                .summary("Ping")
                                .description("Simple ping endpoint")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Pong response")
                                                .example("{\n  \"message\": \"pong\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/api/info")
                                .summary("System Information")
                                .description("Get system and application information")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("System information")
                                                .example("{\n  \"applicationName\": \"MorseMate\",\n  \"version\": \"1.0.0\",\n  \"serverTime\": \"2025-10-09T12:00:00\",\n  \"javaVersion\": \"17.0.14\"\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildAuthGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Authentication")
                .description("User authentication and registration")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("POST")
                                .path("/auth/register")
                                .summary("Register User")
                                .description("Create a new user account")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .requestBody(ApiDocumentation.RequestBody.builder()
                                        .contentType("application/json")
                                        .schema("RegisterRequest")
                                        .example("{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"password\": \"SecurePass123!\",\n  \"fullName\": \"John Doe\"\n}")
                                        .build())
                                .responses(Map.of(
                                        "201", ApiDocumentation.Response.builder()
                                                .description("User registered successfully")
                                                .example("{\n  \"message\": \"User registered successfully\"\n}")
                                                .build(),
                                        "400", ApiDocumentation.Response.builder()
                                                .description("Invalid input")
                                                .example("{\n  \"message\": \"Username already exists\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("POST")
                                .path("/auth/login")
                                .summary("Login")
                                .description("Authenticate user and receive JWT token")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .requestBody(ApiDocumentation.RequestBody.builder()
                                        .contentType("application/json")
                                        .schema("LoginRequest")
                                        .example("{\n  \"username\": \"john_doe\",\n  \"password\": \"SecurePass123!\"\n}")
                                        .build())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Login successful")
                                                .example("{\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"type\": \"Bearer\",\n  \"userId\": 1,\n  \"username\": \"john_doe\"\n}")
                                                .build(),
                                        "401", ApiDocumentation.Response.builder()
                                                .description("Invalid credentials")
                                                .example("{\n  \"message\": \"Invalid username or password\"\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildCategoryGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Categories")
                .description("Morse code learning categories management")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/categories")
                                .summary("List Categories")
                                .description("Get all active categories ordered by display order")
                                .requiresAuth(false)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("List of categories")
                                                .example("[\n  {\n    \"id\": 1,\n    \"name\": \"Basics\",\n    \"description\": \"Basic Morse code lessons\",\n    \"displayOrder\": 1,\n    \"iconUrl\": \"https://example.com/icon.png\",\n    \"isActive\": true\n  }\n]")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/categories/{id}")
                                .summary("Get Category")
                                .description("Get a single category by ID")
                                .requiresAuth(false)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("id")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Category ID")
                                                .example("1")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Category found")
                                                .example("{\n  \"id\": 1,\n  \"name\": \"Basics\",\n  \"description\": \"Basic Morse code lessons\",\n  \"displayOrder\": 1,\n  \"iconUrl\": \"https://example.com/icon.png\",\n  \"isActive\": true\n}")
                                                .build(),
                                        "404", ApiDocumentation.Response.builder()
                                                .description("Category not found")
                                                .example("{\n  \"message\": \"Category not found with id: '1'\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("POST")
                                .path("/v1/categories")
                                .summary("Create Category")
                                .description("Create a new category (Admin only)")
                                .requiresAuth(true)
                                .requiredRoles(Arrays.asList("ADMIN"))
                                .parameters(Collections.emptyList())
                                .requestBody(ApiDocumentation.RequestBody.builder()
                                        .contentType("application/json")
                                        .schema("CreateCategoryRequest")
                                        .example("{\n  \"name\": \"Intermediate\",\n  \"description\": \"Intermediate level lessons\",\n  \"displayOrder\": 2,\n  \"iconUrl\": \"https://example.com/icon.png\",\n  \"isActive\": true\n}")
                                        .build())
                                .responses(Map.of(
                                        "201", ApiDocumentation.Response.builder()
                                                .description("Category created")
                                                .example("{\n  \"id\": 2,\n  \"name\": \"Intermediate\",\n  \"description\": \"Intermediate level lessons\",\n  \"displayOrder\": 2,\n  \"iconUrl\": \"https://example.com/icon.png\",\n  \"isActive\": true\n}")
                                                .build(),
                                        "400", ApiDocumentation.Response.builder()
                                                .description("Invalid input")
                                                .example("{\n  \"message\": \"Category name is required\"\n}")
                                                .build(),
                                        "409", ApiDocumentation.Response.builder()
                                                .description("Category already exists")
                                                .example("{\n  \"message\": \"Category already exists with name: 'Intermediate'\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("PUT")
                                .path("/v1/categories/{id}")
                                .summary("Update Category")
                                .description("Update an existing category (Admin only)")
                                .requiresAuth(true)
                                .requiredRoles(Arrays.asList("ADMIN"))
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("id")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Category ID")
                                                .example("1")
                                                .build()
                                ))
                                .requestBody(ApiDocumentation.RequestBody.builder()
                                        .contentType("application/json")
                                        .schema("UpdateCategoryRequest")
                                        .example("{\n  \"name\": \"Updated Basics\",\n  \"description\": \"Updated description\",\n  \"displayOrder\": 1,\n  \"isActive\": true\n}")
                                        .build())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Category updated")
                                                .example("{\n  \"id\": 1,\n  \"name\": \"Updated Basics\",\n  \"description\": \"Updated description\",\n  \"displayOrder\": 1,\n  \"isActive\": true\n}")
                                                .build(),
                                        "404", ApiDocumentation.Response.builder()
                                                .description("Category not found")
                                                .example("{\n  \"message\": \"Category not found with id: '1'\"\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("DELETE")
                                .path("/v1/categories/{id}")
                                .summary("Delete Category")
                                .description("Delete a category (Admin only)")
                                .requiresAuth(true)
                                .requiredRoles(Arrays.asList("ADMIN"))
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("id")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Category ID")
                                                .example("1")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Category deleted")
                                                .example("{\n  \"message\": \"Category deleted successfully\"\n}")
                                                .build(),
                                        "400", ApiDocumentation.Response.builder()
                                                .description("Cannot delete category with lessons")
                                                .example("{\n  \"message\": \"Cannot delete category 'Basics' as it has 5 associated lessons\"\n}")
                                                .build(),
                                        "404", ApiDocumentation.Response.builder()
                                                .description("Category not found")
                                                .example("{\n  \"message\": \"Category not found with id: '1'\"\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildLessonGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Lessons")
                .description("Morse code lesson management")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/categories/{categoryId}/lessons")
                                .summary("List Lessons by Category")
                                .description("Get all lessons for a specific category")
                                .requiresAuth(false)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("categoryId")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Category ID")
                                                .example("1")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("List of lessons")
                                                .example("[\n  {\n    \"id\": 1,\n    \"title\": \"Letter A\",\n    \"description\": \"Learn the letter A in Morse code\",\n    \"difficulty\": \"BEGINNER\",\n    \"orderIndex\": 1,\n    \"pointsReward\": 10,\n    \"estimatedDuration\": 5\n  }\n]")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/lessons/{id}")
                                .summary("Get Lesson")
                                .description("Get a single lesson by ID")
                                .requiresAuth(false)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("id")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Lesson ID")
                                                .example("1")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Lesson found")
                                                .example("{\n  \"id\": 1,\n  \"title\": \"Letter A\",\n  \"description\": \"Learn the letter A in Morse code\",\n  \"difficulty\": \"BEGINNER\",\n  \"orderIndex\": 1,\n  \"pointsReward\": 10,\n  \"estimatedDuration\": 5\n}")
                                                .build(),
                                        "404", ApiDocumentation.Response.builder()
                                                .description("Lesson not found")
                                                .example("{\n  \"message\": \"Lesson not found with id: '1'\"\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildAchievementGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Achievements")
                .description("User achievement and gamification system")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/achievements")
                                .summary("List All Achievements")
                                .description("Get all available achievements")
                                .requiresAuth(true)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("List of achievements")
                                                .example("[\n  {\n    \"id\": 1,\n    \"name\": \"First Steps\",\n    \"description\": \"Complete your first lesson\",\n    \"icon\": \"🏆\",\n    \"points\": 10,\n    \"gemReward\": 5\n  }\n]")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/achievements/user")
                                .summary("Get User Achievements")
                                .description("Get all achievements earned by the authenticated user")
                                .requiresAuth(true)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("User's achievements")
                                                .example("[\n  {\n    \"achievementId\": 1,\n    \"name\": \"First Steps\",\n    \"earnedAt\": \"2025-10-09T12:00:00\",\n    \"progress\": 100\n  }\n]")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildPowerUpGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Power-ups")
                .description("In-app power-up management and purchases")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/powerups")
                                .summary("List Power-ups")
                                .description("Get all available power-ups")
                                .requiresAuth(true)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("List of power-ups")
                                                .example("[\n  {\n    \"id\": 1,\n    \"name\": \"Double XP\",\n    \"description\": \"Earn 2x experience points\",\n    \"type\": \"XP_BOOST\",\n    \"costGems\": 50,\n    \"durationHours\": 2,\n    \"icon\": \"⚡\"\n  }\n]")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("POST")
                                .path("/v1/powerups/{id}/purchase")
                                .summary("Purchase Power-up")
                                .description("Purchase a power-up using gems")
                                .requiresAuth(true)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("id")
                                                .in("path")
                                                .type("integer")
                                                .required(true)
                                                .description("Power-up ID")
                                                .example("1")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Power-up purchased successfully")
                                                .example("{\n  \"message\": \"Power-up activated\",\n  \"expiresAt\": \"2025-10-09T14:00:00\"\n}")
                                                .build(),
                                        "400", ApiDocumentation.Response.builder()
                                                .description("Insufficient gems")
                                                .example("{\n  \"message\": \"Insufficient gems balance\"\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildGemGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Gems")
                .description("Virtual currency and transaction management")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/gems/balance")
                                .summary("Get Gem Balance")
                                .description("Get the authenticated user's gem balance")
                                .requiresAuth(true)
                                .parameters(Collections.emptyList())
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("User's gem balance")
                                                .example("{\n  \"balance\": 250,\n  \"totalEarned\": 500,\n  \"totalSpent\": 250\n}")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/gems/transactions")
                                .summary("Get Gem Transactions")
                                .description("Get the authenticated user's gem transaction history")
                                .requiresAuth(true)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("page")
                                                .in("query")
                                                .type("integer")
                                                .required(false)
                                                .description("Page number (0-indexed)")
                                                .example("0")
                                                .build(),
                                        ApiDocumentation.Parameter.builder()
                                                .name("size")
                                                .in("query")
                                                .type("integer")
                                                .required(false)
                                                .description("Page size")
                                                .example("20")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Transaction history")
                                                .example("[\n  {\n    \"id\": 1,\n    \"amount\": 10,\n    \"transactionType\": \"EARNED\",\n    \"description\": \"Completed lesson\",\n    \"balanceAfter\": 120,\n    \"createdAt\": \"2025-10-09T12:00:00\"\n  }\n]")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

    private ApiDocumentation.EndpointGroup buildLeaderboardGroup() {
        return ApiDocumentation.EndpointGroup.builder()
                .name("Leaderboard")
                .description("User rankings and competitive features")
                .endpoints(Arrays.asList(
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/leaderboard")
                                .summary("Get Global Leaderboard")
                                .description("Get the global leaderboard rankings")
                                .requiresAuth(true)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("type")
                                                .in("query")
                                                .type("string")
                                                .required(false)
                                                .description("Leaderboard type: xp, gems, achievements, streak")
                                                .example("xp")
                                                .build(),
                                        ApiDocumentation.Parameter.builder()
                                                .name("limit")
                                                .in("query")
                                                .type("integer")
                                                .required(false)
                                                .description("Number of top users to return")
                                                .example("100")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("Leaderboard rankings")
                                                .example("[\n  {\n    \"rank\": 1,\n    \"userId\": 5,\n    \"username\": \"john_doe\",\n    \"totalXP\": 1250,\n    \"level\": 5\n  }\n]")
                                                .build()
                                ))
                                .build(),
                        ApiDocumentation.Endpoint.builder()
                                .method("GET")
                                .path("/v1/leaderboard/me")
                                .summary("Get My Leaderboard Position")
                                .description("Get the authenticated user's position in the leaderboard")
                                .requiresAuth(true)
                                .parameters(Arrays.asList(
                                        ApiDocumentation.Parameter.builder()
                                                .name("type")
                                                .in("query")
                                                .type("string")
                                                .required(false)
                                                .description("Leaderboard type: xp, gems, achievements, streak")
                                                .example("xp")
                                                .build()
                                ))
                                .responses(Map.of(
                                        "200", ApiDocumentation.Response.builder()
                                                .description("User's leaderboard position")
                                                .example("{\n  \"rank\": 42,\n  \"totalXP\": 850,\n  \"level\": 3,\n  \"percentile\": 75\n}")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
