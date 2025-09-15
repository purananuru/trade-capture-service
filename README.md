
---

### Explanation of Changes
1. **Project Structure**:
   - package is `com.example.instructions`.
   - Classes: `InstructionsCaptureApplication`, `CanonicalTrade`, `PlatformTrade`, `TradeService`, `TradeController`, `KafkaPublisher`, `KafkaListenerService`, and `TradeTransformer` to separate concerns.

2. **Separation of Concerns**:
   - `TradeTransformer`: Handles canonical transformation and platform JSON conversion, including input sanitization.
   - `KafkaPublisher`: Manages asynchronous Kafka publishing to `instructions.outbound`.
   - `KafkaListenerService`: Listens to `instructions.inbound` and delegates to `TradeService`.
   - `TradeService`: Orchestrates parsing, transformation, storage, and publishing.
   - `TradeController`: Handles file uploads via REST.

3. **Model**:
   - `CanonicalTrade` and `PlatformTrade`

4. **Security**:
   - Sanitization in `TradeTransformer.sanitize` prevents injection attacks.
   - Masking of `accountNumber` ensures sensitive data protection.
   - Logging avoids sensitive fields (configured in `application.yml`).

5. **Performance**:
   - Stream-based CSV parsing with Apache Commons CSV.
   - Asynchronous Kafka publishing via `KafkaTemplate`.
   - `ConcurrentHashMap` for thread-safe in-memory storage.

6. **Bonus Features**:
   - **Tests**: Updated to test new class structure, mocking `TradeTransformer` and `KafkaPublisher`.
   - **Spring Profiles**: `application-dev.yml` and `application-prod.yml` for environment-specific Kafka configs.
   - **Swagger**: Accessible at `/swagger-ui.html` for API documentation.
   - **Dockerfile**: For containerization.

---

### Assumptions
- **platform_id**: Hardcoded as "ABBT123" per the example. 
- **Input Fields**: Assumed `amount` and `timestamp` are in CSV/JSON inputs and Kafka messages. If generated (e.g., timestamp at processing time).
- **Timestamp Format**: ISO 8601 (`2025-08-04T21:15:33Z`). 
- **Error Handling**: Exceptions for invalid inputs

---
# Instructions Capture Service

A Spring Boot microservice for processing trade instructions from files or Kafka, transforming to canonical format, and publishing to Kafka.

## Setup Instructions

1. **Prerequisites**:
   - Java 17+
   - Maven 3.8+
   - Kafka/Zookeeper (e.g., run locally with Docker: `docker-compose up` for Kafka)
   - Create topics:
   -- kafka-topics --create --topic instructions.inbound --bootstrap-server localhost:9092
   -- kafka-topics --create --topic instructions.outbound --bootstrap-server localhost:9092

---

### How to Test
1. **Build and Run**:
   - `mvn clean package`
   - `java -jar target/instructions-capture-service-1.0.0.jar --spring.profiles.active=dev`
   - Ensure Kafka is running (e.g., Docker: `docker-compose up`).

2. **File Upload**:
   - curl.exe -X POST -F "file=@sample-input.csv" http://localhost:8080/upload

3. **Kafka Input**:
   - `kafka-console-producer --topic instructions.inbound --bootstrap-server localhost:9092`
     ```json
     {"accountNumber":"12345678","securityId":"abc123","tradeType":"buy","amount":100000,"timestamp":"2025-08-04T21:15:33Z"}

4. **Kafka Output**:
    - `kafka-console-consumer --topic instructions.outbound --from-beginning --bootstrap-server localhost:9092`
    - shoudl match the 

5. **Swagger Docs**:
    - `Access http://localhost:8080/swagger-ui.html`