package com.belajar.belajarspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Clock di-expose sebagai bean (bukan Clock.systemUTC() dipanggil langsung di service)
// supaya LoginRateLimiterService bisa diuji dengan Clock palsu yang waktunya bisa dimajukan manual.
@Configuration
public class RateLimitConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
