#!/bin/bash

# ============================================================================
# MorseMate Logical Backup Script (Homebrew PostgreSQL)
# ============================================================================
# For PostgreSQL installed via Homebrew on macOS
# ============================================================================

# Configuration
BACKUP_DIR="$(dirname "$0")/logical"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="morsemate_logical_backup_${TIMESTAMP}"

# PostgreSQL connection details
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="morse_code_db"
DB_USER="morsemate"
POSTGRES_BIN="/opt/homebrew/opt/postgresql@15/bin"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "========================================="
echo "MorseMate Logical Backup (Homebrew)"
echo "========================================="
echo "Timestamp: $TIMESTAMP"
echo "Database: $DB_NAME"
echo "Backup Directory: $BACKUP_DIR"
echo ""

# Method 1: pg_dump (Complete database dump - custom format)
echo "Creating logical backup using pg_dump (custom format)..."
"$POSTGRES_BIN/pg_dump" \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --verbose \
    --format=custom \
    --compress=9 \
    --file="$BACKUP_DIR/${BACKUP_NAME}.dump"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Logical backup (custom format) completed successfully!"
    BACKUP_SIZE=$(du -sh "$BACKUP_DIR/${BACKUP_NAME}.dump" | cut -f1)
    echo "Backup size: $BACKUP_SIZE"
else
    echo ""
    echo "✗ Logical backup failed!"
    exit 1
fi

# Method 2: pg_dump (Plain SQL format - human readable)
echo ""
echo "Creating plain SQL backup..."
"$POSTGRES_BIN/pg_dump" \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --format=plain \
    --file="$BACKUP_DIR/${BACKUP_NAME}.sql"

if [ $? -eq 0 ]; then
    # Compress the SQL file
    gzip "$BACKUP_DIR/${BACKUP_NAME}.sql"
    echo "✓ Plain SQL backup created and compressed"
    SQL_SIZE=$(du -sh "$BACKUP_DIR/${BACKUP_NAME}.sql.gz" | cut -f1)
    echo "SQL backup size: $SQL_SIZE"
fi

# Method 3: Schema-only backup (structure without data)
echo ""
echo "Creating schema-only backup..."
"$POSTGRES_BIN/pg_dump" \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --schema-only \
    --format=plain \
    --file="$BACKUP_DIR/${BACKUP_NAME}_schema_only.sql"

if [ $? -eq 0 ]; then
    gzip "$BACKUP_DIR/${BACKUP_NAME}_schema_only.sql"
    echo "✓ Schema-only backup created"
fi

# Method 4: Data-only backup (data without structure)
echo ""
echo "Creating data-only backup..."
"$POSTGRES_BIN/pg_dump" \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --data-only \
    --format=custom \
    --compress=9 \
    --file="$BACKUP_DIR/${BACKUP_NAME}_data_only.dump"

if [ $? -eq 0 ]; then
    echo "✓ Data-only backup created"
fi

# Create metadata file
cat > "$BACKUP_DIR/${BACKUP_NAME}.info" <<EOF
========================================
MorseMate Logical Backup Information
========================================
Backup Type: Logical (pg_dump)
Database: $DB_NAME
Host: $DB_HOST
Port: $DB_PORT
User: $DB_USER
Timestamp: $TIMESTAMP
PostgreSQL: Homebrew installation

Files Created:
- ${BACKUP_NAME}.dump (custom format, compressed)
- ${BACKUP_NAME}.sql.gz (plain SQL, compressed)
- ${BACKUP_NAME}_schema_only.sql.gz (schema only)
- ${BACKUP_NAME}_data_only.dump (data only)

Backup Sizes:
- Custom format: $(du -sh "$BACKUP_DIR/${BACKUP_NAME}.dump" 2>/dev/null | cut -f1 || echo "N/A")
- SQL format: $(du -sh "$BACKUP_DIR/${BACKUP_NAME}.sql.gz" 2>/dev/null | cut -f1 || echo "N/A")
- Schema only: $(du -sh "$BACKUP_DIR/${BACKUP_NAME}_schema_only.sql.gz" 2>/dev/null | cut -f1 || echo "N/A")
- Data only: $(du -sh "$BACKUP_DIR/${BACKUP_NAME}_data_only.dump" 2>/dev/null | cut -f1 || echo "N/A")

========================================
Restore Commands:
========================================

1. Restore from custom format:
   /opt/homebrew/opt/postgresql@15/bin/pg_restore -h localhost -U morsemate -d morse_code_db ${BACKUP_NAME}.dump

2. Restore from SQL file:
   gunzip -c ${BACKUP_NAME}.sql.gz | /opt/homebrew/opt/postgresql@15/bin/psql -h localhost -U morsemate -d morse_code_db

3. Restore schema only:
   gunzip -c ${BACKUP_NAME}_schema_only.sql.gz | /opt/homebrew/opt/postgresql@15/bin/psql -h localhost -U morsemate -d morse_code_db

4. Restore data only:
   /opt/homebrew/opt/postgresql@15/bin/pg_restore -h localhost -U morsemate -d morse_code_db ${BACKUP_NAME}_data_only.dump

========================================
EOF

echo ""
echo "✓ Metadata saved to: $BACKUP_DIR/${BACKUP_NAME}.info"

echo ""
echo "========================================="
echo "Backup Summary"
echo "========================================="
echo "Backup directory: $BACKUP_DIR"
echo ""
echo "Files created:"
ls -lh "$BACKUP_DIR/${BACKUP_NAME}"* 2>/dev/null || echo "Error listing files"
echo ""
echo "To view metadata:"
echo "  cat $BACKUP_DIR/${BACKUP_NAME}.info"
echo "========================================="
