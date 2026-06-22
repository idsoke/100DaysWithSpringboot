package com.belajar.belajarspring.controller;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.dto.CustomerResponse;
import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.mapper.CustomerMapper;
import com.belajar.belajarspring.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return CustomerMapper.toResponse(customer);
    }

    @PostMapping
    public CustomerResponse createCustomer(@RequestBody CustomerRequest request) {
        Customer customer = customerService.createCustomer(CustomerMapper.toEntity(request));
        return CustomerMapper.toResponse(customer);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(@PathVariable Long id, @RequestBody CustomerRequest request) {
        Customer customer = customerService.updateCustomer(id, CustomerMapper.toEntity(request));
        return CustomerMapper.toResponse(customer);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }
}
