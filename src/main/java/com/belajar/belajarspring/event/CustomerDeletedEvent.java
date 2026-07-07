package com.belajar.belajarspring.event;

import com.belajar.belajarspring.entity.Customer;
import org.springframework.context.ApplicationEvent;

public class CustomerDeletedEvent extends ApplicationEvent {
    private final Customer customer;

    public CustomerDeletedEvent(Object source, Customer customer) {
        super(source);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
