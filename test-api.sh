#!/bin/bash

# MorseMate API Test Script
# Bu script OpenAPI Generator kullanarak API endpoint'lerini test eder

set -e

BASE_URL="http://localhost:8080"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=================================="
echo "  MorseMate API Test Suite"
echo "=================================="
echo ""

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local expected_code=$4
    local data=$5

    echo -n "Testing: $description... "

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        echo "  Response: $(echo $body | head -c 100)"
    else
        echo -e "${RED}✗ FAIL${NC} (Expected: $expected_code, Got: $http_code)"
        echo "  Response: $body"
    fi
    echo ""
}

# Check if server is running
echo "Checking if server is running..."
if ! curl -s "$BASE_URL/api/health" > /dev/null 2>&1; then
    echo -e "${RED}Error: Server is not running on $BASE_URL${NC}"
    echo "Please start the application first:"
    echo "  ./gradlew bootRun"
    exit 1
fi
echo -e "${GREEN}Server is running${NC}"
echo ""

# Test Cases
echo "=================================="
echo "  Running Tests"
echo "=================================="
echo ""

# Health Check
test_endpoint "GET" "/api/health" "Health Check" "200"

# API Root
test_endpoint "GET" "/api" "API Root" "200"

# System Info
test_endpoint "GET" "/api/info" "System Info" "200"

# Get All Categories
test_endpoint "GET" "/v1/categories" "Get All Categories" "200"

# Get Category by ID (assuming ID 1 exists)
test_endpoint "GET" "/v1/categories/1" "Get Category by ID" "200"

# Get Non-existent Category
test_endpoint "GET" "/v1/categories/99999" "Get Non-existent Category" "404"

echo "=================================="
echo "  Test Summary"
echo "=================================="
echo ""
echo "Manual testing complete!"
echo ""
echo "To generate a client SDK, run:"
echo -e "${YELLOW}  npm install @openapitools/openapi-generator-cli -g${NC}"
echo -e "${YELLOW}  openapi-generator-cli generate -i openapi-spec.yaml -g typescript-axios -o ./generated-client${NC}"
echo ""
echo "Or use Docker:"
echo -e "${YELLOW}  docker run --rm -v \${PWD}:/local openapitools/openapi-generator-cli generate \\${NC}"
echo -e "${YELLOW}    -i /local/openapi-spec.yaml \\${NC}"
echo -e "${YELLOW}    -g typescript-axios \\${NC}"
echo -e "${YELLOW}    -o /local/generated-client${NC}"
echo ""
