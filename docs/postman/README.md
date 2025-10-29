# MorseMate Postman Collections

Bu klasor, MorseMate API'lerini test etmek icin hazirlanmis Postman collection'larini icerir.

## Dosyalar

1. **MorseMate_Gamification_API.postman_collection.json**
   - Gamification System icin eksiksiz API collection
   - 60+ endpoint
   - 3 integration test flow

2. **MorseMate_Environment.postman_environment.json**
   - Environment degiskenleri
   - Otomatik token yonetimi

## Kurulum

### 1. Postman'i Yukleyin
- [Postman Desktop](https://www.postman.com/downloads/) indirebilir veya
- [Postman Web](https://web.postman.com/) kullanabilirsiniz

### 2. Collection'i Import Edin

**Yontem 1: Dosyadan Import**
1. Postman'i acin
2. Sol ustteki "Import" butonuna tiklayin
3. "Upload Files" butonuna tiklayin
4. `MorseMate_Gamification_API.postman_collection.json` dosyasini secin
5. "Import" butonuna tiklayin

**Yontem 2: Drag & Drop**
1. Postman'i acin
2. `MorseMate_Gamification_API.postman_collection.json` dosyasini Postman penceresine surukleme

### 3. Environment'i Import Edin

Ayni sekilde `MorseMate_Environment.postman_environment.json` dosyasini da import edin:
1. Postman'de sag ustteki "Environments" tab'ine tiklayin
2. "Import" butonuna tiklayin
3. `MorseMate_Environment.postman_environment.json` dosyasini secin
4. Import edin

### 4. Environment'i Aktif Edin

1. Sag ustteki dropdown menuyu acin
2. "MorseMate Environment"i secin

## Kullanim

### Temel Kullanim

#### 1. Sunucuyu Calistirin
```bash
./gradlew bootRun
```

Sunucu `http://localhost:8080` adresinde calismaya baslayacak.

#### 2. Authentication (Giris)

Once "Authentication" > "Login" request'ini calistirin:
- Otomatik olarak token alinir ve environment'a kaydedilir
- Tum diger istekler bu token'i kullanir

Default credentials:
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```

#### 3. API Endpoint'lerini Test Edin

Collection asagidaki kategorilere ayrilmistir:
- **Authentication**: Login ve Register
- **Achievements**: Basari sistemi (user + admin)
- **Gems**: Gem ekonomisi (user + admin)
- **Power-ups**: Guc arttirici sistemler (user + admin)
- **Leaderboards**: Lider tablolari
- **Integration Test Flows**: Kapsamli test akislari

### Integration Test Flows

Collection'da 3 hazir test akisi bulunur:

#### Flow 1: Complete Learning Journey with Gamification
Tam bir ogrenme yolculugunu test eder:
1. Login
2. Baslangic gem bakiyesini al
3. Egzersiz tamamla (gem ve puan kazan)
4. Gem artisini dogrula
5. Basari ilerlemesini kontrol et

#### Flow 2: Power-up Purchase and Usage
Power-up satin alma ve kullanma akisini test eder:
1. Gem bakiyesini kontrol et
2. Mevcut power-up'lari listele
3. Power-up satin al
4. Power-up'i aktif et
5. Aktif power-up'lari dogrula

#### Flow 3: Leaderboard Competition
Lider tablosu yarismasini test eder:
1. Mevcut siralamani al
2. En iyi oyunculari gor
3. Egzersiz tamamlayarak puan kazan
4. Guncel siralamani kontrol et

### Test Flow'larini Calistirma

1. Bir flow klasorunu secin
2. Sag tiklayin ve "Run folder" secin
3. "Run" butonuna tiklayin
4. Test sonuclarini inceleyin

## API Endpoint'leri

### Achievements (14 endpoints)
- **User**: Get my achievements, check progress, view available
- **Admin**: CRUD operations, statistics

### Gems (15 endpoints)
- **User**: Balance, spend, transactions, statistics
- **Admin**: Add gems, award bonuses, view user gems

### Power-ups (18 endpoints)
- **User**: View owned, purchase, activate, check active
- **Admin**: CRUD operations, cleanup expired

### Leaderboards (10 endpoints)
- View top users by: points, level, streak, achievements
- Get personal ranks in each category

## Environment Degiskenleri

Collection otomatik olarak su degiskenleri kullanir:

| Degisken | Aciklama | Varsayilan |
|----------|----------|------------|
| `baseUrl` | API base URL | `http://localhost:8080` |
| `authToken` | JWT authentication token | Auto-set on login |
| `userId` | Logged-in user ID | Auto-set on login |
| `initialGems` | Initial gem balance | Auto-set in flows |
| `powerUpId` | Selected power-up ID | Auto-set in flows |
| `userPowerUpId` | User's power-up instance ID | Auto-set in flows |

## Pre-request Scripts

Collection'daki tum istekler otomatik olarak `authToken`'i kullanir. Login request'inden sonra token otomatik olarak kaydedilir.

## Test Scripts

Her request, response'u dogrulayan test scriptleri icerir:
- Status code kontrolu
- Response body validation
- Environment variable updates

## Troubleshooting

### "Authorization failed" hatasi
- Once "Authentication" > "Login" request'ini calistirin
- Token'in environment'a kaydedildigini kontrol edin

### "Connection refused" hatasi
- Sunucunun calistigini kontrol edin: `./gradlew bootRun`
- `baseUrl`'in dogru oldugunu kontrol edin

### "Insufficient gems" hatasi
- Admin olarak login yapin
- "Gems" > "Admin Operations" > "Add Gems to User" ile gem ekleyin

## Admin Operations

Bazi endpoint'ler admin yetkisi gerektirir. Admin olarak login olmak icin:

```json
{
    "email": "admin@example.com",
    "password": "adminpassword"
}
```

Veya database'de bir kullaniciyi admin yapin:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'user@example.com';
```

## Ornek Scenarios

### Scenario 1: Yeni Kullanici Journey
1. Register ile yeni kullanici olustur
2. Login yap
3. Available achievements'i gor
4. Bir egzersiz tamamla
5. Kazanilan gem ve achievement'leri kontrol et

### Scenario 2: Power-up ile Ogrenim
1. Login yap
2. XP Boost power-up satin al
3. Power-up'i aktif et
4. Egzersiz tamamla (artmis XP ile)
5. Kazanilan extra XP'yi dogrula

### Scenario 3: Liderlik Yarisma
1. Login yap
2. Mevcut leaderboard pozisyonunu gor
3. Birden fazla egzersiz tamamla
4. Yeni pozisyonunu kontrol et
5. Top 10'a girip girmedigini kontrol et

## Ek Kaynaklar

- API Dokumantasyonu: `/docs/GAMIFICATION.md`
- OpenAPI Specification: `/openapi-spec.yaml`
- Kaynak Kod: `/src/main/java/me/aydgn/MorseMate/controller/`

## Destek

Sorulariniz icin:
- GitHub Issues: [PPAW-MorsaMate-Backend](https://github.com/aydgnme/PPAW-MorsaMate-Backend/issues)
- Branch: `Milestone-3--Gamification`
