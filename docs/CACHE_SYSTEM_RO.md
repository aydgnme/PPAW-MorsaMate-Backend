# Sistemul de Cache (Spring Cache + Caffeine)

Acest document descrie implementarea mecanismului de cache din aplicație, bazat pe Spring Cache Abstraction și provider-ul in-memory Caffeine. Scopul este să reducem încărcarea pe bază de date și latența răspunsurilor pentru resurse frecvent accesate (categorii, lecții, planuri de abonament, abonamentele utilizatorilor).

## Prezentare generală

- Framework: Spring Cache Abstraction (anotații `@Cacheable`, `@CacheEvict`).
- Provider: Caffeine (cache în memorie, performant, cu expirare după scriere și limită de capacitate).
- Politici: TTL per intrare și limită maximă de intrări per cache.
- Domenii cache-uite: `categories`, `lessons`, `subscription-plans`, `user-subscriptions`.

## Configurație

Fișier: `application.yml`

- Tip cache: `spring.cache.type=caffeine`.
- Nume cache-uri: `spring.cache.cache-names` include lista de cache-uri folosite în servicii.
- Parametri aplicație:
  - `app.cache.ttl-minutes` – TTL per intrare (implicit 60 minute).
  - `app.cache.max-size` – număr maxim de intrări per cache (implicit 1000).

Aceste proprietăți pot fi suprascrise prin variabile de mediu folosind relaxed binding Spring (ex.: `APP_CACHE_TTL_MINUTES`, `APP_CACHE_MAX_SIZE`).

Exemplu minim:

```yaml
spring:
  cache:
    type: caffeine
    cache-names:
      - categories
      - lessons
      - subscription-plans
      - user-subscriptions

app:
  cache:
    ttl-minutes: 60    # implicit
    max-size: 1000     # implicit
```

## Implementare (Cod)

- Config: `CacheConfig` configurează `CaffeineCacheManager` cu `expireAfterWrite(ttl)` și `maximumSize(maxSize)` și înregistrează cache-urile din listă.
- Servicii: se folosesc anotațiile Spring pentru populare/evacuare cache:
  - `@Cacheable(value = "categories", key = "'all'")`
  - `@Cacheable(value = "lessons", key = "'category_' + #categoryId")`
  - `@CacheEvict(value = "categories", allEntries = true)` pentru operații de tip create/update/delete

Nota: Pentru obiectele puse în cache, este recomandat să fie serializabile. În proiect, entitățile de bază relevante (ex.: `BaseEntity`, `UserSubscription`) au fost marcate `Serializable` pentru compatibilitate maximă.

## Cum adaug un nou cache

1. Adaugă numele cache-ului în `spring.cache.cache-names` (în `application.yml`).
2. Anotează metodele de citire cu `@Cacheable(value = "<nume-cache>", key = <expresie-cheie>)`.
3. Anotează metodele de scriere (create/update/delete) cu `@CacheEvict(value = "<nume-cache>", key = <cheie> sau allEntries = true)`.
4. Dacă ai nevoie de TTL sau dimensiune diferită global, ajustează `app.cache.ttl-minutes` și/sau `app.cache.max-size`.

### Chei de cache (Keying)

- Folosește chei determinate de parametrii metodei pentru granularitate corectă.
- Exemple:
  - Toate categoriile: `key = "'all'"` (o singură intrare care reprezintă lista completă)
  - Lecții per categorie: `key = "'category_' + #categoryId"`

## Evacuare cache (Eviction)

- La operații de scriere (ex.: creare/actualizare/ștergere), folosește `@CacheEvict` pentru a invalida intrările afectate.
- Pentru liste agregate (ex.: „toate categoriile”), se recomandă `allEntries = true` pentru a evita valori învechite.

## Monitorizare și depanare

- La pornirea aplicației, Caffeine creează cache-urile declarate. Poți crește nivelul de log pentru pachetele `org.springframework.cache` și `com.github.benmanes.caffeine` pentru a observa evenimentele de cache (hit/miss/evict).
- Simptome ale unei configurări greșite:
  - „Valori învechite” după un update: verifică existența `@CacheEvict` pe calea de scriere.
  - „Cache nu pare activ”: verifică tipul (`spring.cache.type=caffeine`), numele cache-urilor și că metodele sunt chemate prin proxy Spring (apeluri inter-bean, nu self-invocation).

## Bune practici

- Cache-ează doar rezultate pur deterministe ale metodelor (fără efecte secundare) sau DTO-uri derivate.
- Evită cache-ul pentru rezultate extrem de volatile sau specifice utilizatorului dacă nu ai chei suficient de granulare.
- Ajustează `ttl-minutes` și `max-size` în funcție de volum și profilul de acces.

## Testare locală

- Pentru dezvoltare, poți seta un TTL mic pentru feedback rapid:

```properties
app.cache.ttl-minutes=1
```

- Pentru a „curăța” cache-ul în timpul testării manuale: repornește aplicația sau adaugă temporar `@CacheEvict(allEntries = true)` pe un endpoint de mentenanță (doar în profilurile non-producție).

## Întrebări frecvente (FAQ)

1) De ce nu se actualizează imediat lista după un update?
- Asigură-te că metoda de update are `@CacheEvict` pe cache-ul corespunzător (de exemplu, lista agregată trebuie ștearsă, nu doar intrarea individuală).

2) Pot avea TTL diferit pe cache-uri diferite?
- În prezent, TTL și dimensiunea sunt globale la nivel de manager. Dacă ai nevoie de politici distincte pe cache, extinde `CacheConfig` pentru a atașa politici per-nume de cache (instanțe Caffeine separate).

3) Pot dezactiva cache-ul în anumite profile?
- Da, poți seta `spring.cache.type=none` într-un profil dedicat sau poți exclude anotațiile în testele slice unde nu ai nevoie de cache.

---

Dacă ai nevoie de un nou cache sau de politică personalizată, deschide un ticket și menționează:
- Numele propus al cache-ului
- Cheile propuse
- TTL/Dimensiune dorite
- Punctele din cod unde se aplică `@Cacheable`/`@CacheEvict`
