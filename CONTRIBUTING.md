# Contributing to MorseMate

Thank you for your interest in contributing to MorseMate! 🎉

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Guidelines](#coding-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Testing](#testing)

## 📜 Code of Conduct

This project adheres to a Code of Conduct that all contributors are expected to follow. Please be respectful and constructive in all interactions.

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL 15+
- Gradle 8.x
- Git

### Development Setup

1. **Fork the repository**
   ```bash
   # Click the "Fork" button on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/MorseMate.git
   cd MorseMate
   ```

3. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Set up PostgreSQL database**
   ```bash
   createdb morse_code_db
   ```

5. **Build and run**
   ```bash
   ./gradlew clean build
   ./gradlew bootRun
   ```

6. **Verify setup**
   ```bash
   curl http://localhost:8080/v1/health
   ```

## 🤝 How to Contribute

### Reporting Bugs
Use the [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md) template to report bugs.

### Suggesting Features
Use the [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md) template to suggest features.

### Code Contributions

1. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. **Make your changes**
   - Write clean, readable code
   - Follow the coding guidelines
   - Add tests for new features
   - Update documentation

3. **Test your changes**
   ```bash
   ./gradlew test
   ./gradlew build
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add amazing feature"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Go to the original repository
   - Click "New Pull Request"
   - Fill out the PR template
   - Wait for review

## 💻 Coding Guidelines

### Java Code Style
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods small and focused
- Add JavaDoc for public methods
- Use Lombok annotations to reduce boilerplate

### Package Structure
```
me.aydgn.MorseMate/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
│   ├── request/    # Request DTOs
│   └── response/   # Response DTOs
├── entity/         # JPA entities
├── repository/     # Data repositories
├── service/        # Business logic
├── security/       # Security components
└── exception/      # Custom exceptions
```

### Best Practices

#### Controllers
```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        // Keep controllers thin - delegate to services
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
```

#### Services
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
```

#### Security
- Always validate user input
- Use parameterized queries (JPA does this automatically)
- Never expose sensitive data in responses
- Always hash passwords with BCrypt
- Implement rate limiting for authentication endpoints

## 📝 Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

### Examples
```bash
feat(auth): add JWT token refresh endpoint

Implements token refresh functionality to allow users to
obtain new access tokens without re-authenticating.

Closes #123
```

```bash
fix(users): prevent duplicate email registration

Added case-insensitive email check before user registration
to prevent duplicate accounts with same email.

Fixes #456
```

## 🔄 Pull Request Process

1. **Update Documentation**
   - Update README if needed
   - Add/update API documentation
   - Update CHANGELOG.md

2. **Ensure Tests Pass**
   ```bash
   ./gradlew test
   ./gradlew build
   ```

3. **Update Dependencies**
   ```bash
   ./gradlew dependencyUpdates
   ```

4. **Fill PR Template**
   - Describe your changes
   - Link related issues
   - Check all applicable boxes

5. **Code Review**
   - Address review comments
   - Keep discussions focused
   - Be open to suggestions

6. **Merge Requirements**
   - ✅ All tests pass
   - ✅ Code review approved
   - ✅ No merge conflicts
   - ✅ CI/CD pipeline succeeds

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Writing Tests
```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("Password123")
            .build();

        // When
        AuthResponse response = userService.register(request);

        // Then
        assertNotNull(response.getAccessToken());
        assertEquals("testuser", response.getUser().getUsername());
    }
}
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JPA/Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## ❓ Questions?

- Open an issue with the `question` label
- Join our discussions on GitHub
- Contact the maintainers

## 🙏 Thank You!

Your contributions make MorseMate better for everyone!
