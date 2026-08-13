package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.event.CustomerCreatedEvent;
import com.belajar.belajarspring.event.CustomerDeletedEvent;
import com.belajar.belajarspring.event.CustomerUpdatedEvent;
import com.belajar.belajarspring.exception.ResourceNotFoundException;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CustomerService(CustomerRepository customerRepository, ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<Customer> getCustomersPaged(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    // Security interceptor berjalan sebelum cache lookup (precedence tertinggi),
    // jadi request tanpa izin tidak akan pernah membaca/mengisi cache.
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Cacheable(value = "customers", key = "#id")
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Customer createCustomer(Customer customer) {
        Customer saved = customerRepository.save(customer);
        eventPublisher.publishEvent(new CustomerCreatedEvent(this, saved));
        return saved;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CachePut(value = "customers", key = "#id")
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        Customer updated = customerRepository.save(existingCustomer);
        eventPublisher.publishEvent(new CustomerUpdatedEvent(this, updated));
        return updated;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(existingCustomer);
        eventPublisher.publishEvent(new CustomerDeletedEvent(this, existingCustomer));
    }
}