# Hari 6 — Bean Validation pada Request

Rencana kerja untuk lanjutan besok: menambahkan validasi input pada `CustomerRequest` memakai Jakarta Bean Validation, supaya request yang tidak valid ditolak sebelum masuk ke service/repository.

## Tujuan

- Mencegah data kosong/tidak valid tersimpan ke database (misal `name` kosong, `email` bukan format email).
- Mengembalikan response error yang jelas (400 Bad Request) ketika validasi gagal, bukan exception generic.

## Langkah

1. **Tambah dependency** di `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
   </dependency>
   ```

2. **Tambah anotasi validasi** di `dto/CustomerRequest.java`:
   ```java
   @Data
   public class CustomerRequest {
       @NotBlank(message = "Nama tidak boleh kosong")
       private String name;

       @NotBlank(message = "Email tidak boleh kosong")
       @Email(message = "Format email tidak valid")
       private String email;
   }
   ```

3. **Aktifkan validasi** di `CustomerController` dengan `@Valid` pada parameter `@RequestBody`:
   ```java
   @PostMapping
   public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) { ... }

   @PutMapping("/{id}")
   public CustomerResponse updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) { ... }
   ```

4. **(Opsional, lanjutan ke topik exception handling)** Tangani `MethodArgumentNotValidException` lewat `@ControllerAdvice` supaya response error berisi daftar field yang gagal validasi, bukan stack trace default Spring.

## Catatan

- Topik ini sengaja dipisah dari exception handling global (`@ControllerAdvice` untuk `RuntimeException` saat resource not found) — akan digabung clean-up-nya saat masuk topik "Global Exception Handling".
- Setelah ini selesai, update tabel Progress di `README.md` (Hari 6) dan commit.
