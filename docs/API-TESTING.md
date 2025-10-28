# MorseMate API Testing Guide

Bu doküman, MorseMate API'yi test etmek için oluşturulan araçları ve kullanımlarını açıklar.

## 📁 Dosya Yapısı

```
MorseMate/
├── openapi-spec.yaml           # OpenAPI 3.0 spesifikasyonu
├── test-api.sh                 # Shell script ile manuel test
├── generated-client/           # OpenAPI Generator ile oluşturulan TypeScript client
│   ├── api.ts                  # API sınıfları
│   ├── base.ts                 # Base configuration
│   ├── configuration.ts        # Client configuration
│   ├── example-usage.ts        # Örnek kullanım kodu
│   └── package.json            # NPM package tanımı
└── API-TESTING.md              # Bu dosya
```

## 🚀 Hızlı Başlangıç

### 1. Uygulamayı Başlatın

```bash
# Java 17 kullanarak uygulamayı başlatın
export JAVA_HOME=/Users/aydgn.me/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
./gradlew bootRun
```

Uygulama `http://localhost:8080` adresinde çalışacaktır.

### 2. Shell Script ile Test

En basit test yöntemi, hazır shell script'ini kullanmaktır:

```bash
chmod +x test-api.sh
./test-api.sh
```

Bu script şu endpoint'leri test eder:
- ✅ Health Check (`/api/health`)
- ✅ API Root (`/api`)
- ✅ System Info (`/api/info`)
- ✅ Get All Categories (`/v1/categories`)
- ✅ Get Category by ID (`/v1/categories/{id}`)
- ✅ 404 Error Handling

### 3. Manuel cURL Testleri

```bash
# Health Check
curl http://localhost:8080/api/health

# API Root
curl http://localhost:8080/api

# System Info
curl http://localhost:8080/api/info

# Get All Categories
curl http://localhost:8080/v1/categories

# Get Category by ID
curl http://localhost:8080/v1/categories/1
```

## 🔧 OpenAPI Generator Client

### TypeScript/Axios Client Oluşturma

Docker kullanarak client oluşturun:

```bash
docker run --rm -v "${PWD}":/local openapitools/openapi-generator-cli generate \
  -i /local/openapi-spec.yaml \
  -g typescript-axios \
  -o /local/generated-client \
  --additional-properties=npmName=morsemate-api-client,npmVersion=1.0.0
```

### Desteklenen Diğer Diller

OpenAPI Generator birçok dil destekler:

```bash
# Java Client
docker run --rm -v "${PWD}":/local openapitools/openapi-generator-cli generate \
  -i /local/openapi-spec.yaml \
  -g java \
  -o /local/generated-client-java

# Python Client
docker run --rm -v "${PWD}":/local openapitools/openapi-generator-cli generate \
  -i /local/openapi-spec.yaml \
  -g python \
  -o /local/generated-client-python

# Go Client
docker run --rm -v "${PWD}":/local openapitools/openapi-generator-cli generate \
  -i /local/openapi-spec.yaml \
  -g go \
  -o /local/generated-client-go
```

Tüm desteklenen diller için:
```bash
docker run --rm openapitools/openapi-generator-cli list
```

## 📦 TypeScript Client Kullanımı

### 1. Bağımlılıkları Yükleyin

```bash
cd generated-client
npm install
```

### 2. TypeScript Kodunda Kullanım

```typescript
import { HealthApi, CategoriesApi, Configuration } from 'morsemate-api-client';

// Configuration
const config = new Configuration({
  basePath: 'http://localhost:8080',
  // JWT token için:
  // accessToken: 'your-jwt-token'
});

// API instance
const categoriesApi = new CategoriesApi(config);

// Get all categories
const response = await categoriesApi.getAllCategories();
console.log(response.data);
```

### 3. Örnek Kodu Çalıştırma

```bash
cd generated-client
npm install
npm install -g ts-node typescript
ts-node example-usage.ts
```

## 📝 OpenAPI Specification

`openapi-spec.yaml` dosyası API'nin tam spesifikasyonunu içerir:

- **Endpoints**: Tüm API endpoint'leri
- **Request/Response Models**: DTO'lar ve yanıt şemaları
- **Authentication**: JWT Bearer token konfigürasyonu
- **Error Responses**: Hata kodları ve mesajları

### Spec'i Görüntüleme

Online araçlar ile görüntüleyin:
- [Swagger Editor](https://editor.swagger.io/) - URL'ye `openapi-spec.yaml` upload edin
- [Stoplight](https://stoplight.io/)
- VS Code extension: "OpenAPI (Swagger) Editor"

## 🔐 Authentication

Protected endpoint'ler için JWT token gereklidir:

```bash
# Login endpoint'i hazır olduğunda:
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}' | jq -r '.token')

# Token ile istek
curl http://localhost:8080/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"name":"New Category","difficulty":"BEGINNER"}'
```

## 🧪 Test Senaryoları

### Senaryo 1: Kategori CRUD İşlemleri

```bash
# 1. Tüm kategorileri listele
curl http://localhost:8080/v1/categories

# 2. Yeni kategori oluştur (AUTH gerekli)
curl -X POST http://localhost:8080/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Advanced Morse",
    "description": "Advanced level exercises",
    "difficulty": "ADVANCED",
    "orderIndex": 3,
    "isActive": true
  }'

# 3. Kategoriyi güncelle (AUTH gerekli)
curl -X PUT http://localhost:8080/v1/categories/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "description": "Updated description",
    "difficulty": "INTERMEDIATE",
    "orderIndex": 2,
    "isActive": true
  }'

# 4. Kategoriyi sil (AUTH gerekli)
curl -X DELETE http://localhost:8080/v1/categories/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Senaryo 2: Error Handling

```bash
# 404 - Not Found
curl http://localhost:8080/v1/categories/99999

# 401 - Unauthorized (token olmadan protected endpoint)
curl -X POST http://localhost:8080/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Test"}'

# 400 - Bad Request (geçersiz data)
curl -X POST http://localhost:8080/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"invalid":"data"}'
```

## 📊 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/health` | ❌ | Health check |
| GET | `/api` | ❌ | API root info |
| GET | `/api/info` | ❌ | System information |
| GET | `/v1/categories` | ❌ | Get all categories |
| GET | `/v1/categories/{id}` | ❌ | Get category by ID |
| POST | `/v1/categories` | ✅ | Create category |
| PUT | `/v1/categories/{id}` | ✅ | Update category |
| DELETE | `/v1/categories/{id}` | ✅ | Delete category |

## 🛠️ Development Tools

### Postman Collection

OpenAPI spec'ten Postman collection oluşturun:

1. Postman'ı açın
2. Import > Upload Files
3. `openapi-spec.yaml` dosyasını seçin
4. Collection otomatik oluşturulacak

### VS Code Extensions

- **OpenAPI (Swagger) Editor**: YAML düzenleme ve preview
- **REST Client**: `.http` dosyaları ile test
- **Thunder Client**: Postman alternatifi

### Swagger UI (Alternatif)

Eğer Swagger UI çalışırsa:
```
http://localhost:8080/swagger-ui.html
```

## 📈 Performans Testi

Apache Bench ile basit performans testi:

```bash
# 100 request, 10 concurrent
ab -n 100 -c 10 http://localhost:8080/api/health

# POST request test
ab -n 100 -c 10 -T 'application/json' -p data.json http://localhost:8080/v1/categories
```

## 🐛 Troubleshooting

### Uygulama Başlamıyor

```bash
# Port kontrolü
lsof -ti:8080

# Process'i öldür
lsof -ti:8080 | xargs kill -9

# Java version kontrolü
java -version  # 17 olmalı
```

### OpenAPI Generator Hataları

```bash
# Docker image'i temizle
docker rmi openapitools/openapi-generator-cli:latest

# Yeniden indir
docker pull openapitools/openapi-generator-cli:latest
```

### TypeScript Client Hataları

```bash
# Node modules temizle
cd generated-client
rm -rf node_modules package-lock.json
npm install
```

## 📚 Kaynaklar

- [OpenAPI Specification](https://swagger.io/specification/)
- [OpenAPI Generator Docs](https://openapi-generator.tech/)
- [Swagger Editor](https://editor.swagger.io/)
- [Axios Documentation](https://axios-http.com/)

## ✅ Checklist

- [x] OpenAPI specification oluşturuldu
- [x] Shell test script'i hazır
- [x] TypeScript client generate edildi
- [x] Örnek kullanım kodu yazıldı
- [x] Dokümentasyon tamamlandı
- [ ] Postman collection oluştur
- [ ] Integration testleri yaz
- [ ] CI/CD pipeline'a entegre et

---

**Son Güncelleme**: 2025-10-15
**Versiyon**: 1.0.0
