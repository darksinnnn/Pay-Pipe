package com.paypipe.payment_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // This creates a global "HTTP Client" that our entire app can use to make API calls
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}