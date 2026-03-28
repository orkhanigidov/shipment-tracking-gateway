# Shipment Tracking Gateway

[![CI/CD Pipeline](https://github.com/orkhanigidov/shipment-tracking-gateway/actions/workflows/ci.yml/badge.svg)](https://github.com/orkhanigidov/shipment-tracking-gateway/actions/workflows/ci.yml)

A learning project to practice JWT authentication, rate limiting with Bucket4j, Redis caching, Elasticsearch for
advanced searching, and the Strategy design pattern (carrier adapters).

The gateway accepts shipment tracking requests, authenticates them via JWT, enforces a per-user rate limit, and routes
each request to the appropriate carrier adapter (DHL, FedEx, UPS).

## Architecture

```
Client
  │
  │ POST /auth/token ──> JWT issued
  │
  │ GET /shipments/{tracking}
  │       │
  │  [JwtAuthFilter] - validates Bearer token
  │       │
  │  [RateLimitInterceptor] - Bucket4j, 20 req/min per user
  │       │
  │  [TrackingService]
  │       │   @Cacheable("tracking") - Redis, TTL 5 min
  │       │
  │  [CarrierAdapterRegistry]
  │       ├── DhlAdapter    (mock)
  │       ├── FedExAdapter  (mock)
  │       └── UpsAdapter    (mock)
  │
  └─ Response: status, location, ETA
```

## Tech Stack

- Java 17, Spring Boot 3.5
- Spring Security - stateless JWT authentication
- Bucket4j - in-memory rate limiting (20 requests/min per user)
- Redis - caches tracking responses for 5 minutes
- PostgreSQL + Flyway - stores shipment records
- Elasticsearch - indexes shipment data for fast, multi-field searching
- Testcontainers - integration tests with real PostgreSQL

## Getting Started

**Prerequisites:** Docker & Docker Compose

```bash
docker compose up --build
```

## Try It Out

**1. Get a JWT token:**

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "apiKey": "key-alice-001"}'
```

Response:

```json
{
  "token": "...",
  "type": "Bearer"
}
```

Available test users: `alice / key-alice-001`, `bob / key-bob-002`

**2. Register a shipment:**

```bash
TOKEN="..."

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
  "origin": "Hamburg, Germany",
  "destination": "Berlin, Germany"
}
```

**3. Track the shipment (cached after first call):**

```bash
curl http://localhost:8080/shipments/DHL123456789 \
  -H "Authorization: Bearer $TOKEN"
```

**4. Search shipments (Elasticsearch):**

By Location:

```bash
curl "http://localhost:8080/shipments/search/location?q=Hamburg" \
  -H "Authorization: Bearer $TOKEN"
```

By Carrier:

```bash
curl "http://localhost:8080/shipments/search/carrier?q=DHL" \
  -H "Authorization: Bearer $TOKEN"
```

By Status:

```bash
curl "http://localhost:8080/shipments/search/status?q=IN_TRANSIT" \
  -H "Authorization: Bearer $TOKEN"
```

**5. Test rate limiting (run 21 times quickly):**

```bash
for i in {1..21}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    http://localhost:8080/shipments/DHL123456789 \
      -H "Authorization: Bearer $TOKEN"
done
# First 20: 200, request 21: 429 Too Many Requests
```

**6. Test with wrong credentials:**

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "apiKey": "wrong-key"}'
# → 401 Unauthorized
```

## API Documentation (Swagger UI)

Interactive API docs are available at:

```
http://localhost:8080/swagger-ui.html
```

## Running Tests

```bash
./gradlew test
```

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration. The pipeline automatically triggers on every push and pull
request to the `master` branch.

It performs the following steps:

1. Sets up the Java 17 environment.
2. Caches Gradle dependencies to speed up builds.
3. Runs all unit and integration tests (spinning up PostgreSQL via Testcontainers).
4. Builds the Docker image to ensure the `Dockerfile` remains valid.

## Supported Carriers

| Carrier | Code    | Notes               |
|---------|---------|---------------------|
| DHL     | `DHL`   | Mock implementation |
| FedEx   | `FedEx` | Mock implementation |
| UPS     | `UPS`   | Mock implementation |

## Known Limitations

- Carrier adapters are mocked - no real API calls
- Rate limiting is in-memory (resets on restart); for production use Redis-backed Bucket4j
- API keys are hardcoded in `AuthController` - should come from a database
- No token refresh mechanism