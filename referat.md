## Laborator 10 – Arhitectura pe straturi și Dependency Injection  

### 1. Introducere

Aplicația mea, **MorseMate Backend**, este un API pentru învățarea codului Morse construit cu **Spring Boot 3**.  
În acest referat arăt cum am aplicat cele trei idei principale din laborator:  

- introducerea clară a unui **Business Layer** între controller și accesul la date;  
- folosirea **Dependency Injection (DI)** în locul instanțierii directe cu `new`;  
- experimentarea cu diferite **scope‑uri** ale bean‑urilor (singleton / request / prototype).  

Documentul este scurt și se bazează doar pe câteva exemple concrete din cod.  

---

### 2. Exercițiul 1 – Business Layer (Nivelul de Servicii)  

În proiect am trei niveluri clare:  

- **Prezentare** – `src/main/java/me/aydgn/MorseMate/controller/ExerciseController.java`  
- **Business Layer** – `src/main/java/me/aydgn/MorseMate/service/ExerciseService.java`  
- **Acces la date** – `src/main/java/me/aydgn/MorseMate/repository/ExerciseRepository.java`  

**Exemplu concret (gestionarea exercițiilor):**  

- În `ExerciseController` metoda `getExerciseById(Long id)` primește cererea HTTP și apelează direct `exerciseService.getExerciseById(id)`.  
- În `ExerciseService` metoda `getExerciseById(Long id)` caută entitatea în `ExerciseRepository`, verifică dacă există și, dacă nu o găsește, aruncă `ResourceNotFoundException`.  

Astfel, controller‑ul nu conține logică de business, ci doar delegă spre Business Layer, iar regulile aplicației sunt izolate în `ExerciseService`.  

---

### 3. Exercițiul 2 – Dependency Injection (DI)  

În loc să folosesc `new` pentru a crea servicii sau repository‑uri, m‑am bazat pe containerul Spring și pe **constructor injection**.  

**Exemplu concret de DI în Business Layer:**  

- Clasa `ExerciseService` este adnotată cu `@Service` și `@RequiredArgsConstructor` și are câmpurile:  
  - `private final ExerciseRepository exerciseRepository;`  
  - `private final LessonService lessonService;`  
- Spring creează automat instanțele pentru `ExerciseRepository` și `LessonService` și le injectează în `ExerciseService` prin constructor.  

Un al doilea exemplu similar este `PaymentService`  
(`src/main/java/me/aydgn/MorseMate/service/PaymentService.java`), care primește prin DI `PaymentRepository`, `UserRepository` și `UserSubscriptionRepository` și implementează logica de creare și rambursare a plăților.  

Prin acest model, codul este mai ușor de testat și respectă cerința laboratorului de a elimina dependențele directe dintre clase.  

---

### 4. Exercițiul 3 – Durata de viață (scope) a obiectelor  

În Spring, scope‑urile recomandate corespund în mare cu cele cerute în laborator:  

- **Singleton** (implicit) – un singur bean pentru întreaga aplicație;  
- **Request** – un bean nou pentru fiecare cerere HTTP;  
- **Prototype** – un bean nou de fiecare dată când este cerut.  

Pentru a testa practic aceste diferențe, pot folosi un serviciu simplu, de exemplu `ScopeDemoService`, care generează un UUID la construcție și îl expune printr‑o metodă.  
Acest serviciu poate fi accesat dintr‑un controller `ScopeDemoController` printr‑un endpoint `/api/scope-demo`.  
Schimbând adnotarea `@Scope("singleton")`, `@Scope("request")` sau `@Scope("prototype")` pe `ScopeDemoService`, pot observa dacă ID‑ul rămâne același sau se schimbă la fiecare cerere, și astfel pot documenta diferențele dintre cele trei moduri.  

---

### 5. Concluzie  

Prin separarea clară între `ExerciseController` – `ExerciseService` – `ExerciseRepository`, prin folosirea constructor injection în servicii precum `ExerciseService` și `PaymentService` și prin testarea scope‑urilor cu un mic exemplu (`ScopeDemoService`), proiectul meu respectă cerințele esențiale ale Laboratorului 10.  
Am obținut o arhitectură pe straturi, ușor de întreținut și ușor de explicat în câteva fișiere Java bine alese.


