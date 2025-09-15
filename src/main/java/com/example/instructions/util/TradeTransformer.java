package com.example.instructions.util;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.PlatformTrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

@Component
public class TradeTransformer {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public CanonicalTrade toCanonical(CanonicalTrade input) {
        CanonicalTrade canonical = new CanonicalTrade();
        // Mask account_number (all but last 4 digits)
        String acc = input.getAccountNumber();
        if (acc != null && acc.length() > 4) {
            canonical.setAccountNumber("*".repeat(acc.length() - 4) + acc.substring(acc.length() - 4));
        } else {
            canonical.setAccountNumber(acc != null ? acc : "");
        }
        // Uppercase and validate security_id (alphanumeric only)
        String sec = input.getSecurityId() != null ? input.getSecurityId().toUpperCase() : "";
        if (!sec.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid security_id format: " + sec);
        }
        canonical.setSecurityId(sec);
        // Normalize trade_type
        String type = input.getTradeType() != null ? input.getTradeType().toLowerCase() : "";
        if ("buy".equals(type)) {
            canonical.setTradeType("B");
        } else if ("sell".equals(type)) {
            canonical.setTradeType("S");
        } else {
            canonical.setTradeType(type.toUpperCase());
        }
        // Pass through amount and timestamp
        canonical.setAmount(input.getAmount());
        canonical.setTimestamp(input.getTimestamp());
        return canonical;
    }

    public String toPlatformJson(CanonicalTrade canonical) throws Exception {
        PlatformTrade pt = new PlatformTrade();
        PlatformTrade.TradeDetails tradeDetails = new PlatformTrade.TradeDetails();
        tradeDetails.setAccount(canonical.getAccountNumber());
        tradeDetails.setSecurity(canonical.getSecurityId());
        tradeDetails.setType(canonical.getTradeType());
        tradeDetails.setAmount(canonical.getAmount());
        tradeDetails.setTimestamp(canonical.getTimestamp());
        pt.setTrade(tradeDetails);
        return objectMapper.writeValueAsString(pt);
    }

    public String sanitize(String input) {
        if (input == null) return "";
        // Remove potential injection chars, trim
        return input.trim().replaceAll("[^a-zA-Z0-9*\\-:.+ ]", "");
    }
}