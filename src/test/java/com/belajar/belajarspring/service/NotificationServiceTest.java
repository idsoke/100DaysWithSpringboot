package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    void sendWelcomeNotification_shouldRunNonBlocking() {
        Customer customer = new Customer(null, "Test User", "test@example.com");
        String threadBefore = Thread.currentThread().getName();

        // Method langsung return tanpa tunggu async selesai
        notificationService.sendWelcomeNotification(customer);

        // Thread utama tidak berubah — async jalan di thread lain
        assertThat(Thread.currentThread().getName()).isEqualTo(threadBefore);
    }

    @Test
    void generateWelcomeReport_shouldReturnCompletableFuture() throws Exception {
        Customer customer = new Customer(null, "Report User", "report@example.com");

        CompletableFuture<String> future = notificationService.generateWelcomeReport(customer);
        String result = future.get(5, TimeUnit.SECONDS);

        assertThat(result).contains("Report User");
        assertThat(result).contains("report@example.com");
    }

    @Test
    void generateWelcomeReport_multipleConcurrent_shouldAllComplete() throws Exception {
        Customer c1 = new Customer(null, "User A", "a@example.com");
        Customer c2 = new Customer(null, "User B", "b@example.com");
        Customer c3 = new Customer(null, "User C", "c@example.com");

        // Trigger tiga task async bersamaan
        CompletableFuture<String> f1 = notificationService.generateWelcomeReport(c1);
        CompletableFuture<String> f2 = notificationService.generateWelcomeReport(c2);
        CompletableFuture<String> f3 = notificationService.generateWelcomeReport(c3);

        CompletableFuture.allOf(f1, f2, f3).get(10, TimeUnit.SECONDS);

        assertThat(f1.get()).contains("User A");
        assertThat(f2.get()).contains("User B");
        assertThat(f3.get()).contains("User C");
    }
}
