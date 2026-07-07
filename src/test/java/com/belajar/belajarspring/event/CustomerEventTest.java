package com.belajar.belajarspring.event;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.repository.CustomerRepository;
import com.belajar.belajarspring.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {"spring.task.execution.pool.core-size=2"})
public class CustomerEventTest {

    @Autowired
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    public void testCustomerCreatedEventPublished() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.createCustomer(customer);

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testCustomerUpdatedEventPublished() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer updatedCustomer = new Customer();
        updatedCustomer.setName("Jane Doe");
        updatedCustomer.setEmail("jane@example.com");

        customerService.updateCustomer(1L, updatedCustomer);

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testCustomerDeletedEventPublished() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        when(customerRepository.findById(1L)).thenReturn(java.util.Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).delete(any(Customer.class));
    }
}
