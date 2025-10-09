#!/bin/bash

# ============================================================================
# MorseMate Logical Backup Script
# ============================================================================
# Logical backup creates SQL dump files that can be used to recreate
# the database structure and data on any PostgreSQL server
# ============================================================================

# Configuration
BACKUP_DIR="$(dirname "$0")/logical"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="morsemate_logical_backup_${TIMESTAMP}"
CONTAINER_NAME="${POSTGRES_CONTAINER:-morse_postgres}"

# PostgreSQL connection details
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="morsemate_db"
DB_USER="${POSTGRES_USER:-postgres}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "========================================="
echo "MorseMate Logical Backup"
echo "========================================="
echo "Timestamp: $TIMESTAMP"
echo "Database: $DB_NAME"
echo "Backup Directory: $BACKUP_DIR"
echo ""

# Method 1: pg_dump (Complete database dump)
echo "Creating logical backup using pg_dump..."
echo "Note: Running inside Docker container..."
echo ""

docker exec -i $CONTAINER_NAME pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --verbose \
    --format=custom \
    --compress=9 \
    --file="/tmp/${BACKUP_NAME}.dump"

# Copy from container to host
docker cp "$CONTAINER_NAME:/tmp/${BACKUP_NAME}.dump" "$BACKUP_DIR/${BACKUP_NAME}.dump"
docker exec $CONTAINER_NAME rm -f "/tmp/${BACKUP_NAME}.dump"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Logical backup (custom format) completed successfully!"

    # Calculate backup size
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

docker exec -i $CONTAINER_NAME pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --format=plain \
    --file="/tmp/${BACKUP_NAME}.sql"

if [ $? -eq 0 ]; then
    # Copy from container to host
    docker cp "$CONTAINER_NAME:/tmp/${BACKUP_NAME}.sql" "$BACKUP_DIR/${BACKUP_NAME}.sql"
    docker exec $CONTAINER_NAME rm -f "/tmp/${BACKUP_NAME}.sql"

    # Compress the SQL file
    gzip "$BACKUP_DIR/${BACKUP_NAME}.sql"
    echo "✓ Plain SQL backup created and compressed"

    SQL_SIZE=$(du -sh "$BACKUP_DIR/${BACKUP_NAME}.sql.gz" | cut -f1)
    echo "SQL backup size: $SQL_SIZE"
fi

# Method 3: Schema-only backup (structure without data)
echo ""
echo "Creating schema-only backup..."

docker exec -i $CONTAINER_NAME pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --schema-only \
    --format=plain \
    --file="/tmp/${BACKUP_NAME}_schema_only.sql"

if [ $? -eq 0 ]; then
    docker cp "$CONTAINER_NAME:/tmp/${BACKUP_NAME}_schema_only.sql" "$BACKUP_DIR/${BACKUP_NAME}_schema_only.sql"
    docker exec $CONTAINER_NAME rm -f "/tmp/${BACKUP_NAME}_schema_only.sql"
    gzip "$BACKUP_DIR/${BACKUP_NAME}_schema_only.sql"
    echo "✓ Schema-only backup created"
fi

# Method 4: Data-only backup (data without structure)
echo ""
echo "Creating data-only backup..."

docker exec -i $CONTAINER_NAME pg_dump \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d "$DB_NAME" \
    --data-only \
    --format=custom \
    --compress=9 \
    --file="/tmp/${BACKUP_NAME}_data_only.dump"

if [ $? -eq 0 ]; then
    docker cp "$CONTAINER_NAME:/tmp/${BACKUP_NAME}_data_only.dump" "$BACKUP_DIR/${BACKUP_NAME}_data_only.dump"
    docker exec $CONTAINER_NAME rm -f "/tmp/${BACKUP_NAME}_data_only.dump"
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
   pg_restore -h localhost -U postgres -d morsemate_db ${BACKUP_NAME}.dump

2. Restore from SQL file:
   gunzip -c ${BACKUP_NAME}.sql.gz | psql -h localhost -U postgres -d morsemate_db

3. Restore schema only:
   gunzip -c ${BACKUP_NAME}_schema_only.sql.gz | psql -h localhost -U postgres -d morsemate_db

4. Restore data only:
   pg_restore -h localhost -U postgres -d morsemate_db ${BACKUP_NAME}_data_only.dump

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
