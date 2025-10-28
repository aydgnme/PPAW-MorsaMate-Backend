# MorseMate Database - Arhitecturi Multi-Tier și Proiectare Aplicație

**Student:** Mert Aydogan
**Proiect:** MorseMate - Morse Code Learning Platform
**Data:** 08 Octombrie 2025

---

## 📁 Structura Proiectului

```
database/
├── schema/
│   ├── 01_create_tables.sql       # Script DDL pentru crearea tabelelor
│   └── 02_insert_test_data.sql    # Date de test
├── backup/
│   ├── physical_backup.sh         # Script backup fizic
│   ├── logical_backup.sh          # Script backup logic
│   ├── physical/                  # Directorul pentru backup-uri fizice
│   └── logical/                   # Directorul pentru backup-uri logice
├── test/
│   └── DatabaseConnectionTest.java # Aplicație de test conexiune
├── BACKUP_STEPS.md                # Documentație detaliată
└── README.md                      # Acest fișier
```

---

## 📦 Fișiere pentru Temă

### 1. Backup Fizic
**Fișier:** `database/backup/physical_backup.sh`
- Script automatizat pentru backup fizic
- Utilizează `pg_basebackup`
- Compresie cu tar+gzip
- Generează fișier de metadate

### 2. Backup Logic
**Fișier:** `database/backup/logical_backup.sh`
- Script automatizat pentru backup logic
- Utilizează `pg_dump`
- Creează 4 tipuri de backup:
  - Custom format (comprimat)
  - Plain SQL (gzip)
  - Schema-only
  - Data-only

### 3. Aplicație de Test
**Fișier:** `database/test/DatabaseConnectionTest.java`
- Aplicație standalone Java
- Testează conexiunea la baza de date
- Afișează date din tabele
- Verifică relațiile dintre tabele

### 4. Documentație
**Fișier:** `database/BACKUP_STEPS.md`
- Pașii detaliați pentru fiecare operație
- Comenzi și exemple
- Proceduri de restaurare
- Troubleshooting

---

## 🛠 Instalare și Configurare

### Cerințe Preliminarii

1. **PostgreSQL 14+ (Docker)**
   ```bash
   # PostgreSQL rulează în Docker container
   docker ps | grep postgres
   # Container: morse_postgres
   # Port: 5432
   ```

2. **JDK 11+**
   ```bash
   brew install openjdk@11
   ```

3. **PostgreSQL JDBC Driver**
   ```bash
   # Adaugă în pom.xml sau build.gradle:
   # Maven: org.postgresql:postgresql:42.6.0
   # Gradle: implementation 'org.postgresql:postgresql:42.6.0'
   ```

### Pașii de Instalare (Docker)

```bash
# Metodă automată (Recomandat)
chmod +x database/docker-setup.sh
./database/docker-setup.sh

# SAU manual:

# 1. Creează baza de date în container
docker exec -i morse_postgres psql -U postgres -c "CREATE DATABASE morsemate_db;"

# 2. Execută schema DDL
docker exec -i morse_postgres psql -U postgres -d morsemate_db < database/schema/01_create_tables.sql

# 3. Inserează date de test
docker exec -i morse_postgres psql -U postgres -d morsemate_db < database/schema/02_insert_test_data.sql

# 4. Verifică instalarea
docker exec -i morse_postgres psql -U postgres -d morsemate_db -c "\dt"
```

---

## 🚀 Utilizare

### A. Crearea Backup-ului Fizic

```bash
cd database/backup
./physical_backup.sh
```

**Output:**
- `physical/morsemate_physical_backup_YYYYMMDD_HHMMSS.tar.gz`
- `physical/morsemate_physical_backup_YYYYMMDD_HHMMSS.info`

### B. Crearea Backup-ului Logic

```bash
cd database/backup
./logical_backup.sh
```

**Output:**
- `logical/morsemate_logical_backup_YYYYMMDD_HHMMSS.dump`
- `logical/morsemate_logical_backup_YYYYMMDD_HHMMSS.sql.gz`
- `logical/morsemate_logical_backup_YYYYMMDD_HHMMSS_schema_only.sql.gz`
- `logical/morsemate_logical_backup_YYYYMMDD_HHMMSS_data_only.dump`
- `logical/morsemate_logical_backup_YYYYMMDD_HHMMSS.info`

### C. Rularea Aplicației de Test

#### Opțiunea 1: Compilare și Rulare cu javac

```bash
# Compilare
javac -cp postgresql-42.6.0.jar \
    database/test/DatabaseConnectionTest.java

# Rulare
java -cp "database/test:postgresql-42.6.0.jar" \
    com.morsemate.database.test.DatabaseConnectionTest
```

#### Opțiunea 2: Cu Maven

```bash
# Adaugă dependency în pom.xml
mvn compile
mvn exec:java -Dexec.mainClass="com.morsemate.database.test.DatabaseConnectionTest"
```

#### Opțiunea 3: Cu Gradle

```bash
# Adaugă task în build.gradle
./gradlew testDatabase
```

#### Opțiunea 4: Cu IntelliJ IDEA

1. Deschide `DatabaseConnectionTest.java`
2. Click dreapta pe `main` method
3. Selectează "Run"

---

## 📊 Detalii Tehnice

### Structura Bazei de Date

**Tabele Principale:**
- `users` - 4 utilizatori de test
- `categories` - 6 categorii de învățare
- `lessons` - 11 lecții
- `exercises` - 10 exerciții
- `user_progress` - 6 înregistrări de progres
- `achievements` - 6 realizări
- `power_ups` - 4 power-up-uri
- `leaderboard` - 3 poziții

**Relații:**
- Users ← User Progress → Lessons
- Categories ← Lessons → Exercises
- Users ← Exercise Attempts → Exercises
- Users ← User Achievements → Achievements
- Users ← User Inventory → Power Ups

### Diferențe: Backup Fizic vs Logic

| Caracteristică | Backup Fizic | Backup Logic |
|----------------|--------------|--------------|
| **Metodă** | Copie binară | Dump SQL |
| **Viteză** | ⚡ Foarte rapid | 🐌 Mai lent |
| **Dimensiune** | 📦 Mai mic | 📦 Mai mare |
| **Portabilitate** | ❌ Platformă specifică | ✅ Cross-platform |
| **Versiune** | ❌ Aceeași versiune PG | ✅ Orice versiune |
| **Restaurare Selectivă** | ❌ Nu | ✅ Da |
| **Lizibilitate** | ❌ Binar | ✅ SQL text |
| **Best For** | Disaster recovery | Migrări, Dev/Test |

### Backup Fizic - Avantaje și Dezavantaje

**Avantaje:**
- ✅ Cea mai rapidă metodă de backup
- ✅ Dimensiune mică (comprimat)
- ✅ Point-in-time recovery posibil
- ✅ Include toate bazele de date din cluster

**Dezavantaje:**
- ❌ Dependent de platformă (Linux/macOS/Windows)
- ❌ Necesită aceeași versiune PostgreSQL
- ❌ Restaurare all-or-nothing

### Backup Logic - Avantaje și Dezavantaje

**Avantaje:**
- ✅ Independent de platformă
- ✅ Independent de versiune PostgreSQL
- ✅ Lizibil (format SQL)
- ✅ Restaurare selectivă (tabele individuale)
- ✅ Poate fi editat înainte de restaurare

**Dezavantaje:**
- ❌ Mai lent decât backup fizic
- ❌ Dimensiune mai mare (necomprimat)
- ❌ Necesită mai mult timp pentru restaurare

---

## 🎯 Rezumat pentru Profesor

### Exercițiul 1: Crearea Bazei de Date ✅
- [x] Tabele create: 12 tabele
- [x] Relații definite: Foreign keys, indexes
- [x] Date de test: ~50 înregistrări
- [x] Constraints și triggers implementate

### Exercițiul 2: Backup-uri ✅
- [x] Backup fizic: `physical_backup.sh`
- [x] Backup logic: `logical_backup.sh`
- [x] Metadate generate pentru ambele
- [x] Scripturi automatizate și documentate

### Exercițiul 3: Aplicație de Test ✅
- [x] Conexiune la baza de date testată
- [x] Afișare date din multiple tabele
- [x] Verificare relații și integritate
- [x] Output formatat și informativ

### Exercițiul 4: Documentație ✅
- [x] Pași detaliați pentru backup fizic
- [x] Pași detaliați pentru backup logic
- [x] Comenzi de restaurare
- [x] Troubleshooting și FAQ

---

## 📞 Contact

**Student:** Mert Aydogan
**Email:** aydgnme@example.com
**GitHub:** github.com/aydgnme/PPAW-MorsaMate-Backend
**Proiect:** MorseMate - Morse Code Learning Platform

---


**Document Version:** 1.0
**Last Updated:** 08 Octombrie 2025
