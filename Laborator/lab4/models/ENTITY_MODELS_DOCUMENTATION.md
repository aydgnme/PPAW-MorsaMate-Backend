# MorseMate - Entity Models Documentation
## Laborator 4 - PPAW - ORM Code First

**Student:** Aydogan Mert
**Proiect:** MorseMate - Morse Code Learning Application
**Tehnologie:** Spring Boot + JPA/Hibernate + PostgreSQL

---

## 1. Structura Generală a Entităților

Aplicația MorseMate utilizează **17 entități principale** organizate ierarhic pentru gestionarea unui sistem de învățare Morse Code cu gamificare.

### Ierarhie:
```
BaseEntity (Abstract)
├── User (Entitate Centrală)
├── Category
├── Lesson
├── Exercise
├── Achievement
├── PowerUp
├── SubscriptionPlan
├── PromoCode
└── Relații (Many-to-Many / One-to-Many):
    ├── UserProgress
    ├── UserAchievement
    ├── ExerciseAttempt
    ├── UserGems
    ├── GemTransaction
    ├── UserPowerUp
    ├── UserSubscription
    └── Payment
```

---

## 2. Entități de Bază

### 2.1 BaseEntity (Clasa Abstractă)
**Rol:** Clasa de bază pentru toate entitățile, oferă câmpuri comune de audit.

**Atribute:**
- `id` (Long) - Cheie primară, generată automat
- `createdAt` (LocalDateTime) - Data creării (audit)
- `updatedAt` (LocalDateTime) - Data ultimei modificări (audit)

**Adnotări JPA:**
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
```

**Caracteristici:**
- Utilizează `@CreatedDate` și `@LastModifiedDate` pentru audit automat
- Toate entitățile moștenesc această clasă

---

### 2.2 User (Entitatea Principală)
**Rol:** Reprezintă utilizatorii aplicației cu sistem de gamificare și autentificare.

**Atribute Principale:**
- `id` (Long) - PK
- `username` (String, UNIQUE) - Nume utilizator unic
- `email` (String, UNIQUE) - Email unic
- `passwordHash` (String) - Parolă criptată
- `fullName` (String) - Nume complet
- `role` (Role) - Rol: USER, ADMIN, PREMIUM
- `level` (int) - Nivel actual (gamificare)
- `totalPoints` (int) - Total puncte acumulate
- `hearts` (int) - Inimi rămase (viață în joc)
- `maxHearts` (int) - Număr maxim de inimi
- `currentStreak` (int) - Zile consecutive de activitate
- `longestStreak` (int) - Cel mai lung streak
- `profilePictureUrl` (String) - URL poză profil
- `isActive` (boolean) - Cont activ/inactiv
- `emailVerified` (boolean) - Email verificat
- `lastLogin` (LocalDateTime) - Ultima autentificare
- `lastHeartRefill` (LocalDateTime) - Ultima completare inimi

**Relații:**
- One-to-Many cu `UserProgress` (progresul în lecții)
- One-to-Many cu `ExerciseAttempt` (încercări exerciții)
- One-to-Many cu `UserAchievement` (realizări obținute)
- One-to-One cu `UserGems` (gemuri virtuale)
- One-to-Many cu `GemTransaction` (istoric tranzacții)
- One-to-Many cu `UserPowerUp` (power-up-uri active)
- One-to-One cu `UserSubscription` (abonament)
- One-to-Many cu `Payment` (plăți)

**Constrângeri:**
```java
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Check(constraints = "hearts >= 0 AND hearts <= max_hearts AND level >= 1")
```

**Enum Role:**
```java
public enum Role {
    USER,    // Utilizator normal
    ADMIN,   // Administrator
    PREMIUM  // Utilizator premium (abonat)
}
```

---

## 3. Entități de Conținut

### 3.1 Category
**Rol:** Categorii de lecții (ex: Basics, Intermediate, Advanced)

**Atribute:**
- `id` (Long) - PK
- `name` (String, UNIQUE) - Nume categorie
- `description` (String) - Descriere
- `displayOrder` (int) - Ordine afișare
- `isActive` (boolean) - Activ/inactiv

**Relații:**
- One-to-Many cu `Lesson`

---

### 3.2 Lesson
**Rol:** Lecții individuale de învățare Morse Code

**Atribute:**
- `id` (Long) - PK
- `categoryId` (Long) - FK către Category
- `title` (String) - Titlu lecție
- `description` (String) - Descriere
- `content` (String, TEXT) - Conținut lecție
- `difficulty` (Difficulty) - Dificultate: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
- `requiredLevel` (int) - Nivel minim necesar
- `displayOrder` (int) - Ordine în categorie
- `estimatedMinutes` (int) - Timp estimat (minute)
- `pointsReward` (int) - Puncte pentru finalizare
- `isActive` (boolean) - Activ/inactiv

**Relații:**
- Many-to-One cu `Category`
- One-to-Many cu `Exercise`
- One-to-Many cu `UserProgress`

---

### 3.3 Exercise
**Rol:** Exerciții practice pentru lecții

**Atribute:**
- `id` (Long) - PK
- `lessonId` (Long) - FK către Lesson
- `type` (ExerciseType) - Tip: LISTENING, WRITING, TRANSLATION, SPEED_TEST, DICTATION
- `question` (String, TEXT) - Întrebare
- `correctAnswer` (String) - Răspuns corect
- `options` (String, JSON) - Opțiuni multiple (JSON array)
- `morseCode` (String) - Cod Morse asociat
- `audioUrl` (String) - URL audio
- `displayOrder` (int) - Ordine
- `difficulty` (Difficulty) - Dificultate
- `pointsReward` (int) - Puncte
- `timeLimit` (int) - Limită timp (secunde)
- `hint` (String) - Indiciu
- `isActive` (boolean)

**Relații:**
- Many-to-One cu `Lesson`
- One-to-Many cu `ExerciseAttempt`

---

## 4. Entități de Progres și Gamificare

### 4.1 UserProgress
**Rol:** Urmărește progresul utilizatorilor în lecții

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK către User
- `lessonId` (Long) - FK către Lesson
- `status` (ProgressStatus) - NOT_STARTED, IN_PROGRESS, COMPLETED
- `progressPercentage` (int) - Procent progres (0-100)
- `score` (int) - Scor obținut
- `completedAt` (LocalDateTime) - Data finalizării

**Relații:**
- Many-to-One cu `User`
- Many-to-One cu `Lesson`

**Constrângere:**
```java
@Check(constraints = "progress_percentage >= 0 AND progress_percentage <= 100")
```

---

### 4.2 ExerciseAttempt
**Rol:** Istoric încercări exerciții

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK
- `exerciseId` (Long) - FK
- `userAnswer` (String) - Răspuns utilizator
- `isCorrect` (boolean) - Corect/greșit
- `pointsEarned` (int) - Puncte câștigate
- `timeTaken` (int) - Timp (secunde)
- `attemptedAt` (LocalDateTime) - Data încercării

**Relații:**
- Many-to-One cu `User`
- Many-to-One cu `Exercise`

---

### 4.3 Achievement
**Rol:** Realizări (badges) disponibile

**Atribute:**
- `id` (Long) - PK
- `name` (String, UNIQUE) - Nume
- `description` (String) - Descriere
- `iconUrl` (String) - URL icon
- `type` (AchievementType) - STREAK, SCORE, COMPLETION, SPEED, SPECIAL
- `requirement` (int) - Cerință pentru obținere
- `pointsReward` (int) - Puncte recompensă

**Relații:**
- One-to-Many cu `UserAchievement`

---

### 4.4 UserAchievement
**Rol:** Realizări obținute de utilizatori

**Atribute:**
- `id` (Long) - PK (Composite: userId + achievementId)
- `userId` (Long) - FK
- `achievementId` (Long) - FK
- `unlockedAt` (LocalDateTime) - Data deblocării

**Relații:**
- Many-to-One cu `User`
- Many-to-One cu `Achievement`

---

## 5. Sistem Economic (Gemuri și Power-Ups)

### 5.1 UserGems
**Rol:** Gemuri virtuale ale utilizatorilor (monedă în joc)

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK (UNIQUE)
- `totalGems` (int) - Total gemuri

**Relații:**
- One-to-One cu `User`

---

### 5.2 GemTransaction
**Rol:** Istoric tranzacții gemuri

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK
- `type` (TransactionType) - EARNED, SPENT, PURCHASED, REFUNDED
- `amount` (int) - Sumă
- `balanceAfter` (int) - Sold după tranzacție
- `description` (String) - Descriere
- `relatedEntityType` (String) - Entitate asociată
- `relatedEntityId` (Long) - ID entitate

**Relații:**
- Many-to-One cu `User`

---

### 5.3 PowerUp
**Rol:** Power-up-uri disponibile pentru cumpărare

**Atribute:**
- `id` (Long) - PK
- `name` (String, UNIQUE) - Nume
- `description` (String) - Descriere
- `type` (PowerUpType) - FREEZE_TIME, HINT, SKIP_EXERCISE, DOUBLE_POINTS, HEART_REFILL
- `iconUrl` (String) - URL icon
- `gemsCost` (int) - Cost în gemuri
- `durationMinutes` (int) - Durată efect (minute)

**Relații:**
- One-to-Many cu `UserPowerUp`

---

### 5.4 UserPowerUp
**Rol:** Power-up-uri active ale utilizatorilor

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK
- `powerUpId` (Long) - FK
- `purchasedAt` (LocalDateTime) - Data cumpărării
- `expiresAt` (LocalDateTime) - Data expirării
- `isActive` (boolean) - Activ/inactiv

**Relații:**
- Many-to-One cu `User`
- Many-to-One cu `PowerUp`

---

## 6. Sistem de Abonamente și Plăți

### 6.1 SubscriptionPlan
**Rol:** Planuri de abonament disponibile

**Atribute:**
- `id` (Long) - PK
- `name` (String, UNIQUE) - Nume plan
- `description` (String) - Descriere
- `price` (BigDecimal) - Preț
- `currency` (String) - Monedă (RON, USD, EUR)
- `billingPeriod` (BillingPeriod) - MONTHLY, YEARLY
- `features` (String, JSON) - Caracteristici (JSON array)
- `maxHearts` (int) - Inimi maxime
- `gemBonus` (int) - Bonus gemuri
- `isActive` (boolean)

**Relații:**
- One-to-Many cu `UserSubscription`

---

### 6.2 UserSubscription
**Rol:** Abonamente active ale utilizatorilor

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK (UNIQUE)
- `planId` (Long) - FK
- `status` (SubscriptionStatus) - ACTIVE, CANCELLED, EXPIRED
- `startDate` (LocalDateTime) - Data începerii
- `endDate` (LocalDateTime) - Data expirării
- `autoRenew` (boolean) - Reînnoire automată

**Relații:**
- One-to-One cu `User`
- Many-to-One cu `SubscriptionPlan`

---

### 6.3 Payment
**Rol:** Plăți efectuate

**Atribute:**
- `id` (Long) - PK
- `userId` (Long) - FK
- `planId` (Long) - FK
- `amount` (BigDecimal) - Sumă
- `currency` (String) - Monedă
- `status` (PaymentStatus) - PENDING, COMPLETED, FAILED, REFUNDED
- `paymentMethod` (String) - Metodă plată
- `transactionId` (String, UNIQUE) - ID tranzacție Stripe
- `paidAt` (LocalDateTime) - Data plății

**Relații:**
- Many-to-One cu `User`
- Many-to-One cu `SubscriptionPlan`

---

### 6.4 PromoCode
**Rol:** Coduri promoționale

**Atribute:**
- `id` (Long) - PK
- `code` (String, UNIQUE) - Cod
- `discountPercentage` (int) - Procent discount
- `maxUses` (int) - Utilizări maxime
- `currentUses` (int) - Utilizări curente
- `validFrom` (LocalDateTime) - Valid de la
- `validUntil` (LocalDateTime) - Valid până la
- `isActive` (boolean)

---

## 7. Caracteristici Avansate ORM

### 7.1 Lazy Loading vs Eager Loading

**Lazy Loading (Implicit):**
```java
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
private List<ExerciseAttempt> exerciseAttempts;
```
- Datele sunt încărcate doar când sunt accesate
- Mai eficient pentru performanță
- Utilizat pentru colecții mari (exerciseAttempts, userProgress)

**Eager Loading (Explicit):**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "lesson_id")
private Lesson lesson;
```
- Datele sunt încărcate imediat cu entitatea principală
- Utilizat pentru relații critice (lesson în Exercise)

### 7.2 Cascade Operations

**Exemple:**
```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<UserProgress> userProgress;
```
- `CascadeType.ALL` - Toate operațiile se propagă
- `orphanRemoval = true` - Șterge entitățile orfane

### 7.3 Indexuri și Constrângeri

**Indexuri:**
```java
@Table(name = "exercise_attempts", indexes = {
    @Index(name = "idx_user_exercise", columnList = "user_id, exercise_id"),
    @Index(name = "idx_attempted_at", columnList = "attempted_at")
})
```

**Constrângeri:**
```java
@Check(constraints = "price >= 0")
@Check(constraints = "gems_cost >= 0")
@Check(constraints = "discount_percentage >= 0 AND discount_percentage <= 100")
```

---

## 8. Enumerări (Enums)

### Role
- `USER` - Utilizator normal
- `ADMIN` - Administrator
- `PREMIUM` - Utilizator premium

### Difficulty
- `BEGINNER` - Începător
- `INTERMEDIATE` - Intermediar
- `ADVANCED` - Avansat
- `EXPERT` - Expert

### ExerciseType
- `LISTENING` - Ascultare cod Morse
- `WRITING` - Scriere cod Morse
- `TRANSLATION` - Traducere text ↔ Morse
- `SPEED_TEST` - Test viteză
- `DICTATION` - Dictare

### ProgressStatus
- `NOT_STARTED` - Neînceput
- `IN_PROGRESS` - În progres
- `COMPLETED` - Finalizat

### AchievementType
- `STREAK` - Bazat pe streak
- `SCORE` - Bazat pe scor
- `COMPLETION` - Bazat pe finalizare
- `SPEED` - Bazat pe viteză
- `SPECIAL` - Special

### TransactionType
- `EARNED` - Câștigat
- `SPENT` - Cheltuit
- `PURCHASED` - Cumpărat
- `REFUNDED` - Returnat

### PowerUpType
- `FREEZE_TIME` - Îngheață timpul
- `HINT` - Indiciu
- `SKIP_EXERCISE` - Sari exercițiul
- `DOUBLE_POINTS` - Puncte duble
- `HEART_REFILL` - Reumple inimi

### BillingPeriod
- `MONTHLY` - Lunar
- `YEARLY` - Anual

### SubscriptionStatus
- `ACTIVE` - Activ
- `CANCELLED` - Anulat
- `EXPIRED` - Expirat

### PaymentStatus
- `PENDING` - În așteptare
- `COMPLETED` - Finalizat
- `FAILED` - Eșuat
- `REFUNDED` - Returnat

---

## 9. Statistici Proiect

- **Total Entități:** 17
- **Entități de Bază:** 3 (BaseEntity, User, Category)
- **Entități de Conținut:** 3 (Lesson, Exercise, Achievement)
- **Entități de Progres:** 3 (UserProgress, ExerciseAttempt, UserAchievement)
- **Entități Economice:** 4 (UserGems, GemTransaction, PowerUp, UserPowerUp)
- **Entități Financiare:** 4 (SubscriptionPlan, UserSubscription, Payment, PromoCode)
- **Total Enumerări:** 10
- **Total Relații:** 24 (One-to-Many, Many-to-One, One-to-One)

---

**Data Creării:** 2025-10-22
**Autor:** Aydogan Mert
**Versiune:** 1.0
