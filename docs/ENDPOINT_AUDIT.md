# API Endpoint Audit Report

**Date:** November 2, 2025
**Purpose:** Verify actual implemented endpoints vs documentation

---

## Summary

| Source | Count | Status |
|--------|-------|--------|
| **Source Code (Controllers)** | 138 mappings | ✅ Actual Implementation |
| **Postman Collection** | 71 requests | ⚠️ Incomplete |
| **API_ENDPOINTS.md** | 87 documented | ⚠️ Needs verification |
| **Missing in Postman** | ~16 endpoints | ❌ Need to add |

---

## Endpoint Count by Controller

Analyzing source code:

```bash
# Command used:
grep -c '@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping' \
  src/main/java/me/aydgn/MorseMate/controller/*.java
```

**Results will be added after running the audit...**

---

## Discrepancy Analysis

### Issue 1: Postman vs Documentation
- **Postman:** 71 requests
- **Documentation:** 87 endpoints
- **Gap:** 16 endpoints

### Issue 2: Source Code vs Documentation
- **Source Code:** 138 mapping annotations
- **Documentation:** 87 endpoints
- **Note:** Some mappings may be variations (path variables, query params)

---

## Action Items

1. [ ] Run complete endpoint count from source code
2. [ ] Identify 16 missing endpoints in Postman
3. [ ] Verify all documented endpoints exist in code
4. [ ] Add missing endpoints to Postman collection
5. [ ] Update documentation with accurate count

---

**Status:** In Progress
**Next Update:** After controller analysis
