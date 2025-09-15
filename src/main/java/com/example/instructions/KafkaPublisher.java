package com.example.instructions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.topics.outbound}")
    private String outboundTopic;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String message) {
        kafkaTemplate.send(outboundTopic, message); // Asynchronous
    }
}