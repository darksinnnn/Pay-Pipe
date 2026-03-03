package com.paypipe.ledger_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        // Step 1: Tell Spring to send failed messages to a topic named {originalTopic}.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Step 2: Configure the retry policy
        // If a message fails, wait 2 seconds (2000ms), and try again.
        // Do this a maximum of 2 times. If it STILL fails, send to the DLQ!
        FixedBackOff backOff = new FixedBackOff(2000L, 2);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}