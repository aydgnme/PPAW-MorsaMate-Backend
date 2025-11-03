# MorseMate API Documentation

Welcome to the MorseMate API documentation! This directory contains all the documentation you need to integrate with the MorseMate API.

---

## 📚 Documentation Structure

```
docs/
├── README.md                           # This file - Documentation overview
│
├── API Documentation/
│   ├── API_DOCUMENTATION_INDEX.md      # Main API index and overview
│   ├── API_ENDPOINTS.md                # Complete API reference (all 91 endpoints)
│   ├── API_QUICK_REFERENCE.md          # Quick reference table
│   ├── API-TESTING.md                  # API testing guide
│   ├── API_VERSIONING.md               # API versioning strategy
│   ├── API.md                          # Legacy API documentation
│   └── api.json                        # OpenAPI/Swagger specification
│
├── Integration Guides/
│   ├── frontend-examples.md            # React/Vue/Angular examples
│   ├── IOS_INTEGRATION_GUIDE.md        # Complete iOS integration guide
│   ├── IOS_QUICK_START.md              # iOS quick start guide
│   └── POSTMAN_GUIDE.md                # Postman collection usage
│
├── Technical Documentation/
│   ├── CORS_CONFIGURATION.md           # CORS setup and configuration
│   ├── SECURITY_AUDIT_REPORT.md        # Security audit findings
│   ├── SECURITY_FLOW_ARCHITECTURE.md   # Security architecture
│   ├── SOFT_DELETE_IMPLEMENTATION.md   # Soft delete implementation guide
│   ├── IMPROVEMENT_SUMMARY.md          # Recent improvements summary
│   ├── GAMIFICATION.md                 # Gamification system docs
│   ├── QA_TEST_COVERAGE_REPORT.md      # Test coverage report
│   └── TEST_FIXING_GUIDE.md            # Guide for fixing tests
│
├── api-clients/                        # Ready-to-use API clients
│   ├── frontend-api-client.ts          # TypeScript client (800+ lines)
│   └── ios-api-client.swift            # Swift client (1000+ lines)
│
└── ios/                                # iOS-specific documentation
    └── API_ENDPOINTS_FOR_IOS.md        # iOS endpoint reference
```

**Note:** Postman collections have been moved to `/postman` directory at project root.

---

## 🚀 Quick Start

### For Everyone
👉 **Start here:** [API_DOCUMENTATION_INDEX.md](API_DOCUMENTATION_INDEX.md)

This is your main entry point with an overview of all documentation.

---

### For Frontend Developers (React/Vue/Angular)

1. **Copy API Client:**
   ```bash
   cp docs/api-clients/frontend-api-client.ts src/services/api.ts
   ```

2. **Read Examples:**
   - [frontend-examples.md](frontend-examples.md) - Complete React/Vue examples
   - Includes: Hooks, Components, Error handling, Token management

3. **API Reference:**
   - [API_ENDPOINTS.md](API_ENDPOINTS.md) - Detailed API documentation
   - [API_QUICK_REFERENCE.md](API_QUICK_REFERENCE.md) - Quick lookup

---

### For iOS Developers (Swift/SwiftUI)

1. **Add API Client:**
   - Drag `docs/api-clients/ios-api-client.swift` into your Xcode project

2. **Read Guides:**
   - [IOS_QUICK_START.md](IOS_QUICK_START.md) - Quick start with examples
   - [IOS_INTEGRATION_GUIDE.md](IOS_INTEGRATION_GUIDE.md) - Complete integration guide

3. **API Reference:**
   - [API_ENDPOINTS.md](API_ENDPOINTS.md) - Detailed API documentation

---

### For QA/Testing Engineers

1. **Import Postman Collections:**
   - Open Postman → Import
   - Select all 4 files from `docs/postman/`

2. **Read Testing Guide:**
   - [POSTMAN_GUIDE.md](POSTMAN_GUIDE.md) - Complete testing guide
   - Includes: Environment setup, Test flows, Automation

3. **Run Tests:**
   - Select "MorseMate - Local Development" environment
   - Run "MorseMate - Test Flows" collection

---

## 📖 Documentation Files

### Core Documentation

| File | Description | Size | For |
|------|-------------|------|-----|
| **API_DOCUMENTATION_INDEX.md** | Main index and overview | Master | Everyone |
| **API_ENDPOINTS.md** | Complete API reference with examples | 400+ lines | All Developers |
| **API_QUICK_REFERENCE.md** | Quick reference table | 1 page | Quick Lookup |
| **API-TESTING.md** | API testing documentation | - | Testing |

---

### Frontend Documentation

| File | Description | Size | For |
|------|-------------|------|-----|
| **frontend-examples.md** | React/Vue examples, hooks, components | 600+ lines | Frontend Devs |
| **api-clients/frontend-api-client.ts** | Production-ready TypeScript client | 800+ lines | Frontend Devs |

**Features:**
- ✅ TypeScript with full type safety
- ✅ Async/await support
- ✅ Automatic token management
- ✅ Error handling
- ✅ React Hooks & Vue Composables
- ✅ Complete component examples

---

### iOS Documentation

| File | Description | Size | For |
|------|-------------|------|-----|
| **IOS_QUICK_START.md** | Quick start guide with examples | 300+ lines | iOS Devs |
| **IOS_INTEGRATION_GUIDE.md** | Complete integration guide | 700+ lines | iOS Devs |
| **api-clients/ios-api-client.swift** | Production-ready Swift client | 1000+ lines | iOS Devs |

**Features:**
- ✅ Swift 5.9+ with async/await
- ✅ Combine integration
- ✅ SwiftUI ViewModels (MVVM)
- ✅ Complete UI examples
- ✅ Custom error handling
- ✅ iOS 15.0+ support

---

### Testing Documentation

| File | Description | Size | For |
|------|-------------|------|-----|
| **POSTMAN_GUIDE.md** | Postman usage guide | 200+ lines | QA/Testing |
| **postman/MorseMate_Postman_Collection.json** | Main API collection | 36 endpoints | Testing |
| **postman/MorseMate_Test_Flows.json** | Automated test flows | 5 flows | Automation |
| **postman/MorseMate_Local_Environment.json** | Local environment | 12 variables | Local Testing |
| **postman/MorseMate_Production_Environment.json** | Production environment | 12 variables | Prod Testing |

**Test Flows:**
1. New User Registration & First Login
2. Existing User Login & Browse Categories
3. Heart System Testing
4. Admin Category Management
5. Error Handling & Validation

---

## 🎯 API Status

### ✅ Implemented (Working Now)
- Authentication (Register, Login, Get Current User)
- User Management (Profile, Statistics, Hearts)
- Categories (CRUD operations)
- Health & System endpoints

**Total:** 28 endpoints

### ⏳ Coming Soon
- Lessons (CRUD, Complete, Progress)
- Exercises (CRUD, Attempts, Results)
- Progress Tracking
- Achievements System
- Subscription & Payments
- Gems & Power-Ups
- Leaderboard & Social

**Total Planned:** 50+ endpoints

---

## 🔐 Authentication

All authenticated endpoints require JWT Bearer token:

```http
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

Get token from:
- `POST /auth/register`
- `POST /auth/login`

Token expires: 24 hours (default)

---

## 📊 Response Format

### Success Response
```json
{
  "id": 1,
  "name": "Example",
  "data": { ... }
}
```

### Error Response
```json
{
  "timestamp": "2025-01-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/endpoint",
  "errors": [
    {
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

---

## 🌐 Base URLs

- **Local Development:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **API Docs:** `http://localhost:8080/api-docs`

---

## 📱 Supported Platforms

| Platform | Client | Documentation | Status |
|----------|--------|---------------|--------|
| Web (React/Vue) | ✅ TypeScript | ✅ Complete | Ready |
| iOS | ✅ Swift | ✅ Complete | Ready |
| Android | 🔄 Use Web API | ✅ API Docs | Via Web API |
| Backend | ✅ Spring Boot | ✅ Complete | Ready |

---

## 🛠️ Development Tools

### Required
- **Backend:** Java 21, Spring Boot 3.5.6, PostgreSQL
- **Frontend:** Node.js 18+, TypeScript 5.0+
- **iOS:** Xcode 14+, Swift 5.9+, iOS 15.0+
- **Testing:** Postman

### Optional
- **Frontend Frameworks:** React 18+, Vue 3+, Angular 16+
- **State Management:** Redux, Zustand, Pinia
- **UI Libraries:** Material-UI, Ant Design, SwiftUI

---

## 📝 Code Examples

### TypeScript (React/Vue)
```typescript
import { createApiClient } from './services/api';

const api = createApiClient({
  baseUrl: 'http://localhost:8080'
});

// Login
const response = await api.login({
  identifier: 'user@example.com',
  password: 'password123'
});

// Get categories
const categories = await api.getAllCategories();
```

### Swift (iOS)
```swift
let api = MorseMateAPI.shared

// Login
Task {
    let response = try await api.login(
        request: LoginRequest(
            identifier: "user@example.com",
            password: "password123"
        )
    )
    print("Logged in: \(response.user.username)")
}

// Get categories
Task {
    let categories = try await api.getAllCategories()
}
```

---

## 🎓 Learning Path

### Week 1: Setup & Authentication
1. Read: [API_DOCUMENTATION_INDEX.md](API_DOCUMENTATION_INDEX.md)
2. Setup: Copy API client to your project
3. Implement: Login & Register screens
4. Test: Using Postman collections

### Week 2: Core Features
1. Implement: Categories list
2. Implement: User profile
3. Implement: Statistics display
4. Test: All user flows

### Week 3: Learning System
1. Implement: Lessons list
2. Implement: Exercise view
3. Implement: Progress tracking
4. Test: Learning flows

### Week 4: Polish & Deploy
1. Add: Loading states
2. Add: Error handling
3. Test: Edge cases
4. Deploy: To production

---

## 📞 Support

### Documentation Issues
If you find any issues with the documentation:
1. Check [API_DOCUMENTATION_INDEX.md](API_DOCUMENTATION_INDEX.md)
2. Review relevant platform guide
3. Check Postman collections for examples

### API Issues
- Check API status: `GET /api/health`
- Review error response format
- Check authentication token

### Development Issues
- Review code examples in platform guides
- Check API client source code
- Test with Postman collections

---

## ✅ Documentation Checklist

### For Developers Starting Now
- [ ] Read [API_DOCUMENTATION_INDEX.md](API_DOCUMENTATION_INDEX.md)
- [ ] Copy appropriate API client to project
- [ ] Read platform-specific guide
- [ ] Import Postman collections
- [ ] Test basic endpoints (health, login)
- [ ] Implement authentication flow
- [ ] Build first feature (categories)

### For Reviewers
- [ ] All endpoints documented
- [ ] Code examples tested
- [ ] Error responses documented
- [ ] Authentication explained
- [ ] Platform guides complete
- [ ] Postman collections working

---

## 🔄 Version History

### v1.0.0 (Current - January 2025)
- ✅ Complete API documentation
- ✅ TypeScript API client
- ✅ Swift iOS API client
- ✅ React/Vue examples
- ✅ SwiftUI examples
- ✅ Postman collections
- ✅ Test automation
- ✅ 28 working endpoints

### v1.1.0 (Planned - February 2025)
- ⏳ Lessons implementation
- ⏳ Exercises system
- ⏳ Progress tracking
- ⏳ Additional 20+ endpoints

---

## 📊 Statistics

- **Total Documentation:** 2000+ lines
- **Code Examples:** 5000+ lines
- **API Endpoints:** 78 (28 live, 50 planned)
- **Supported Platforms:** 3 (Web, iOS, Android via Web)
- **Test Coverage:** 5 automated flows
- **Languages:** 3 (TypeScript, Swift, Java)

---

## 🚀 Ready to Start?

Pick your platform and dive in:

- 🎨 **Frontend Developer** → [frontend-examples.md](frontend-examples.md)
- 📱 **iOS Developer** → [IOS_QUICK_START.md](IOS_QUICK_START.md)
- 🧪 **QA Engineer** → [POSTMAN_GUIDE.md](POSTMAN_GUIDE.md)
- 📚 **Need Overview?** → [API_DOCUMENTATION_INDEX.md](API_DOCUMENTATION_INDEX.md)

---

**Happy Coding! 🎉**

*All documentation is production-ready and follows industry best practices.*

---

**Last Updated:** January 20, 2025
**Version:** 1.0.0
**Maintained By:** MorseMate Team
