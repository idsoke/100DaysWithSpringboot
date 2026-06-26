package com.belajar.belajarspring;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.dto.CustomerResponse;
import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

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
}
