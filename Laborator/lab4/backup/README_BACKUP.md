# MorseMate - Database Backup Instructions
## Laborator 4 - PPAW

**Data Backup:** 2025-10-22
**Database:** morse_code_db (PostgreSQL 15.14)
**Total Size:** ~69 KB

---

## Fișiere Backup

### 1. morse_code_db_full_backup.sql (35 KB)
**Conținut:** Schema completă + Date
**Comenzi incluse:** DROP, CREATE, INSERT

**Restore:**
```bash
# Drop existing database (ATENȚIE: șterge totul!)
dropdb -U postgres morse_code_db

# Restore full backup (recreează database + schema + data)
psql -U postgres -f morse_code_db_full_backup.sql

# SAU cu create database
psql -U postgres -f morse_code_db_full_backup.sql postgres
```

---

### 2. morse_code_db_schema_only.sql (26 KB)
**Conținut:** Doar schema (tabele, indexuri, constrângeri)
**Comenzi incluse:** CREATE TABLE, CREATE INDEX, ALTER TABLE

**Restore:**
```bash
# Creare bază de date goală
createdb -U postgres morse_code_db

# Restore schema only
psql -U postgres -d morse_code_db -f morse_code_db_schema_only.sql
```

**Use Case:**
- Creare environment de development nou
- Testing cu date fresh
- Migrare la alt server (fără date de producție)

---

### 3. morse_code_db_data_only.sql (8.3 KB)
**Conținut:** Doar date (INSERT statements)
**Comenzi incluse:** INSERT, COPY (dacă există date mari)

**Restore:**
```bash
# Presupune că schema există deja
psql -U postgres -d morse_code_db -f morse_code_db_data_only.sql
```

**Use Case:**
- Restore date după schema update
- Populare bază de date de test
- Seed data pentru development

---

## Procedură Completă Restore

### Opțiune 1: Full Restore (Recomandat pentru Recovery)
```bash
# Step 1: Drop existing database
dropdb -U postgres morse_code_db

# Step 2: Restore full backup (recrează automat database)
psql -U postgres -f morse_code_db_full_backup.sql

# Step 3: Verify
psql -U postgres -d morse_code_db -c "\dt"
psql -U postgres -d morse_code_db -c "SELECT COUNT(*) FROM users;"
```

### Opțiune 2: Schema + Data Separate
```bash
# Step 1: Create empty database
createdb -U postgres morse_code_db

# Step 2: Restore schema
psql -U postgres -d morse_code_db -f morse_code_db_schema_only.sql

# Step 3: Restore data
psql -U postgres -d morse_code_db -f morse_code_db_data_only.sql

# Step 4: Verify
psql -U postgres -d morse_code_db -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"
```

### Opțiune 3: Selective Restore (Doar anumite tabele)
```bash
# Extract specific table from full backup
pg_restore -U postgres -d morse_code_db -t users morse_code_db_full_backup.sql

# SAU manual din data_only.sql
# Edit file și păstrează doar INSERT-urile pentru tabelele dorite
```

---

## Verificare Post-Restore

### 1. Check Tables
```sql
-- Lista toate tabelele
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- Expected: 16 tables
```

### 2. Check Data
```sql
-- Count records in each table
SELECT 'users' AS table, COUNT(*) FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL SELECT 'exercises', COUNT(*) FROM exercises
UNION ALL SELECT 'achievements', COUNT(*) FROM achievements
UNION ALL SELECT 'power_ups', COUNT(*) FROM power_ups
UNION ALL SELECT 'subscription_plans', COUNT(*) FROM subscription_plans
UNION ALL SELECT 'promo_codes', COUNT(*) FROM promo_codes
UNION ALL SELECT 'user_progress', COUNT(*) FROM user_progress
UNION ALL SELECT 'exercise_attempts', COUNT(*) FROM exercise_attempts
UNION ALL SELECT 'user_achievements', COUNT(*) FROM user_achievements
UNION ALL SELECT 'user_gems', COUNT(*) FROM user_gems
UNION ALL SELECT 'gem_transactions', COUNT(*) FROM gem_transactions
UNION ALL SELECT 'user_power_ups', COUNT(*) FROM user_power_ups
UNION ALL SELECT 'user_subscriptions', COUNT(*) FROM user_subscriptions
UNION ALL SELECT 'payments', COUNT(*) FROM payments;
```

**Expected Output (la data backup-ului):**
```
table      | count
-----------|------
users      |   6
categories |   0
...        | ...
```

### 3. Check Constraints
```sql
-- Verify foreign keys
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;

-- Expected: 24 foreign keys
```

### 4. Check Indexes
```sql
-- List all indexes
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- Expected: ~42 indexes
```

---

## Backup Automation (Script)

### create_backup.sh
```bash
#!/bin/bash

# Configuration
DB_NAME="morse_code_db"
DB_USER="postgres"
BACKUP_DIR="/Users/aydgn/Developer/Dev/PPAW/MorseMate/Laborator/lab4/backup"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Create backup directory if not exists
mkdir -p "$BACKUP_DIR"

# Full backup
pg_dump -U "$DB_USER" -d "$DB_NAME" \
    --clean --if-exists --create \
    > "$BACKUP_DIR/${DB_NAME}_full_${TIMESTAMP}.sql"

# Schema only
pg_dump -U "$DB_USER" -d "$DB_NAME" \
    --schema-only \
    > "$BACKUP_DIR/${DB_NAME}_schema_${TIMESTAMP}.sql"

# Data only
pg_dump -U "$DB_USER" -d "$DB_NAME" \
    --data-only \
    > "$BACKUP_DIR/${DB_NAME}_data_${TIMESTAMP}.sql"

# Compressed backup (pentru backup-uri mari)
pg_dump -U "$DB_USER" -d "$DB_NAME" -Fc \
    > "$BACKUP_DIR/${DB_NAME}_compressed_${TIMESTAMP}.dump"

echo "Backup completed: $TIMESTAMP"
echo "Files created:"
ls -lh "$BACKUP_DIR"/*${TIMESTAMP}*
```

**Usage:**
```bash
chmod +x create_backup.sh
./create_backup.sh
```

---

## Restore Automation (Script)

### restore_backup.sh
```bash
#!/bin/bash

# Configuration
DB_NAME="morse_code_db"
DB_USER="postgres"
BACKUP_FILE="$1"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: ./restore_backup.sh <backup_file>"
    exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "WARNING: This will drop the existing database!"
read -p "Are you sure? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

# Drop existing database
echo "Dropping existing database..."
dropdb -U "$DB_USER" "$DB_NAME" 2>/dev/null || true

# Restore from backup
echo "Restoring from backup: $BACKUP_FILE"
psql -U "$DB_USER" -f "$BACKUP_FILE"

# Verify
echo "Verification:"
psql -U "$DB_USER" -d "$DB_NAME" -c "\dt"
psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT COUNT(*) AS user_count FROM users;"

echo "Restore completed successfully!"
```

**Usage:**
```bash
chmod +x restore_backup.sh
./restore_backup.sh morse_code_db_full_backup.sql
```

---

## Troubleshooting

### Problem 1: Role "postgres" does not exist
```bash
# Solution
psql -U aydgn -d postgres -c "CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD 'postgres';"
```

### Problem 2: Database already exists
```bash
# Solution 1: Drop existing
dropdb -U postgres morse_code_db

# Solution 2: Use different name
createdb -U postgres morse_code_db_restored
psql -U postgres -d morse_code_db_restored -f backup.sql
```

### Problem 3: Permission denied
```bash
# Solution: Check file permissions
chmod 644 morse_code_db_full_backup.sql

# Check user permissions
psql -U postgres -c "\du"
```

### Problem 4: Out of memory during restore
```bash
# Solution: Use compressed format
pg_dump -U postgres -d morse_code_db -Fc > backup.dump
pg_restore -U postgres -d morse_code_db_new backup.dump
```

---

## Best Practices

### 1. Regular Backups
✅ **Daily:** Automated full backup
✅ **Weekly:** Archived backup (keep for 1 month)
✅ **Before migrations:** Manual backup
✅ **Before deployments:** Manual backup

### 2. Backup Storage
✅ **Local:** Development backups
✅ **Remote:** Production backups (AWS S3, Google Cloud Storage)
✅ **Encrypted:** Sensitive data
✅ **Version control:** Schema-only backups (Git)

### 3. Testing Restores
✅ **Monthly:** Test restore procedure
✅ **After changes:** Verify backup integrity
✅ **New environment:** Use schema + seed data

### 4. Monitoring
✅ **Backup size:** Track growth
✅ **Backup time:** Optimize for large databases
✅ **Verify checksums:** Ensure data integrity

---

## Informații Backup Curent

**Data:** 2025-10-22 19:22
**Database Version:** PostgreSQL 15.14
**Total Tables:** 16
**Total Data:** 6 users, 0 în alte tabele
**Schema Size:** 26 KB
**Data Size:** 8.3 KB
**Total Size:** 35 KB (full backup)

**Conținut:**
- ✅ Schema completă (toate 16 tabele)
- ✅ Indexuri (42 indexuri)
- ✅ Constrângeri (28 constrângeri)
- ✅ Foreign Keys (24 relații)
- ✅ Data (6 utilizatori)

**Checksum:**
```bash
md5 morse_code_db_full_backup.sql
# MD5 (morse_code_db_full_backup.sql) = <hash_value>
```

---

**Autor:** Aydogan Mert
**Data:** 2025-10-22
**Versiune:** 1.0
