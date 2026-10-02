# QueueLess — Smart Virtual Queue & Crowd Management System

> **Don't wait in line. Own your time.**

QueueLess is a full-stack microservices application that eliminates physical waiting queues. Customers join queues digitally, receive a token, track their position in real time, and arrive only when it's their turn.

---

## Screenshots

> _Add screenshots here after running the project locally._

| Landing Page | Business Discovery | My Token | Staff Dashboard |
|---|---|---|---|
| ![landing]() | ![discovery]() | ![token]() | ![staff]() |

---

## Problem Statement

Physical waiting queues at clinics, salons, government offices, and service centers waste hours of people's time every day. There is no visibility into wait times and no way to step out and return at the right moment.

**QueueLess** solves this by giving every customer a digital token with a live position tracker and estimated wait time — accessible from any device.

---

## Features

### Customer
- Browse businesses by category, city, and open status
- View current queue size and estimated wait time
- Join a virtual queue and receive a digital token
- Track position live (auto-refreshes every 15 seconds)
- See "shorter queue nearby" recommendations
- Leave the queue at any time

### Staff
- View live waiting queue for their business
- Call the next customer
- Mark tokens as completed or skipped
- Pause, resume, or close the queue
- View today's served / skipped / waiting stats

### Admin
- Create and manage businesses
- Configure average service time per business
- Create and manage queues per business
- Open / close / pause queues
- System-wide overview with live statistics

---

## Architecture

```
React Frontend (port 3000)
        │
        ▼
API Gateway (port 8080)  ◄── JWT validation here
        │
   ┌────┴────┬──────────┐
   ▼         ▼          ▼
User      Business    Queue
Service   Service     Service
(8081)    (8082)      (8083)
   │         │          │
   ▼         ▼          │
MySQL      MySQL     OpenFeign ──► Business Service
(user_db) (biz_db)      │
                        ▼
                      MySQL
                    (queue_db)
                        │
                        ▼
                  Eureka Server (8761)
```

- Every microservice registers with **Eureka**
- API Gateway routes using `lb://service-name` — no hardcoded URLs
- Queue Service calls Business Service via **OpenFeign**
- JWT is validated at the Gateway and forwarded as headers (`X-User-Id`, `X-User-Role`)

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Service Communication | Spring Cloud OpenFeign |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| Database | MySQL 8, Spring Data JPA |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito |
| Frontend | React 18, React Router v6, Axios |
| Build | Maven |

---

## Microservices

| Service | Port | Database | Responsibility |
|---|---|---|---|
| `queueless-eureka-server` | 8761 | — | Service registry |
| `queueless-api-gateway` | 8080 | — | Routing + JWT validation |
| `queueless-user-service` | 8081 | `queueless_user_db` | Auth, registration, profiles |
| `queueless-business-service` | 8082 | `queueless_business_db` | Businesses and services |
| `queueless-queue-service` | 8083 | `queueless_queue_db` | Queue lifecycle, tokens, wait time |

---

## Database Design

Each service owns its own database — no cross-service JPA relationships.

### queueless_user_db
```
users
  id               UUID (PK)
  name             VARCHAR
  email            VARCHAR (unique)
  password         VARCHAR (BCrypt)
  role             ENUM(CUSTOMER, STAFF, ADMIN)
  active           BOOLEAN
  created_at       DATETIME
  updated_at       DATETIME
```

### queueless_business_db
```
businesses
  id                   UUID (PK)
  name                 VARCHAR
  category             ENUM(CLINIC, SALON, DIAGNOSTIC_CENTER, ...)
  description          VARCHAR
  address              VARCHAR
  city                 VARCHAR
  phone                VARCHAR
  average_service_time INT (minutes)
  open                 BOOLEAN
  created_at           DATETIME

services
  id                   UUID (PK)
  business_id          UUID  ← plain UUID, no @ManyToOne
  name                 VARCHAR
  description          VARCHAR
  estimated_minutes    INT
  active               BOOLEAN
```

### queueless_queue_db
```
queues
  id                   UUID (PK)
  business_id          UUID  ← plain UUID, no @ManyToOne
  queue_name           VARCHAR
  status               ENUM(OPEN, PAUSED, CLOSED)
  current_token_number INT
  average_service_time INT
  opened_at            DATETIME
  closed_at            DATETIME

queue_tokens
  id             UUID (PK)
  queue_id       UUID
  customer_id    UUID
  token_number   INT
  status         ENUM(WAITING, CALLED, SERVING, COMPLETED, SKIPPED, CANCELLED)
  joined_at      DATETIME
  called_at      DATETIME
  completed_at   DATETIME
  skipped_at     DATETIME
```

---

## Authentication Flow

```
POST /api/users/register  →  creates user
POST /api/users/login     →  returns JWT

JWT payload:
{
  "sub": "<userId>",
  "email": "user@example.com",
  "role": "CUSTOMER",
  "exp": <timestamp>
}

React stores token in localStorage
Axios interceptor adds: Authorization: Bearer <token>

API Gateway:
  1. Validates JWT signature
  2. Extracts claims
  3. Forwards headers to downstream:
       X-User-Id    → user UUID
       X-User-Role  → CUSTOMER / STAFF / ADMIN
       X-User-Email → email
```

---

## Queue Flow

```
1. Admin creates a Queue for a Business
2. Staff opens the Queue  →  status = OPEN

3. Customer joins Queue
   POST /api/queues/{queueId}/join
   → Token #27 assigned
   → peopleAhead  = WAITING tokens with tokenNumber < 27
   → estimatedWait = peopleAhead × averageServiceTime

4. Staff calls next
   POST /api/queues/{queueId}/next
   → First WAITING token → status = CALLED

5. Customer sees "You've been called!" on /my-token

6. Staff completes token
   POST /api/queues/tokens/{tokenId}/complete
   → status = COMPLETED

7. Customer sees COMPLETED status
```

---

## OpenFeign Communication

Queue Service calls Business Service using Feign — no hardcoded URLs:

```java
@FeignClient(name = "business-service")  // Eureka resolves this at runtime
public interface BusinessClient {

    @GetMapping("/api/businesses/{id}")
    BusinessDto getBusinessById(@PathVariable UUID id);

    @GetMapping("/api/businesses")
    List<BusinessDto> getBusinessesByCategory(@RequestParam String category);
}
```

---

## How to Run Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running on `localhost:3306`
- Node.js 18+ and npm

### Step 1 — MySQL Setup

```sql
CREATE DATABASE queueless_user_db;
CREATE DATABASE queueless_business_db;
CREATE DATABASE queueless_queue_db;
```

> Default credentials used: `root / root`
> Change in each service's `src/main/resources/application.yml` if different.

### Step 2 — Start Backend Services (in order)

```bash
# Terminal 1 — Eureka Server (start this first, wait ~15 seconds)
cd backend/queueless-eureka-server
mvn spring-boot:run

# Terminal 2 — API Gateway
cd backend/queueless-api-gateway
mvn spring-boot:run

# Terminal 3 — User Service
cd backend/queueless-user-service
mvn spring-boot:run

# Terminal 4 — Business Service
cd backend/queueless-business-service
mvn spring-boot:run

# Terminal 5 — Queue Service
cd backend/queueless-queue-service
mvn spring-boot:run
```

**Or use the convenience script (macOS):**
```bash
chmod +x start-all.sh
./start-all.sh
```

### Step 3 — Load Sample Data (optional)

```bash
mysql -u root -p < backend/db/seed.sql
```

### Step 4 — Start React Frontend

```bash
cd frontend
npm install
npm start
```

App runs at: **http://localhost:3000**

---

## Default Users

These are auto-created on first startup of user-service:

| Email | Password | Role |
|---|---|---|
| `admin@gmail.com` | `admin123` | ADMIN |
| `staff@gmail.com` | `staff123` | STAFF |
| `customer@gmail.com` | `customer123` | CUSTOMER |

---

## Swagger / API Docs

| Service | URL |
|---|---|
| User Service | http://localhost:8081/swagger-ui.html |
| Business Service | http://localhost:8082/swagger-ui.html |
| Queue Service | http://localhost:8083/swagger-ui.html |
| Eureka Dashboard | http://localhost:8761 |

---

## Sample API Requests

```bash
# Register a new customer
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya Sharma","email":"priya@example.com","password":"secret123","role":"CUSTOMER"}'

# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priya@example.com","password":"secret123"}'

# Get all open clinics in Pune
curl "http://localhost:8080/api/businesses?category=CLINIC&city=Pune&open=true"

# Join a queue (replace with real IDs)
curl -X POST http://localhost:8080/api/queues/{queueId}/join \
  -H "Authorization: Bearer <token>"

# Get my active token
curl http://localhost:8080/api/queues/my-token \
  -H "Authorization: Bearer <token>"

# Staff: call next customer
curl -X POST http://localhost:8080/api/queues/{queueId}/next \
  -H "Authorization: Bearer <staff-token>"
```

---

## Project Structure

```
QueueLess/
├── backend/
│   ├── queueless-eureka-server/
│   ├── queueless-api-gateway/
│   ├── queueless-user-service/
│   │   └── src/main/java/com/queueless/userservice/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       ├── exception/
│   │       ├── security/
│   │       └── config/
│   ├── queueless-business-service/
│   └── queueless-queue-service/
│       └── src/main/java/com/queueless/queueservice/
│           ├── client/          ← OpenFeign
│           ├── controller/
│           ├── service/
│           ├── repository/
│           ├── entity/
│           ├── dto/
│           ├── exception/
│           └── config/
├── frontend/
│   └── src/
│       ├── api/                 ← userApi, businessApi, queueApi
│       ├── components/
│       ├── context/             ← AuthContext
│       ├── pages/
│       │   ├── auth/
│       │   ├── customer/
│       │   ├── staff/
│       │   └── admin/
│       └── styles/
├── start-all.sh
└── stop-all.sh
```

---

## Environment Variables

| Variable | Default |
|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/queueless_*_db` |
| `spring.datasource.username` | `root` |
| `spring.datasource.password` | `root` |
| `jwt.secret` | `queueless-super-secret-key-must-be-at-least-256-bits-long-for-hs256` |
| `jwt.expiration-ms` | `86400000` (24 hours) |
| `REACT_APP_API_BASE_URL` | `http://localhost:8080` |

---

## Key Concepts for Interviews

| Concept | How it's used in this project |
|---|---|
| **Eureka** | All 3 services register on startup. Gateway uses `lb://service-name` for load-balanced routing |
| **API Gateway** | Single entry point for all requests. JWT filter validates tokens before forwarding |
| **OpenFeign** | Queue Service calls Business Service by name — Eureka resolves the actual host at runtime |
| **JWT** | Generated in User Service on login. Validated in Gateway. Forwarded as `X-User-Id` / `X-User-Role` headers |
| **Spring Security** | Stateless session. BCrypt password hashing. Role-based access control |
| **JPA** | UUID primary keys. No cross-service `@ManyToOne`. Indexes on frequently searched columns |
| **DTOs** | Strict request/response separation. JPA entities never exposed directly from controllers |
| **Exception Handling** | `@RestControllerAdvice` with custom exceptions. Consistent error response shape |
| **Microservices** | Each service has its own database, port, and single responsibility |
| **Wait Time Calc** | `peopleAhead × averageServiceTime` — isolated in `WaitTimeCalculator` component |

---

## Future Improvements

- WebSocket / SSE for real-time push instead of polling
- SMS / email notification when token is called
- Docker Compose for one-command startup
- Multi-counter support per queue
- Appointment-based booking alongside walk-in queues
- Rate limiting on join queue endpoint
- Business analytics dashboard with historical data

---

## Author

Built as a portfolio project to demonstrate Java microservices architecture.

**Tech demonstrated:** Spring Boot · Spring Cloud · Eureka · API Gateway · OpenFeign · JWT · Spring Security · JPA · React · REST API design · DTO pattern · Exception handling · Unit testing
