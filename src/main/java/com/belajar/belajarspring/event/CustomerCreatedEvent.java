package com.belajar.belajarspring.event;

import com.belajar.belajarspring.entity.Customer;
import org.springframework.context.ApplicationEvent;

public class CustomerCreatedEvent extends ApplicationEvent {
    private final Customer customer;

    public CustomerCreatedEvent(Object source, Customer customer) {
        super(source);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
