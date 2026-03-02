# PayPipe – Distributed FinTech Payment Ecosystem

PayPipe is a fault-tolerant, event-driven microservices architecture that simulates a high-throughput enterprise payment gateway.

It demonstrates advanced distributed systems patterns including Saga choreography, event sourcing, strict API idempotency, and asynchronous message brokering.

---

## Architecture Overview

PayPipe follows an event-driven choreography model.

The Payment Gateway handles synchronous HTTP requests and processes payments via Stripe. After successful processing, it publishes events to Kafka. Downstream services (Ledger, Fraud, Notification) consume these events asynchronously.


```
[ Client / Postman ]
        |
        |  HTTP POST (Idempotency-Key)
        v
+--------------------------------------------------+
|              PAYMENT GATEWAY (8080)              |
|--------------------------------------------------|
| 1. Check Redis Lock (O(1) Idempotency)           |
| 2. Call Stripe API (Process Payment)             |
| 3. Publish Event to Kafka                        |
+---------------------------+----------------------+
                            |
                            | PAYMENT_SUCCESS_EVENT
                            v
                     +------------------+
                     |   APACHE KAFKA   |
                     |  (DLQ Enabled)   |
                     +--------+---------+
                              |
          ------------------------------------------------------
          |                |                |                  |
          v                v                v                  v
+----------------+ +----------------+ +----------------+ +--------------+
| LEDGER SERVICE | | FRAUD SERVICE  | | NOTIFICATION   | |   DLQ / Ops  |
|     (8081)     | |     (8082)     | |    (8083)      | |  (Failures)  |
+--------+-------+ +--------+-------+ +--------+-------+ +--------------+
         |                  |                  |
         v                  v                  v
   PostgreSQL         PostgreSQL           Mock SMTP
   (Append-Only)      (Rules Engine)       / Logs
```

Key components:

* Redis for idempotency
* Apache Kafka for event streaming
* PostgreSQL for append-only ledger storage
* Stripe API for payment processing
* Docker for infrastructure orchestration

---

## Core Features

Strict API Idempotency
Redis-based distributed locking prevents duplicate charges during retries or concurrent requests.

Event-Sourced Ledger
Append-only PostgreSQL ledger ensures ACID compliance and accurate balance aggregation without data mutation.

Fault Tolerance
Kafka retains events if downstream services are unavailable. Processing resumes automatically after recovery.

Saga Pattern (Choreography)
Distributed transaction management without a central coordinator.

---

## Tech Stack

* Java 17
* Spring Boot 3 (Web, JPA, Kafka)
* Apache Kafka
* PostgreSQL
* Redis
* Stripe Java SDK
* Docker & Docker Compose

---

## Getting Started

### Prerequisites

* Java 17+
* Maven
* Docker
* Stripe test API key

### 1. Start Infrastructure

```bash
docker-compose up -d
```

### 2. Configure Stripe

Update `application.properties` in the payment-gateway service:

```properties
stripe.secret.key=sk_test_YOUR_KEY_HERE
```

### 3. Start Services

In separate terminals:

```bash
cd payment-gateway
./mvnw spring-boot:run
```

```bash
cd ledger-service
./mvnw spring-boot:run
```

---

## Testing

### Create a Payment

Send the same request twice to test idempotency:

```bash
curl -X POST http://localhost:8080/api/payments/charge \
     -H "Content-Type: application/json" \
     -H "Idempotency-Key: unique-click-001" \
     -d '{"userId": "bruce_wayne", "amount": 5000.00, "currency": "USD"}'
```

### Verify Balance

```bash
curl -X GET http://localhost:8081/api/ledger/balance/bruce_wayne
```

Expected output:

```
5000.0
```

---

## Roadmap

* Migrate internal synchronous calls fully to Kafka
* Complete Fraud and Notification services
* Implement DLQ retry worker
* Add metrics (Prometheus, Grafana)
* Add CI/CD pipeline

