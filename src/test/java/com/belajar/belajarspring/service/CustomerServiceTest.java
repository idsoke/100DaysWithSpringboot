package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.exception.ResourceNotFoundException;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "Budi", "budi@example.com");
    }

    @Test
    void getAllCustomers_shouldReturnAllCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1).containsExactly(customer);
    }

    @Test
    void getCustomerById_whenFound_shouldReturnCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertThat(result).isEqualTo(customer);
    }

    @Test
    void getCustomerById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createCustomer_shouldSaveAndReturnCustomer() {
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertThat(result).isEqualTo(customer);
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_whenFound_shouldUpdateFieldsAndSave() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer updatedDetails = new Customer(null, "Budi Santoso", "budi.santoso@example.com");
        Customer result = customerService.updateCustomer(1L, updatedDetails);

        assertThat(result.getName()).isEqualTo("Budi Santoso");
        assertThat(result.getEmail()).isEqualTo("budi.santoso@example.com");
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_whenNotFound_shouldThrowAndNotSave() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, customer))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomer_whenFound_shouldDelete() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void deleteCustomer_whenNotFound_shouldThrowAndNotDelete() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void getCustomersPaged_shouldReturnPagedResult() {
        PageRequest pageable = PageRequest.of(0, 2);
        Page<Customer> pagedResult = new PageImpl<>(List.of(customer), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(pagedResult);

        Page<Customer> result = customerService.getCustomersPaged(pageable);

        assertThat(result.getContent()).hasSize(1).containsExactly(customer);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
