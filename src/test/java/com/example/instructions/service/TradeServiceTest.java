package com.example.instructions.service;

import com.example.instructions.KafkaPublisher;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeTransformer transformer;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @InjectMocks
    private TradeService tradeService;

    @Test
    void testProcessKafkaMessageStoresTrade() throws Exception {
        String message = "{\"accountNumber\":\"12345678\",\"securityId\":\"abc123\",\"tradeType\":\"buy\",\"amount\":100000,\"timestamp\":\"2025-08-04T21:15:33Z\"}";
        CanonicalTrade canonical = new CanonicalTrade();
        canonical.setAccountNumber("****5678");
        canonical.setSecurityId("ABC123");
        canonical.setTradeType("B");
        canonical.setAmount(100000);
        canonical.setTimestamp(ZonedDateTime.parse("2025-08-04T21:15:33Z"));

        when(transformer.toCanonical(any())).thenReturn(canonical);
        when(transformer.toPlatformJson(any())).thenReturn("...");

        tradeService.processKafkaMessage(message);

        CanonicalTrade stored = tradeService.getStoredTrade("****5678_ABC123");
        assertNotNull(stored);
        assertEquals("****5678", stored.getAccountNumber());
    }

    @Test
    void testProcessKafkaMessageInvalidJson() {
        String message = "invalid-json";
        assertThrows(Exception.class, () -> tradeService.processKafkaMessage(message));
    }

    @Test
    void testProcessKafkaMessage() throws Exception {
        String message = "{\"accountNumber\":\"12345678\",\"securityId\":\"abc123\",\"tradeType\":\"buy\",\"amount\":100000,\"timestamp\":\"2025-08-04T21:15:33Z\"}";
        CanonicalTrade input = new CanonicalTrade();
        input.setAccountNumber("12345678");
        input.setSecurityId("abc123");
        input.setTradeType("buy");
        input.setAmount(100000);
        input.setTimestamp(ZonedDateTime.parse("2025-08-04T21:15:33Z"));

        CanonicalTrade canonical = new CanonicalTrade();
        canonical.setAccountNumber("****5678");
        canonical.setSecurityId("ABC123");
        canonical.setTradeType("B");
        canonical.setAmount(100000);
        canonical.setTimestamp(ZonedDateTime.parse("2025-08-04T21:15:33Z"));

        ArgumentCaptor<CanonicalTrade> captor = ArgumentCaptor.forClass(CanonicalTrade.class);
        when(transformer.toCanonical(captor.capture())).thenReturn(canonical);
        when(transformer.toPlatformJson(any())).thenReturn(
                "{\"platform_id\":\"ACCT123\",\"trade\":{\"account\":\"****5678\",\"security\":\"ABC123\",\"type\":\"B\",\"amount\":100000,\"timestamp\":\"2025-08-04T21:15:33Z\"}}");

        tradeService.processKafkaMessage(message);

        CanonicalTrade capturedInput = captor.getValue();
        assertEquals(input.getAccountNumber(), capturedInput.getAccountNumber());
        assertEquals(input.getSecurityId(), capturedInput.getSecurityId());
        assertEquals(input.getTradeType(), capturedInput.getTradeType());
        assertEquals(input.getAmount(), capturedInput.getAmount());
        assertEquals(input.getTimestamp(), capturedInput.getTimestamp());

        verify(transformer).toCanonical(any());
        verify(transformer).toPlatformJson(any());
        verify(kafkaPublisher).publish(anyString());
    }

}