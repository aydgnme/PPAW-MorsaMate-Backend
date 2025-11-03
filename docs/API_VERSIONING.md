# API Versiyonlama

MorseMate API'si dinamik versiyonlamayı destekler. API versiyonu `.env` dosyasından yönetilir.

## Kullanım

### 1. API Versiyonunu Değiştirme

`.env` dosyasında `API_VERSION` değerini güncelleyin:

```bash
# .env dosyası
API_VERSION=v1  # veya v2, v3, etc.
```

### 2. Otomatik Güncelleme

API versiyon değişikliği yapıldığında, tüm endpoint'ler otomatik olarak güncellenir:

**v1 kullanırken:**
```
POST /v1/auth/login
GET  /v1/users/me
POST /v1/lessons
GET  /v1/achievements
```

**v2'ye geçince:**
```
POST /v2/auth/login
GET  /v2/users/me
POST /v2/lessons
GET  /v2/achievements
```

### 3. Yapılandırma Dosyaları

API versiyonu şu dosyalarda tanımlanır:

1. **`.env`** (environment variable)
   ```bash
   API_VERSION=v1
   ```

2. **`application.properties`** (Spring configuration)
   ```properties
   api.version=${API_VERSION:v1}
   ```

3. **`ApiConfig.java`** (Java configuration class)
   ```java
   @Value("${api.version:v1}")
   private String version;
   ```

### 4. Controller'larda Kullanım

Tüm controller'lar dinamik versiyon kullanır:

```java
@RestController
@RequestMapping("/${api.version}/lessons")
public class LessonController {
    // ...
}
```

## Versiyonlanmış Controller'lar

Aşağıdaki controller'lar dinamik versiyonlamayı kullanır:

### Gamification System
- ✅ `/v1/achievements` - Achievement Controller
- ✅ `/v1/gems` - Gem Controller
- ✅ `/v1/powerups` - PowerUp Controller
- ✅ `/v1/leaderboard` - Leaderboard Controller

### Learning System
- ✅ `/v1/categories` - Category Controller
- ✅ `/v1/lessons` - Lesson Controller
- ✅ `/v1/exercises` - Exercise Controller
- ✅ `/v1/attempts` - Exercise Attempt Controller
- ✅ `/v1/progress` - User Progress Controller

### User Management
- ✅ `/v1/auth` - Authentication Controller
- ✅ `/v1/users` - User Controller

**Toplam: 11 controller versiyonlandı**

## Versiyonlanmayan Endpoint'ler

Bazı endpoint'ler API versiyonundan bağımsızdır:

- `/api/health` - Health Check (HealthController)
- `/api/system` - System Info (SystemInfoController)
- `/api-docs` - API Documentation (ApiDocumentationController)
- `/` - API Root (ApiRootController)

Bu endpoint'ler versiyon değişikliğinden etkilenmez.

## Test

### Mevcut Versiyonu Kontrol Etme

```bash
# Sunucuyu başlat
./gradlew bootRun

# API'yi test et
curl http://localhost:8080/v1/api/health
```

### Versiyon Değiştirme

```bash
# .env dosyasını güncelle
echo "API_VERSION=v2" >> .env

# Sunucuyu yeniden başlat
./gradlew bootRun

# Yeni versiyonu test et
curl http://localhost:8080/v2/api/health
```

## Postman Collection

Postman collection'ı da dinamik versiyonlamayı destekler:

1. Environment variable olarak tanımlanmış: `{{baseUrl}}/{{apiVersion}}/...`
2. API versiyonu değiştiğinde collection otomatik güncellenir
3. Örnek: `{{baseUrl}}/v1/auth/login` → `{{baseUrl}}/v2/auth/login`

## Avantajlar

✅ **Merkezi Yönetim**: Tek bir yerden tüm API versiyonunu yönetin
✅ **Kolay Geçiş**: v1'den v2'ye geçiş bir satır değişiklik
✅ **Geriye Uyumluluk**: Eski versiyonları destekleyebilirsiniz
✅ **Temiz Kod**: Controller'larda hardcoded versiyon yok
✅ **Esnek**: Farklı ortamlarda farklı versiyonlar kullanabilirsiniz

## Örnek Senaryolar

### Senaryo 1: Production'da v1, Development'ta v2

**Development (.env):**
```bash
API_VERSION=v2
```

**Production (.env):**
```bash
API_VERSION=v1
```

### Senaryo 2: Beta Test

Beta kullanıcıları için v2'yi test ederken, normal kullanıcılar v1 kullanabilir:

```bash
# Beta sunucu
API_VERSION=v2

# Production sunucu
API_VERSION=v1
```

### Senaryo 3: Gradual Migration

Yavaş yavaş v2'ye geçiş yapabilirsiniz:

1. Önce v2'yi development'ta test edin
2. Sonra staging'de test edin
3. Son olarak production'da aktif edin

## Notlar

- Default versiyon: `v1`
- Versiyon formatı: `v` + numara (örn: v1, v2, v3)
- Versiyon değişikliği server restart gerektirir
- Environment variable yoksa `v1` kullanılır

## Troubleshooting

**Problem:** API endpoint'leri bulunamıyor
- **Çözüm:** `.env` dosyasında `API_VERSION` tanımlı mı kontrol edin

**Problem:** Yeni versiyon çalışmıyor
- **Çözüm:** Sunucuyu yeniden başlatın (`./gradlew bootRun`)

**Problem:** Postman'de endpoint'ler çalışmıyor
- **Çözüm:** Postman environment'ı güncelleyin veya URL'leri manuel olarak değiştirin
