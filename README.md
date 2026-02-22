# Document Access Control System

A REST API built with Spring Boot 4 for managing documents with role-based access control.

---

## Tech Stack

- **Java 21** + **Spring Boot 4**
- **Spring Security** — custom header-based authentication
- **Spring Data JPA** + **PostgreSQL**
- **Liquibase** — database migrations
- **Testcontainers** — E2E tests with real PostgreSQL

---

## How It Works

Every request must include an `X-User` header identifying the requester.

- `admin` can do everything
- Regular users need explicit permissions: `READ`, `WRITE`, or `DELETE`

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/documents` | Create a document (admin only) |
| `GET` | `/documents` | Get all accessible documents |
| `GET` | `/documents/{id}` | Get document by ID (requires READ) |
| `DELETE` | `/documents/{id}` | Delete document (requires DELETE) |
| `POST` | `/documents/{id}/grant` | Grant permission (admin or WRITE) |
| `POST` | `/documents/access-check` | Batch permission check |

---

## Running Locally

**Prerequisites:** Java 21, Docker

```bash
# clone the repo
git clone https://github.com/hamzalamin/AccessControlSystem
cd AccessControlSystem

# copy and configure environment variables
cp .env.example .env

# start everything
make run

# follow logs
make logs
```

---

## Make Commands

| Command | Description |
|---------|-------------|
| `make build` | Build the application image |
| `make run` | Start all services |
| `make stop` | Stop all services |
| `make restart` | Restart all services |
| `make logs` | Follow application logs |
| `make clean` | Stop and remove containers and volumes |
| `make test` | Run unit tests |
| `make verify` | Run E2E tests (requires Docker) |

---

## Running Tests

```bash
# unit tests
make test

# E2E tests (requires Docker)
make verify
```

E2E tests spin up a real PostgreSQL container automatically via Testcontainers.

---

## Project Structure

```
src/
├── main/java/com/progresssoft/docaccess/
│   ├── controller/       # REST endpoints
│   ├── service/          # Business logic
│   ├── repository/       # JPA queries
│   ├── entity/           # Document, DocumentAccess
│   ├── security/         # UserHeaderFilter, UserContextHolder
│   ├── dto/              # Request / Response records
│   ├── mapper/           # Entity ↔ DTO
│   ├── enums/            # Permission
│   └── exception/        # Global exception handler
└── e2e/java/             # Testcontainers E2E tests
```

---

## Example Request

```bash
# Create a document
curl -X POST http://localhost:8081/documents \
  -H "X-User: admin" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Document",
    "content": "Hello World",
    "fileType": "pdf",
    "accessibleUsers": [
      { "username": "user1", "permission": "READ" }
    ]
  }'
```
