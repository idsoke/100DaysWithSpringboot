package com.belajar.belajarspring.service;

import com.belajar.belajarspring.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Fire-and-forget: caller tidak perlu tunggu notifikasi selesai dikirim
    @Async
    public void sendWelcomeNotification(Customer customer) {
        log.info("[{}] Mengirim notifikasi ke {}", Thread.currentThread().getName(), customer.getEmail());
        try {
            Thread.sleep(1000); // simulasi delay pengiriman email
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[{}] Notifikasi terkirim ke {}", Thread.currentThread().getName(), customer.getEmail());
    }

    // Async dengan return value: caller bisa .get() saat butuh hasilnya
    @Async
    public CompletableFuture<String> generateWelcomeReport(Customer customer) {
        log.info("[{}] Membuat laporan untuk {}", Thread.currentThread().getName(), customer.getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String report = "Welcome report: " + customer.getName() + " <" + customer.getEmail() + ">";
        return CompletableFuture.completedFuture(report);
    }
}
