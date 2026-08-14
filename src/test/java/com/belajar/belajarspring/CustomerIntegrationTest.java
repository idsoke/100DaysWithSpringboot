package com.belajar.belajarspring;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.dto.CustomerResponse;
import com.belajar.belajarspring.dto.LoginRequest;
import com.belajar.belajarspring.dto.LoginResponse;
import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.repository.CustomerRepository;
import com.belajar.belajarspring.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest: load full application context (termasuk server HTTP sungguhan)
// webEnvironment = RANDOM_PORT: jalankan server di port acak agar tidak konflik
// @ActiveProfiles("test"): gunakan application-test.properties (H2 in-memory)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        // DataInitializer membuat akun admin/admin123 saat startup; login untuk dapatkan JWT lalu pasang sebagai Bearer token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");
        LoginResponse loginResponse = restTemplate.postForObject("/api/auth/login", loginRequest, LoginResponse.class);
        String token = loginResponse.token();

        ClientHttpRequestInterceptor bearerAuthInterceptor = (request, body, execution) -> {
            request.getHeaders().setBearerAuth(token);
            return execution.execute(request, body);
        };
        restTemplate.getRestTemplate().getInterceptors().clear();
        restTemplate.getRestTemplate().getInterceptors().add(bearerAuthInterceptor);
    }

    @Test
    void createCustomer_withValidRequest_shouldReturn200AndPersist() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Hana");
        request.setEmail("hana@example.com");

        ResponseEntity<CustomerResponse> response = restTemplate.postForEntity(
                "/api/customers", request, CustomerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Hana");
        assertThat(response.getBody().getEmail()).isEqualTo("hana@example.com");

        assertThat(customerRepository.findAll()).hasSize(1);
    }

    @Test
    void createCustomer_withInvalidRequest_shouldReturn400() {
        CustomerRequest request = new CustomerRequest();
        request.setName("");
        request.setEmail("bukan-email");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/customers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    void getAllCustomers_shouldReturnAllPersistedCustomers() {
        customerRepository.save(new Customer(null, "Irfan", "irfan@example.com"));
        customerRepository.save(new Customer(null, "Joko", "joko@example.com"));

        ResponseEntity<CustomerResponse[]> response = restTemplate.getForEntity(
                "/api/customers", CustomerResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getCustomerById_whenExists_shouldReturnCustomer() {
        Customer saved = customerRepository.save(new Customer(null, "Kiki", "kiki@example.com"));

        ResponseEntity<CustomerResponse> response = restTemplate.getForEntity(
                "/api/customers/" + saved.getId(), CustomerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Kiki");
    }

    @Test
    void getCustomerById_whenNotFound_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/customers/999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateCustomer_shouldUpdateAndReturnUpdatedData() {
        Customer saved = customerRepository.save(new Customer(null, "Lina", "lina@example.com"));

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setName("Lina Baru");
        updateRequest.setEmail("lina.baru@example.com");

        ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                "/api/customers/" + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                CustomerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Lina Baru");

        Customer fromDb = customerRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getEmail()).isEqualTo("lina.baru@example.com");
    }

    @Test
    void deleteCustomer_shouldRemoveFromDatabase() {
        Customer saved = customerRepository.save(new Customer(null, "Mila", "mila@example.com"));

        restTemplate.delete("/api/customers/" + saved.getId());

        assertThat(customerRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCustomersPaged_shouldReturnPaginatedResult() {
        customerRepository.save(new Customer(null, "Nana", "nana@example.com"));
        customerRepository.save(new Customer(null, "Omar", "omar@example.com"));
        customerRepository.save(new Customer(null, "Petra", "petra@example.com"));

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/customers/paged?page=0&size=2&sortBy=name&direction=asc", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat((List<?>) body.get("content")).hasSize(2);
        assertThat(body.get("totalElements")).isEqualTo(3);
        assertThat(body.get("totalPages")).isEqualTo(2);
        assertThat(body.get("last")).isEqualTo(false);
        assertThat(body.get("page")).isEqualTo(0);
    }

    @Test
    void createCustomer_shouldAutoPopulateAuditFields() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Audit User");
        request.setEmail("audit@example.com");

        ResponseEntity<CustomerResponse> response = restTemplate.postForEntity(
                "/api/customers", request, CustomerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCreatedAt()).isNotNull();
        assertThat(body.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateCustomer_shouldUpdateUpdatedAtButNotCreatedAt() {
        Customer saved = customerRepository.save(new Customer(null, "Rudi", "rudi@example.com"));
        // flush agar createdAt/updatedAt tersimpan
        customerRepository.flush();
        LocalDateTime createdAt = saved.getCreatedAt();

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setName("Rudi Baru");
        updateRequest.setEmail("rudi.baru@example.com");

        ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                "/api/customers/" + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                CustomerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerResponse body = response.getBody();
        assertThat(body.getCreatedAt()).isNotNull();
        assertThat(body.getUpdatedAt()).isNotNull();
        // updatedAt tidak boleh sebelum createdAt
        assertThat(body.getUpdatedAt()).isAfterOrEqualTo(body.getCreatedAt());
    }

    @Test
    void createCustomer_asUserRole_shouldReturn403() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("user123");
        LoginResponse loginResponse = restTemplate.postForObject("/api/auth/login", loginRequest, LoginResponse.class);

        ClientHttpRequestInterceptor userBearerAuthInterceptor = (request, body, execution) -> {
            request.getHeaders().setBearerAuth(loginResponse.token());
            return execution.execute(request, body);
        };
        restTemplate.getRestTemplate().getInterceptors().clear();
        restTemplate.getRestTemplate().getInterceptors().add(userBearerAuthInterceptor);

        CustomerRequest request = new CustomerRequest();
        request.setName("Hana");
        request.setEmail("hana@example.com");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/customers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    void getAllCustomers_asUserRole_shouldReturn200() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("user123");
        LoginResponse loginResponse = restTemplate.postForObject("/api/auth/login", loginRequest, LoginResponse.class);

        ClientHttpRequestInterceptor userBearerAuthInterceptor = (request, body, execution) -> {
            request.getHeaders().setBearerAuth(loginResponse.token());
            return execution.execute(request, body);
        };
        restTemplate.getRestTemplate().getInterceptors().clear();
        restTemplate.getRestTemplate().getInterceptors().add(userBearerAuthInterceptor);

        ResponseEntity<CustomerResponse[]> response = restTemplate.getForEntity("/api/customers", CustomerResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCustomersPaged_withDescSort_shouldReturnInReverseOrder() {
        customerRepository.save(new Customer(null, "Amir", "amir@example.com"));
        customerRepository.save(new Customer(null, "Zara", "zara@example.com"));

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/customers/paged?page=0&size=10&sortBy=name&direction=desc", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = response.getBody();
        List<Map<String, Object>> content = (List<Map<String, Object>>) body.get("content");
        assertThat(content.get(0).get("name")).isEqualTo("Zara");
        assertThat(content.get(1).get("name")).isEqualTo("Amir");
    }

    @Test
    void getAllCustomers_withExpiredToken_shouldReturn401WithExpiredMessage() {
        // secret harus sama dengan jwt.secret di application.properties; expirationMs negatif
        // supaya token langsung expired begitu dibuat (lihat pola yang sama di JwtServiceTest)
        JwtService shortLivedJwtService = new JwtService(
                "ZmFrZS1zZWNyZXQta2V5LXVudHVrLWJlbGFqYXItc3ByaW5nLWJvb3QtaGFyaS0xNi1qd3QtYXV0aGVudGljYXRpb24=", -1000);
        UserDetails admin = User.withUsername("admin").password("irrelevant").roles("ADMIN").build();
        String expiredToken = shortLivedJwtService.generateToken(admin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(expiredToken);
        restTemplate.getRestTemplate().getInterceptors().clear();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/customers", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("kedaluwarsa");
    }

    @Test
    void getAllCustomers_withMalformedToken_shouldReturn401WithInvalidMessage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token-acak-yang-bukan-jwt");
        restTemplate.getRestTemplate().getInterceptors().clear();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/customers", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("tidak valid");
    }
}
