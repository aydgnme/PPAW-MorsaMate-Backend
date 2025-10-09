#!/bin/bash

# ============================================================================
# MorseMate Database Setup for Docker
# ============================================================================
# This script creates the database and loads schema + test data
# ============================================================================

CONTAINER_NAME="morse_postgres"
DB_NAME="morsemate_db"
DB_USER="postgres"

echo "========================================="
echo "MorseMate Database Setup (Docker)"
echo "========================================="
echo "Container: $CONTAINER_NAME"
echo "Database: $DB_NAME"
echo ""

# Check if container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "✗ Container '$CONTAINER_NAME' is not running!"
    echo "Please start your Docker container first."
    exit 1
fi

echo "✓ Container is running"
echo ""

# Step 1: Create database
echo "Step 1: Creating database..."
docker exec -i $CONTAINER_NAME psql -U $DB_USER -c "CREATE DATABASE $DB_NAME;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Database '$DB_NAME' created successfully"
else
    echo "⚠ Database '$DB_NAME' might already exist, continuing..."
fi
echo ""

# Step 2: Create tables
echo "Step 2: Creating tables..."
docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < database/schema/01_create_tables.sql

if [ $? -eq 0 ]; then
    echo "✓ Tables created successfully"
else
    echo "✗ Failed to create tables"
    exit 1
fi
echo ""

# Step 3: Insert test data
echo "Step 3: Inserting test data..."
docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME < database/schema/02_insert_test_data.sql

if [ $? -eq 0 ]; then
    echo "✓ Test data inserted successfully"
else
    echo "✗ Failed to insert test data"
    exit 1
fi
echo ""

# Step 4: Verify installation
echo "Step 4: Verifying installation..."
TABLE_COUNT=$(docker exec -i $CONTAINER_NAME psql -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")

echo "✓ Total tables created: $(echo $TABLE_COUNT | tr -d ' ')"
echo ""

echo "========================================="
echo "Setup completed successfully!"
echo "========================================="
echo ""
echo "Connection details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""
echo "Next steps:"
echo "  1. Run backup scripts: ./database/backup/logical_backup.sh"
echo "  2. Test connection: java -cp ... DatabaseConnectionTest"
echo "========================================="
