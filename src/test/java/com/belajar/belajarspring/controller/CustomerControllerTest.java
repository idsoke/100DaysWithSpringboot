package com.belajar.belajarspring.controller;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.exception.ResourceNotFoundException;
import com.belajar.belajarspring.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void getAllCustomers_shouldReturnList() throws Exception {
        Customer customer = new Customer(1L, "Budi", "budi@example.com");
        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Budi"));
    }

    @Test
    void getCustomerById_whenFound_shouldReturnCustomer() throws Exception {
        Customer customer = new Customer(1L, "Budi", "budi@example.com");
        when(customerService.getCustomerById(1L)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("budi@example.com"));
    }

    @Test
    void getCustomerById_whenNotFound_shouldReturn404() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: 99"));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createCustomer_withValidRequest_shouldReturn200() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setName("Budi");
        request.setEmail("budi@example.com");

        Customer saved = new Customer(1L, "Budi", "budi@example.com");
        when(customerService.createCustomer(any(Customer.class))).thenReturn(saved);

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCustomer_withBlankName_shouldReturn400() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setName("");
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void deleteCustomer_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomersPaged_shouldReturnPagedResponse() throws Exception {
        Customer customer = new Customer(1L, "Budi", "budi@example.com");
        Page<Customer> pagedResult = new PageImpl<>(List.of(customer), PageRequest.of(0, 10), 1);
        when(customerService.getCustomersPaged(any(Pageable.class))).thenReturn(pagedResult);

        mockMvc.perform(get("/api/customers/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Budi"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.last").value(true));
    }
}
