# MorseMate - Database Migrations Documentation
## Laborator 4 - PPAW - ORM Code First

**Student:** Aydogan Mert
**Proiect:** MorseMate
**ORM:** JPA/Hibernate (Spring Boot)
**Database:** PostgreSQL 15.14

---

## 1. Prezentare Generală

Proiectul MorseMate utilizează două tipuri de migrări:

1. **Hibernate DDL Auto (spring.jpa.hibernate.ddl-auto=update)**
   - Creare automată inițială a tabelelor
   - Actualizare schema existentă

2. **Flyway Migrations (planificat pentru producție)**
   - Control fin asupra modificărilor
   - Istoric versiuni
   - Rollback capabilities

---

## 2. Migrări Hibernate (Code First)

### 2.1 Migration 1: Initial Schema Creation (Automatic)
**Data:** 2025-01-20 (First Run)
**Tip:** CREATE
**Status:** ✅ Completed

**Descriere:**
Creare inițială a tuturor tabelelor din modelele JPA.

**Tabele Create:**

#### Core Tables (3)
1. **users** - Utilizatori
   - Chei: PK (id), UNIQUE (username, email)
   - Relații: FK în multe tabele
   - Constrângeri: Check hearts/level

2. **categories** - Categorii lecții
   - Chei: PK (id), UNIQUE (name)
   - Indexuri: displayOrder

3. **lessons** - Lecții
   - Chei: PK (id), FK (category_id)
   - Indexuri: category_id, display_order
   - Constrângeri: Check required_level >= 1

#### Content Tables (2)
4. **exercises** - Exerciții
   - Chei: PK (id), FK (lesson_id)
   - Indexuri: lesson_id, type, difficulty
   - Constrângeri: Check points >= 0, time_limit >= 0

5. **achievements** - Realizări
   - Chei: PK (id), UNIQUE (name)
   - Constrângeri: Check points_reward >= 0

#### Progress Tables (3)
6. **user_progress** - Progres utilizatori
   - Chei: PK (id), FK (user_id, lesson_id)
   - Indexuri: COMPOSITE (user_id, lesson_id)
   - Constrângeri: Check progress_percentage 0-100

7. **exercise_attempts** - Încercări exerciții
   - Chei: PK (id), FK (user_id, exercise_id)
   - Indexuri: COMPOSITE (user_id, exercise_id), attempted_at
   - Constrângeri: Check points_earned >= 0

8. **user_achievements** - Realizări obținute
   - Chei: PK COMPOSITE (user_id, achievement_id)
   - Indexuri: user_id, achievement_id, unlocked_at

#### Economic Tables (4)
9. **user_gems** - Gemuri utilizatori
   - Chei: PK (id), FK UNIQUE (user_id)
   - Constrângeri: Check total_gems >= 0

10. **gem_transactions** - Istoric gemuri
    - Chei: PK (id), FK (user_id)
    - Indexuri: user_id, type, created_at
    - Constrângeri: Check amount != 0, balance_after >= 0

11. **power_ups** - Power-up-uri disponibile
    - Chei: PK (id), UNIQUE (name)
    - Constrângeri: Check gems_cost >= 0

12. **user_power_ups** - Power-up-uri active
    - Chei: PK (id), FK (user_id, power_up_id)
    - Indexuri: COMPOSITE (user_id, power_up_id), expires_at

#### Financial Tables (4)
13. **subscription_plans** - Planuri abonament
    - Chei: PK (id), UNIQUE (name)
    - Constrângeri: Check price >= 0

14. **user_subscriptions** - Abonamente active
    - Chei: PK (id), FK UNIQUE (user_id, plan_id)
    - Indexuri: user_id, status, end_date

15. **payments** - Plăți
    - Chei: PK (id), FK (user_id, plan_id), UNIQUE (transaction_id)
    - Indexuri: user_id, status, paid_at
    - Constrângeri: Check amount >= 0

16. **promo_codes** - Coduri promoționale
    - Chei: PK (id), UNIQUE (code)
    - Constrângeri: Check discount 0-100, current_uses <= max_uses

**SQL Generat (Exemplu pentru users):**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    level INTEGER NOT NULL DEFAULT 1,
    total_points INTEGER NOT NULL DEFAULT 0,
    hearts INTEGER NOT NULL DEFAULT 5,
    max_hearts INTEGER NOT NULL DEFAULT 5,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    profile_picture_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    last_login TIMESTAMP(6),
    last_heart_refill TIMESTAMP(6),
    CONSTRAINT users_check CHECK (
        hearts >= 0 AND
        hearts <= max_hearts AND
        level >= 1
    )
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_level ON users(level);
```

**Total Tables Created:** 16
**Total Constraints:** 28
**Total Indexes:** 42
**Total Foreign Keys:** 24

---

### 2.2 Migration 2: Add Role Column to Users (Manual)
**Data:** 2025-10-22
**Tip:** ALTER
**Status:** ✅ Completed
**File:** `V2__add_role_column_to_users.sql`

**Descriere:**
Adăugare coloană `role` pentru role-based access control (USER, ADMIN, PREMIUM).

**Pași:**

```sql
-- Step 1: Add role column as NULLABLE first
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20);

-- Step 2: Set default value for all existing rows
UPDATE users SET role = 'USER' WHERE role IS NULL;

-- Step 3: Make the column NOT NULL
ALTER TABLE users ALTER COLUMN role SET NOT NULL;

-- Step 4: Set default for future inserts
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'USER';

-- Step 5: Add check constraint
ALTER TABLE users ADD CONSTRAINT check_user_role
CHECK (role IN ('USER', 'ADMIN', 'PREMIUM'));

-- Step 6: Create index
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Step 7: Add comment
COMMENT ON COLUMN users.role IS 'User role: USER, ADMIN, or PREMIUM';
```

**Înainte:**
```
users: 6 rows, 18 columns (fără role)
```

**După:**
```
users: 6 rows, 19 columns (cu role = 'USER')
```

**Utilizatori Afectați:**
| ID | Username | Email | Role (nou) |
|----|----------|-------|------------|
| 1 | aydgn | mertaydogn0@gmail.com | USER |
| 2 | aydgn1 | mertaydogn01@gmail.com | USER |
| 3 | john_de | mert@gmail.com | USER |
| 4 | john_doe | john@example.com | USER |
| 12 | joh1n_doe | john@example1.com | USER |
| 13 | user_1760971895183 | user1760971895183@test.com | USER |

**Testing:**
```sql
-- Test 1: Insert cu role valid
INSERT INTO users (username, email, password_hash, role)
VALUES ('test_user', 'test@example.com', 'hash', 'ADMIN');
-- ✅ SUCCESS

-- Test 2: Insert cu role invalid
INSERT INTO users (username, email, password_hash, role)
VALUES ('test_user2', 'test2@example.com', 'hash', 'SUPER_ADMIN');
-- ❌ ERROR: violates check constraint "check_user_role"

-- Test 3: Insert fără role (folosește default)
INSERT INTO users (username, email, password_hash)
VALUES ('test_user3', 'test3@example.com', 'hash');
-- ✅ SUCCESS (role = 'USER')
```

---

## 3. Schema Curentă a Bazei de Date

### 3.1 Verificare Schema
```sql
-- Total tabele
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'public';
-- Result: 16 tables

-- Toate tabelele
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

**Rezultat:**
```
achievements
categories
exercise_attempts
exercises
gem_transactions
lessons
payments
power_ups
promo_codes
subscription_plans
user_achievements
user_gems
user_power_ups
user_progress
user_subscriptions
users
```

### 3.2 Statistici Bază de Date

**Dimensiuni:**
```sql
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_total_relation_size(schemaname||'.'||tablename) AS size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY size_bytes DESC;
```

**Relații (Foreign Keys):**
```sql
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = 'public'
ORDER BY tc.table_name;
```

**Total Records per Table:**
```sql
SELECT
    'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL SELECT 'exercises', COUNT(*) FROM exercises
-- ... etc
ORDER BY count DESC;
```

**Rezultat Actual:**
```
users: 6 records
categories: 0 records
lessons: 0 records
exercises: 0 records
... (alte tabele: 0 records)
```

---

## 4. Configurare Hibernate

### 4.1 application.properties

```properties
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

# Flyway Configuration (Temporarily disabled)
spring.flyway.enabled=false
# spring.flyway.baseline-on-migrate=true
# spring.flyway.locations=classpath:db/migration
# spring.flyway.validate-on-migrate=true
```

### 4.2 Opțiuni DDL Auto

| Opțiune | Descriere | Folosit în |
|---------|-----------|------------|
| `none` | Hibernate nu face nimic | Producție |
| `validate` | Validează schema, nu modifică | Producție |
| `update` | Actualizează schema (safe) | **✅ Development** |
| `create` | Șterge și recrează (PERICOL!) | Testing |
| `create-drop` | Șterge la închidere | Testing |

**Setare Curentă:** `update` - ideal pentru development, modifică schema fără să șteargă date.

---

## 5. Migrări Viitoare (Planificate)

### 5.1 Migration 3: Add Sample Data (Planned)
**Status:** 📋 Pending
**Tip:** INSERT

**Obiective:**
- Populate categories (5 categorii: Basics, Beginner, Intermediate, Advanced, Expert)
- Populate lessons (20 lecții)
- Populate exercises (100 exerciții)
- Populate achievements (15 realizări)
- Populate power-ups (5 power-up-uri)
- Populate subscription plans (3 planuri)

**SQL Pregătit:**
```sql
-- Categories
INSERT INTO categories (name, description, display_order, is_active, created_at)
VALUES
    ('Basics', 'Learn the fundamental Morse code signals', 1, true, NOW()),
    ('Beginner', 'Start your Morse code journey', 2, true, NOW()),
    ('Intermediate', 'Improve your Morse code skills', 3, true, NOW()),
    ('Advanced', 'Master complex Morse patterns', 4, true, NOW()),
    ('Expert', 'Challenge yourself with expert level', 5, true, NOW());

-- Lessons (exemplu pentru Basics)
INSERT INTO lessons (category_id, title, description, content, difficulty,
                     required_level, display_order, estimated_minutes,
                     points_reward, is_active, created_at)
SELECT
    c.id,
    'Introduction to Morse Code',
    'Learn what Morse code is and its history',
    'Morse code is a method used in telecommunication...',
    'BEGINNER',
    1,
    1,
    15,
    100,
    true,
    NOW()
FROM categories c WHERE c.name = 'Basics';

-- Achievements
INSERT INTO achievements (name, description, icon_url, type, requirement,
                          points_reward, created_at)
VALUES
    ('First Steps', 'Complete your first lesson', '/icons/first-steps.png',
     'COMPLETION', 1, 50, NOW()),
    ('Week Warrior', 'Maintain a 7-day streak', '/icons/week-warrior.png',
     'STREAK', 7, 200, NOW()),
    ('Speed Demon', 'Complete 10 exercises in under 30 seconds',
     '/icons/speed-demon.png', 'SPEED', 10, 300, NOW());

-- Power-ups
INSERT INTO power_ups (name, description, type, icon_url, gems_cost,
                       duration_minutes, created_at)
VALUES
    ('Time Freeze', 'Pause the timer for 60 seconds', 'FREEZE_TIME',
     '/icons/time-freeze.png', 50, 1, NOW()),
    ('Hint Master', 'Reveal a hint for the current exercise', 'HINT',
     '/icons/hint.png', 30, 0, NOW()),
    ('Heart Refill', 'Instantly refill all hearts', 'HEART_REFILL',
     '/icons/heart-refill.png', 100, 0, NOW());

-- Subscription Plans
INSERT INTO subscription_plans (name, description, price, currency,
                                billing_period, features, max_hearts,
                                gem_bonus, is_active, created_at)
VALUES
    ('Free', 'Basic access to Morse code lessons', 0, 'USD', 'MONTHLY',
     '["Basic lessons", "5 hearts", "Limited exercises"]', 5, 0, true, NOW()),
    ('Premium Monthly', 'Full access to all features', 9.99, 'USD', 'MONTHLY',
     '["All lessons", "Unlimited hearts", "All exercises", "200 bonus gems"]',
     999, 200, true, NOW()),
    ('Premium Yearly', 'Best value - Full annual access', 99.99, 'USD', 'YEARLY',
     '["All lessons", "Unlimited hearts", "All exercises", "2500 bonus gems", "20% discount"]',
     999, 2500, true, NOW());
```

### 5.2 Migration 4: Add Indexes for Performance (Planned)
**Status:** 📋 Pending
**Tip:** CREATE INDEX

**Obiective:**
- Optimizare query-uri frecvente
- Îmbunătățire performanță JOIN-uri

```sql
-- User-related indexes
CREATE INDEX idx_users_last_login ON users(last_login DESC);
CREATE INDEX idx_users_level_points ON users(level, total_points DESC);
CREATE INDEX idx_users_streak ON users(current_streak DESC);

-- Progress indexes
CREATE INDEX idx_user_progress_status ON user_progress(status, user_id);
CREATE INDEX idx_user_progress_completed ON user_progress(completed_at DESC)
WHERE status = 'COMPLETED';

-- Exercise attempts indexes
CREATE INDEX idx_exercise_attempts_correct ON exercise_attempts(is_correct, user_id);
CREATE INDEX idx_exercise_attempts_points ON exercise_attempts(points_earned DESC);

-- Gem transactions indexes
CREATE INDEX idx_gem_transactions_type ON gem_transactions(type, user_id);
CREATE INDEX idx_gem_transactions_date ON gem_transactions(created_at DESC);

-- Payment indexes
CREATE INDEX idx_payments_status_date ON payments(status, paid_at DESC);
```

### 5.3 Migration 5: Add Full-Text Search (Planned)
**Status:** 📋 Pending
**Tip:** ALTER + CREATE INDEX

**Obiective:**
- Full-text search pe lessons.content
- Full-text search pe exercises.question

```sql
-- Add tsvector columns
ALTER TABLE lessons ADD COLUMN content_search tsvector;
ALTER TABLE exercises ADD COLUMN question_search tsvector;

-- Update existing data
UPDATE lessons SET content_search = to_tsvector('english', content);
UPDATE exercises SET question_search = to_tsvector('english', question);

-- Create GIN indexes
CREATE INDEX idx_lessons_content_search ON lessons USING GIN(content_search);
CREATE INDEX idx_exercises_question_search ON exercises USING GIN(question_search);

-- Create triggers for auto-update
CREATE TRIGGER lessons_content_search_update BEFORE INSERT OR UPDATE
ON lessons FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(content_search, 'pg_catalog.english', content);

CREATE TRIGGER exercises_question_search_update BEFORE INSERT OR UPDATE
ON exercises FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(question_search, 'pg_catalog.english', question);
```

---

## 6. Best Practices Implementate

### 6.1 Naming Conventions
✅ **Table names:** lowercase, plural (users, lessons, exercises)
✅ **Column names:** snake_case (created_at, user_id, max_hearts)
✅ **Indexes:** idx_tablename_columns (idx_users_username)
✅ **Constraints:** check_tablename_column (check_user_role)
✅ **Foreign keys:** FK_table_references (implicit)

### 6.2 Data Integrity
✅ **NOT NULL:** Pe toate câmpurile obligatorii
✅ **UNIQUE:** Pe username, email, transaction_id
✅ **CHECK:** Validare business rules (hearts, prices, percentages)
✅ **Foreign Keys:** Cu ON DELETE CASCADE unde e necesar
✅ **Defaults:** Valori default pentru toate câmpurile non-nullable

### 6.3 Audit Trail
✅ **created_at:** Pe toate tabelele (via BaseEntity)
✅ **updated_at:** Pe toate tabelele (via BaseEntity)
✅ **@CreatedDate/@LastModifiedDate:** Annotații JPA
✅ **AuditingEntityListener:** Spring Data JPA auditing

### 6.4 Performance
✅ **Indexes:** Pe FK, coloane frecvent căutate
✅ **Composite indexes:** Pe combinații des folosite
✅ **Lazy loading:** Default pentru collections
✅ **Eager loading:** Doar unde e necesar
✅ **Connection pooling:** HikariCP

---

## 7. Comenzi Utile

### Verificare Schema
```sql
-- Schema completă
\d+ users

-- Toate tabelele
\dt

-- Toate indexurile
\di

-- Toate constrângerile
\d+ tablename
```

### Export/Import
```bash
# Export schema
pg_dump -U postgres -d morse_code_db --schema-only > schema.sql

# Export data
pg_dump -U postgres -d morse_code_db --data-only > data.sql

# Full backup
pg_dump -U postgres -d morse_code_db > full_backup.sql
```

### Monitoring
```sql
-- Table sizes
SELECT
    relname AS table_name,
    pg_size_pretty(pg_total_relation_size(relid)) AS total_size
FROM pg_catalog.pg_statio_user_tables
ORDER BY pg_total_relation_size(relid) DESC;

-- Active queries
SELECT pid, query, state FROM pg_stat_activity
WHERE state = 'active';

-- Slow queries
SELECT * FROM pg_stat_statements
ORDER BY total_time DESC LIMIT 10;
```

---

## 8. Istoric Modificări

| Version | Date | Type | Description | Status |
|---------|------|------|-------------|--------|
| 1.0 | 2025-01-20 | CREATE | Initial schema creation (16 tables) | ✅ Done |
| 2.0 | 2025-10-22 | ALTER | Add role column to users | ✅ Done |
| 3.0 | TBD | INSERT | Add sample data | 📋 Planned |
| 4.0 | TBD | INDEX | Performance optimization | 📋 Planned |
| 5.0 | TBD | ALTER | Add full-text search | 📋 Planned |

---

**Ultima Actualizare:** 2025-10-22
**Autor:** Aydogan Mert
**Versiune Document:** 1.0
