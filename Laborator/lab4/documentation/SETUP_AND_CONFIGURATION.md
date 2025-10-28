# MorseMate - Setup și Configurare
## Laborator 4 - PPAW - ORM Code First

**Student:** Aydogan Mert
**Proiect:** MorseMate
**Tehnologie:** Spring Boot 3.5.6 + JPA/Hibernate 6.6 + PostgreSQL 15.14

---

## PARTEA I: Pașii Efectuați pentru Generarea Bazei de Date

### 1. Pregătirea Mediului

#### 1.1 Instalare PostgreSQL
```bash
# macOS (Homebrew)
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Verificare instalare
/opt/homebrew/opt/postgresql@15/bin/psql --version
# Output: psql (PostgreSQL) 15.14 (Homebrew)
```

#### 1.2 Creare Bază de Date și Utilizator
```bash
# Conectare la PostgreSQL
/opt/homebrew/opt/postgresql@15/bin/psql -U aydgn -d postgres

# Creare rol postgres (dacă nu există)
CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD 'postgres';

# Creare bază de date
CREATE DATABASE morse_code_db
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8';

# Verificare
\l morse_code_db
```

**Rezultat:**
```
 Name          | Owner    | Encoding | Collate     | Ctype
---------------|----------|----------|-------------|-------------
 morse_code_db | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8
```

---

### 2. Configurare Proiect Spring Boot

#### 2.1 build.gradle - Dependențe
```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Database
    implementation 'org.postgresql:postgresql:42.7.7'

    // Flyway Migration (optional)
    // implementation 'org.flywaydb:flyway-core:9.22.3'

    // Lombok (reduce boilerplate)
    compileOnly 'org.projectlombok:lombok:1.18.40'
    annotationProcessor 'org.projectlombok:lombok:1.18.40'

    // MapStruct (DTO mapping)
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
}
```

#### 2.2 application.properties - Configurare Completă
```properties
# ==============================================
# APPLICATION CONFIGURATION
# ==============================================
spring.application.name=MorseMate
app.version=1.0.0

# ==============================================
# SERVER CONFIGURATION
# ==============================================
server.port=${SERVER_PORT:8080}

# ==============================================
# DATABASE CONFIGURATION
# ==============================================
# PostgreSQL Connection
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/morse_code_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# HikariCP Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ==============================================
# JPA / HIBERNATE CONFIGURATION
# ==============================================
# DDL Auto Mode (Code First)
spring.jpa.hibernate.ddl-auto=update

# Show SQL in console
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Hibernate Dialect
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Disable Open Session In View (better performance)
spring.jpa.open-in-view=false

# Naming Strategy
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl

# ==============================================
# FLYWAY CONFIGURATION (Optional - Currently Disabled)
# ==============================================
spring.flyway.enabled=false
# spring.flyway.baseline-on-migrate=true
# spring.flyway.locations=classpath:db/migration
# spring.flyway.validate-on-migrate=true

# ==============================================
# LOGGING CONFIGURATION
# ==============================================
logging.level.root=INFO
logging.level.me.aydgn=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# ==============================================
# JWT CONFIGURATION
# ==============================================
jwt.secret=${JWT_SECRET:MorseMate-Super-Secret-Key-For-Development-Only}
jwt.expiration=${JWT_EXPIRATION:86400000}

# ==============================================
# CORS CONFIGURATION
# ==============================================
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}
cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
cors.allowed-headers=*
cors.allow-credentials=true

# ==============================================
# FILE UPLOAD CONFIGURATION
# ==============================================
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# ==============================================
# JACKSON CONFIGURATION (JSON)
# ==============================================
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC
spring.jackson.default-property-inclusion=non_null

# ==============================================
# OPENAPI / SWAGGER CONFIGURATION
# ==============================================
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.packages-to-scan=me.aydgn.MorseMate.controller
```

#### 2.3 Variabile de Mediu (.env sau sistem)
```bash
# Database
export DB_URL="jdbc:postgresql://localhost:5432/morse_code_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"

# Server
export SERVER_PORT=8080

# JWT
export JWT_SECRET="C4v70Mjy7KZrOjtG5YeDIXD/hGWTGTcfUqguf7cao7zU0V78tzZq7zN3lX+W7wSM"
export JWT_EXPIRATION="86400000"

# CORS
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:5173"
```

---

### 3. Creare Entity Models (Code First)

#### 3.1 Structura Package-urilor
```
src/main/java/me/aydgn/MorseMate/
├── entity/               # JPA Entities
│   ├── BaseEntity.java          # Abstract base
│   ├── User.java                # Core entity
│   ├── Category.java
│   ├── Lesson.java
│   ├── Exercise.java
│   ├── Achievement.java
│   ├── PowerUp.java
│   ├── SubscriptionPlan.java
│   ├── PromoCode.java
│   ├── UserProgress.java        # Relations
│   ├── UserAchievement.java
│   ├── ExerciseAttempt.java
│   ├── UserGems.java
│   ├── GemTransaction.java
│   ├── UserPowerUp.java
│   ├── UserSubscription.java
│   └── Payment.java
├── repository/           # JPA Repositories
├── service/             # Business Logic
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── config/              # Configuration Classes
└── enums/               # Enumerations
```

#### 3.2 Exemplu Entity (User.java)
```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Check(constraints = "hearts >= 0 AND hearts <= max_hearts AND level >= 1")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false)
    private int level = 1;

    @Column(name = "total_points", nullable = false)
    private int totalPoints = 0;

    @Column(nullable = false)
    private int hearts = 5;

    @Column(name = "max_hearts", nullable = false)
    private int maxHearts = 5;

    // ... rest of fields

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProgress> userProgress = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ExerciseAttempt> exerciseAttempts = new ArrayList<>();

    // ... rest of relationships
}
```

#### 3.3 Exemplu Repository (UserRepository.java)
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE u.level >= :minLevel ORDER BY u.totalPoints DESC")
    List<User> findTopUsersByLevel(@Param("minLevel") int minLevel, Pageable pageable);
}
```

---

### 4. Activare JPA Auditing

#### 4.1 JpaConfig.java
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Activează automatic @CreatedDate și @LastModifiedDate
}
```

#### 4.2 BaseEntity cu Auditing
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

### 5. Generare Automată Bază de Date

#### 5.1 Prima Rulare (Initial Schema Creation)
```bash
# Build proiect
./gradlew clean build

# Run application
./gradlew bootRun

# SAU cu IntelliJ IDEA
# Click pe Run -> Run 'MorseMateApplication'
```

**Output Hibernate:**
```sql
Hibernate:
    create table users (
       id bigserial not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6),
        username varchar(50) not null unique,
        email varchar(100) not null unique,
        password_hash varchar(255) not null,
        full_name varchar(100),
        role varchar(20) not null,
        level int4 not null,
        total_points int4 not null,
        hearts int4 not null,
        max_hearts int4 not null,
        current_streak int4 not null,
        longest_streak int4 not null,
        profile_picture_url varchar(255),
        is_active boolean not null,
        email_verified boolean not null,
        last_login timestamp(6),
        last_heart_refill timestamp(6),
        primary key (id),
        constraint users_check check (hearts >= 0 AND hearts <= max_hearts AND level >= 1),
        constraint check_user_role check (role in ('USER','ADMIN','PREMIUM'))
    )

Hibernate: create index idx_users_username on users (username)
Hibernate: create index idx_users_email on users (email)
Hibernate: create index idx_users_role on users (role)

# ... similar pentru toate celelalte 15 tabele
```

**Verificare în PostgreSQL:**
```sql
-- Lista tabelelor
\dt

-- Output:
              List of relations
 Schema |        Name        | Type  |  Owner
--------+--------------------+-------+----------
 public | achievements       | table | postgres
 public | categories         | table | postgres
 public | exercise_attempts  | table | postgres
 public | exercises          | table | postgres
 public | gem_transactions   | table | postgres
 public | lessons            | table | postgres
 public | payments           | table | postgres
 public | power_ups          | table | postgres
 public | promo_codes        | table | postgres
 public | subscription_plans | table | postgres
 public | user_achievements  | table | postgres
 public | user_gems          | table | postgres
 public | user_power_ups     | table | postgres
 public | user_progress      | table | postgres
 public | user_subscriptions | table | postgres
 public | users              | table | postgres
(16 rows)
```

#### 5.2 A Doua Rulare (Schema Update - Add role column)
```bash
# Modificare entitate User.java - adăugare câmp role
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
private Role role = Role.USER;

# Enum Role
public enum Role {
    USER, ADMIN, PREMIUM
}

# Rulare aplicație
./gradlew bootRun
```

**Output Hibernate:**
```sql
Hibernate:
    alter table if exists users
       add column role varchar(20) not null check (role in ('USER','ADMIN','PREMIUM'))

# EROARE: column "role" of relation "users" contains null values
```

**Fix Manual (Migration V2):**
```sql
-- Pas cu pas pentru a evita eroarea
ALTER TABLE users ADD COLUMN role VARCHAR(20);
UPDATE users SET role = 'USER' WHERE role IS NULL;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE users ADD CONSTRAINT check_user_role CHECK (role IN ('USER', 'ADMIN', 'PREMIUM'));
CREATE INDEX idx_users_role ON users(role);
```

---

### 6. Testing și Validare

#### 6.1 Test Insert
```java
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(1, saved.getLevel());
        assertEquals(5, saved.getHearts());
    }

    @Test
    void testFindByUsername() {
        Optional<User> user = userRepository.findByUsernameIgnoreCase("test_user");
        assertTrue(user.isPresent());
        assertEquals("test@example.com", user.get().getEmail());
    }
}
```

#### 6.2 Test REST API
```bash
# Register user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe"
  }'

# Response: 201 Created
{
  "message": "User registered successfully",
  "userId": 1
}

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'

# Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "level": 1,
  "totalPoints": 0
}
```

---

## PARTEA II: Setări pentru Eager/Lazy Loading

### 1. Concepte Fundamentale

#### 1.1 Lazy Loading (Default)
**Definiție:** Entitățile relacionate sunt încărcate DOAR când sunt accesate explicit.

**Avantaje:**
- ✅ Performanță mai bună (queries mai puține)
- ✅ Memorie economisită
- ✅ Ideal pentru colecții mari

**Dezavantaje:**
- ❌ N+1 query problem
- ❌ `LazyInitializationException` dacă session-ul e închis

**Exemplu:**
```java
@Entity
public class User extends BaseEntity {

    // Lazy loading (IMPLICIT pentru @OneToMany)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ExerciseAttempt> exerciseAttempts;

    // Când accesezi:
    User user = userRepository.findById(1L).get();
    // Query 1: SELECT * FROM users WHERE id = 1

    user.getExerciseAttempts().size();
    // Query 2: SELECT * FROM exercise_attempts WHERE user_id = 1
}
```

#### 1.2 Eager Loading
**Definiție:** Entitățile relacionate sunt încărcate IMEDIAT cu entitatea principală.

**Avantaje:**
- ✅ Nu există `LazyInitializationException`
- ✅ Toate datele disponibile imediat
- ✅ Bun pentru relații mici și critice

**Dezavantaje:**
- ❌ Queries mari și complexe
- ❌ Overhead de memorie
- ❌ Performanță scăzută pentru date mari

**Exemplu:**
```java
@Entity
public class Exercise extends BaseEntity {

    // Eager loading (EXPLICIT pentru @ManyToOne)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    // Când accesezi:
    Exercise exercise = exerciseRepository.findById(1L).get();
    // Query 1: SELECT * FROM exercises e
    //          LEFT JOIN lessons l ON e.lesson_id = l.id
    //          WHERE e.id = 1
}
```

---

### 2. Configurare Lazy/Eager Loading în MorseMate

#### 2.1 Setări Default JPA
```properties
# application.properties

# Disable Open Session In View (LAZY loading funcționează doar în transacții)
spring.jpa.open-in-view=false

# Enable lazy loading statistics
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

#### 2.2 Entități cu Lazy Loading (Recomandat)

**User.java:**
```java
@Entity
public class User extends BaseEntity {

    // ✅ LAZY: Colecții mari de date
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserProgress> userProgress = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ExerciseAttempt> exerciseAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<GemTransaction> gemTransactions = new ArrayList<>();

    // ✅ EAGER: Relație 1:1 critică
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private UserGems userGems;
}
```

**Exercise.java:**
```java
@Entity
public class Exercise extends BaseEntity {

    // ✅ EAGER: Lesson e necesar întotdeauna
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // ✅ LAZY: Attempts pot fi multe
    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    private List<ExerciseAttempt> attempts = new ArrayList<>();
}
```

#### 2.3 Folosire JPQL cu JOIN FETCH (Explicit Eager)
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ❌ Lazy loading - Multiple queries
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdLazy(@Param("id") Long id);

    // ✅ Eager loading explicit - Single query cu JOIN
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userProgress " +
           "LEFT JOIN FETCH u.userGems " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithProgress(@Param("id") Long id);

    // ✅ Eager loading pentru multiple relații
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.exerciseAttempts ea " +
           "LEFT JOIN FETCH ea.exercise e " +
           "LEFT JOIN FETCH e.lesson " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithExerciseHistory(@Param("id") Long id);
}
```

#### 2.4 Entity Graph (Alternativă Modernă)
```java
@Entity
@NamedEntityGraph(
    name = "User.withProgressAndGems",
    attributeNodes = {
        @NamedAttributeNode("userProgress"),
        @NamedAttributeNode("userGems")
    }
)
public class User extends BaseEntity {
    // ... entity fields
}

// Repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(value = "User.withProgressAndGems", type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findById(Long id);
}
```

---

### 3. Demonstrație Practică Lazy vs Eager

#### 3.1 Scenario 1: Lazy Loading (N+1 Problem)
```java
@Service
public class UserService {

    @Transactional
    public void demonstrateLazyLoading() {
        List<User> users = userRepository.findAll();
        // Query 1: SELECT * FROM users

        for (User user : users) {
            System.out.println("User: " + user.getUsername());

            // LAZY: Query separat pentru fiecare user!
            int attempts = user.getExerciseAttempts().size();
            // Query 2: SELECT * FROM exercise_attempts WHERE user_id = 1
            // Query 3: SELECT * FROM exercise_attempts WHERE user_id = 2
            // ... Query N+1 pentru N useri

            System.out.println("Attempts: " + attempts);
        }
        // Total Queries: 1 + N (N+1 problem!)
    }
}
```

**Output SQL:**
```sql
-- Query 1
SELECT * FROM users;

-- Query 2 (pentru user_id = 1)
SELECT * FROM exercise_attempts WHERE user_id = 1;

-- Query 3 (pentru user_id = 2)
SELECT * FROM exercise_attempts WHERE user_id = 2;

-- ... N queries pentru N useri
```

#### 3.2 Scenario 2: Eager Loading (JOIN FETCH)
```java
@Service
public class UserService {

    @Transactional
    public void demonstrateEagerLoading() {
        List<User> users = userRepository.findAllWithExerciseAttempts();
        // Query 1: SELECT * FROM users u
        //          LEFT JOIN exercise_attempts ea ON u.id = ea.user_id

        for (User user : users) {
            System.out.println("User: " + user.getUsername());

            // EAGER: Data deja încărcată, NU face query nou!
            int attempts = user.getExerciseAttempts().size();

            System.out.println("Attempts: " + attempts);
        }
        // Total Queries: 1 (optimal!)
    }
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.exerciseAttempts")
    List<User> findAllWithExerciseAttempts();
}
```

**Output SQL:**
```sql
-- Single Query cu JOIN
SELECT u.*, ea.*
FROM users u
LEFT JOIN exercise_attempts ea ON u.id = ea.user_id;

-- Total: 1 query pentru TOȚI userii + attempts!
```

#### 3.3 Scenario 3: Batch Fetching (Compromis)
```java
@Entity
public class User extends BaseEntity {

    // Batch fetching: încarcă în loturi de 10
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<ExerciseAttempt> exerciseAttempts = new ArrayList<>();
}

// Rezultat:
// Query 1: SELECT * FROM users (returneză 50 useri)
// Query 2: SELECT * FROM exercise_attempts WHERE user_id IN (1,2,3,...,10)
// Query 3: SELECT * FROM exercise_attempts WHERE user_id IN (11,12,13,...,20)
// ...
// Total: 1 + ceil(50/10) = 6 queries (mult mai bine decât 51!)
```

---

### 4. Best Practices și Recomandări

#### 4.1 Când să folosești LAZY
✅ **Use Cases:**
- Colecții mari (`List<ExerciseAttempt>`, `List<UserProgress>`)
- Date rar accesate
- API endpoints care nu necesită toate relațiile
- Dashboard-uri cu date sumarizate

✅ **Implementare:**
```java
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
private List<ExerciseAttempt> exerciseAttempts;

// În service
@Transactional(readOnly = true)
public UserDTO getUserSummary(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    // Nu accesăm exerciseAttempts, deci nu se face query
    return new UserDTO(user.getId(), user.getUsername(), user.getLevel());
}
```

#### 4.2 Când să folosești EAGER
✅ **Use Cases:**
- Relații 1:1 critice (`UserGems`, `UserSubscription`)
- Date ÎNTOTDEAUNA necesare
- Relații @ManyToOne frecvent accesate
- Admin dashboards cu date complete

✅ **Implementare:**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "lesson_id")
private Lesson lesson;

// Sau cu JOIN FETCH
@Query("SELECT e FROM Exercise e JOIN FETCH e.lesson WHERE e.id = :id")
Optional<Exercise> findByIdWithLesson(@Param("id") Long id);
```

#### 4.3 Când să folosești JOIN FETCH
✅ **Use Cases:**
- Lists/tables cu mai multe entități
- API endpoints care returnează obiecte complexe
- Reports și statistici
- Optimizare performance-critical queries

✅ **Implementare:**
```java
@Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.userProgress up
    LEFT JOIN FETCH up.lesson
    LEFT JOIN FETCH u.userGems
    WHERE u.level >= :minLevel
    ORDER BY u.totalPoints DESC
    """)
List<User> findTopUsersWithDetails(@Param("minLevel") int minLevel, Pageable pageable);
```

#### 4.4 Evitare LazyInitializationException

**Problem:**
```java
// ❌ Excepție dacă session-ul e închis
@GetMapping("/users/{id}")
public UserDTO getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    // Session închis aici
    int attempts = user.getExerciseAttempts().size();
    // LazyInitializationException!
}
```

**Solution 1: @Transactional pe service layer**
```java
@Service
public class UserService {

    @Transactional(readOnly = true)
    public UserDTO getUserWithAttempts(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        // Session încă activ în @Transactional
        int attempts = user.getExerciseAttempts().size();
        return new UserDTO(user, attempts);
    }
}
```

**Solution 2: DTO Projection**
```java
@Query("""
    SELECT new me.aydgn.MorseMate.dto.UserSummaryDTO(
        u.id, u.username, u.email, u.level, u.totalPoints,
        COUNT(ea.id)
    )
    FROM User u
    LEFT JOIN u.exerciseAttempts ea
    WHERE u.id = :id
    GROUP BY u.id
    """)
Optional<UserSummaryDTO> getUserSummary(@Param("id") Long id);
```

**Solution 3: Open Session In View (NOT RECOMMENDED)**
```properties
# application.properties
# ❌ Anti-pattern, dar funcționează
spring.jpa.open-in-view=true
```

---

### 5. Monitoring și Debugging

#### 5.1 Activare Query Logging
```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Hibernate statistics
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

#### 5.2 Numărare Queries
```java
@Configuration
public class HibernateStatisticsConfig {

    @Bean
    public ServletContextInitializer statisticsInitializer(EntityManagerFactory emf) {
        return servletContext -> {
            SessionFactoryImpl sf = emf.unwrap(SessionFactoryImpl.class);
            sf.getStatistics().setStatisticsEnabled(true);
        };
    }
}

// În teste
@Test
void testQueryCount() {
    SessionFactory sf = entityManagerFactory.unwrap(SessionFactory.class);
    Statistics stats = sf.getStatistics();
    stats.clear();

    userService.getUserWithAttempts(1L);

    long queryCount = stats.getPrepareStatementCount();
    System.out.println("Queries executed: " + queryCount);
    assertEquals(1, queryCount); // Expect 1 query cu JOIN FETCH
}
```

#### 5.3 P6Spy (Query Inspector)
```gradle
// build.gradle
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
```

```properties
# application.properties
decorator.datasource.p6spy.enable-logging=true
decorator.datasource.p6spy.logging=slf4j
decorator.datasource.p6spy.multiline=true
```

**Output:**
```
Hibernate:
    SELECT u.* FROM users u WHERE u.id = ?
    Parameters: [1]
    Execution Time: 3ms

Hibernate:
    SELECT ea.* FROM exercise_attempts ea WHERE ea.user_id = ?
    Parameters: [1]
    Execution Time: 2ms
```

---

### 6. Comparație Performance

| Aspect | Lazy Loading | Eager Loading | JOIN FETCH |
|--------|--------------|---------------|------------|
| **Queries** | N+1 queries | 1 big query | 1 optimized query |
| **Memory** | ✅ Low | ❌ High | ⚠️ Medium |
| **Speed (small data)** | ⚠️ Medium | ✅ Fast | ✅ Fast |
| **Speed (large data)** | ✅ Fast | ❌ Slow | ⚠️ Medium |
| **Complexity** | ✅ Simple | ⚠️ Medium | ❌ Complex SQL |
| **Best for** | Large collections | 1:1 relations | API responses |

**Recomandare Generală:**
1. **Default:** LAZY pentru @OneToMany
2. **Optimize:** JOIN FETCH pentru queries specifice
3. **Critice:** EAGER pentru @ManyToOne și @OneToOne esențiale
4. **Performance:** DTO Projections pentru read-heavy endpoints

---

### 7. Exemple Practice MorseMate

#### 7.1 User Profile API (Eager + Lazy mix)
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}/profile")
    @Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long id) {
        // EAGER: Basic info + userGems (1:1)
        User user = userRepository.findById(id).orElseThrow();

        // LAZY: Load only count, not full collection
        int completedLessons = user.getUserProgress().stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();

        return ResponseEntity.ok(UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .level(user.getLevel())
                .totalPoints(user.getTotalPoints())
                .gems(user.getUserGems().getTotalGems()) // EAGER
                .completedLessons(completedLessons) // LAZY
                .build());
    }
}
```

#### 7.2 Leaderboard API (JOIN FETCH optimization)
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.userGems
        WHERE u.level >= :minLevel
        ORDER BY u.totalPoints DESC
        """)
    List<User> findTopUsersOptimized(@Param("minLevel") int minLevel, Pageable pageable);
}

@Service
public class LeaderboardService {

    @Transactional(readOnly = true)
    public List<LeaderboardDTO> getTopUsers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> users = userRepository.findTopUsersOptimized(1, pageable);

        // Single query cu JOIN FETCH!
        return users.stream()
                .map(u -> new LeaderboardDTO(
                        u.getUsername(),
                        u.getLevel(),
                        u.getTotalPoints(),
                        u.getUserGems().getTotalGems()
                ))
                .toList();
    }
}
```

---

**Ultima Actualizare:** 2025-10-22
**Autor:** Aydogan Mert
**Versiune Document:** 1.0
