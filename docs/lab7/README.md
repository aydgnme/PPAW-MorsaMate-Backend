# PPAW - Laborator 7: API REST

## Obiective

Acest laborator demonstrează implementarea unui API REST complet pentru aplicația MorseMate, utilizând Spring Boot Framework. Laboratorul acoperă toate operațiunile CRUD (Create, Read, Update, Delete) și respectă cele mai bune practici din industrie pentru dezvoltarea API-urilor.

## Descriere Generală

MorseMate este o aplicație web pentru învățarea codului Morse, care oferă un API REST comprehensiv pentru gestionarea utilizatorilor, lecțiilor, exercițiilor și progresului de învățare. API-ul este implementat folosind Spring Boot și include autentificare JWT, validare a datelor și documentație interactivă.

## Cerințe Îndeplinite

### Exercițiul 1: Operațiile GET și GET(id)

**Implementare**: Am creat multiple controllere REST care expun endpoint-uri pentru toate entitățile principale din aplicație.

**Exemple de endpoint-uri implementate**:
- `GET /api/categories` - Returnează toate categoriile
- `GET /api/categories/{id}` - Returnează detaliile unei categorii specifice
- `GET /api/lessons` - Returnează toate lecțiile
- `GET /api/lessons/{id}` - Returnează detaliile unei lecții specifice
- `GET /api/exercises` - Returnează toate exercițiile
- `GET /api/exercises/{id}` - Returnează detaliile unui exercițiu specific

**Tehnologii utilizate**:
- **Spring Boot** (echivalent .NET Core pentru Java)
- **Spring Data JPA** pentru accesul la baza de date
- **PostgreSQL** ca bază de date
- **Hibernate** ca ORM (Object-Relational Mapping)

### Exercițiul 2: Modele DTO și Mapping Automat

**Implementare**: Am creat modele DTO (Data Transfer Objects) separate pentru toate entitățile, care sunt diferite de modelele de bază de date. Acestea oferă o separare clară între layer-ul de persistență și layer-ul de prezentare.

**Structura proiectului**:
```
src/main/java/me/aydgn/MorseMate/
├── entity/           # Entități pentru baza de date
│   ├── User.java
│   ├── Category.java
│   ├── Lesson.java
│   └── Exercise.java
├── dto/             # Modele DTO pentru API
│   ├── request/     # DTO-uri pentru request-uri
│   └── response/    # DTO-uri pentru response-uri
└── controller/      # Controllere REST
    ├── CategoryController.java
    ├── LessonController.java
    └── ExerciseController.java
```

**Mapping automat**: Am implementat metode de conversie între entități și DTO-uri în interiorul claselor de service, oferind o separare clară a responsabilităților.

**Exemple de DTO-uri**:
- `CategoryResponse` - pentru afișarea categoriilor
- `LessonResponse` - pentru afișarea lecțiilor
- `CreateLessonRequest` - pentru crearea lecțiilor noi
- `UpdateLessonRequest` - pentru actualizarea lecțiilor

### Exercițiul 3: Operațiile POST și PUT

**Implementare**: Am implementat operațiuni complete de creare și actualizare pentru toate entitățile.

**Exemple de endpoint-uri**:
- `POST /api/categories` - Creează o categorie nouă
- `PUT /api/categories/{id}` - Actualizează o categorie existentă
- `POST /api/lessons` - Creează o lecție nouă
- `PUT /api/lessons/{id}` - Actualizează o lecție existentă
- `POST /api/exercises` - Creează un exercițiu nou
- `PUT /api/exercises/{id}` - Actualizează un exercițiu existent

**Validare**: Toate endpoint-urile POST și PUT includ validare completă a datelor folosind Jakarta Bean Validation (JSR 380).

## Funcționalități Suplimentare

Pe lângă cerințele de bază ale laboratorului, am implementat următoarele funcționalități:

### 1. Operațiunea DELETE
- `DELETE /api/categories/{id}` - Șterge o categorie
- `DELETE /api/lessons/{id}` - Șterge o lecție
- `DELETE /api/exercises/{id}` - Șterge un exercițiu

### 2. Autentificare și Autorizare
- Autentificare JWT (JSON Web Token)
- Roluri de utilizator (USER, ADMIN, PREMIUM)
- Protecție endpoint-uri bazată pe roluri
- Endpoints de autentificare:
  - `POST /api/auth/register` - Înregistrare utilizator nou
  - `POST /api/auth/login` - Autentificare utilizator

### 3. Gestionare Erori Globală
- Handler centralizat pentru toate excepțiile
- Mesaje de eroare clare și consistente
- Coduri de status HTTP corecte
- Format JSON pentru toate răspunsurile de eroare

### 4. Documentație Interactivă
- **Swagger UI** disponibil la `/swagger-ui.html`
- Documentație completă pentru toate endpoint-urile
- Posibilitate de testare directă din browser
- Exemple de request/response pentru fiecare endpoint

### 5. Filtrare și Sortare
- Filtrare după dificultate
- Filtrare după categorie
- Sortare ascendentă/descendentă
- Parametri query flexibili

### 6. Statistici și Metrici
- `GET /api/categories/{id}/stats` - Statistici pe categorii
- Număr de lecții per categorie
- Număr de exerciții per lecție
- Progres utilizator

## Structura API-ului

### Categorii (Categories)
```
GET    /api/categories           - Listează toate categoriile
GET    /api/categories/{id}      - Detalii despre o categorie
POST   /api/categories           - Creează o categorie nouă
PUT    /api/categories/{id}      - Actualizează o categorie
DELETE /api/categories/{id}      - Șterge o categorie
GET    /api/categories/{id}/stats - Statistici categorie
```

### Lecții (Lessons)
```
GET    /api/lessons              - Listează toate lecțiile
GET    /api/lessons/{id}         - Detalii despre o lecție
POST   /api/lessons              - Creează o lecție nouă
PUT    /api/lessons/{id}         - Actualizează o lecție
DELETE /api/lessons/{id}         - Șterge o lecție
GET    /api/lessons/category/{categoryId} - Lecții după categorie
```

### Exerciții (Exercises)
```
GET    /api/exercises            - Listează toate exercițiile
GET    /api/exercises/{id}       - Detalii despre un exercițiu
POST   /api/exercises            - Creează un exercițiu nou
PUT    /api/exercises/{id}       - Actualizează un exercițiu
DELETE /api/exercises/{id}       - Șterge un exercițiu
GET    /api/exercises/lesson/{lessonId} - Exerciții după lecție
```

### Autentificare (Authentication)
```
POST   /api/auth/register        - Înregistrare utilizator
POST   /api/auth/login           - Autentificare utilizator
GET    /api/auth/me              - Informații utilizator curent
```

## Testarea API-ului

### 1. Utilizând Postman

Am pregătit o colecție completă Postman care include:
- Toate endpoint-urile API
- Exemple de request-uri valide
- Teste automate pentru validarea răspunsurilor
- Variabile de mediu pentru configurare ușoară

**Fișier**: `docs/lab7/POSTMAN_COLLECTION.json`

### 2. Utilizând Swagger UI

Accesați `http://localhost:8080/swagger-ui.html` pentru:
- Documentație interactivă
- Testare directă din browser
- Vizualizare scheme de date
- Exemple de request/response

### 3. Utilizând cURL

Exemple de comenzi cURL pentru testare rapidă:

```bash
# GET - Listează toate categoriile
curl http://localhost:8080/api/categories

# GET - Detalii despre o categorie
curl http://localhost:8080/api/categories/1

# POST - Creează o categorie nouă
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Litere de Bază","description":"Învață literele de bază în cod Morse","difficulty":"BEGINNER"}'

# PUT - Actualizează o categorie
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Litere Actualizate","description":"Descriere actualizată","difficulty":"INTERMEDIATE"}'

# DELETE - Șterge o categorie
curl -X DELETE http://localhost:8080/api/categories/1
```

## Configurare și Rulare

### Cerințe Prealabile
- Java 17 sau superior
- Gradle 8.x
- PostgreSQL 15.x
- Postman (opțional, pentru testare)

### Pași de Configurare

1. **Clonați repository-ul**:
```bash
git clone <repository-url>
cd MorseMate
```

2. **Configurați baza de date**:
Editați `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/morse_code_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

3. **Rulați migrațiile de bază de date**:
```bash
./gradlew flywayMigrate
```

4. **Porniți aplicația**:
```bash
./gradlew bootRun
```

5. **Accesați API-ul**:
- API Base URL: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Validare și Tratare Erori

### Validări Implementate

Toate request-urile sunt validate folosind Jakarta Bean Validation:
- **@NotBlank** - Pentru câmpurile text obligatorii
- **@NotNull** - Pentru câmpurile obligatorii
- **@Size** - Pentru limitarea lungimii textului
- **@Pattern** - Pentru validarea formatelor
- **@Valid** - Pentru validarea obiectelor nested

### Exemple de Mesaje de Eroare

```json
{
  "timestamp": "2025-11-11T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "name",
      "message": "Name is required"
    }
  ]
}
```

## Securitate

### JWT Authentication

API-ul folosește JSON Web Tokens pentru autentificare:

1. **Înregistrare**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login` - Returnează token JWT
3. **Folosire**: Include token-ul în header:
   ```
   Authorization: Bearer <token>
   ```

### Roluri și Permisiuni

- **USER** - Acces la lecții și exerciții
- **PREMIUM** - Acces extins la conținut premium
- **ADMIN** - Acces complet, inclusiv operații CRUD

## Documentație Suplimentară

Pentru informații detaliate despre implementare și testare, consultați:

1. **API_IMPLEMENTATION.md** - Detalii tehnice despre implementare
2. **POSTMAN_TESTING.md** - Ghid complet de testare cu Postman
3. **API_ENDPOINTS.md** - Referință completă endpoint-uri
4. **../API.md** - Documentație generală API

## Concluzie

Acest laborator demonstrează o implementare completă și profesională a unui API REST folosind Spring Boot. Proiectul acoperă toate cerințele laboratorului și include multe funcționalități suplimentare care reflectă cele mai bune practici din industrie.

API-ul este:
- ✅ Complet (CRUD pentru toate entitățile)
- ✅ Securizat (JWT authentication)
- ✅ Validat (Bean Validation)
- ✅ Documentat (Swagger UI)
- ✅ Testat (Postman collections)
- ✅ Scalabil (Arhitectură în straturi)
- ✅ Mentenabil (Cod clean, separation of concerns)

## Autor

**Student**: Aydogan
**Curs**: PPAW - Programarea și Proiectarea Aplicațiilor Web
**Laborator**: 7 - API REST
**Data**: Noiembrie 2025
