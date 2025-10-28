# LAB 3 - SUBMISSION FOLDER

**Student:** [Mert Aydogan]
**Course:** PPAW - Programare Pe Aplicatii Web
**Lab:** Laborator 3 – ORM – Database First
**Project:** MorseMate - Morse Code Learning Platform

---

## 📦 Conținutul Dosarului de Predare

Acest folder conține toate fișierele necesare pentru predarea Lab 3:

### ✅ 1. Două Modele Generate (cu relație FK între ele)

**Fișiere:**
- `1_Model_Category.java` - Entitatea parent (Category)
- `1_Model_Lesson.java` - Entitatea child (Lesson) cu FK către Category

**Relația demonstrată:**
```
Category (1) ──────< (Many) Lesson
                ↑
                │
         category_id (FK)
```

**Detalii tehnice:**
- **Category**:
  - PK: `id` (SERIAL)
  - Relație: `@OneToMany` către Lesson
  - Câmpuri: name, description, displayOrder, iconUrl, isActive

- **Lesson**:
  - PK: `id` (SERIAL)
  - FK: `category_id` → Category.id
  - Relație: `@ManyToOne` către Category
  - Câmpuri: title, description, difficulty, content, orderIndex, pointsReward

---

### ✅ 2. Fișier DbContext (Repository Interfaces)

**Fișier:**
- `1_DbContext_Repositories.java` - Conține ambele repository interfaces

**Conținut:**
- `CategoryRepository` - Echivalent cu DbSet<Category>
- `LessonRepository` - Echivalent cu DbSet<Lesson>

**Funcționalități demonstrate:**
- Derived query methods (Spring generează SQL automat)
- Custom JPQL queries cu @Query
- @EntityGraph pentru eager loading
- Foreign key queries
- Aggregation functions (COUNT, AVG, SUM)

**Comparație cu .NET:**
```java
// .NET EF DbContext:
public DbSet<Category> Categories { get; set; }
public DbSet<Lesson> Lessons { get; set; }

// Spring JPA Repositories:
public interface CategoryRepository extends JpaRepository<Category, Long> { }
public interface LessonRepository extends JpaRepository<Lesson, Long> { }
```

---

### ✅ 3. Clase Accessor (Service Layer)

**Fișier:**
- `2_Accessor_CategoryService.java` - Business logic pentru Category

**Funcționalități implementate:**
- **CRUD Operations:**
  - `getAllCategories()` - Retrieve all
  - `getCategoryById(id)` - Retrieve by PK
  - `createCategory(request)` - Create new
  - `updateCategory(id, request)` - Update existing
  - `deleteCategory(id)` - Delete with FK validation

- **Business Logic:**
  - Validare duplicat (existsByNameIgnoreCase)
  - Verificare FK constraints înainte de delete
  - Logging cu SLF4J
  - Exception handling (ResourceNotFoundException, DuplicateResourceException)

- **Advanced Queries:**
  - `getCategoryWithLessons(id)` - Eager loading
  - `getLessonCount(categoryId)` - Agregare

**Echivalent .NET:**
```csharp
// .NET Accessor Class
public class CategoryAccessor {
    private ApplicationDbContext _context;
    // Similar methods with LINQ queries
}
```

---

### ✅ 4. Document cu Pașii Efectuați

**Fișier:**
- `3_Pasii_Generare_Entitati_JPA.md` - Documentație detaliată

**Conținut:**
1. **Partea 1:** Pregătirea bazei de date (SQL scripts)
2. **Partea 2:** Configurarea proiectului Spring Boot
3. **Partea 3:** Generarea entităților (Database First)
4. **Partea 4:** Crearea repository interfaces
5. **Partea 5:** Crearea service layer
6. **Partea 6:** Actualizarea modelelor (Exercise 3 & 4)

**Include:**
- SQL scripts pentru crearea tabelelor
- Configurarea application.properties
- Mappingul JPA annotations
- Explicații pentru fiecare adnotare folosită
- Comparații .NET EF vs Spring JPA
- Exemple de testare

---

## 🔍 Structura Fișierelor

```
lab3_1/
├── README.md                              # Acest fișier
├── 1_Model_Category.java                  # ✅ Model 1 (Parent)
├── 1_Model_Lesson.java                    # ✅ Model 2 (Child cu FK)
├── 1_DbContext_Repositories.java          # ✅ Repository interfaces
├── 2_Accessor_CategoryService.java        # ✅ Service/Accessor class
└── 3_Pasii_Generare_Entitati_JPA.md      # ✅ Documentație pași
```

---

## 🔗 Relația Foreign Key Demonstrată

### Schema Bazei de Date:

```sql
-- PARENT TABLE
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INTEGER,
    icon_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CHILD TABLE (cu FK)
CREATE TABLE lessons (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL,  -- FOREIGN KEY
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20),
    content TEXT,
    order_index INTEGER,
    points_reward INTEGER DEFAULT 10,
    estimated_duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- CONSTRAINT FK
    CONSTRAINT lessons_category_id_fkey
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE CASCADE
);
```

### Mapping JPA:

**În Category.java:**
```java
@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
private List<Lesson> lessons = new ArrayList<>();
```

**În Lesson.java:**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
    name = "category_id",
    nullable = false,
    foreignKey = @ForeignKey(name = "lessons_category_id_fkey")
)
private Category category;
```

---

## 🎯 Cerințe Lab Îndeplinite

### ✅ 1. Două modele generate cu relație FK
- Category.java (parent)
- Lesson.java (child cu @ManyToOne)
- Relație bidirectională demonstrată

### ✅ 2. Fișier DbContext
- CategoryRepository (extends JpaRepository)
- LessonRepository (extends JpaRepository)
- Query methods implementate (15+ methods)

### ✅ 3. Cod Accessor
- CategoryService cu business logic
- CRUD operations complete
- Validări și exception handling
- FK constraint validation

### ✅ 4. Document pași efectuați
- 6 părți detaliate
- SQL scripts incluse
- Explicații pentru fiecare adnotare
- Comparații .NET vs Spring
- Exemple de testare

---

## 🔧 Tehnologii Folosite

| Tehnologie | Versiune | Scop |
|------------|----------|------|
| Spring Boot | 3.2.0 | Framework principal |
| Spring Data JPA | 3.2.0 | ORM abstraction |
| Hibernate | 6.x | JPA implementation |
| PostgreSQL | 15 | Database |
| Lombok | Latest | Reduce boilerplate |
| Jakarta Validation | 3.0 | Bean validation |

---

## 📖 Cum să Folosești Aceste Fișiere

### 1. Citește documentația:
```bash
cat 3_Pasii_Generare_Entitati_JPA.md
```

### 2. Inspectează modelele:
- `1_Model_Category.java` - Vezi cum se mapează tabelul categories
- `1_Model_Lesson.java` - Vezi cum se mapează FK

### 3. Studiază repository interfaces:
- `1_DbContext_Repositories.java` - Echivalent cu DbContext din .NET

### 4. Analizează service layer:
- `2_Accessor_CategoryService.java` - Business logic pattern

---

## 🎓 Concepte Demonstrate

1. **Database First Approach**
   - Schema creată mai întâi în PostgreSQL
   - Entities mapate la tabele existente

2. **JPA Annotations**
   - @Entity, @Table, @Id, @GeneratedValue
   - @Column cu constraints
   - @ManyToOne, @OneToMany pentru relații
   - @JoinColumn pentru FK
   - @EntityGraph pentru eager loading

3. **Repository Pattern**
   - Interface-uri care extind JpaRepository
   - Derived query methods
   - Custom JPQL queries

4. **Service Layer Pattern**
   - Separarea business logic de data access
   - Transaction management cu @Transactional
   - Exception handling

5. **Foreign Key Relationships**
   - Mapping bidirectional
   - Cascade operations
   - Lazy loading pentru performanță

---

## ⚠️ Note Importante

1. **Lombok Dependency:**
   - Fișierele folosesc Lombok pentru @Getter, @Setter, @Builder
   - Asigurați-vă că aveți Lombok plugin în IDE

2. **PostgreSQL:**
   - Proiectul este configurat pentru PostgreSQL
   - Pentru alte DB-uri, modificați dialect-ul în application.properties

3. **Package Structure:**
   - Fișierele originale sunt în `me.aydgn.MorseMate.*`
   - Păstrați această structură când integrați în proiect

4. **Dependencies:**
   - Verificați pom.xml pentru toate dependințele necesare
   - Spring Boot Starter Data JPA
   - PostgreSQL driver
   - Lombok
   - Validation

---

## 📝 Checklistă Predare

- [x] Două modele cu FK relationship (Category + Lesson)
- [x] Fișier cu repository interfaces (DbContext equivalent)
- [x] Clasă Accessor/Service cu business logic
- [x] Document cu pașii efectuați (Markdown detaliat)
- [x] Relație Many-to-One demonstrată
- [x] Foreign Key constraint definit
- [x] Query methods implementate
- [x] JPQL custom queries
- [x] Eager loading cu @EntityGraph
- [x] Exception handling
- [x] Transaction management
- [x] Logging
- [x] Comparații .NET vs Spring

---

## 🚀 Next Steps

După predare, puteți:
1. Extinde modelele cu mai multe entități
2. Implementa Lesson Service
3. Adăuga unit tests
4. Implementa REST endpoints
5. Integra cu frontend

---

**Preparat pentru:** Lab 3 - ORM Database First
**Framework:** Spring Boot + JPA/Hibernate
**Database:** PostgreSQL
