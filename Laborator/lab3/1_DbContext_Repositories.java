/*
 * ============================================================================
 * LAB 3 - SUBMISSION FILE 1 (Part 3)
 * ============================================================================
 *
 * TITLU: DbContext Equivalent - Repository Interfaces
 *
 * În .NET Entity Framework, clasa DbContext gestionează conexiunile la baza de date
 * și urmărirea entităților. În Spring Boot JPA, această funcționalitate este împărțită între:
 *
 * 1. Repository Interfaces (extend JpaRepository) - Similar cu DbSet<T>
 * 2. EntityManager (gestionat automat de Spring)
 * 3. application.properties (configurare connection string)
 *
 * Acest fișier conține interfețele Repository care reprezintă funcționalitatea "DbContext"
 * pentru cele două entități principale: Category și Lesson
 *
 * ============================================================================
 */

package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository - Echivalent cu DbSet<Category> în .NET EF DbContext
 *
 * Extinde JpaRepository care oferă:
 * - save(entity) - INSERT/UPDATE
 * - findById(id) - SELECT by primary key
 * - findAll() - SELECT all
 * - deleteById(id) - DELETE
 * - count() - COUNT(*)
 *
 * Plus metodele custom query definite mai jos
 *
 * COMPARAȚIE .NET vs Spring:
 * ---------------------------
 * .NET EF:              public DbSet<Category> Categories { get; set; }
 * Spring JPA:           public interface CategoryRepository extends JpaRepository<Category, Long>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    // ============================================
    // DERIVED QUERY METHODS
    // Spring generează automat SQL din numele metodei
    // ============================================

    /**
     * Găsește toate categoriile active
     * SQL generat: SELECT * FROM categories WHERE is_active = true
     *
     * .NET LINQ echivalent: context.Categories.Where(c => c.IsActive == true).ToList()
     */
    List<Category> findByIsActiveTrue();

    /**
     * Găsește toate categoriile ordonate după displayOrder
     * SQL generat: SELECT * FROM categories ORDER BY display_order ASC
     *
     * .NET LINQ: context.Categories.OrderBy(c => c.DisplayOrder).ToList()
     */
    List<Category> findAllByOrderByDisplayOrderAsc();

    /**
     * Găsește categorie după nume (case-insensitive)
     * SQL generat: SELECT * FROM categories WHERE LOWER(name) = LOWER(?)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Verifică dacă există o categorie cu acest nume
     * SQL generat: SELECT COUNT(*) > 0 FROM categories WHERE LOWER(name) = LOWER(?)
     *
     * .NET LINQ: context.Categories.Any(c => c.Name.ToLower() == name.ToLower())
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Găsește toate categoriile ordonate după nume
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Găsește categorii active ordonate după displayOrder
     * SQL: SELECT * FROM categories WHERE is_active = true ORDER BY display_order ASC
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    // ============================================
    // CUSTOM QUERIES CU @EntityGraph
    // Pentru încărcarea eager a relațiilor (evită N+1 problem)
    // ============================================

    /**
     * Găsește categorie cu lecțiile sale (EAGER FETCH)
     * @EntityGraph încarcă relația "lessons" într-un singur query
     *
     * Fără @EntityGraph: SELECT * FROM categories WHERE id = ? (apoi N queries pentru lessons)
     * Cu @EntityGraph: SELECT c.*, l.* FROM categories c LEFT JOIN lessons l WHERE c.id = ?
     *
     * .NET EF echivalent: context.Categories.Include(c => c.Lessons).FirstOrDefault(c => c.Id == id)
     */
    @EntityGraph(attributePaths = "lessons")
    Optional<Category> findWithLessonsById(Long lessonId);

    /**
     * Găsește toate categoriile cu lecțiile lor
     *
     * .NET EF: context.Categories.Include(c => c.Lessons).OrderBy(c => c.DisplayOrder).ToList()
     */
    @EntityGraph(attributePaths = "lessons")
    List<Category> findAllWithLessonsByOrderByDisplayOrderAsc();

    // ============================================
    // CUSTOM JPQL QUERIES
    // JPQL = Java Persistence Query Language (similar cu HQL din Hibernate)
    // Este similar cu SQL dar folosește nume de entități în loc de tabele
    // ============================================

    /**
     * Numără lecțiile pentru o categorie
     * JPQL folosește numele entității (Lesson) și numele proprietății (category.id)
     * nu numele tabelului (lessons) și coloanei (category_id)
     *
     * .NET LINQ: context.Lessons.Count(l => l.CategoryId == categoryId)
     */
    @Query("select count(1) from Lesson l where l.category.id = :categoryId")
    long countLessons(@Param("categoryId") Long categoryId);
}

// ============================================
// ============================================

package me.aydgn.MorseMate.repository;

import me.aydgn.MorseMate.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LessonRepository - Echivalent cu DbSet<Lesson> în .NET EF DbContext
 *
 * Acest repository gestionează entitatea Lesson care are o relație de cheie străină
 * cu entitatea Category (Many-to-One)
 *
 * RELAȚIA FOREIGN KEY:
 * --------------------
 * Lesson.category_id -> Category.id
 * @ManyToOne în Lesson entity
 * @OneToMany în Category entity
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {

    // ============================================
    // DERIVED QUERY METHODS - Foreign Key Queries
    // ============================================

    /**
     * Găsește lecții după category_id (foreign key), ordonate
     * SQL generat: SELECT * FROM lessons WHERE category_id = ? ORDER BY order_index ASC
     *
     * .NET LINQ: context.Lessons.Where(l => l.CategoryId == categoryId).OrderBy(l => l.OrderIndex).ToList()
     */
    List<Lesson> findByCategoryIdOrderByOrderIndexAsc(Long categoryId);

    /**
     * Găsește lecții după numele categoriei (navigând relația)
     * Spring parcurge automat relația: Lesson -> Category -> Name
     * SQL: SELECT l.* FROM lessons l JOIN categories c ON l.category_id = c.id
     *      WHERE LOWER(c.name) = LOWER(?) ORDER BY l.order_index ASC
     *
     * .NET LINQ: context.Lessons
     *              .Where(l => l.Category.Name.ToLower() == categoryName.ToLower())
     *              .OrderBy(l => l.OrderIndex)
     *              .ToList()
     */
    List<Lesson> findByCategory_NameIgnoreCaseOrderByOrderIndexAsc(String categoryName);

    /**
     * Caută lecții după titlu cu paginare
     * Page<T> oferă suport pentru paginare (similar cu Skip/Take în LINQ)
     * SQL: SELECT * FROM lessons WHERE LOWER(title) LIKE LOWER(?) LIMIT ? OFFSET ?
     *
     * .NET LINQ cu paginare:
     * context.Lessons
     *   .Where(l => l.Title.ToLower().Contains(title.ToLower()))
     *   .Skip(page * pageSize)
     *   .Take(pageSize)
     *   .ToList()
     */
    Page<Lesson> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // ============================================
    // EAGER LOADING cu @EntityGraph
    // ============================================

    /**
     * Găsește lecție cu exercițiile sale (EAGER FETCH)
     * @EntityGraph previne N+1 query problem
     *
     * Fără @EntityGraph:
     *   Query 1: SELECT * FROM lessons WHERE id = ?
     *   Query 2-N: SELECT * FROM exercises WHERE lesson_id = ? (pentru fiecare lecție)
     *
     * Cu @EntityGraph:
     *   Query 1: SELECT l.*, e.* FROM lessons l
     *            LEFT JOIN exercises e ON l.id = e.lesson_id WHERE l.id = ?
     *
     * .NET EF: context.Lessons.Include(l => l.Exercises).FirstOrDefault(l => l.Id == id)
     */
    @EntityGraph(attributePaths = "exercises")
    Optional<Lesson> findWithExercisesById(Long id);

    // ============================================
    // CUSTOM JPQL QUERIES
    // ============================================

    /**
     * Numără exercițiile pentru o lecție
     * JPQL query custom cu agregare
     *
     * .NET LINQ: context.Exercises.Count(e => e.LessonId == lessonId)
     */
    @Query("select count(e) from Exercise e where e.lesson.id = :lessonId")
    long countExercises(@Param("lessonId") Long lessonId);

    /**
     * Actualizează order_index pentru o lecție
     * @Modifying indică că este un query UPDATE/DELETE (nu SELECT)
     *
     * .NET EF:
     * var lesson = context.Lessons.Find(lessonId);
     * lesson.OrderIndex = orderIndex;
     * context.SaveChanges();
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Lesson l set l.orderIndex = :orderIndex where l.id = :lessonId")
    int setOrderIndex(@Param("lessonId") Long lessonId, @Param("orderIndex") int orderIndex);
}

/*
 * ============================================================================
 * CONFIGURAREA CONEXIUNII LA BAZA DE DATE
 * ============================================================================
 *
 * În .NET EF, connection string-ul este în app.config/web.config:
 * ------------------------------------------------------------
 * <connectionStrings>
 *   <add name="DefaultConnection"
 *        connectionString="Server=localhost;Database=morsemate;User Id=postgres;Password=pass;"
 *        providerName="Npgsql" />
 * </connectionStrings>
 *
 * În Spring Boot, este în application.properties:
 * ------------------------------------------------
 * # Database Connection
 * spring.datasource.url=jdbc:postgresql://localhost:5432/morsemate
 * spring.datasource.username=postgres
 * spring.datasource.password=pass
 * spring.datasource.driver-class-name=org.postgresql.Driver
 *
 * # JPA/Hibernate Configuration
 * spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
 * spring.jpa.hibernate.ddl-auto=update
 * spring.jpa.show-sql=true
 * spring.jpa.properties.hibernate.format_sql=true
 *
 * Spring Boot creează automat connection pool-ul și gestionează EntityManager
 * (echivalent cu instanțele DbContext)
 */

/*
 * ============================================================================
 * COMPARAȚIE DETALIATĂ: .NET EF DbContext vs Spring JPA Repositories
 * ============================================================================
 *
 * 1. DEFINIREA DBCONTEXT / REPOSITORIES
 * --------------------------------------
 *
 * .NET Entity Framework:
 * ----------------------
 * public class ApplicationDbContext : DbContext
 * {
 *     // DbSet-uri pentru fiecare entitate
 *     public DbSet<Category> Categories { get; set; }
 *     public DbSet<Lesson> Lessons { get; set; }
 *
 *     // Constructor
 *     public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
 *         : base(options) { }
 *
 *     // Configurare suplimentară
 *     protected override void OnModelCreating(ModelBuilder modelBuilder)
 *     {
 *         // Relații, constrângeri, etc.
 *         modelBuilder.Entity<Lesson>()
 *             .HasOne(l => l.Category)
 *             .WithMany(c => c.Lessons)
 *             .HasForeignKey(l => l.CategoryId);
 *     }
 * }
 *
 * Spring Boot JPA:
 * ----------------
 * // Nu există o clasă centrală DbContext!
 * // În schimb, avem interfețe separate pentru fiecare entitate:
 *
 * @Repository
 * public interface CategoryRepository extends JpaRepository<Category, Long> {
 *     // Metodele sunt auto-implementate de Spring
 * }
 *
 * @Repository
 * public interface LessonRepository extends JpaRepository<Lesson, Long> {
 *     // Metodele sunt auto-implementate de Spring
 * }
 *
 * // Relațiile sunt definite în entități cu adnotări:
 * @Entity
 * public class Lesson {
 *     @ManyToOne
 *     @JoinColumn(name = "category_id")
 *     private Category category;
 * }
 *
 *
 * 2. UTILIZAREA ÎN SERVICE LAYER
 * -------------------------------
 *
 * .NET Entity Framework:
 * ----------------------
 * public class CategoryService
 * {
 *     private readonly ApplicationDbContext _context;
 *
 *     public CategoryService(ApplicationDbContext context)
 *     {
 *         _context = context;
 *     }
 *
 *     public List<Category> GetAllCategories()
 *     {
 *         return _context.Categories.ToList();
 *     }
 *
 *     public Category GetById(long id)
 *     {
 *         return _context.Categories.Find(id);
 *     }
 *
 *     public void Save(Category category)
 *     {
 *         _context.Categories.Add(category);
 *         _context.SaveChanges();
 *     }
 * }
 *
 * Spring Boot JPA:
 * ----------------
 * @Service
 * public class CategoryService {
 *
 *     private final CategoryRepository categoryRepository;
 *
 *     public CategoryService(CategoryRepository categoryRepository) {
 *         this.categoryRepository = categoryRepository;
 *     }
 *
 *     public List<Category> getAllCategories() {
 *         return categoryRepository.findAll();
 *     }
 *
 *     public Category getById(Long id) {
 *         return categoryRepository.findById(id).orElse(null);
 *     }
 *
 *     public void save(Category category) {
 *         categoryRepository.save(category);
 *         // Nu există SaveChanges() - salvarea este imediată
 *     }
 * }
 *
 *
 * 3. DIFERENȚE CHEIE
 * ------------------
 *
 * Aspect                 | .NET Entity Framework          | Spring Boot JPA
 * -----------------------|--------------------------------|----------------------------------
 * Clasă centrală         | DbContext (o clasă)           | Repository interfaces (multiple)
 * DbSet echivalent       | DbSet<T>                      | JpaRepository<T, ID>
 * Salvare modificări     | context.SaveChanges()         | Automat (no explicit call needed)
 * Query methods          | LINQ în cod                    | Derived queries (method names)
 * Eager loading          | .Include()                     | @EntityGraph sau JOIN FETCH
 * Transaction management | Explicit sau [TransactionScope]| @Transactional annotation
 * Change tracking        | Automat (DetectChanges)        | Managed by EntityManager
 * Connection management  | Manual dispose sau using       | Automat (Spring manages lifecycle)
 *
 *
 * 4. EXEMPLE DE QUERY-URI ECHIVALENTE
 * ------------------------------------
 *
 * Operație: Găsește toate categoriile active
 * .NET LINQ:     context.Categories.Where(c => c.IsActive).ToList()
 * Spring JPA:    categoryRepository.findByIsActiveTrue()
 *
 * Operație: Găsește lecții după categorie cu include
 * .NET LINQ:     context.Lessons.Include(l => l.Category).Where(l => l.CategoryId == id).ToList()
 * Spring JPA:    lessonRepository.findByCategoryIdOrderByOrderIndexAsc(id)
 *
 * Operație: Numără înregistrări
 * .NET LINQ:     context.Lessons.Count(l => l.CategoryId == categoryId)
 * Spring JPA:    lessonRepository.countByCategoryId(categoryId)
 *
 * Operație: Verifică existență
 * .NET LINQ:     context.Categories.Any(c => c.Name == name)
 * Spring JPA:    categoryRepository.existsByNameIgnoreCase(name)
 */
