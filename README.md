# Shipment Tracking Gateway

[![CI/CD Pipeline](https://github.com/orkhanigidov/shipment-tracking-gateway/actions/workflows/ci.yml/badge.svg)](https://github.com/orkhanigidov/shipment-tracking-gateway/actions/workflows/ci.yml)

A learning project to practice JWT authentication, tier-based rate limiting, Redis caching, Elasticsearch search,
Spring AI integration, and clean architecture using SOLID design patterns (Strategy, Dependency Inversion, etc.).

The gateway accepts shipment tracking requests, authenticates them via JWT, enforces a tier-based rate limit, routes
each request to the appropriate carrier adapter (DHL, FedEx, UPS), and provides an AI assistant that can answer
natural language questions about shipments using RAG search and tool-calling with persistent chat memory.

## Architecture

```
Client
  │
  │ POST /auth/token ──────────> [AuthService] ──> JWT issued
  │
  │ POST /shipments  ──────────> [ShipmentRegistrationService]
  │                                   │
  │                                   ├── [ShipmentIndexer]       → Elasticsearch (keyword index)
  │                                   └── [ShipmentVectorIndexer] → Elasticsearch (vector index, Spring AI)
  │
  │ GET /shipments/{id}
  │       │
  │  [JwtAuthFilter] ─── validates Bearer token
  │       │
  │  [RateLimitInterceptor] ─── dynamic limits via atomic Redis keys per user/minute
  │       │
  │  [LiveTrackingService]
  │       │   @Cacheable("tracking") ─── Redis, TTL 5 min
  │       │
  │  [CarrierAdapterRegistry]
  │       ├── DhlAdapter    (mock)
  │       ├── FedExAdapter  (mock)
  │       └── UpsAdapter    (mock)
  │
  │ GET  /ai/search?q=... ──────> [AiTrackingService]
  │                                   │
  │                                   ├── VectorStore.similaritySearch() → top-5 shipment docs
  │                                   └── ChatClient (gpt-4o-mini) → answer grounded in data
  │
  │ POST /ai/chat ──────────────> [AiTrackingService]
  │                                   │
  │                                   ├── @Tool getShipmentStatus() → LiveTrackingService (LLM decides when to call)
  │                                   └── MessageChatMemoryAdvisor → JdbcChatMemoryRepository (Postgres)
  │
  └─ Response
```

## Tech Stack

| Layer              | Technology                                                      |
|--------------------|-----------------------------------------------------------------|
| Language & Runtime | Java 17, Spring Boot 3.5                                        |
| Authentication     | Spring Security, JWT                                            |
| Rate Limiting      | Redis — atomic fixed-window counter (`INCR`) per user           |
| Caching            | Redis — tracking responses, TTL 5 min                           |
| Database           | PostgreSQL + Flyway migrations                                  |
| Keyword Search     | Elasticsearch — multi-field shipment search                     |
| AI / LLM           | Spring AI — OpenAI gpt-4o-mini                                  |
| Vector Search      | Elasticsearch vector store — cosine similarity, 1536 dimensions |
| Chat Memory        | Spring AI JDBC memory — per-user history in PostgreSQL          |
| API Docs           | SpringDoc OpenAPI / Swagger UI                                  |
| Testing            | JUnit 5, Testcontainers (PostgreSQL + Elasticsearch)            |
| Infrastructure     | Docker, Docker Compose                                          |

## Getting Started

**Prerequisites:** Docker, Docker Compose, and an OpenAI API key.

**1. Create a `.env` file** in the project root (never commit this):

```bash
JWT_SECRET=your-random-64-char-secret-generated-by-openssl-rand-hex-32
OPENAI_API_KEY=sk-...
```

**2. Start all services:**

```bash
docker compose up --build
```

This starts PostgreSQL, Redis, Elasticsearch, and the application. Flyway runs migrations automatically on startup,
including the `spring_ai_chat_memory` table.

## Environment Variables

| Variable         | Required | Description                                 |
|------------------|----------|---------------------------------------------|
| `JWT_SECRET`     | Yes      | HS256 signing secret, minimum 32 characters |
| `OPENAI_API_KEY` | Yes      | OpenAI API key for chat and embeddings      |
| `DB_HOST`        | No       | PostgreSQL host (default: `localhost`)      |
| `DB_NAME`        | No       | Database name (default: `gatewaydb`)        |
| `DB_USER`        | No       | Database user (default: `gateway`)          |
| `DB_PASS`        | No       | Database password (default: `gateway`)      |
| `REDIS_HOST`     | No       | Redis host (default: `localhost`)           |
| `REDIS_PORT`     | No       | Redis port (default: `6379`)                |
| `ES_HOST`        | No       | Elasticsearch host (default: `localhost`)   |
| `ES_PORT`        | No       | Elasticsearch port (default: `9200`)        |

## API Reference

### Authentication

#### Issue a JWT token

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "apiKey": "key-alice-001"}'
```

Response:

```json
{
  "token": "<access_token>",
  "refreshToken": "<refresh_token>",
  "type": "Bearer"
}
```

Available test users: `alice / key-alice-001`, `bob / key-bob-002`

#### Refresh a JWT token

When the access token (1h) expires, use the refresh token (7d) to get a new pair:

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refresh_token>"}'
```

---

### Shipment Tracking

All endpoints below require `Authorization: Bearer <token>`.

#### Register a shipment

```bash
TOKEN="<your_access_token>"

curl -X POST http://localhost:8080/shipments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "trackingNumber": "DHL123456789",
    "carrier": "DHL",
    "origin": "Hamburg, Germany",
    "destination": "Berlin, Germany"
  }'
```

Response:

```json
{
  "trackingNumber": "DHL123456789",
  "carrier": "DHL",
  "status": "IN_TRANSIT",
  "currentLocation": "Frankfurt, Germany",
  "estimatedDelivery": "2026-03-12",
  "lastUpdate": "2026-03-10",
  "origin": "Hamburg, Germany",
  "destination": "Berlin, Germany"
}
```

Registration automatically indexes the shipment into both the Elasticsearch keyword index and the vector store
for AI search.

#### Track a shipment

Tracking responses are cached in Redis for 5 minutes. The first call fetches live data from the carrier adapter;
subsequent calls within the TTL window return the cached response.

```bash
curl http://localhost:8080/shipments/DHL123456789 \
  -H "Authorization: Bearer $TOKEN"
```

---

### Keyword Search (Elasticsearch)

#### By location

```bash
curl "http://localhost:8080/shipments/search/location?q=Hamburg" \
  -H "Authorization: Bearer $TOKEN"
```

#### By carrier

```bash
curl "http://localhost:8080/shipments/search/carrier?q=DHL" \
  -H "Authorization: Bearer $TOKEN"
```

#### By status

```bash
curl "http://localhost:8080/shipments/search/status?q=IN_TRANSIT" \
  -H "Authorization: Bearer $TOKEN"
```

---

### AI Assistant

Both endpoints require `Authorization: Bearer <token>`. They use OpenAI `gpt-4o-mini` with your actual shipment data.

#### Natural language search (RAG)

Ask a plain English question. Spring AI embeds the question, performs cosine similarity search in the Elasticsearch
vector index, retrieves the top 5 matching shipments, and sends them to the LLM as context. The answer is grounded
only in your real data — the model cannot invent tracking numbers or statuses.

```bash
curl "http://localhost:8080/ai/search?q=Are+there+any+DHL+shipments+in+transit+to+Berlin" \
  -H "Authorization: Bearer $TOKEN"
```

Example response:

```
Yes, shipment DHL123456789 via DHL is currently IN_TRANSIT from Hamburg to Berlin,
with an estimated delivery of 2026-03-12.
```

#### Conversational chat with tool calling

Send a message in natural language. The assistant has access to a `getShipmentStatus` tool — it decides on its own
when to call it. Conversation history is persisted per user in PostgreSQL, so follow-up questions work across
requests and app restarts.

```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "Where is my shipment DHL123456789?"}'
```

Example response:

```
Your shipment DHL123456789 is currently IN_TRANSIT at Frankfurt, Germany.
The estimated delivery date is 2026-03-12.
```

Follow-up (memory keeps context):

```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "When will it arrive in Berlin?"}'
```

The assistant uses the previous exchange to understand "it" without needing the tracking number again.

---

### Rate Limiting

Users are rate-limited based on their assigned `Tier`. Requests are tracked inside distributed Redis windows that
automatically reset at the boundary of every minute.

| Tier       | Requests / minute |
|------------|-------------------|
| FREE       | 10                |
| PREMIUM    | 100               |
| ENTERPRISE | 1000              |

When the limit is exceeded, the API returns `429 Too Many Requests` with the header
`X-Rate-Limit-Remaining: 0`.

Test rate limiting:

```bash
for i in {1..15}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    http://localhost:8080/shipments/DHL123456789 \
    -H "Authorization: Bearer $TOKEN"
done
# FREE tier: first 10 return 200, requests 11–15 return 429
```

---

## API Documentation (Swagger UI)

Interactive docs with JWT auth support are available at:

```
http://localhost:8080/swagger-ui.html
```

Click **Authorize**, paste your Bearer token, and explore all endpoints.

---

## Running Tests

```bash
./gradlew test
```

Integration tests use Testcontainers to spin up real PostgreSQL and Elasticsearch instances. No manual setup required.

---

## CI/CD Pipeline

GitHub Actions runs on every push and pull request to `master`:

1. Sets up Java 17
2. Caches Gradle dependencies
3. Runs all unit and integration tests via Testcontainers
4. Builds the Docker image to validate the `Dockerfile`

---

## Supported Carriers

| Carrier | Code    | Notes               |
|---------|---------|---------------------|
| DHL     | `DHL`   | Mock implementation |
| FedEx   | `FedEx` | Mock implementation |
| UPS     | `UPS`   | Mock implementation |

---

## Spring AI — How It Works

### RAG Search (`GET /ai/search`)

```
User question
     │
     ▼
EmbeddingModel (text-embedding-3-small)
     │  produces 1536-dimension vector
     ▼
VectorStore.similaritySearch() → Elasticsearch cosine similarity
     │  returns top-5 matching shipment documents
     ▼
ChatClient prompt (context + question)
     │
     ▼
gpt-4o-mini → grounded answer
```

### Chat with Tool Calling (`POST /ai/chat`)

```
User message
     │
     ▼
ChatClient + MessageChatMemoryAdvisor
     │  injects last 20 messages from Postgres
     ▼
gpt-4o-mini reasons about the message
     │  if it needs live data → calls @Tool getShipmentStatus(trackingNumber)
     ▼
LiveTrackingService.getTracking() → Redis cache → CarrierAdapter
     │
     ▼
Tool result sent back to model
     │
     ▼
Final reply returned + conversation saved to Postgres
```

### Vector Indexing (on `POST /shipments`)

Every registered shipment is indexed in two ways simultaneously:

- **Elasticsearch keyword index** — powers the existing `/search/location`, `/search/carrier`, `/search/status`
  endpoints
- **Elasticsearch vector index** — powers the new `/ai/search` semantic endpoint

---

## Known Limitations

- Carrier adapters are mocked — no real external API calls are made
- Spring AI vector search requires an OpenAI API key for embedding; the chat endpoints will fail gracefully if
  `OPENAI_API_KEY` is not set