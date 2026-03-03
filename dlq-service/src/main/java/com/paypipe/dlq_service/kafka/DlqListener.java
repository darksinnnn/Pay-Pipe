package com.paypipe.dlq_service.kafka;

import com.paypipe.dlq_service.entity.FailedMessage;
import com.paypipe.dlq_service.repository.FailedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class DlqListener {

    @Autowired
    private FailedMessageRepository repository;

    //listening to the .DLT (Dead Letter Topic) version!
    @KafkaListener(topics = "payment-success-topic.DLT", groupId = "dlq-group")
    public void consumeFailedMessage(
            String message,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) byte[] exceptionMessageBytes,
            @Header(KafkaHeaders.ORIGINAL_TOPIC) byte[] originalTopicBytes) {

        System.out.println("\n🚨🚨 ALARM: DEAD LETTER QUEUE TRIGGERED! 🚨🚨");

        // Spring Kafka sends headers as bytes, so we convert them to Strings
        String exceptionMessage = new String(exceptionMessageBytes, StandardCharsets.UTF_8);
        String originalTopic = new String(originalTopicBytes, StandardCharsets.UTF_8);

        System.out.println("Failed Message: " + message);
        System.out.println("Reason: " + exceptionMessage);

        // Save to Database for the DevOps team to fix!
        FailedMessage failedRecord = new FailedMessage(message, exceptionMessage, originalTopic);
        repository.save(failedRecord);

        System.out.println("✅ Saved to DLQ Database for manual review.\n");
    }
}