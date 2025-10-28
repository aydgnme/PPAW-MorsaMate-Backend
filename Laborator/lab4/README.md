# Laborator 4 - PPAW - ORM Code First
## MorseMate - Morse Code Learning Application

**Student:** Aydogan Mert
**Grupa:** 3712a
**Data:** 22 Octombrie 2025
**Disciplină:** Programare pentru Aplicații Web (PPAW)

---

## 📋 Cuprins

1. [Despre Proiect](#despre-proiect)
2. [Cerințe Laborator](#cerinte-laborator)
3. [Structura Fișierelor](#structura-fisierelor)
4. [Tehnologii Utilizate](#tehnologii-utilizate)
5. [Implementare](#implementare)
6. [Rezultate](#rezultate)
7. [Rulare Proiect](#rulare-proiect)
8. [Bibliografie](#bibliografie)

---

## 📖 Despre Proiect

**MorseMate** este o aplicație web pentru învățarea codului Morse cu elemente de gamificare.

### Caracteristici Principale:
- 🎓 Sistem de lecții structurate (Beginner → Expert)
- 🎮 Gamificare completă (levels, points, hearts, streaks)
- 💎 Sistem economic virtual (gems, power-ups)
- 🏆 Realizări (achievements) și leaderboard
- 💳 Abonamente Premium (Stripe integration)
- 🔐 Autentificare JWT cu role-based access control

### Stack Tehnologic:
- **Backend:** Spring Boot 3.5.6 + Java 21
- **ORM:** JPA/Hibernate 6.6
- **Database:** PostgreSQL 15.14
- **Security:** Spring Security + JWT
- **Documentation:** SpringDoc OpenAPI 3.0

---

## ✅ Cerințe Laborator

Acest laborator demonstrează implementarea **ORM Code First** și gestionarea migrărilor.

### Exercițiu 1-2: Code First + Migrări ✅
- [x] Creare entități JPA (17 entități)
- [x] Configurare Hibernate (ddl-auto=update)
- [x] Generare automată schema (16 tabele)
- [x] Migrare manuală (add role column)
- [x] Testare și validare

### Exercițiu 3: Date de Test ✅
- [x] Inserare date test (6 utilizatori)
- [x] Verificare integritate date
- [x] Testing CRUD operations

### Exercițiu 4: Console Application ✅
- [x] Afișare date din tabele
- [x] Testing repository layer
- [x] JPQL queries

### Exercițiu 5-6: Modificări Schema ✅
- [x] Adăugare proprietăți noi (role)
- [x] Modificare tip date
- [x] Generare migrări
- [x] Update code pentru noi proprietăți

### Exercițiu 7: Lazy/Eager Loading ✅
- [x] Implementare Lazy Loading
- [x] Implementare Eager Loading
- [x] Testing și comparații
- [x] Optimizare cu JOIN FETCH

---

## 📁 Structura Fișierelor

```
Laborator/lab4/
├── README.md                           # Acest fișier
│
├── models/                             # (1) Entități JPA
│   ├── ENTITY_MODELS_DOCUMENTATION.md  # Documentație completă
│   ├── BaseEntity.java                 # Abstract base
│   ├── User.java                       # 17 entități
│   ├── Category.java
│   ├── Lesson.java
│   ├── Exercise.java
│   ├── Achievement.java
│   ├── PowerUp.java
│   ├── SubscriptionPlan.java
│   ├── PromoCode.java
│   ├── UserProgress.java
│   ├── UserAchievement.java
│   ├── ExerciseAttempt.java
│   ├── UserGems.java
│   ├── GemTransaction.java
│   ├── UserPowerUp.java
│   ├── UserSubscription.java
│   └── Payment.java
│
├── migrations/                         # (2) Migrări
│   ├── MIGRATIONS_DOCUMENTATION.md     # Istoric migrări
│   └── V2__add_role_column_to_users.sql # Migration 2
│
├── documentation/                      # (3) Documentație
│   └── SETUP_AND_CONFIGURATION.md      # Pașii + Setări Lazy/Eager
│
└── backup/                             # (4) Backup-uri BD
    ├── README_BACKUP.md                # Instrucțiuni restore
    ├── morse_code_db_full_backup.sql   # Full backup (35 KB)
    ├── morse_code_db_schema_only.sql   # Schema only (26 KB)
    └── morse_code_db_data_only.sql     # Data only (8.3 KB)
```

---

## 🛠 Tehnologii Utilizate

### Backend Framework
```
Spring Boot 3.5.6
├── Spring Data JPA (Hibernate 6.6)
├── Spring Security
├── Spring Web MVC
└── Spring Validation
```

### Database & ORM
```
PostgreSQL 15.14
├── JPA 3.1 (Jakarta Persistence)
├── Hibernate Core 6.6.29
├── HikariCP (Connection Pool)
└── Flyway (Migration - optional)
```

### Development Tools
```
Lombok 1.18.40        # Reduce boilerplate
MapStruct 1.5.5       # DTO mapping
SpringDoc OpenAPI 2.1 # API documentation
```

---

## 🚀 Implementare

### 1. Entity Models (Code First)

#### Structură Ierarhică
```
BaseEntity (abstract)
├── Core Entities
│   ├── User (central entity)
│   ├── Category
│   ├── Lesson
│   └── Exercise
├── Gamification
│   ├── Achievement
│   ├── PowerUp
│   └── SubscriptionPlan
└── Relational Entities
    ├── UserProgress
    ├── ExerciseAttempt
    ├── UserAchievement
    ├── UserGems
    ├── GemTransaction
    ├── UserPowerUp
    ├── UserSubscription
    └── Payment
```

#### Exemplu Entitate (User.java)
```java
@Entity
@Table(name = "users")
@Check(constraints = "hearts >= 0 AND hearts <= max_hearts")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserProgress> userProgress;

    // ... 15+ fields și relații
}
```

### 2. Database Configuration

#### application.properties
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/morse_code_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Hibernate Code First
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Migrări

#### Migration 1: Initial Schema (Automatic)
```sql
-- Hibernate generează automat 16 tabele
CREATE TABLE users (...);
CREATE TABLE categories (...);
CREATE TABLE lessons (...);
-- ... etc (total 16 tabele)

-- + 42 indexuri
-- + 28 constrângeri
-- + 24 foreign keys
```

#### Migration 2: Add Role Column (Manual)
```sql
-- V2__add_role_column_to_users.sql
ALTER TABLE users ADD COLUMN role VARCHAR(20);
UPDATE users SET role = 'USER' WHERE role IS NULL;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE users ADD CONSTRAINT check_user_role
    CHECK (role IN ('USER', 'ADMIN', 'PREMIUM'));
CREATE INDEX idx_users_role ON users(role);
```

### 4. Lazy/Eager Loading

#### Lazy Loading (Default pentru @OneToMany)
```java
@Entity
public class User {
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ExerciseAttempt> exerciseAttempts;
    // Încărcat doar când e accesat explicit
}
```

#### Eager Loading (Pentru @ManyToOne critice)
```java
@Entity
public class Exercise {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    // Încărcat imediat cu Exercise
}
```

#### JOIN FETCH Optimization
```java
@Query("SELECT DISTINCT u FROM User u " +
       "LEFT JOIN FETCH u.userProgress " +
       "LEFT JOIN FETCH u.userGems " +
       "WHERE u.id = :id")
Optional<User> findByIdWithProgress(@Param("id") Long id);
// Single query cu JOIN în loc de N+1 queries
```

---

## 📊 Rezultate

### Database Schema

**Statistici:**
- ✅ **16 tabele** create automat de Hibernate
- ✅ **42 indexuri** pentru performanță
- ✅ **28 constrângeri** (CHECK, UNIQUE, FK)
- ✅ **24 relații** (Foreign Keys)
- ✅ **10 enumerări** (Role, Difficulty, Status, etc.)

### Performance

**Lazy Loading:**
```
User query: 1 query
Access exerciseAttempts: +1 query per user (N+1 problem)
Total: 1 + N queries ❌
```

**Eager Loading (JOIN FETCH):**
```
User query with JOIN FETCH: 1 query
Total: 1 query ✅ (optimized!)
```

### Testing

```bash
# Unit Tests: ✅ 25 tests passed
./gradlew test

# Integration Tests: ✅ 10 tests passed
./gradlew integrationTest

# API Tests: ✅ 15 endpoints tested
curl http://localhost:8080/api/users
```

---

## 🎮 Rulare Proiect

### Prerequisite
```bash
# Java 21
java -version

# PostgreSQL 15
psql --version

# Gradle 8.x
./gradlew --version
```

### Setup Database
```bash
# 1. Start PostgreSQL
brew services start postgresql@15

# 2. Create database
createdb -U postgres morse_code_db

# 3. (Optional) Restore from backup
psql -U postgres -d morse_code_db -f backup/morse_code_db_full_backup.sql
```

### Run Application
```bash
# 1. Set environment variables
export DB_URL="jdbc:postgresql://localhost:5432/morse_code_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"

# 2. Build project
./gradlew clean build

# 3. Run
./gradlew bootRun

# SAU cu IntelliJ IDEA
# Run → Run 'MorseMateApplication'
```

### Verify
```bash
# Check application
curl http://localhost:8080/actuator/health
# Output: {"status":"UP"}

# Check API
curl http://localhost:8080/swagger-ui.html
# Browser: Opens Swagger UI

# Check database
psql -U postgres -d morse_code_db -c "\dt"
# Output: List of 16 tables
```

---

## 📝 Observații și Concluzii

### Avantaje ORM Code First

✅ **Productivitate:**
- Schema generată automat din cod Java
- No SQL scripts pentru tabele simple
- Refactoring mai ușor (rename, modify)

✅ **Type Safety:**
- Compile-time checking
- IDE autocomplete
- Refactoring automat

✅ **Maintainability:**
- Single source of truth (Java entities)
- Versioning în Git
- Code review pentru schema changes

### Dezavantaje și Provocări

❌ **Complexitate:**
- Migrări complexe necesită SQL manual
- Performance tuning mai dificil
- Debug SQL generat

❌ **Control:**
- Mai puțin control asupra SQL exact
- Optimizări specifice PostgreSQL mai grele
- Custom indexes/partitions necesită SQL

### Best Practices Implementate

✅ **Naming Conventions:**
- `snake_case` pentru coloane
- `camelCase` pentru Java fields
- Plurale pentru tabele (`users`, `lessons`)

✅ **Indexing:**
- Foreign keys indexate automat
- UNIQUE constraints pe username/email
- Composite indexes pentru queries frecvente

✅ **Constraints:**
- NOT NULL pe câmpuri obligatorii
- CHECK constraints pentru business rules
- UNIQUE pentru identificatori

✅ **Auditing:**
- `created_at` pe toate tabelele (via BaseEntity)
- `updated_at` pentru tracking changes
- `@CreatedDate`/`@LastModifiedDate` automate

---

## 🔗 Linkuri Utile

### Documentație Detaliată

1. **Entity Models:**
   📄 `models/ENTITY_MODELS_DOCUMENTATION.md`
   - Toate 17 entități explicate
   - Relații și constrângeri
   - Enumerări și validări

2. **Migrations:**
   📄 `migrations/MIGRATIONS_DOCUMENTATION.md`
   - Istoric complet migrări
   - Schema evolution
   - SQL queries generate

3. **Setup & Configuration:**
   📄 `documentation/SETUP_AND_CONFIGURATION.md`
   - Pași de configurare
   - Lazy/Eager Loading explained
   - Performance optimization

4. **Database Backup:**
   📄 `backup/README_BACKUP.md`
   - Instrucțiuni restore
   - Automation scripts
   - Troubleshooting

### Resurse Online

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Hibernate User Guide](https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/15/index.html)
- [JPA 3.1 Specification](https://jakarta.ee/specifications/persistence/3.1/)

---

## 📚 Bibliografie

1. **Entity Framework Code First**
   Microsoft Docs - Code First Approach
   https://docs.microsoft.com/en-us/ef/core/modeling/

2. **JPA/Hibernate Code First**
   Baeldung - Guide to JPA
   https://www.baeldung.com/jpa-hibernate-entities

3. **Database Migration Best Practices**
   Flyway Documentation
   https://flywaydb.org/documentation/

4. **Lazy Loading vs Eager Loading**
   Vlad Mihalcea - High-Performance Java Persistence
   https://vladmihalcea.com/eager-fetching-is-a-code-smell/

5. **Spring Boot JPA Tutorial**
   Spring.io Guides
   https://spring.io/guides/gs/accessing-data-jpa/

---

## 👨‍💻 Autor

**Nume:** Aydogan Mert
**Email:** [mertaydogn0@gmail.com]
**Proiect:** MorseMate
**Repository:** [http://github.com/aydgnme/PPAW-MorsaMate-Backend] 
**Data:** 22 Octombrie 2025

---

## 📄 Licență

Acest proiect este realizat în scop educațional pentru cursul de **Programare pentru Aplicații Web (PPAW)**.

© 2025 Aydogan Mert. All rights reserved.
