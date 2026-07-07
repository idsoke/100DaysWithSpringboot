package com.belajar.belajarspring.event;

import com.belajar.belajarspring.entity.Customer;
import com.belajar.belajarspring.service.NotificationService;
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
public class CustomerEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void testCustomerCreatedEventListener() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        eventPublisher.publishEvent(new CustomerCreatedEvent(this, customer));

        verify(notificationService, timeout(2000)).sendWelcomeNotification(any(Customer.class));
    }

    @Test
    public void testCustomerUpdatedEventListener() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Jane Doe");
        customer.setEmail("jane@example.com");

        eventPublisher.publishEvent(new CustomerUpdatedEvent(this, customer));

        verify(notificationService, timeout(2000)).sendUpdateNotification(any(Customer.class));
    }

    @Test
    public void testCustomerDeletedEventListener() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setEmail("john@example.com");

        eventPublisher.publishEvent(new CustomerDeletedEvent(this, customer));

        verify(notificationService, timeout(2000)).sendDeletionNotification(any(Customer.class));
    }
}
