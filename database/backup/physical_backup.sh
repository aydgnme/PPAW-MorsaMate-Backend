#!/bin/bash

# ============================================================================
# MorseMate Physical Backup Script
# ============================================================================
# Physical backup creates a binary copy of the entire database cluster
# This is the fastest backup method and includes all databases
# ============================================================================

# Configuration
BACKUP_DIR="$(dirname "$0")/physical"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="morsemate_physical_backup_${TIMESTAMP}"
CONTAINER_NAME="${POSTGRES_CONTAINER:-morse_postgres}"

# PostgreSQL connection details
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="morsemate_db"
DB_USER="${POSTGRES_USER:-postgres}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "========================================="
echo "MorseMate Physical Backup"
echo "========================================="
echo "Timestamp: $TIMESTAMP"
echo "Backup Directory: $BACKUP_DIR"
echo ""

# Method 1: pg_basebackup (Recommended for physical backup)
echo "Creating physical backup using pg_basebackup..."
echo "Note: Running inside Docker container..."
echo ""

docker exec -i $CONTAINER_NAME pg_basebackup \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -D "/tmp/$BACKUP_NAME" \
    -Ft \
    -z \
    -P \
    --checkpoint=fast

# Copy from container to host
if [ $? -eq 0 ]; then
    docker cp "$CONTAINER_NAME:/tmp/$BACKUP_NAME" "$BACKUP_DIR/$BACKUP_NAME"
    docker exec $CONTAINER_NAME rm -rf "/tmp/$BACKUP_NAME"
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Physical backup completed successfully!"
    echo "Backup location: $BACKUP_DIR/$BACKUP_NAME"

    # Calculate backup size
    BACKUP_SIZE=$(du -sh "$BACKUP_DIR/$BACKUP_NAME" | cut -f1)
    echo "Backup size: $BACKUP_SIZE"

    # Create a metadata file
    cat > "$BACKUP_DIR/${BACKUP_NAME}.info" <<EOF
Backup Type: Physical (pg_basebackup)
Database: $DB_NAME
Host: $DB_HOST
Port: $DB_PORT
User: $DB_USER
Timestamp: $TIMESTAMP
Size: $BACKUP_SIZE
Format: Tar + Gzip
Backup Directory: $BACKUP_DIR/$BACKUP_NAME
EOF

    echo ""
    echo "Metadata saved to: $BACKUP_DIR/${BACKUP_NAME}.info"
else
    echo ""
    echo "✗ Physical backup failed!"
    exit 1
fi

# Optional: Create a compressed archive of the backup
echo ""
echo "Creating compressed archive..."
tar -czf "$BACKUP_DIR/${BACKUP_NAME}.tar.gz" -C "$BACKUP_DIR" "$BACKUP_NAME"

if [ $? -eq 0 ]; then
    echo "✓ Compressed archive created: ${BACKUP_NAME}.tar.gz"

    # Remove uncompressed backup to save space
    rm -rf "$BACKUP_DIR/$BACKUP_NAME"
    echo "✓ Uncompressed backup removed"
fi

echo ""
echo "========================================="
echo "Backup Summary"
echo "========================================="
echo "Backup file: $BACKUP_DIR/${BACKUP_NAME}.tar.gz"
echo "Metadata: $BACKUP_DIR/${BACKUP_NAME}.info"
echo ""
echo "To restore this backup, use:"
echo "  tar -xzf ${BACKUP_NAME}.tar.gz"
echo "  pg_restore -d $DB_NAME ${BACKUP_NAME}"
echo "========================================="
