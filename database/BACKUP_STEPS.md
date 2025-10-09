# MorseMate Database Backup Guide

**Proiect:** MorseMate - Morse Code Learning Platform
**Student:** Mert Aydogan
**Data:** 08 Octombrie 2025

---

## Table of Contents
1. [Database Schema Creation](#1-database-schema-creation)
2. [Physical Backup](#2-physical-backup)
3. [Logical Backup](#3-logical-backup)
4. [Database Connection Testing](#4-database-connection-testing)
5. [Restoration Procedures](#5-restoration-procedures)

---

## 1. Database Schema Creation

### 1.1 Prerequisites
- PostgreSQL 14+ installed
- Database user with CREATE privileges
- psql command-line tool

### 1.2 Steps to Create Database

```bash
# Step 1: Create database
createdb -U postgres morsemate_db

# Step 2: Connect to database
psql -U postgres -d morsemate_db

# Step 3: Execute schema creation script
psql -U postgres -d morsemate_db -f database/schema/01_create_tables.sql

# Step 4: Insert test data
psql -U postgres -d morsemate_db -f database/schema/02_insert_test_data.sql
```

### 1.3 Verification

```sql
-- Check all tables
\dt

-- Verify row counts
SELECT
    'users' as table_name, COUNT(*) as row_count FROM users
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL
SELECT 'exercises', COUNT(*) FROM exercises
UNION ALL
SELECT 'user_progress', COUNT(*) FROM user_progress;
```

### 1.4 Database Structure

**Tables Created:**
- `users` - User accounts and authentication
- `categories` - Learning categories
- `lessons` - Lesson content
- `exercises` - Practice exercises
- `user_progress` - Progress tracking
- `exercise_attempts` - User exercise attempts
- `achievements` - Achievement definitions
- `user_achievements` - User earned achievements
- `gem_transactions` - Virtual currency transactions
- `power_ups` - Power-up items
- `user_inventory` - User items inventory
- `leaderboard` - Global leaderboard

**Total Tables:** 12
**Total Indexes:** 25+
**Total Triggers:** 5

---

## 2. Physical Backup

### 2.1 What is Physical Backup?

Physical backup creates a **binary copy** of the entire database cluster, including:
- All database files
- Transaction logs (WAL files)
- Configuration files
- Complete data directory structure

**Advantages:**
- ✓ Fastest backup method
- ✓ Smallest backup size (compressed)
- ✓ Point-in-time recovery possible
- ✓ Includes all databases in cluster

**Disadvantages:**
- ✗ Platform-dependent
- ✗ Requires same PostgreSQL version
- ✗ All-or-nothing restore

### 2.2 Steps for Physical Backup

#### Method 1: Using pg_basebackup (Recommended)

```bash
# Step 1: Navigate to backup directory
cd database/backup

# Step 2: Run physical backup script
./physical_backup.sh

# Script will:
# - Create timestamped backup
# - Use pg_basebackup utility
# - Compress backup with tar+gzip
# - Generate metadata file
```

#### Manual Physical Backup:

```bash
# Stop PostgreSQL (required for manual backup)
pg_ctl stop -D /usr/local/var/postgres

# Create backup directory
mkdir -p backup/physical/$(date +%Y%m%d)

# Copy entire data directory
cp -R /usr/local/var/postgres backup/physical/$(date +%Y%m%d)/

# Compress backup
tar -czf backup/physical/morsemate_physical_$(date +%Y%m%d).tar.gz \
    backup/physical/$(date +%Y%m%d)/

# Start PostgreSQL
pg_ctl start -D /usr/local/var/postgres
```

### 2.3 Physical Backup Output

**Files Generated:**
- `morsemate_physical_backup_YYYYMMDD_HHMMSS.tar.gz` - Compressed backup
- `morsemate_physical_backup_YYYYMMDD_HHMMSS.info` - Metadata file

**Metadata Contains:**
- Backup timestamp
- Database name and connection details
- Backup size
- Restoration commands

### 2.4 Verification

```bash
# Check backup file exists
ls -lh database/backup/physical/*.tar.gz

# Verify backup integrity
tar -tzf database/backup/physical/morsemate_physical_*.tar.gz | head

# Read metadata
cat database/backup/physical/morsemate_physical_*.info
```

---

## 3. Logical Backup

### 3.1 What is Logical Backup?

Logical backup creates **SQL dump files** containing:
- CREATE TABLE statements
- INSERT statements with data
- Index definitions
- Constraints and triggers
- Human-readable SQL

**Advantages:**
- ✓ Platform-independent
- ✓ Version-independent
- ✓ Human-readable (SQL format)
- ✓ Selective restore possible
- ✓ Can restore individual tables

**Disadvantages:**
- ✗ Slower than physical backup
- ✗ Larger backup size (uncompressed)
- ✗ Requires more restore time

### 3.2 Steps for Logical Backup

#### Method 1: Using pg_dump (Automated)

```bash
# Step 1: Navigate to backup directory
cd database/backup

# Step 2: Run logical backup script
./logical_backup.sh

# Script will create 4 backup files:
# 1. Custom format (compressed)
# 2. Plain SQL (gzipped)
# 3. Schema-only
# 4. Data-only
```

#### Method 2: Manual pg_dump

```bash
# Complete database dump (custom format)
pg_dump -U postgres -d morsemate_db \
    --format=custom \
    --compress=9 \
    --file=morsemate_backup.dump

# Plain SQL format (human-readable)
pg_dump -U postgres -d morsemate_db \
    --format=plain \
    --file=morsemate_backup.sql

# Compress SQL file
gzip morsemate_backup.sql
```

### 3.3 Logical Backup Output

**Files Generated:**
1. **morsemate_logical_backup_YYYYMMDD_HHMMSS.dump**
   - Custom format, compressed
   - Best for full restoration

2. **morsemate_logical_backup_YYYYMMDD_HHMMSS.sql.gz**
   - Plain SQL, gzipped
   - Human-readable
   - Can edit before restore

3. **morsemate_logical_backup_YYYYMMDD_HHMMSS_schema_only.sql.gz**
   - Database structure only
   - No data

4. **morsemate_logical_backup_YYYYMMDD_HHMMSS_data_only.dump**
   - Data only
   - No structure

5. **morsemate_logical_backup_YYYYMMDD_HHMMSS.info**
   - Metadata file
   - Restoration instructions

### 3.4 Verification

```bash
# List all backup files
ls -lh database/backup/logical/

# View SQL backup (first 50 lines)
gunzip -c database/backup/logical/*_backup_*.sql.gz | head -50

# Check backup metadata
cat database/backup/logical/*_backup_*.info

# Verify backup integrity
pg_restore --list database/backup/logical/*_backup_*.dump | head -20
```

---

## 4. Database Connection Testing

### 4.1 Test Application Overview

The test application is a standalone Kotlin program that:
- Tests database connectivity
- Displays data from multiple tables
- Verifies relationships
- Shows database metadata

### 4.2 Running the Test Application

#### Prerequisites:
```bash
# Install PostgreSQL JDBC Driver
# Maven (pom.xml):
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>

# Gradle (build.gradle):
dependencies {
    implementation 'org.postgresql:postgresql:42.6.0'
}
```

#### Compilation:
```bash
# Navigate to project root
cd /Users/aydgn.me/Developer/PPAW/MorseMate

# Compile test application
javac -cp postgresql-42.6.0.jar \
    database/test/DatabaseConnectionTest.java
```

#### Execution:
```bash
# Run test application
java -cp "database/test:postgresql-42.6.0.jar" \
    com.morsemate.database.test.DatabaseConnectionTest
```

#### Alternative (Using Maven):
```bash
# Run with Maven
mvn compile
mvn exec:java -Dexec.mainClass="com.morsemate.database.test.DatabaseConnectionTest"
```

#### Alternative (Using Gradle):
```groovy
// Add to build.gradle
tasks.register('testDatabase', JavaExec) {
    mainClass = 'com.morsemate.database.test.DatabaseConnectionTest'
    classpath = sourceSets.main.runtimeClasspath
}

// Run with:
./gradlew testDatabase
```

### 4.3 Expected Output

```
╔═══════════════════════════════════════════════════╗
║     MorseMate Database Connection Test Tool      ║
║                                                   ║
║  Purpose: Test database connectivity and         ║
║           display test data from tables          ║
╚═══════════════════════════════════════════════════╝

========================================
Testing Database Connection
========================================
URL: jdbc:postgresql://localhost:5432/morsemate_db
User: postgres

✓ PostgreSQL JDBC Driver loaded successfully
✓ Connection successful!

Database Information:
  Product: PostgreSQL
  Version: 14.x
  Driver: PostgreSQL JDBC Driver
  Driver Version: 42.6.0

========================================
USERS TABLE
========================================

User #1:
  Username: admin
  Email: admin@morsemate.com
  Full Name: Admin User
  Role: ADMIN
  Total Gems: 1000
  Total XP: 5000
  Current Streak: 10 days
  Created: 2025-10-08 ...

[... more output ...]

========================================
Test Completed Successfully!
========================================
```

### 4.4 Troubleshooting

**Problem:** Connection refused
```bash
# Solution: Check PostgreSQL is running
pg_ctl status
# Or
brew services list | grep postgresql
```

**Problem:** Authentication failed
```bash
# Solution: Check pg_hba.conf
# Edit: /usr/local/var/postgres/pg_hba.conf
# Add: host all all 127.0.0.1/32 md5
# Reload: pg_ctl reload
```

**Problem:** Database not found
```bash
# Solution: Create database
createdb -U postgres morsemate_db
```

---

## 5. Restoration Procedures

### 5.1 Restore from Physical Backup

```bash
# Step 1: Stop PostgreSQL
pg_ctl stop -D /usr/local/var/postgres

# Step 2: Extract backup
tar -xzf database/backup/physical/morsemate_physical_*.tar.gz \
    -C /usr/local/var/postgres

# Step 3: Set permissions
chmod 700 /usr/local/var/postgres

# Step 4: Start PostgreSQL
pg_ctl start -D /usr/local/var/postgres
```

### 5.2 Restore from Logical Backup

#### Option 1: Custom Format
```bash
# Create new database
createdb -U postgres morsemate_db_restored

# Restore from custom format
pg_restore -U postgres \
    -d morsemate_db_restored \
    database/backup/logical/morsemate_logical_*.dump
```

#### Option 2: SQL Format
```bash
# Create new database
createdb -U postgres morsemate_db_restored

# Restore from SQL file
gunzip -c database/backup/logical/morsemate_logical_*.sql.gz | \
    psql -U postgres -d morsemate_db_restored
```

#### Option 3: Schema + Data Separately
```bash
# Step 1: Create database
createdb -U postgres morsemate_db_restored

# Step 2: Restore schema
gunzip -c database/backup/logical/*_schema_only.sql.gz | \
    psql -U postgres -d morsemate_db_restored

# Step 3: Restore data
pg_restore -U postgres \
    -d morsemate_db_restored \
    database/backup/logical/*_data_only.dump
```

### 5.3 Verification After Restore

```sql
-- Connect to restored database
psql -U postgres -d morsemate_db_restored

-- Verify table count
SELECT COUNT(*) FROM information_schema.tables
WHERE table_schema = 'public';

-- Verify data count
SELECT 'users' as table, COUNT(*) FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons;

-- Check last update timestamps
SELECT MAX(created_at) as last_created,
       MAX(updated_at) as last_updated
FROM users;
```

---

## Appendix A: File Structure

```
database/
├── schema/
│   ├── 01_create_tables.sql       # DDL script
│   └── 02_insert_test_data.sql    # Test data
├── backup/
│   ├── physical_backup.sh         # Physical backup script
│   ├── logical_backup.sh          # Logical backup script
│   ├── physical/                  # Physical backups
│   │   ├── *.tar.gz              # Compressed backups
│   │   └── *.info                # Metadata files
│   └── logical/                   # Logical backups
│       ├── *.dump                # Custom format
│       ├── *.sql.gz              # SQL format
│       ├── *_schema_only.sql.gz  # Schema only
│       ├── *_data_only.dump      # Data only
│       └── *.info                # Metadata files
└── test/
    └── DatabaseConnectionTest.java # Connection test app
```

---

## Appendix B: Backup Comparison

| Feature | Physical Backup | Logical Backup |
|---------|----------------|----------------|
| Speed | ⚡ Very Fast | 🐌 Slower |
| Size | 📦 Smaller (compressed) | 📦 Larger |
| Platform Independence | ❌ No | ✅ Yes |
| Version Independence | ❌ No | ✅ Yes |
| Selective Restore | ❌ No | ✅ Yes |
| Human Readable | ❌ No | ✅ Yes (SQL) |
| Point-in-time Recovery | ✅ Yes | ❌ No |
| Best For | Full system backup | Migrations, Selective restore |

---

## Appendix C: Automation

### Automated Daily Backups

```bash
# Add to crontab (crontab -e)

# Daily logical backup at 2 AM
0 2 * * * /path/to/database/backup/logical_backup.sh >> /var/log/morsemate_backup.log 2>&1

# Weekly physical backup on Sunday at 3 AM
0 3 * * 0 /path/to/database/backup/physical_backup.sh >> /var/log/morsemate_backup.log 2>&1

# Delete backups older than 30 days
0 4 * * * find /path/to/database/backup -name "*.tar.gz" -mtime +30 -delete
```

---

## Contact Information

**Student:** Mert Aydogan
**Email:** mert.aydogan1@student.usv.ro
**Project:** MorseMate
**Repository:** github.com/aydgnme/PPAW-MorsaMate-Backend

---

**Document Version:** 1.0
**Last Updated:** 08 Octombrie 2025
