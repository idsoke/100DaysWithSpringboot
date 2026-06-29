package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.exception.ResourceNotFoundException;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Page<Customer> getCustomersPaged(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    // Simpan hasil ke cache dengan key = id. Panggilan berikutnya tidak hit DB.
    @Cacheable(value = "customers", key = "#id")
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Selalu eksekusi method dan update cache dengan data terbaru.
    @CachePut(value = "customers", key = "#id")
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        return customerRepository.save(existingCustomer);
    }

    // Hapus entry dari cache saat data dihapus.
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(existingCustomer);
    }
}