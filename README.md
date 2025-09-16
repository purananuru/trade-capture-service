# Trade Capture Service

A Spring Boot microservice for processing trade instructions from files or Kafka, transforming to canonical format, and publishing to Kafka.

## Setup Instructions

1. **Prerequisites**:
   - Java 17+
   - Maven 3.8+
   
---
### How to Test Mock

1. **Build,  Run, Test**:
   - `mvn clean package`

---
### Docker setup and testing the setup

1. **Build the image**:
   - `mvn clean package`
   - `docker build -t instructions-capture-service .`
   - `docker-compose up -d`
   - `docker ps`

2. **Create Topics**:
   - `docker exec trade-capture-service-kafka-1 kafka-topics --create --topic instructions.inbound --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1` 
   - `docker exec trade-capture-service-kafka-1 kafka-topics --create --topic instructions.outbound --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1` 
   - `docker exec trade-capture-service-kafka-1 kafka-topics --list --bootstrap-server kafka:9092` 

2. **File Upload**:
   - `curl.exe -X POST -F "file=@sample-input.csv" http://localhost:8080/upload`

3. **Postman Upload**:
   - Alternatively, use Postman to upload the CSV
   - Import postman-collection.json and ensure the right file value is uploded in the body
   - Hit Send

3. **Kafka Input**:
   - `docker exec -it trade-capture-service-kafka-1 kafka-console-producer --topic instructions.inbound --bootstrap-server kafka:9092`
     ```json
     {"accountNumber":"12345678","securityId":"abc123","tradeType":"buy","amount":100000,"timestamp":"2025-08-04T21:15:33Z"}
   - Ctrl-C

4. **Kafka Output**:
    - `docker exec -it trade-capture-service-kafka-1 kafka-console-consumer --topic instructions.outbound --from-beginning --bootstrap-server kafka:9092`
    - Ctrl-C

5. **Swagger Docs**:
    - `http://localhost:8080/swagger-ui.html`

---
### Overview of the project
1. **Separation of Concerns**:
   - `TradeTransformer`: Handles canonical transformation and platform JSON conversion, including input sanitization.
   - `KafkaPublisher`: Manages asynchronous Kafka publishing to `instructions.outbound`.
   - `KafkaListenerService`: Listens to `instructions.inbound` and delegates to `TradeService`.
   - `TradeService`: Orchestrates parsing, transformation, storage, and publishing.
   - `TradeController`: Handles file uploads via REST.

2. **Model**:
   - `CanonicalTrade` and `PlatformTrade`

3. **Security**:
   - Sanitization in `TradeTransformer.sanitize` prevents injection attacks.
   - Masking of `accountNumber` ensures sensitive data protection.
   - Logging avoids sensitive fields (configured in `application.yml`).

4. **Performance**:
   - Stream-based CSV parsing with Apache Commons CSV.
   - Asynchronous Kafka publishing via `KafkaTemplate`.
   - `ConcurrentHashMap` for thread-safe in-memory storage.

6. **Bonus Features**:
   - **Tests**: Mocking `TradeTransformer` and `KafkaPublisher`.
   - **Spring Profiles**: `application-dev.yml` and `application-prod.yml` for environment-specific Kafka configs.
   - **Swagger**: Accessible at `/swagger-ui.html` for API documentation.
   - **Dockerfile**: For containerization.

---


