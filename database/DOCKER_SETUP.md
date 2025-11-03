# MorseMate Database - Docker Setup Guide

**Student:** Mert Aydogan
**Project:** MorseMate - Morse Code Learning Platform
**Date:** 08 Octombrie 2025

---

## 🐳 Docker Environment

### Container Information
- **Container Name:** `morse_postgres`
- **Port Mapping:** `5432:5432`
- **Database:** `morsemate_db`
- **User:** `postgres`

---

## 🚀 Quick Start

### 1. Database Setup (One Command)

```bash
chmod +x database/docker-setup.sh
./database/docker-setup.sh
```

This script will:
- ✓ Verify Docker container is running
- ✓ Create `morsemate_db` database
- ✓ Create all 12 tables with relationships
- ✓ Insert test data (~50 records)
- ✓ Verify installation

### 2. Create Logical Backup

```bash
./database/backup/logical_backup.sh
```

**Output:**
- `morsemate_logical_backup_YYYYMMDD_HHMMSS.dump` (51KB)
- `morsemate_logical_backup_YYYYMMDD_HHMMSS.sql.gz` (8KB)
- `morsemate_logical_backup_YYYYMMDD_HHMMSS_schema_only.sql.gz` (3.7KB)
- `morsemate_logical_backup_YYYYMMDD_HHMMSS_data_only.dump` (10KB)
- `morsemate_logical_backup_YYYYMMDD_HHMMSS.info` (metadata)

### 3. Create Physical Backup

```bash
./database/backup/physical_backup.sh
```

**Note:** Physical backup runs inside the Docker container and copies files to host.

### 4. Test Database Connection

```bash
# Compile Java test application
javac -cp postgresql-42.6.0.jar database/test/DatabaseConnectionTest.java

# Create package structure
mkdir -p database/test/com/morsemate/database/test
mv database/test/DatabaseConnectionTest*.class database/test/com/morsemate/database/test/

# Run test
java -cp "database/test:postgresql-42.6.0.jar" com.morsemate.database.test.DatabaseConnectionTest
```

---

## 📋 Manual Database Commands

### Access Database Shell

```bash
docker exec -it morse_postgres psql -U postgres -d morsemate_db
```

### Run SQL Queries

```bash
# List all tables
docker exec -i morse_postgres psql -U postgres -d morsemate_db -c "\dt"

# Count records
docker exec -i morse_postgres psql -U postgres -d morsemate_db -c "SELECT COUNT(*) FROM users;"

# View data
docker exec -i morse_postgres psql -U postgres -d morsemate_db -c "SELECT * FROM users;"
```

### Execute SQL Files

```bash
docker exec -i morse_postgres psql -U postgres -d morsemate_db < your_script.sql
```

---

## 🔄 Backup & Restore

### Backup Operations

All backup scripts automatically work with Docker:

```bash
# Logical backup (recommended for portability)
./database/backup/logical_backup.sh

# Physical backup (faster, larger files)
./database/backup/physical_backup.sh
```

### Restore from Backup

#### Logical Backup Restore

```bash
# 1. Drop existing database (if needed)
docker exec -i morse_postgres psql -U postgres -c "DROP DATABASE morsemate_db;"

# 2. Create new database
docker exec -i morse_postgres psql -U postgres -c "CREATE DATABASE morsemate_db;"

# 3. Restore from custom format
docker exec -i morse_postgres pg_restore -U postgres -d morsemate_db /path/to/backup.dump

# OR restore from SQL file
gunzip -c backup.sql.gz | docker exec -i morse_postgres psql -U postgres -d morsemate_db
```

#### Physical Backup Restore

```bash
# Stop container
docker stop morse_postgres

# Extract backup to container's data directory
docker start morse_postgres
```

---

## 📊 Database Structure

### Tables Created (12 total)

1. **users** - User accounts (4 test users)
2. **categories** - Learning categories (6 categories)
3. **lessons** - Lesson content (11 lessons)
4. **exercises** - Practice exercises (11 exercises)
5. **user_progress** - Progress tracking (6 records)
6. **exercise_attempts** - User attempts (9 records)
7. **achievements** - Achievement definitions (6 achievements)
8. **user_achievements** - User earned achievements (3 records)
9. **gem_transactions** - Virtual currency (6 transactions)
10. **power_ups** - Power-up items (4 items)
11. **user_inventory** - User items (3 records)
12. **leaderboard** - Global leaderboard (3 positions)

### Relationships

- Users ← User Progress → Lessons
- Categories ← Lessons → Exercises
- Users ← Exercise Attempts → Exercises
- Users ← User Achievements → Achievements
- Users ← User Inventory → Power Ups

---

## 🧪 Testing

### Connection Test Output

```
╔═══════════════════════════════════════════════════╗
║     MorseMate Database Connection Test Tool      ║
║                                                   ║
║  Purpose: Test database connectivity and         ║
║           display test data from tables          ║
╚═══════════════════════════════════════════════════╝

✓ PostgreSQL JDBC Driver loaded successfully

========================================
Testing Database Connection
========================================
URL: jdbc:postgresql://localhost:5432/morsemate_db
User: postgres

✓ Connection successful!

Database Information:
  Product: PostgreSQL
  Version: 15.14
  Driver: PostgreSQL JDBC Driver
  Driver Version: 42.6.0

[... displays users, categories, lessons, and progress ...]

========================================
Test Completed Successfully!
========================================

All database operations completed without errors.
Connection test: PASSED
Data retrieval: SUCCESS
```

---

## 🛠️ Troubleshooting

### Container Not Running

```bash
# Check container status
docker ps -a | grep morse_postgres

# Start container
docker start morse_postgres

# Check logs
docker logs morse_postgres
```

### Connection Refused

```bash
# Verify port mapping
docker port morse_postgres

# Should show: 5432/tcp -> 0.0.0.0:5432
```

### Database Not Found

```bash
# List databases
docker exec -i morse_postgres psql -U postgres -c "\l"

# Recreate database
./database/docker-setup.sh
```

### Permission Denied

```bash
# Make scripts executable
chmod +x database/docker-setup.sh
chmod +x database/backup/physical_backup.sh
chmod +x database/backup/logical_backup.sh
```

---

## 📁 File Structure

```
database/
├── docker-setup.sh              # Automated Docker setup
├── backup/
│   ├── physical_backup.sh       # Physical backup (Docker-compatible)
│   ├── logical_backup.sh        # Logical backup (Docker-compatible)
│   ├── physical/                # Physical backup files
│   └── logical/                 # Logical backup files
├── schema/
│   ├── 01_create_tables.sql     # DDL script
│   └── 02_insert_test_data.sql  # Test data
├── test/
│   └── DatabaseConnectionTest.java  # Java JDBC test app
├── README.md                    # Main documentation
├── BACKUP_STEPS.md              # Detailed backup guide
└── DOCKER_SETUP.md              # This file
```

---

## ✅ Assignment Deliverables

### Exercise 1: Database Creation ✓
- [x] 12 tables with relationships
- [x] Foreign keys and constraints
- [x] Indexes and triggers
- [x] Test data loaded

### Exercise 2: Backups ✓
- [x] Physical backup script (Docker-compatible)
- [x] Logical backup script (Docker-compatible)
- [x] Backup files generated
- [x] Metadata files created

### Exercise 3: Test Application ✓
- [x] Java JDBC application
- [x] Connection test successful
- [x] Data retrieval working
- [x] All tables queried

### Exercise 4: Documentation ✓
- [x] README.md (Romanian)
- [x] BACKUP_STEPS.md (detailed guide)
- [x] DOCKER_SETUP.md (this file)

---

## 📝 Notes

- All scripts are Docker-compatible
- Backups are created on host machine (not inside container)
- Connection from Java app works via localhost:5432
- PostgreSQL 15.14 running in container
- JDBC Driver: postgresql-42.6.0.jar

---

## 🎯 Success Confirmation

```bash
# Verify everything is working:

# 1. Check database
docker exec -i morse_postgres psql -U postgres -d morsemate_db -c "SELECT COUNT(*) FROM users;"
# Expected: 4

# 2. Check backups
ls -lh database/backup/logical/
# Should show backup files

# 3. Run test application
java -cp "database/test:postgresql-42.6.0.jar" com.morsemate.database.test.DatabaseConnectionTest
# Expected: Test Completed Successfully!
```

---

**All requirements completed successfully! ✓**

**Document Version:** 1.0
**Last Updated:** 08 Octombrie 2025
