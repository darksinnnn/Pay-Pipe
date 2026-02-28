💸 PayPipe: Distributed FinTech Payment EcosystemA fault-tolerant, event-driven microservices architecture simulating a high-throughput enterprise payment gateway.This project demonstrates advanced distributed system design patterns, including the Saga Pattern, Event Sourcing, API Idempotency, and Asynchronous Message Brokering to handle complex financial transactions with strict data integrity.📑 Table of ContentsSystem ArchitectureCore Features & Engineering HighlightsTech StackGetting StartedTesting the EcosystemProject Roadmap🧠 System ArchitecturePayPipe utilizes an event-driven choreography approach. The Payment Gateway handles synchronous user requests and interacts with third-party processors (Stripe), while downstream services react to events asynchronously via Apache Kafka.[ Client / Postman ] 
       │ (HTTP POST with Idempotency-Key)
       ▼
┌──────────────────────────────────────────────┐
│             PAYMENT GATEWAY (Port 8080)      │
│  1. Check Redis Lock (O(1) Idempotency)      │
│  2. Call Stripe API (Process Payment)        │
│  3. Publish Event to Kafka                   │
└──────────────────────┬───────────────────────┘
                       │ (PAYMENT_SUCCESS_EVENT)
                       ▼
             [ APACHE KAFKA ] ──(Dead Letter Queues configured)──┐
                       │                                         │
       ┌───────────────┼───────────────┐                         │
       ▼               ▼               ▼                         ▼
┌────────────┐  ┌─────────────┐  ┌──────────────┐         ┌─────────────┐
│   LEDGER   │  │    FRAUD    │  │ NOTIFICATION │         │  DLQ / Ops  │
│  SERVICE   │  │   SERVICE   │  │   SERVICE    │         │ (Failures)  │
│(Port 8081) │  │ (Port 8082) │  │ (Port 8083)  │         └─────────────┘
└──────┬─────┘  └──────┬──────┘  └──────┬───────┘
       ▼               ▼                ▼
 [PostgreSQL]      [PostgreSQL]    [Mock SMTP/Log]
 (Append-Only)     (Rules Engine)
✨ Core Features & Engineering HighlightsStrict API Idempotency: Implemented Redis distributed locks to prevent double-charging users during concurrent UI clicks or network retries, guaranteeing O(1) time complexity checks.Event-Sourced Ledger: Designed an append-only financial ledger in PostgreSQL. Strict ACID compliance ensures 0% data loss, and dynamic state aggregation allows for real-time balance calculations without UPDATE or DELETE anomalies.Fault Tolerance & Eventual Consistency: Downstream microservices are decoupled using Apache Kafka. If the Ledger or Fraud service experiences an outage, messages are safely retained in Kafka and processed when the node recovers.Saga Pattern Choreography: Manages distributed transactions across multiple microservices without a centralized database, ensuring high availability and system resilience.🛠️ Tech StackCategoryTechnologyLanguages & FrameworksJava 17, Spring Boot 3 (Web, Data JPA, Kafka)Message BrokerApache Kafka (with Zookeeper)Databases/CachesPostgreSQL (Relational), Redis (In-Memory)Third-Party APIsStripe Java SDKInfrastructureDocker & Docker Compose🚦 Getting StartedPrerequisitesJava 17+ and Maven installed.Docker Desktop installed and running.A free Stripe Developer Account (for test API keys).1. Start the Infrastructure (Kafka, Zookeeper, Redis, Postgres)Run the provided Docker Compose file to spin up the required databases and message broker:docker-compose up -d
2. Configure API KeysUpdate the application.properties in the payment-gateway service with your Stripe Test Key:stripe.secret.key=sk_test_YOUR_KEY_HERE
3. Start the MicroservicesStart each Spring Boot service in its respective directory:# Terminal 1: Start Payment Gateway
cd payment-gateway
./mvnw spring-boot:run

# Terminal 2: Start Ledger Service
cd ledger-service
./mvnw spring-boot:run
🧪 Testing the EcosystemUse Postman or cURL to simulate a transaction.1. Initiate a Payment (Hits Gateway)Try sending this exact request twice to witness the Redis Idempotency Bouncer block the duplicate in <10ms!curl -X POST http://localhost:8080/api/payments/charge \
     -H "Content-Type: application/json" \
     -H "Idempotency-Key: unique-click-001" \
     -d '{"userId": "bruce_wayne", "amount": 5000.00, "currency": "USD"}'
2. Verify the Ledger Balance (Hits Ledger)curl -X GET http://localhost:8081/api/ledger/balance/bruce_wayne
Expected Output:5000.0
📈 Project Roadmap[x] Integrate Stripe API for payment processing[x] Implement Redis Idempotency layer[x] Build Append-Only PostgreSQL Ledger[ ] Migrate Synchronous HTTP calls to Apache Kafka (Event-Driven)[ ] Build standalone Fraud Detection Service[ ] Build standalone Notification Service[ ] Implement a Dead Letter Queue (DLQ) retry worker[ ] Integrate Prometheus & Grafana for JVM metrics
