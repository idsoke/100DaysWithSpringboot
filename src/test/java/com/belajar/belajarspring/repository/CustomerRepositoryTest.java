package com.belajar.belajarspring.repository;

import com.belajar.belajarspring.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @DataJpaTest: hanya load JPA slice (Repository, Entity, Hibernate)
// Secara otomatis mengganti datasource dengan H2 in-memory
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void save_shouldPersistCustomer() {
        Customer customer = new Customer(null, "Ani", "ani@example.com");

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Ani");
        assertThat(saved.getEmail()).isEqualTo("ani@example.com");
    }

    @Test
    void findById_shouldReturnCustomer_whenExists() {
        Customer customer = customerRepository.save(new Customer(null, "Budi", "budi@example.com"));

        Optional<Customer> result = customerRepository.findById(customer.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Budi");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Customer> result = customerRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnCustomer_whenEmailExists() {
        customerRepository.save(new Customer(null, "Citra", "citra@example.com"));

        Optional<Customer> result = customerRepository.findByEmail("citra@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Citra");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotExists() {
        Optional<Customer> result = customerRepository.findByEmail("tidakada@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void save_withDuplicateEmail_shouldThrowException() {
        customerRepository.save(new Customer(null, "Dani", "dani@example.com"));

        assertThatThrownBy(() -> {
            customerRepository.saveAndFlush(new Customer(null, "Dani Lain", "dani@example.com"));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void delete_shouldRemoveCustomer() {
        Customer customer = customerRepository.save(new Customer(null, "Eko", "eko@example.com"));

        customerRepository.delete(customer);

        assertThat(customerRepository.findById(customer.getId())).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllSavedCustomers() {
        customerRepository.save(new Customer(null, "Fajar", "fajar@example.com"));
        customerRepository.save(new Customer(null, "Gita", "gita@example.com"));

        assertThat(customerRepository.findAll()).hasSize(2);
    }
}
