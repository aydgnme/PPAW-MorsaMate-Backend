# MorseMate Postman Collections

Bu klasörde MorseMate API'sini test etmek için Postman collection'ları bulunmaktadır.

## 📦 Dosyalar

- **Lesson-API.postman_collection.json** - Lesson API için kapsamlı test collection'ı
- **MorseMate-Local.postman_environment.json** - Local development environment değişkenleri

## 🚀 Kurulum

### 1. Postman'e Import Etme

1. Postman'i açın
2. Sol üst köşede **Import** butonuna tıklayın
3. **File** tab'ini seçin
4. Her iki JSON dosyasını da seçin ve import edin

### 2. Environment Seçimi

1. Sağ üst köşede environment dropdown'ı açın
2. **MorseMate - Local** environment'ı seçin

## 📋 Collection İçeriği

### 1. Authentication (2 endpoint)
- **Login as Admin** - Admin token alır ve otomatik olarak environment'a kaydeder
- **Login as User** - Normal kullanıcı token'ı alır

### 2. Lessons - Public Endpoints (5 endpoint)
- **Get Lesson by ID** - ID'ye göre ders getir
- **Get Lesson with Exercises** - Exercises dahil ders getir
- **Get Lessons by Category** - Kategoriye göre dersleri listele
- **Search Lessons by Title** - Başlığa göre ders ara (pagination)
- **Get Exercise Count** - Ders için exercise sayısı

### 3. Lessons - Admin Endpoints (5 endpoint)
- **Create Lesson** - Yeni ders oluştur (Admin)
- **Create Intermediate Lesson** - İleri seviye ders oluştur (Admin)
- **Update Lesson** - Dersi güncelle (Admin)
- **Update Lesson Order** - Ders sırasını güncelle (Admin)
- **Delete Lesson** - Dersi sil (Admin)

### 4. Error Scenarios (4 endpoint)
- **Get Non-Existent Lesson** - Olmayan ders (404)
- **Create Lesson Without Auth** - Yetkilendirme olmadan (401/403)
- **Create Lesson with Invalid Difficulty** - Geçersiz difficulty (400)
- **Create Lesson with Invalid Category** - Geçersiz category (404)

## 🔧 Kullanım

### İlk Adımlar

1. **Authentication** klasöründeki **Login as Admin** endpoint'ini çalıştırın
   - Bu otomatik olarak `admin_token` değişkenini environment'a kaydeder
   - Admin endpoint'leri test etmek için gereklidir

2. **Public Endpoints** klasöründeki endpoint'leri test edin
   - Bu endpoint'ler authentication gerektirmez
   - Herkes erişebilir

3. **Admin Endpoints** klasöründeki endpoint'leri test edin
   - Bu endpoint'ler admin token gerektirir
   - Bearer token otomatik olarak eklenir

### Test Senaryoları

#### Senaryo 1: Yeni Ders Oluşturma ve Test Etme

```
1. Login as Admin
2. Create Lesson
   → Response'dan lesson ID'yi not edin veya {{created_lesson_id}} değişkenini kullanın
3. Get Lesson by ID
   → Oluşturulan dersi görüntüleyin
4. Update Lesson
   → Dersi güncelleyin
5. Get Exercise Count
   → Exercise sayısını kontrol edin
6. Delete Lesson
   → Dersi silin
```

#### Senaryo 2: Kategori Bazlı Test

```
1. Environment'da category_id = 1 ayarlayın
2. Create Lesson (2-3 kez farklı başlıklarla)
3. Get Lessons by Category
   → Kategorideki tüm dersleri görün
4. Update Lesson Order
   → Ders sıralamasını değiştirin
```

#### Senaryo 3: Arama Testi

```
1. Create Lesson (Başlık: "Introduction to Morse Code")
2. Create Intermediate Lesson (Başlık: "Advanced Morse Techniques")
3. Search Lessons by Title (title=morse)
   → Her iki dersi de bulmalı
```

## 🔐 Authentication

Admin endpoint'leri Bearer token gerektirir. Token otomatik olarak:
- Login endpoint'lerinden alınır
- Environment değişkenlerine kaydedilir
- Request'lere otomatik olarak eklenir

### Manuel Token Ekleme

Eğer token manuel eklemek isterseniz:

1. Collection veya Request'i seçin
2. **Authorization** tab'ine gidin
3. Type: **Bearer Token**
4. Token: `{{admin_token}}`

## 📊 Environment Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|------------|
| `base_url` | API base URL | http://localhost:8080 |
| `admin_token` | Admin JWT token | (Login sonrası otomatik) |
| `user_token` | User JWT token | (Login sonrası otomatik) |
| `category_id` | Test için kategori ID | 1 |
| `lesson_id` | Test için ders ID | 1 |
| `created_lesson_id` | Yeni oluşturulan ders ID | (Create sonrası otomatik) |

## ✅ Test Scripts

Her endpoint otomatik test script'leri içerir:

- **Status Code Kontrolü** - Response status code'u kontrol eder
- **Response Validation** - Response yapısını doğrular
- **Auto Variable Setting** - Gerekli değişkenleri otomatik kaydeder

### Test Sonuçlarını Görüntüleme

1. Request'i çalıştırın
2. Response altında **Test Results** tab'ine tıklayın
3. ✅ Yeşil = Başarılı, ❌ Kırmızı = Başarısız

## 🎯 Best Practices

1. **Her Zaman Environment Kullanın** - Hard-coded değerler kullanmayın
2. **Sıralı Test Yapın** - Authentication → Public → Admin
3. **Test Scripts'i Kontrol Edin** - Otomatik validasyon'ları inceleyin
4. **Environment'ı Güncelleyin** - Test ID'leri değiştiğinde güncelleyin

## 🐛 Troubleshooting

### 401 Unauthorized

- `admin_token` değişkenini kontrol edin
- **Login as Admin** endpoint'ini tekrar çalıştırın
- Token'ın expire olmadığından emin olun

### 404 Not Found

- `lesson_id` veya `category_id` değişkenlerini kontrol edin
- Veritabanında bu ID'lerin var olduğundan emin olun

### 400 Bad Request

- Request body'yi kontrol edin
- Required field'ların dolu olduğundan emin olun
- Enum değerlerinin doğru olduğunu kontrol edin (BEGINNER, INTERMEDIATE, ADVANCED)

## 📖 API Endpoint Referansı

### Lesson Difficulty Levels
- `BEGINNER` - Başlangıç seviyesi
- `INTERMEDIATE` - Orta seviye
- `ADVANCED` - İleri seviye

### Response Codes
- `200` - Başarılı
- `201` - Oluşturuldu
- `400` - Hatalı istek
- `401` - Yetkisiz
- `403` - Yasak
- `404` - Bulunamadı

## 🔄 Güncelleme

Collection güncellemelerini almak için:

1. Bu dosyaları güncelleyin
2. Postman'de **Import** → **Replace** ile yeniden import edin

## 📞 Destek

Sorun yaşarsanız:
- API dokümantasyonunu kontrol edin: `/docs/API.md`
- Backend loglarını inceleyin
- GitHub issues'a sorun bildirin

---

**Not:** Bu collection'lar local development için hazırlanmıştır. Production environment için ayrı bir environment dosyası oluşturun.
