package com.belajar.belajarspring.mapper;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.dto.CustomerResponse;
import com.belajar.belajarspring.entity.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        return customer;
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail());
    }
}
