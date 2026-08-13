package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

// @SpringBootTest (bukan @ExtendWith(MockitoExtension.class)) sengaja dipakai di sini:
// @PreAuthorize hanya ditegakkan lewat AOP proxy milik Spring, jadi CustomerService
// harus jadi bean asli dalam ApplicationContext, bukan instance biasa yang di-@InjectMocks.
@SpringBootTest
@ActiveProfiles("test")
class CustomerServiceSecurityTest {

    @Autowired
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    @WithMockUser(roles = "USER")
    void getAllCustomers_withUserRole_shouldSucceed() {
        when(customerRepository.findAll()).thenReturn(List.of());

        assertThat(customerService.getAllCustomers()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCustomer_withUserRole_shouldBeDenied() {
        Customer customer = new Customer(null, "Budi", "budi@example.com");

        assertThatThrownBy(() -> customerService.createCustomer(customer))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCustomer_withUserRole_shouldBeDenied() {
        Customer customer = new Customer(null, "Budi", "budi@example.com");

        assertThatThrownBy(() -> customerService.updateCustomer(1L, customer))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCustomer_withUserRole_shouldBeDenied() {
        assertThatThrownBy(() -> customerService.deleteCustomer(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_withAdminRole_shouldSucceed() {
        Customer customer = new Customer(1L, "Budi", "budi@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);
    }

    @Test
    void getAllCustomers_withoutAuthentication_shouldBeDenied() {
        assertThatThrownBy(() -> customerService.getAllCustomers())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCustomersPaged_withAdminRole_shouldSucceed() {
        var pageable = PageRequest.of(0, 10);
        when(customerRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(customerService.getCustomersPaged(pageable)).isEmpty();
    }
}
