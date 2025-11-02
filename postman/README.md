# MorseMate API - Postman Collection

## Overview
This directory contains a comprehensive Postman collection for testing all MorseMate API endpoints.

## Collection Contents

### MorseMate_Complete_API_Collection.json
Complete collection with **87 endpoints** organized into 10 categories:

1. **Authentication (4 endpoints)** - Register, Login, Current User, Health
2. **Users (14 endpoints)** - Profile, Statistics, Hearts, Points, Account Management
3. **Categories (6 endpoints)** - Full CRUD operations with Admin controls
4. **Lessons (8 endpoints)** - CRUD, Search, Ordering
5. **Exercises (8 endpoints)** - CRUD, Filtering, Random selection
6. **Achievements (14 endpoints)** - Management, User tracking, Awards
7. **Gems (11 endpoints)** - Balance, Transactions, Admin operations
8. **PowerUps (11 endpoints)** - Purchase, Activation, Management
9. **Leaderboard (10 endpoints)** - Rankings and user statistics
10. **Health & System (4 endpoints)** - API root, Health check, System info, Ping

### MorseMate-Local.postman_environment.json
Postman environment file with pre-configured variables:
- `base_url`: http://localhost:8080
- `api_version`: v1
- `jwt_token`: Auto-saved after login
- `user_id`: Auto-saved after login

## Quick Start

1. **Import Environment:**
   - In Postman, click "Import"
   - Select `MorseMate-Local.postman_environment.json`
   - Set as active environment

2. **Import Collection:**
   - Click "Import" again
   - Select `MorseMate_Complete_API_Collection.json`

3. **Test the API:**
   - Run "Register" or "Login" request
   - JWT token will be auto-saved
   - All authenticated endpoints will work automatically

## Important Notes

### Endpoint Prefixes
- **Most endpoints** use `/v1` prefix (e.g., `/v1/categories`)
- **Health & System endpoints** use `/api` prefix (e.g., `/api/health`, `/api/ping`)
- **Auth endpoints** use no version prefix (e.g., `/auth/login`)

### Environment Variables
All variables are defined in the environment file and can be customized:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | http://localhost:8080 | API server base URL |
| `api_version` | v1 | API version for versioned endpoints |
| `jwt_token` | (auto-saved) | JWT authentication token |
| `user_id` | (auto-saved) | Current user ID |

## Total Endpoints: 87

**Last Updated:** 2025-11-02
