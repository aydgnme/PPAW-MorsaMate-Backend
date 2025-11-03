# CORS Configuration Guide

## Overview

CORS (Cross-Origin Resource Sharing) is now fully configured in the MorseMate API. This allows frontend applications running on different origins (domains/ports) to access the API.

## Configuration

### Environment Variables

Set the following in your `.env` file:

```bash
# Single origin
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Multiple origins (comma-separated)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://morsemate.app
```

### Default Configuration

If `CORS_ALLOWED_ORIGINS` is not set, the default is:
- `http://localhost:3000` (React/Next.js default)

### Other CORS Settings

Configured in `application.properties`:
- **Allowed Methods:** GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Allowed Headers:** * (all headers)
- **Allow Credentials:** true (cookies/auth headers allowed)
- **Max Age:** 3600 seconds (1 hour preflight cache)

## Implementation

CORS is implemented in `src/main/java/me/aydgn/MorseMate/config/CorsConfig.java`:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

## Testing CORS

### 1. Using Browser Developer Tools

1. Open your frontend application (e.g., http://localhost:3000)
2. Open Browser DevTools (F12)
3. Go to Network tab
4. Make an API request
5. Check the response headers:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: *
```

### 2. Using cURL (Preflight Request)

Test OPTIONS preflight request:

```bash
curl -X OPTIONS http://localhost:8080/v1/categories \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

Expected response headers:
```
< HTTP/1.1 200 OK
< Access-Control-Allow-Origin: http://localhost:3000
< Access-Control-Allow-Methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
< Access-Control-Allow-Headers: Content-Type
< Access-Control-Allow-Credentials: true
< Access-Control-Max-Age: 3600
```

### 3. Using JavaScript Fetch

```javascript
// In your frontend application
fetch('http://localhost:8080/v1/categories', {
  method: 'GET',
  credentials: 'include', // Important for cookies/auth
  headers: {
    'Content-Type': 'application/json',
  }
})
.then(response => response.json())
.then(data => console.log('CORS working!', data))
.catch(error => console.error('CORS error:', error));
```

### 4. Test with Authenticated Request

```javascript
// With JWT token
fetch('http://localhost:8080/v1/progress/me', {
  method: 'GET',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  }
})
.then(response => response.json())
.then(data => console.log('Authenticated CORS working!', data));
```

## Common Issues & Solutions

### Issue 1: "No 'Access-Control-Allow-Origin' header"

**Cause:** Origin not in allowed origins list

**Solution:** Add your frontend origin to `CORS_ALLOWED_ORIGINS`:
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### Issue 2: "Credentials flag is true, but Access-Control-Allow-Credentials is missing"

**Cause:** Using `credentials: 'include'` but CORS not configured for credentials

**Solution:** Already configured! `allowCredentials=true` is set.

### Issue 3: "Method not allowed by CORS"

**Cause:** HTTP method not in allowed methods list

**Solution:** Already configured! All common methods (GET, POST, PUT, DELETE, PATCH, OPTIONS) are allowed.

### Issue 4: "Request header not allowed by CORS"

**Cause:** Custom header not in allowed headers list

**Solution:** Already configured! `allowedHeaders=*` allows all headers.

## Production Deployment

### For Production Domains

Update your production `.env`:

```bash
# Production domains (comma-separated)
CORS_ALLOWED_ORIGINS=https://morsemate.app,https://www.morsemate.app,https://app.morsemate.com
```

### Security Best Practices

1. **Never use `*` for allowed origins in production**
   - ✅ Current: Specific origins from environment variable
   - ❌ Bad: `allowedOrigins("*")`

2. **Only allow necessary methods**
   - ✅ Current: Specific methods (GET, POST, PUT, DELETE, PATCH, OPTIONS)

3. **Use HTTPS in production**
   - ✅ Ensure all production origins use `https://`

4. **Limit allowed headers if possible**
   - Current: `*` (all headers)
   - Consider: List specific headers for stricter security

5. **Set appropriate maxAge**
   - Current: 3600 seconds (1 hour)
   - Good balance between performance and flexibility

## Frontend Integration Examples

### React

```javascript
// src/api/config.js
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// src/api/client.js
export const apiClient = {
  get: (endpoint) =>
    fetch(`${API_BASE_URL}${endpoint}`, {
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
      }
    }),

  post: (endpoint, data) =>
    fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
      },
      body: JSON.stringify(data)
    })
};
```

### Vue.js

```javascript
// src/plugins/axios.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add auth token to all requests
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
```

### Angular

```typescript
// src/app/services/api.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  get<T>(endpoint: string) {
    return this.http.get<T>(`${this.apiUrl}${endpoint}`, {
      withCredentials: true
    });
  }

  post<T>(endpoint: string, data: any) {
    return this.http.post<T>(`${this.apiUrl}${endpoint}`, data, {
      withCredentials: true
    });
  }
}
```

## Verification Checklist

Before deploying:

- [ ] CORS_ALLOWED_ORIGINS set in production .env
- [ ] All production frontend origins included
- [ ] All origins use HTTPS in production
- [ ] Test OPTIONS preflight request
- [ ] Test GET request from frontend
- [ ] Test POST request with JSON body
- [ ] Test authenticated request with JWT
- [ ] Verify CORS headers in browser DevTools
- [ ] Test from all allowed origins
- [ ] Test that unauthorized origins are rejected

## Troubleshooting

Enable debug logging to see CORS processing:

```properties
# application.properties
logging.level.org.springframework.web.cors=DEBUG
```

Check logs for:
```
Request received with CORS headers
CORS configuration for path: /**
Allowed origins: [http://localhost:3000, http://localhost:5173]
Access-Control-Allow-Origin: http://localhost:3000
```

---

**CORS Status:** ✅ **FULLY CONFIGURED AND PRODUCTION-READY**

Last Updated: October 29, 2025
