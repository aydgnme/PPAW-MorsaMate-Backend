# Documentatie Proiect PPAW

## Proiectare

### 1. Paradigme Utilizate
În dezvoltarea aplicației MorseMate, am utilizat următoarele paradigme și arhitecturi moderne:

*   **Arhitectura Stratificată (Layered Architecture - MVC/REST):** Aplicația este structurată pe straturi distincte (Controller, Service, Repository, Entity) pentru a asigura separarea responsabilităților (Separation of Concerns).
    *   **Controller (API Layer):** Gestionează cererile HTTP, validarea intrărilor și serializarea răspunsurilor.
    *   **Service (Business Layer):** Conține logica de business pură, independentă de tehnologia web.
    *   **Repository (Data Access Layer):** Abstracție peste baza de date utilizând Spring Data JPA.
*   **ORM (Object-Relational Mapping) - Code First:** Am utilizat Hibernate via Spring Data JPA. Abordarea "Code First" ne-a permis să definim modelul de date direct în clasele Java (`@Entity`), lăsând framework-ul să genereze schema bazei de date.
*   **Dependency Injection (DI):** Folosită intensiv prin Spring Framework pentru a gestiona dependențele între componente, facilitând testarea și modularitatea.

### 2. De ce au fost alese?
*   **MVC/REST (Separarea Responsabilităților):** Permite dezvoltarea independentă a frontend-ului și backend-ului. Backend-ul expune un API JSON standardizat.
*   **Spring Boot:** Framework-ul de facto pentru Java Enterprise, oferind configurare automată, server integrat și ecosistem bogat.
*   **ORM Code First:** Crește viteza de dezvoltare și menține consistența între cod și baza de date. Modificările în entități se reflectă automat în baza de date.

### 3. Arhitectura Aplicației
Modulele principale care interacționează:
1.  **Client Web/Mobile:** Consumă API-ul REST securizat prin JWT.
2.  **API Gateway / Controller Layer:** Punctul de intrare, direcționează cererile către servicii.
3.  **Security Layer:** Interceptează cererile prin filtre (`JwtAuthenticationFilter`) pentru autentificare și autorizare.
4.  **Service Layer:** "Inima" aplicației (ex: `LessonService`, `UserProgressService`). Gestionează tranzacțiile.
5.  **Persistence Layer:** `CategoryRepository`, `LessonRepository` etc., interacționează cu PostgreSQL.
6.  **External Services:** Integrare cu Stripe (plăți).

---

## Implementare

### 1. Business Layer - Explicat
Stratul de servicii implementează logica complexă a aplicației:
*   **UserProgressService:** Calculează automat progresul utilizatorului, actualizează statisticile și gestionează mecanismul de "streak" (zile consecutive).
*   **LessonService:** Gestionează curriculum-ul. Interesant este mecanismul de **Logical Delete** implementat.
*   **PaymentService:** Gestionează abonamentele utilizatorilor și sincronizarea cu Stripe.

### 2. Librării Suplimentare Utilizate
*   **Lombok:** Pentru reducerea codului "boilerplate" (getters, setters, builders).
*   **MapStruct:** Pentru maparea eficientă și type-safe între Entități și DTO-uri.
*   **Caffeine:** Cache "in-memory" de înaltă performanță pentru datele accesate frecvent (ex: categorii, lecții publice).
*   **JJWT (Java JWT):** Pentru generarea și validarea token-urilor de securitate.
*   **SpringDoc OpenAPI (Swagger):** Pentru generarea automată a documentației API.
*   **Stripe Java:** Pentru procesarea plăților.

### 3. Secțiuni de Cod sau Abordări Deosebite
*   **Soft Delete (Ștergere Logică):**
    Nu ștergem fizic datele. Am folosit anotările Hibernate `@SQLDelete` și `@Where` pe entități.
    ```java
    @SQLDelete(sql = "UPDATE categories SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
    @Where(clause = "deleted_at IS NULL")
    public class Category extends BaseEntity { ... }
    ```
    Aceasta asigură că orice ștergere devine un UPDATE, iar orice interogare (SELECT) filtrează automat elementele șterse, fără a modifica logica de business din servicii.

*   **Auditare Automată:**
    Clasa `BaseEntity` și listenerii `@PrePersist` / `@PreUpdate` asigură completarea automată a timestamp-urilor `created_at` și `updated_at`.


### 4. Funcționalități Cheie Detaliate (Backend Logic)
Pe lângă arhitectura standard, backend-ul gestionează logica specifică domeniului de e-learning:

*   **Sistemul de Învățare (Learning Engine):**
    *   Structură ierarhică: **Category** -> **Lesson** -> **Exercise**.
    *   **Validarea Exercițiilor:** Backend-ul primește input-ul utilizatorului (ex: cod Morse `... --- ...`) și îl validează față de răspunsul corect.
    *   **Calculul Punctajului:** Algoritmi dedicați în `ExerciseAttemptService` care calculează punctele (XP) și monedele (Gems) bazate pe corectitudine și timpul de răspuns.

*   **Gamification & Progres:**
    *   **Hearts System:** Utilizatorii au un număr limitat de "vieți" (Hearts). Greșelile scad numărul de vieți; acestea se regenerează în timp sau pot fi cumpărate cu Gems.
    *   **Streak Tracking:** Logica complexă pentru a urmări activitatea zilnică și a acorda bonusuri pentru consecvență.

*   **Sistemul de Abonamente (Monetizare):**
    *   Integrare cu **Stripe** pentru plăți securizate.
    *   **Webhooks:** Backend-ul ascultă evenimente asincrone de la Stripe (ex: `invoice.payment_succeeded`) pentru a activa/dezactiva automat funcționalitățile Premium (vieți nelimitate, exerciții avansate).

### 5. Interfața Web (Frontend)

Aplicația folosește o arhitectură hibridă pentru a optimiza performanța și securitatea:

#### A. Admin Panel (Server-Side Rendering)
Pentru zona de administrare, am ales o abordare clasică și robustă:
*   **Tehnologie:** **Thymeleaf**.
*   **Motiv:** Randarea pe server (SSR) asigură că datele sensibile sunt populate securizat înainte de a ajunge la client. Simplifică dezvoltarea operațiunilor CRUD complexe (gestionare utilizatori, lecții, plăți).
*   **Locație:** `src/main/resources/templates/admin`
*   **Funcționalități:** Dashboard, User Management, Content Management (Lecții/Exerciții), Vizualizare Tranzacții.

#### B. User Client (Client-Side Rendering)
Pentru experiența de învățare a utilizatorilor, interfața este dinamică și rapidă:
*   **Tehnologie:** **HTML5, CSS3, JavaScript (Vanilla)**.
*   **Interacțiune:** Pagini statice (`src/main/resources/static/pages`) care comunică asincron (AJAX/Fetch API) cu Backend-ul REST.
*   **Autentificare:** JWT-ul primit la login este stocat securizat și trimis automat în header-ul `Authorization` la fiecare cerere.
*   **Payment Flow:** Integrare directă cu **Stripe.js** (`src/main/resources/static/js/payment.js`) pentru procesarea cardurilor în browser, fără a atinge serverul nostru (PCI Compliance).

---

## Utilizare

### 1. Pașii de Instalare

#### a) Instalare și configurare pentru programator
1.  **Cerințe:** Java 21 JDK, Docker (pentru PostgreSQL).
2.  **Clonare:** `git clone <repo-url>`
3.  **Baza de date:**
    Rulați `docker-compose up -d` pentru a porni containerul PostgreSQL.
4.  **Configurare:**
    Copiați `.env.example` în `.env` și completați cheile (DB URL, Stripe Keys, JWT Secret).
5.  **Rulare:**
    `./gradlew bootRun`

#### b) Instalare și configurare la beneficiar (Deploy)
1.  Se recomandă utilizarea imaginii Docker a aplicației.
2.  `docker build -t morsemate-backend .`
3.  `docker run -p 8080:8080 --env-file .env morsemate-backend`

### 2. Mod de Utilizare
*   **Admin:** Accesează endpoint-urile protejate (`/admin/**`) pentru a crea conținut (Categorii, Lecții, Exerciții).
*   **Utilizator:** Se înregistrează (`/auth/register`), primește un JWT. Folosește token-ul în header-ul `Authorization: Bearer <token>` pentru a accesa lecțiile și a salva progresul.
*   **Documentație API:** Odată pornită aplicația, navigați la `http://localhost:8080/swagger-ui.html` pentru a interacționa cu API-ul.
