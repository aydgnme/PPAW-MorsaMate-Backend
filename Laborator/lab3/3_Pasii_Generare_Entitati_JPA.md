# LAB 3 - Pașii Efectuați Pentru Generarea Entităților JPA (Database First)

**Student:** [Aydogan Mert]
**Proiect:** MorseMate - Morse Code Learning Platform
**Framework:** Spring Boot 3.2.0 + JPA/Hibernate
**Baza de date:** PostgreSQL

---

## 📋 INTRODUCERE

Deoarece folosim **Spring Boot cu JPA/Hibernate** în loc de .NET Entity Framework, procesul de "Database First" este diferit. În .NET EF, se folosește un designer vizual (.edmx) pentru a genera modelele din baza de date. În Spring Boot JPA, există mai multe abordări:

1. **Manual** - Scriem entities cu adnotări JPA (recomandat)
2. **JPA Buddy** - Plugin IntelliJ IDEA pentru reverse engineering
3. **Hibernate Tools** - Command-line reverse engineering
4. **JHipster** - Generator de entități din schema

**Am folosit abordarea MANUALĂ** deoarece oferă cel mai mare control și este metoda standard în industrie.

---

## 🔧 PARTEA 1: Pregătirea Proiectului

### Pasul 1: Crearea Structurii de Bază de Date

**Tool folosit:** PostgreSQL + SQL Scripts

**Schema creată:**
```sql
-- Tabelul categories (PARENT)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabelul lessons (CHILD - FK către categories)
CREATE TABLE lessons (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20) CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    content TEXT,
    order_index INTEGER,
    points_reward INTEGER DEFAULT 10 NOT NULL,
    estimated_duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- FOREIGN KEY CONSTRAINT
    CONSTRAINT lessons_category_id_fkey
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE CASCADE
);

-- Index pentru performanță
CREATE INDEX idx_lessons_category_id ON lessons(category_id);
CREATE INDEX idx_lessons_difficulty ON lessons(difficulty);
CREATE INDEX idx_categories_is_active ON categories(is_active);
```

**Verificare:**
```sql
-- Verificăm că tabelele au fost create
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('categories', 'lessons');

-- Verificăm foreign key constraint
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'lessons';
```

---

## 🔧 PARTEA 2: Configurarea Proiectului Spring Boot

### Pasul 2: Adăugarea Dependințelor Maven

**Fișier:** `pom.xml`

```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok (pentru reducerea boilerplate code) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### Pasul 3: Configurarea Conexiunii la Baza de Date

**Fișier:** `src/main/resources/application.properties`

```properties
# Database Connection (echivalent cu connectionStrings în .NET)
spring.datasource.url=jdbc:postgresql://localhost:5432/morsemate
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# DDL Strategy (echivalent cu Migrations în .NET)
# update = actualizează schema dacă există diferențe (similar cu Update-Database în EF)
# validate = verifică doar că schema match-uiește entitățile
# none = nu face nimic (pentru producție)
spring.jpa.hibernate.ddl-auto=validate

# SQL Logging (pentru debugging)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## 🔧 PARTEA 3: Generarea Entităților (Database First)

### Pasul 4: Crearea Entității Category

**Fișier:** `src/main/java/me/aydgn/MorseMate/entity/Category.java`

**Proces:**

1. **Analizăm structura tabelului în PostgreSQL:**
   ```sql
   \d categories
   ```

   Output:
   ```
   Column        | Type         | Nullable | Default
   --------------|--------------|----------|-------------------
   id            | integer      | not null | nextval('...')
   name          | varchar(100) | not null |
   description   | text         |          |
   display_order | integer      |          |
   icon_url      | varchar(255) |          |
   is_active     | boolean      | not null | true
   created_at    | timestamp    |          | CURRENT_TIMESTAMP
   updated_at    | timestamp    |          | CURRENT_TIMESTAMP
   ```

2. **Mapăm fiecare coloană la un câmp Java cu adnotări JPA:**

```java
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    // SQL: id SERIAL PRIMARY KEY
    // SERIAL în PostgreSQL = auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SQL: name VARCHAR(100) NOT NULL
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // SQL: description TEXT
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // SQL: display_order INTEGER
    @Column(name = "display_order")
    private Integer displayOrder;

    // SQL: icon_url VARCHAR(255)
    @Size(max = 255)
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    // SQL: is_active BOOLEAN DEFAULT TRUE
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // RELAȚIE: One-to-Many către Lesson
    // mappedBy = "category" indică că Lesson.category este owner-ul relației
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    // Lifecycle hooks pentru validări
    @PrePersist
    private void prePersist() {
        if (this.name != null) this.name = this.name.trim();
        if (this.iconUrl != null) this.iconUrl = this.iconUrl.trim();
        if (this.isActive == null) this.isActive = true;
    }
}
```

**Adnotări JPA folosite:**
- `@Entity` - Marchează clasa ca entitate JPA
- `@Table(name = "categories")` - Specifică numele tabelului
- `@Id` - Marchează cheia primară
- `@GeneratedValue` - Auto-increment pentru PK
- `@Column` - Mapare coloană (opțional, dar explicit)
- `@OneToMany` - Relație one-to-many
- `@PrePersist` - Hook executat înainte de INSERT

**Adnotări Lombok:**
- `@Getter/@Setter` - Generează getter/setter automat
- `@NoArgsConstructor/@AllArgsConstructor` - Constructori
- `@Builder` - Pattern Builder pentru construire obiecte

### Pasul 5: Crearea Entității Lesson (cu Foreign Key)

**Fișier:** `src/main/java/me/aydgn/MorseMate/entity/Lesson.java`

**Proces:**

1. **Analizăm tabelul lessons:**
   ```sql
   \d lessons
   ```

2. **Identificăm FOREIGN KEY:**
   ```sql
   -- Cheia străină
   category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE CASCADE
   ```

3. **Mapăm entitatea cu relația @ManyToOne:**

```java
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FOREIGN KEY MAPPING
    // SQL: category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "category_id",           // Numele coloanei FK în tabel
        nullable = false,                // NOT NULL constraint
        foreignKey = @ForeignKey(name = "lessons_category_id_fkey")  // FK constraint name
    )
    private Category category;

    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ENUM mapping pentru difficulty
    // SQL: difficulty VARCHAR(20) CHECK (...)
    public enum Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private Difficulty difficulty;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "points_reward", nullable = false)
    @Builder.Default
    private Integer pointsReward = 10;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // RELAȚIE: One-to-Many către Exercise
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        final LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.title != null) this.title = this.title.trim();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.title != null) this.title = this.title.trim();
    }
}
```

**Adnotări pentru Foreign Key:**
- `@ManyToOne` - Many lessons → One category
- `@JoinColumn(name = "category_id")` - Specifică coloana FK
- `foreignKey = @ForeignKey(name = "...")` - Numele constraint-ului FK
- `fetch = FetchType.LAZY` - Încărcare lazy (pentru performanță)
- `optional = false` - NOT NULL constraint

---

## 🔧 PARTEA 4: Crearea Repository Interfaces (DbContext Equivalent)

### Pasul 6: CategoryRepository (DbSet<Category> Equivalent)

**Fișier:** `src/main/java/me/aydgn/MorseMate/repository/CategoryRepository.java`

```java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Spring generează automat implementarea din numele metodei
    List<Category> findByIsActiveTrue();
    List<Category> findAllByOrderByDisplayOrderAsc();
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // Custom JPQL query
    @Query("select count(1) from Lesson l where l.category.id = :categoryId")
    long countLessons(@Param("categoryId") Long categoryId);

    // Eager loading pentru relații
    @EntityGraph(attributePaths = "lessons")
    Optional<Category> findWithLessonsById(Long id);
}
```

**Ce face JpaRepository automat:**
- `save(entity)` - INSERT sau UPDATE
- `findById(id)` - SELECT by PK
- `findAll()` - SELECT all
- `deleteById(id)` - DELETE
- `count()` - COUNT(*)

### Pasul 7: LessonRepository (cu Foreign Key Queries)

**Fișier:** `src/main/java/me/aydgn/MorseMate/repository/LessonRepository.java`

```java
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Query după foreign key
    List<Lesson> findByCategoryIdOrderByOrderIndexAsc(Long categoryId);

    // Query prin relație (JOIN automat)
    List<Lesson> findByCategory_NameIgnoreCaseOrderByOrderIndexAsc(String categoryName);

    // Paginare
    Page<Lesson> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Eager loading
    @EntityGraph(attributePaths = "exercises")
    Optional<Lesson> findWithExercisesById(Long id);

    // Custom query
    @Query("select count(e) from Exercise e where e.lesson.id = :lessonId")
    long countExercises(@Param("lessonId") Long lessonId);
}
```

---

## 🔧 PARTEA 5: Crearea Service Layer (Accessor Classes)

### Pasul 8: CategoryService (CategoryAccessor Equivalent)

**Fișier:** `src/main/java/me/aydgn/MorseMate/service/CategoryService.java`

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAllByOrderByDisplayOrderAsc()
            .stream()
            .map(CategoryResponse::from)
            .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse::from(category);
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // Validare duplicat
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        Category category = Category.builder()
            .name(request.getName())
            .description(request.getDescription())
            .displayOrder(request.getDisplayOrder())
            .isActive(true)
            .build();

        category = categoryRepository.save(category);
        return CategoryResponse::from(category);
    }

    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);

        // Verificare foreign key constraint
        long lessonCount = categoryRepository.countLessons(id);
        if (lessonCount > 0) {
            throw new InvalidOperationException(
                "Cannot delete category with associated lessons"
            );
        }

        categoryRepository.delete(category);
    }
}
```

---

## 🔄 PARTEA 6: Actualizarea Modelelor (Exercise 3 & 4)

### Exercițiul 3: Adăugarea unui Câmp Nou la Tabel Existent

**Scenario:** Adăugăm câmpul `color_code` la tabelul `categories`

**Pași:**

1. **Actualizăm schema PostgreSQL:**
   ```sql
   ALTER TABLE categories
   ADD COLUMN color_code VARCHAR(7);

   -- Adăugăm constraint pentru validare
   ALTER TABLE categories
   ADD CONSTRAINT check_color_code
   CHECK (color_code ~ '^#[0-9A-Fa-f]{6}$');
   ```

2. **Actualizăm entitatea Category.java:**
   ```java
   @Entity
   @Table(name = "categories")
   public class Category extends BaseEntity {
       // ... câmpuri existente ...

       // CÂMP NOU
       @Size(max = 7)
       @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Invalid color code")
       @Column(name = "color_code", length = 7)
       private String colorCode;

       // ... rest of class ...
   }
   ```

3. **Nu este nevoie de alte modificări!**
   - Spring Boot va detecta automat noul câmp
   - Repository-ul va funcționa automat
   - Service-ul nu necesită modificări (doar dacă vrem să adăugăm logică)

4. **Verificare:**
   ```bash
   # Repornire aplicație
   mvn spring-boot:run

   # În log-uri, Hibernate va valida schema
   # Schema-validation: successful
   ```

### Exercițiul 4: Adăugarea unui Tabel Nou

**Scenario:** Adăugăm tabelul `tags` cu relație Many-to-Many către `lessons`

**Pași:**

1. **Creăm schema în PostgreSQL:**
   ```sql
   -- Tabelul tags
   CREATE TABLE tags (
       id SERIAL PRIMARY KEY,
       name VARCHAR(50) NOT NULL UNIQUE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   -- Tabelul de joncțiune pentru Many-to-Many
   CREATE TABLE lesson_tags (
       lesson_id INTEGER NOT NULL,
       tag_id INTEGER NOT NULL,
       PRIMARY KEY (lesson_id, tag_id),
       FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
       FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
   );
   ```

2. **Creăm entitatea Tag.java:**
   ```java
   @Entity
   @Table(name = "tags")
   @Getter
   @Setter
   public class Tag {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(nullable = false, unique = true, length = 50)
       private String name;

       @Column(name = "created_at")
       private LocalDateTime createdAt;

       @ManyToMany(mappedBy = "tags")
       private Set<Lesson> lessons = new HashSet<>();
   }
   ```

3. **Actualizăm entitatea Lesson.java:**
   ```java
   @Entity
   public class Lesson extends BaseEntity {
       // ... câmpuri existente ...

       // RELAȚIE NOUĂ Many-to-Many
       @ManyToMany
       @JoinTable(
           name = "lesson_tags",
           joinColumns = @JoinColumn(name = "lesson_id"),
           inverseJoinColumns = @JoinColumn(name = "tag_id")
       )
       private Set<Tag> tags = new HashSet<>();
   }
   ```

4. **Creăm TagRepository:**
   ```java
   @Repository
   public interface TagRepository extends JpaRepository<Tag, Long> {
       Optional<Tag> findByName(String name);
       List<Tag> findByLessons_Id(Long lessonId);
   }
   ```

5. **Creăm TagService:**
   ```java
   @Service
   @RequiredArgsConstructor
   public class TagService {
       private final TagRepository tagRepository;

       public List<Tag> getAllTags() {
           return tagRepository.findAll();
       }

       public Tag createTag(String name) {
           Tag tag = new Tag();
           tag.setName(name);
           return tagRepository.save(tag);
       }
   }
   ```

---

## ✅ VERIFICAREA FINALĂ

### Testarea Relațiilor

**Test 1: Crearea unei categorii cu lecții:**
```java
Category category = Category.builder()
    .name("Beginner")
    .description("For beginners")
    .build();
categoryRepository.save(category);

Lesson lesson1 = Lesson.builder()
    .category(category)
    .title("Alphabet")
    .difficulty(Difficulty.BEGINNER)
    .build();
lessonRepository.save(lesson1);

// Verificare foreign key
assert lesson1.getCategory().getId().equals(category.getId());
```

**Test 2: Query prin relație:**
```java
// Găsește toate lecțiile pentru o categorie
List<Lesson> lessons = lessonRepository.findByCategoryIdOrderByOrderIndexAsc(categoryId);

// Sau prin numele categoriei
List<Lesson> lessons = lessonRepository.findByCategory_NameIgnoreCaseOrderByOrderIndexAsc("Beginner");
```

**Test 3: Eager loading:**
```java
// Include lessons când aducem category
Category category = categoryRepository.findWithLessonsById(id).orElseThrow();
System.out.println("Lessons: " + category.getLessons().size());
```

---

## 📊 COMPARAȚIE CU .NET ENTITY FRAMEWORK

| Pas                          | .NET Entity Framework                      | Spring Boot JPA                          |
|------------------------------|-------------------------------------------|------------------------------------------|
| 1. Design schema             | SQL Server Management Studio              | PostgreSQL / pgAdmin                     |
| 2. Generate models           | EF Designer (.edmx) - Visual              | Manual + JPA Annotations                 |
| 3. DbContext                 | Clasă ApplicationDbContext                | Repository Interfaces (separate)         |
| 4. Connection string         | app.config / web.config                   | application.properties                   |
| 5. Update model from DB      | Right-click → Update Model from Database  | Modificăm manual entity classes          |
| 6. Transform templates       | Transform All T4 Templates                | Nu este necesar (no code generation)     |
| 7. Migration changes         | Add-Migration, Update-Database            | Hibernate ddl-auto sau Flyway/Liquibase  |

---

## 🎯 CONCLUZIE

Am implementat cu succes abordarea **Database First** folosind Spring Boot JPA:

1. ✅ Am creat schema în PostgreSQL (categories + lessons cu FK)
2. ✅ Am generat entitățile Java cu adnotări JPA
3. ✅ Am creat repository interfaces (DbContext equivalent)
4. ✅ Am creat service classes (Accessor classes)
5. ✅ Am testat relațiile și query-urile
6. ✅ Am demonstrat cum să actualizăm modelele

**Diferența față de .NET EF:**
În loc de un designer vizual, folosim **adnotări JPA declarative** care oferă:
- Mai mult control asupra mappingului
- Mai bună integrare cu IDE (IntelliJ IDEA)
- Cod mai ușor de versionat (nu XML/EDMX)
- Performanță mai bună (no runtime code generation)

---

**Framework:** Spring Boot 3.2.0 + JPA/Hibernate 6.x
**Database:** PostgreSQL 15
