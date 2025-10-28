# MorseMate API - Postman Testing Guide

## 📦 Files Overview

This directory contains comprehensive Postman collections and environments for testing the MorseMate API:

1. **MorseMate_Postman_Collection.json** - Main API collection with all endpoints
2. **MorseMate_Test_Flows.json** - Complete test flows with automated scripts
3. **MorseMate_Local_Environment.json** - Local development environment variables
4. **MorseMate_Production_Environment.json** - Production environment variables

---

## 🚀 Quick Start

### Step 1: Import Files to Postman

1. Open Postman
2. Click **Import** button (top left)
3. Drag and drop all 4 JSON files or click "Upload Files"
4. Click **Import**

### Step 2: Select Environment

1. Click the environment dropdown (top right)
2. Select **"MorseMate - Local Development"**

### Step 3: Start Testing

1. Open the **MorseMate API Collection**
2. Navigate to **Authentication** folder
3. Run **"Register New User"** or **"Login"**
4. JWT token will be automatically saved
5. Now you can run any authenticated endpoint!

---

## 📚 Collections Explained

### 1. MorseMate API Collection

**Purpose:** Complete API reference with all endpoints organized by feature

**Folders:**
- **Authentication** - User registration, login, profile
- **User Management** - Profile updates, statistics, hearts system
- **Categories** - Browse and manage learning categories
- **Health & System** - API health checks and monitoring

**Features:**
- Automatic JWT token management
- Auto-save user_id after login/registration
- Pre-configured bearer authentication
- Sample request bodies for all POST/PUT endpoints
- Detailed descriptions for each endpoint

**Best for:** Manual testing, API exploration, documentation reference

---

### 2. MorseMate Test Flows

**Purpose:** Automated test scenarios with assertions and validations

**Test Flows:**

#### Flow 1: New User Registration & First Login
Complete onboarding flow for new users:
1. ✓ Health check
2. ✓ Register with unique credentials
3. ✓ Get user profile
4. ✓ Update profile
5. ✓ Get user statistics

**Assertions:**
- Registration returns 201 status
- User has correct initial values (5 hearts, level 1)
- Profile updates persist correctly

---

#### Flow 2: Existing User Login & Browse Categories
Standard user flow:
1. ✓ Login with credentials
2. ✓ Browse all categories
3. ✓ View category details

**Assertions:**
- Categories are sorted by displayOrder
- All required fields are present
- Public endpoints work without authentication

---

#### Flow 3: Heart System Testing
Complete heart (life) system validation:
1. ✓ Login user
2. ✓ Use one heart
3. ✓ Verify hearts decreased
4. ✓ Refill hearts
5. ✓ Verify hearts restored to max

**Assertions:**
- Hearts decrease by exactly 1
- Refill restores to maxHearts
- System prevents negative hearts

---

#### Flow 4: Admin Category Management
Full CRUD operations for categories:
1. ✓ Login as admin
2. ✓ Create new category
3. ✓ Update category
4. ✓ View all categories (admin view)
5. ✓ Delete category

**Assertions:**
- Only admin users can perform operations
- All CRUD operations return correct status codes
- Data persists correctly

---

#### Flow 5: Error Handling & Validation
Negative testing scenarios:
1. ✗ Invalid registration data
2. ✗ Wrong login credentials
3. ✗ Unauthorized access
4. ✗ Non-existent resources
5. ✗ Invalid input validation

**Assertions:**
- Proper error status codes (400, 401, 404)
- Validation messages are clear
- Security rules are enforced

---

## 🌍 Environment Variables

### Local Development Environment

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `base_url` | http://localhost:8080 | API base URL |
| `jwt_token` | (auto-saved) | JWT authentication token |
| `user_id` | (auto-saved) | Current user ID |
| `test_username` | john_doe | Test user username |
| `test_email` | john@example.com | Test user email |
| `test_password` | SecurePass123 | Test user password |
| `admin_email` | admin@morsemate.com | Admin account email |
| `admin_password` | AdminPass123 | Admin account password |
| `category_id` | (auto-saved) | Current category ID |
| `lesson_id` | (auto-saved) | Current lesson ID |
| `exercise_id` | (auto-saved) | Current exercise ID |

### Production Environment

Same variables but with production URLs and credentials.

**⚠️ Security Note:** Never commit production credentials to version control!

---

## 🎯 Running Test Flows

### Option 1: Run Individual Requests

1. Open **MorseMate - Test Flows** collection
2. Navigate to any flow (e.g., "Flow 1: New User Registration")
3. Click on first request
4. Click **Send**
5. Check **Test Results** tab for assertions
6. Continue with next request in order

### Option 2: Run Entire Flow

1. Right-click on a flow folder
2. Select **Run folder**
3. Optionally adjust settings:
   - Delay between requests
   - Persist responses
   - Save cookies
4. Click **Run MorseMate - Test...**
5. View results in Collection Runner

### Option 3: Run All Tests

1. Click **Collections** in sidebar
2. Hover over **MorseMate - Test Flows**
3. Click **Run** button (▶)
4. Select all flows
5. Click **Run MorseMate - Test...**

---

## 🔧 Advanced Usage

### Using Variables in Requests

You can use environment variables in any field:

```
URL: {{base_url}}/users/{{user_id}}
Headers: Authorization: Bearer {{jwt_token}}
Body: { "email": "{{test_email}}" }
```

### Setting Variables in Tests

```javascript
// Save a value from response
const response = pm.response.json();
pm.environment.set('category_id', response.id);

// Use saved value in next request
pm.environment.get('category_id');
```

### Custom Pre-request Scripts

Generate dynamic data before requests:

```javascript
// Generate unique username
const timestamp = Date.now();
pm.environment.set('unique_username', 'user_' + timestamp);
```

### Writing Custom Tests

```javascript
// Status code assertion
pm.test('Status is 200', function() {
    pm.response.to.have.status(200);
});

// Response body assertion
pm.test('Response has token', function() {
    const response = pm.response.json();
    pm.expect(response).to.have.property('token');
});

// Performance assertion
pm.test('Response time < 500ms', function() {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

---

## 📊 Monitoring & CI/CD Integration

### Newman (Command Line Runner)

Install Newman:
```bash
npm install -g newman
```

Run collection:
```bash
newman run MorseMate_Test_Flows.json \
  -e MorseMate_Local_Environment.json \
  --reporters cli,json \
  --reporter-json-export results.json
```

### GitHub Actions Integration

```yaml
name: API Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install Newman
        run: npm install -g newman
      - name: Run Tests
        run: newman run MorseMate_Test_Flows.json -e MorseMate_Local_Environment.json
```

---

## 🐛 Troubleshooting

### Issue: "Unauthorized" errors on all requests

**Solution:**
1. Run **Authentication > Login** request first
2. Check that JWT token is saved: `{{jwt_token}}`
3. Verify token in Console: `pm.environment.get('jwt_token')`

### Issue: "Cannot find variable: base_url"

**Solution:**
1. Ensure environment is selected (top right dropdown)
2. Select **"MorseMate - Local Development"**

### Issue: Tests are failing

**Solution:**
1. Verify your server is running on http://localhost:8080
2. Check environment variable values
3. Run requests in order (some depend on previous requests)
4. Check test user exists in database

### Issue: "Category not found" errors

**Solution:**
1. Run **Categories > Get All Active Categories** first
2. Verify `category_id` is saved
3. Create a category using admin account if needed

---

## 📝 Best Practices

### 1. Test Data Management

- Use unique usernames/emails for registration tests
- Clean up test data regularly
- Use descriptive names for test accounts

### 2. Environment Separation

- Never test against production with local credentials
- Keep production environment variables secret
- Use different test accounts per environment

### 3. Test Organization

- Run smoke tests first (health checks)
- Group related tests in folders
- Use descriptive test names

### 4. Continuous Testing

- Run tests after code changes
- Automate regression testing
- Monitor test results over time

---

## 🔐 Security Notes

1. **JWT Tokens:** Tokens are stored as environment variables and visible in Postman
2. **Passwords:** Use test passwords only, never production passwords
3. **Admin Credentials:** Store admin credentials securely, use vault for production
4. **Token Expiration:** Tokens expire after configured time (default: 24h)
5. **HTTPS:** Always use HTTPS in production

---

## 📞 Support

For issues or questions:
- Check server logs: `./gradlew bootRun`
- Verify database connection
- Review Postman Console for detailed errors
- Check API documentation at: http://localhost:8080/swagger-ui.html

---

## 🎓 Learning Resources

- [Postman Learning Center](https://learning.postman.com/)
- [Writing Tests in Postman](https://learning.postman.com/docs/writing-scripts/test-scripts/)
- [Newman Documentation](https://learning.postman.com/docs/running-collections/using-newman-cli/command-line-integration-with-newman/)
- [Chai Assertion Library](https://www.chaijs.com/api/bdd/)

---

## ✅ Quick Test Checklist

Before deploying to production:

- [ ] All health checks pass
- [ ] User registration works
- [ ] Login returns valid JWT
- [ ] Protected endpoints require authentication
- [ ] Hearts system works correctly
- [ ] Categories can be created/updated/deleted (admin)
- [ ] Public endpoints work without auth
- [ ] Error responses have correct status codes
- [ ] Validation catches invalid input
- [ ] Performance is acceptable (< 500ms)

---

**Happy Testing! 🚀**
