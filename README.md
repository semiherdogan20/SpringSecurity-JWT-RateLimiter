Spring Security Authentication System (JWT + OTP + Rate Limiter)
📌 Projeye Genel Bakış

Bu proje, Spring Security kullanılarak geliştirilmiş güvenlik odaklı bir Authentication sistemidir.
Sistem; email-password doğrulaması, OTP tabanlı two-factor verification ve gelişmiş audit logging mekanizmalarını içerir.

Proje ağırlıklı olarak backend mimarisi ve güvenlik mekanizmalarına odaklanır, frontend kısmı minimum seviyede tutulmuştur.

🎯 Temel Amaç

Bu projenin temel amacı, basit login sistemlerinin ötesine geçerek production odaklı bir authentication mimarisi göstermektir.

Sistem şu prensipler üzerine tasarlanmıştır:

Stateless authentication

Multi-layer security mekanizmaları

Detaylı login auditing

Brute-force saldırılarına karşı koruma

🚀 Öne Çıkan Özellikler
🔐 Two-Factor Authentication (OTP)

JavaMailSender email gönderimini simüle etmek için kullanılmıştır.

Hem register hem de login süreçleri OTP doğrulaması içerir.

6 haneli OTP kodu üretilir

Kod email üzerinden kullanıcıya gönderilir

Kullanıcı doğrulamayı tamamlamadan authentication süreci bitmez

👤 Akıllı User Status Yönetimi

Kullanıcı hesapları üç farklı durumda yönetilir:

PENDING   → Email doğrulaması tamamlanmamış
ACTIVE    → Hesap doğrulanmış ve kullanılabilir
BLOCKED   → Sistem tarafından engellenmiş kullanıcı

Bu yapı sayesinde account lifecycle yönetimi ile authentication flow birbirinden ayrılmış olur.

📊 Gelişmiş Audit Logging

Her login attempt aşağıdaki security metadata ile kayıt altına alınır:

IP Address

Timestamp

Device information (User-Agent)

Country bilgisi

Success / failure reason

Bu loglar sayesinde security monitoring ve audit işlemleri yapılabilir.

🛡 Rate Limiting Koruması

Sistemi brute-force login saldırılarına karşı korumak için Rate Limiting mekanizması uygulanmıştır.

Kurallar:

1 dakika içinde maksimum 5 login attempt

Limit aşılırsa:

HTTP 429 - Too Many Requests
🧠 Teknik Detaylar
Hybrid Key-Based Rate Limiter

Standart sistemlerde sadece IP adresi kullanılırken, bu projede şu anahtar kullanılır:

Key = IP + Email

Avantajları:

Aynı ağı kullanan masum kullanıcıların engellenmesini önler

Saldırıya uğrayan hesap özel olarak korunur

Daha hassas saldırı önleme sağlar

Sliding Window Rate Limiting

Sistem yalnızca son 1 dakika içindeki denemeleri takip eder.

Çalışma mantığı:

1 dakikadan eski denemeler silinir

Kalan denemeler sayılır

Limit aşılmışsa istek reddedilir

Bu yaklaşım Sliding Window Rate Limiting olarak bilinir.

Bilinçli Mimari Tercih (In-Memory vs Redis)

Rate limiting verileri in-memory HashMap yapısında tutulur.

Bu tercihin sebepleri:

Geliştirme sürecinde karmaşıklığı azaltmak

Single-instance uygulamalarda yüksek performans sağlamak

Rate limiting algoritmasını daha kolay inceleyebilmek

⚠️ Production Notu

Distributed veya multi-server sistemlerde Redis tabanlı distributed rate limiting önerilir.

Güçlendirilmiş OTP Security

OTP sistemi aşağıdaki ek güvenlik önlemlerini içerir:

Her OTP için maksimum 5 verification attempt

OTP için expiration süresi

2 dakika resend cooldown

Kullanılmış OTP kodları tekrar kullanılamaz

Bu mekanizmalar OTP brute-force saldırılarını zorlaştırır.

Security State Ayrımı

Bu projedeki önemli mimari kararlardan biri şu iki durumun ayrılmasıdır:

User account state
ve
Login verification state

Bu ayrım sayesinde authentication süreci account lifecycle durumunu yanlışlıkla değiştirmez ve security ambiguity oluşmaz.

🧰 Technology Stack

Java

Spring Boot

Spring Security

JWT (JJWT)

Spring Data JPA

MySQL

JavaMailSender

⚙️ Nasıl Çalıştırılır
1️⃣ Ortamı Yapılandırın

Aşağıdaki dosyada gerekli ayarları yapın:

src/main/resources/application.properties

Eklenmesi gereken bilgiler:

MySQL database bilgileri

Gmail SMTP App Password

2️⃣ Uygulamayı Çalıştırın

Uygulama başlatıldığında:

Hibernate gerekli database şemasını otomatik oluşturur

Database adı:

springsecurityauth
3️⃣ API'yi Test Edin

Yeni bir kullanıcı oluşturmak için:

POST /api/register

Daha sonra OTP doğrulamasını tamamlayarak authentication işlemini gerçekleştirebilirsiniz.

💡 Projenin Amacı

Bu proje aşağıdaki backend security mekanizmalarını göstermek amacıyla geliştirilmiştir:

authentication flows

OTP management

login auditing

rate limiting

multi-layer login protection
