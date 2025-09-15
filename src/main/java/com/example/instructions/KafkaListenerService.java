package com.example.instructions;

import com.example.instructions.service.TradeService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerService {

    private final TradeService tradeService;

    public KafkaListenerService(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @KafkaListener(topics = "${app.topics.inbound}", groupId = "instructions-group")
    public void listen(String message) {
        try {
            tradeService.processKafkaMessage(message);
        } catch (Exception e) {
            // Handle error: log non-sensitive info, retry, or send to DLQ
            System.err.println("Error processing Kafka message: " + e.getMessage());
        }
    }
}