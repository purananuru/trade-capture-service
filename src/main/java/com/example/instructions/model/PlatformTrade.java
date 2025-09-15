package com.example.instructions.model;

import java.time.ZonedDateTime;

public class PlatformTrade {
    private String platformId = "ACCT123"; 
    private TradeDetails trade;

    public static class TradeDetails {
        private String account;
        private String security;
        private String type;
        private double amount;
        private ZonedDateTime timestamp;

        // Getters and Setters
        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getSecurity() {
            return security;
        }

        public void setSecurity(String security) {
            this.security = security;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Getters and Setters
    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public TradeDetails getTrade() {
        return trade;
    }

    public void setTrade(TradeDetails trade) {
        this.trade = trade;
    }
}