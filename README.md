# 🚀 100 Days With Spring Boot

Selamat datang di perjalanan 100 hari membangun dan memperdalam skill dengan **Spring Boot**!  
Proyek ini mendokumentasikan pembelajaran harian saya selama 100 hari, mencakup topik-topik penting seperti:

- Spring Boot dasar
- REST API
- Spring Data JPA
- Integrasi dengan database (PostgreSQL)
- Caching & Memory Maintenance
- Asynchronous processing
- Apache Camel
- Microservices

## 📅 Progress

| Hari | Topik | Status |
|------|-------|--------|
| 1 | Membuat project Spring Boot dari Spring Initializr | ✅ |
| 2 | Struktur folder & konfigurasi Maven | ✅ |
| 3 | Membuat REST API pertama | ✅ |
| 4 | Melengkapi CRUD Customer (getById, create, update, delete) | ✅ |
| 5 | Pisahkan Entity dari API response dengan DTO & Mapper | ✅ |
| 6 | Bean Validation pada request (`@NotBlank`, `@Email`, `@Valid`) | ✅ |
| 7 | Global Exception Handling (`@RestControllerAdvice`) | ✅ |
| 8 | Unit Testing dengan JUnit & Mockito (`CustomerService`, `CustomerController`) | ✅ |
| 9 | Integration Testing dengan `@SpringBootTest`, `TestRestTemplate`, dan H2 In-Memory Database | ✅ |
| 10 | Pagination & Sorting dengan Spring Data JPA (`Pageable`, `PagedResponse`) | ✅ |
| 11 | JPA Auditing (`@EnableJpaAuditing`, `@CreatedDate`, `@LastModifiedDate`, `BaseEntity`) | ✅ |
| 12 | Spring Cache (`@EnableCaching`, `@Cacheable`, `@CachePut`, `@CacheEvict`) | ✅ |
| 13 | Asynchronous Processing (`@EnableAsync`, `@Async`, `CompletableFuture`, `ThreadPoolTaskExecutor`) | ✅ |
| ... | ... | ⏳ |

## 🧰 Teknologi

- Java 17+
- Spring Boot 3.x
- Maven
- Spring Data JPA
- PostgreSQL
- Lombok
- Apache Camel
- Docker (untuk deploy)
- Git & GitHub

## 📌 Tujuan

- Konsisten belajar 100 hari tanpa jeda
- Meningkatkan pemahaman konsep Spring Boot secara mendalam
- Menjadi lebih siap untuk proyek real-world dan low-latency backend development

## 💡 Catatan

Posting harian juga akan dibagikan di [LinkedIn saya](https://www.linkedin.com/in/idris-9577bb56)

---

## ✅ Cara Clone Repo GitHub

Jalankan perintah ini di terminal (Command Prompt, Git Bash, atau PowerShell):

```bash
git clone https://github.com/idsoke/100DaysWithSpringboot.git
```

Setelah itu, akan terbentuk folder bernama `100DaysWithSpringboot` di direktori kamu sekarang, berisi semua file dari repo GitHub.

### 📌 Langkah Tambahan Setelah Clone (opsional)

Masuk ke folder project:

```bash
cd 100DaysWithSpringboot
```

Cek branch yang aktif:

```bash
git branch
```

Jalankan project (kalau project Spring Boot):

```bash
./mvnw spring-boot:run
```

atau

```bash
mvn spring-boot:run
```
