package com.belajar.belajarspring.event;

import com.belajar.belajarspring.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventListener {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventListener.class);
    private final NotificationService notificationService;

    public CustomerEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onCustomerCreated(CustomerCreatedEvent event) {
        log.info("Event received: Customer created - {}", event.getCustomer().getId());
        notificationService.sendWelcomeNotification(event.getCustomer());
    }

    @EventListener
    public void onCustomerUpdated(CustomerUpdatedEvent event) {
        log.info("Event received: Customer updated - {}", event.getCustomer().getId());
        notificationService.sendUpdateNotification(event.getCustomer());
    }

    @EventListener
    public void onCustomerDeleted(CustomerDeletedEvent event) {
        log.info("Event received: Customer deleted - {}", event.getCustomer().getId());
        notificationService.sendDeletionNotification(event.getCustomer());
    }
}
