package com.example.instructions.service;

import com.example.instructions.KafkaPublisher;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TradeService {

    private final ConcurrentHashMap<String, CanonicalTrade> storage = new ConcurrentHashMap<>();
    private final TradeTransformer transformer;
    private final KafkaPublisher kafkaPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public TradeService(TradeTransformer transformer, KafkaPublisher kafkaPublisher) {
        this.transformer = transformer;
        this.kafkaPublisher = kafkaPublisher;
    }

    public void processFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        List<CanonicalTrade> trades = parseFile(file);
        for (CanonicalTrade trade : trades) {
            processTrade(trade);
        }
    }

    public void processKafkaMessage(String message) throws Exception {
        CanonicalTrade trade = objectMapper.readValue(message, CanonicalTrade.class);
        processTrade(trade);
    }

    private void processTrade(CanonicalTrade trade) throws Exception {
        CanonicalTrade canonical = transformer.toCanonical(trade);
        if (canonical == null) {
            throw new IllegalStateException("Canonical trade is null after transformation");
        }
        String id = generateId(canonical);
        storage.put(id, canonical); 
        String json = transformer.toPlatformJson(canonical);
        kafkaPublisher.publish(json);
    }

    private List<CanonicalTrade> parseFile(MultipartFile file) throws Exception {
        List<CanonicalTrade> trades = new ArrayList<>();
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }
        try (InputStream is = file.getInputStream()) {
            if (filename.endsWith(".csv")) {
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build();
                CSVParser parser = CSVParser.parse(new InputStreamReader(is, StandardCharsets.UTF_8), csvFormat);
                for (CSVRecord record : parser) {
                    CanonicalTrade trade = new CanonicalTrade();
                    trade.setAccountNumber(transformer.sanitize(record.get("account_number")));
                    trade.setSecurityId(transformer.sanitize(record.get("security_id")));
                    trade.setTradeType(transformer.sanitize(record.get("trade_type")));
                    trade.setAmount(Double.parseDouble(transformer.sanitize(record.get("amount"))));
                    trade.setTimestamp(ZonedDateTime.parse(transformer.sanitize(record.get("timestamp"))));
                    trades.add(trade);
                }
            } else if (filename.endsWith(".json")) {
                trades = objectMapper.readValue(is, new TypeReference<>() {});
            } else {
                throw new IllegalArgumentException("Unsupported file type");
            }
        }
        return trades;
    }

    private String generateId(CanonicalTrade trade) {
        String account = trade != null && trade.getAccountNumber() != null ? trade.getAccountNumber() : "";
        String security = trade != null && trade.getSecurityId() != null ? trade.getSecurityId() : "";
        return account + "_" + security;
    }

    public CanonicalTrade getStoredTrade(String id) {
        return storage.get(id);
    }
}