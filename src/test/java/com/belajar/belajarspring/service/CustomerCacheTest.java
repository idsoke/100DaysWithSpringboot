package com.belajar.belajarspring.service;

import com.belajar.belajarspring.dto.CustomerRequest;
import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// @SpyBean memungkinkan kita memverifikasi berapa kali method repository dipanggil
// tanpa menggantikan implementasinya (berbeda dengan @MockBean)
@SpringBootTest
@ActiveProfiles("test")
class CustomerCacheTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @SpyBean
    private CustomerRepository customerRepositorySpy;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        // Bersihkan cache sebelum setiap test agar tidak ada efek samping antar test
        cacheManager.getCache("customers").clear();
    }

    @Test
    void getCustomerById_secondCall_shouldHitCacheNotDb() {
        Customer saved = customerRepository.save(new Customer(null, "Cache User", "cache@example.com"));
        Long id = saved.getId();

        // Panggilan pertama: hit DB
        Customer first = customerService.getCustomerById(id);
        // Panggilan kedua: harus dari cache, bukan DB
        Customer second = customerService.getCustomerById(id);

        assertThat(first.getName()).isEqualTo("Cache User");
        assertThat(second.getName()).isEqualTo("Cache User");
        // findById di repository hanya dipanggil 1x meskipun getCustomerById dipanggil 2x
        verify(customerRepositorySpy, times(1)).findById(id);
    }

    @Test
    void updateCustomer_shouldRefreshCache() {
        Customer saved = customerRepository.save(new Customer(null, "Before Update", "before@example.com"));
        Long id = saved.getId();

        // Panaskan cache
        customerService.getCustomerById(id);

        // Update: @CachePut memperbarui cache dengan data baru
        CustomerRequest updateReq = new CustomerRequest();
        updateReq.setName("After Update");
        updateReq.setEmail("after@example.com");

        Customer updated = customerService.updateCustomer(id, new Customer(null, "After Update", "after@example.com"));

        // Ambil dari cache — harus sudah berisi data baru tanpa hit DB lagi
        Customer fromCache = customerService.getCustomerById(id);
        assertThat(fromCache.getName()).isEqualTo("After Update");
        // findById dipanggil 1x saat pemanasan + 1x saat update (karena @CachePut tetap eksekusi method)
        verify(customerRepositorySpy, times(2)).findById(id);
    }

    @Test
    void deleteCustomer_shouldEvictCache() {
        Customer saved = customerRepository.save(new Customer(null, "To Delete", "delete@example.com"));
        Long id = saved.getId();

        // Panaskan cache
        customerService.getCustomerById(id);

        // Hapus: @CacheEvict menghapus entry dari cache
        customerService.deleteCustomer(id);

        // Verifikasi cache sudah kosong
        assertThat(cacheManager.getCache("customers").get(id)).isNull();
    }
}
