# 🛒 E-commerce Microservices Ecosystem

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-Event--Driven-orange.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Caching-red.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)

A professional-grade, event-driven e-commerce platform built with Spring Boot microservices, featuring centralized security, distributed caching, and eventually consistent stock management.

---

## 🏗️ System Architecture

The platform utilizes a modern microservices architecture with a centralized **API Gateway** managing security and traffic routing.

```mermaid
graph TD
    Client[Web/Mobile Client] --> Gateway[API Gateway - Spring Cloud Gateway]
    
    subgraph "Core Services"
        Gateway --> UserService[User Service]
        Gateway --> ProductService[Product Service]
        Gateway --> CartService[Cart Service]
        Gateway --> OrderService[Order Service]
        Gateway --> InventoryService[Inventory Service]
        Gateway --> PaymentService[Payment Service]
    end
    
    subgraph "Infrastructure"
        Kafka((Kafka Event Broker))
        Redis[(Redis Cache)]
        Postgres[(PostgreSQL DB)]
    end
    
    UserService --- Postgres
    ProductService --- Postgres
    ProductService --- Redis
    CartService --- Redis
    OrderService --- Postgres
    InventoryService --- Postgres
    PaymentService --- Postgres
    
    OrderService -.-> Kafka
    InventoryService -.-> Kafka
    PaymentService -.-> Kafka
```

---

## 🔐 Centralized Security Workflow

Security is decoupled from business logic and enforced at the **Edge Level**.

1.  **Authentication**: Handled by `UserService`. Client receives a JWT upon login.
2.  **Validation**: Every request is intercepted by the **API Gateway**'s `JwtFilter`.
3.  **Identity Propagation**: Upon successful validation, the Gateway injects user identity into headers:
    *   `X-User-Id`
    *   `X-User-Role`
    *   `X-User-Email`
4.  **Trust Model**: Downstream services use a shared `GatewayHeaderFilter` to automatically populate the `SecurityContext` based on these trusted headers.

---

## 🔄 Event-Driven Flows (Saga Pattern)

The system manages distributed transactions using the Saga pattern via Kafka to ensure eventual consistency.

### 🟢 Order Success Path
```mermaid
sequenceDiagram
    participant OS as Order Service
    participant IS as Inventory Service
    participant PS as Payment Service
    
    OS->>OS: Create Order (Status: CREATED)
    OS->>IS: Publish order-created
    IS->>IS: Reserve Stock
    IS->>PS: Publish inventory-reserved
    PS->>PS: Process Payment (Success)
    PS->>OS: Publish payment-success
    PS->>IS: Publish payment-success
    OS->>OS: Update Order (Status: COMPLETED)
    IS->>IS: Confirm Stock (Deduct permanently)
    OS->>OS: Publish order-completed (Clear Cart)
```

### 🔴 Payment Failure Compensating Flow
```mermaid
sequenceDiagram
    participant OS as Order Service
    participant IS as Inventory Service
    participant PS as Payment Service
    
    OS->>OS: Create Order (Status: CREATED)
    OS->>IS: Publish order-created
    IS->>IS: Reserve Stock
    IS->>PS: Publish inventory-reserved
    PS->>PS: Process Payment (Failed)
    PS->>OS: Publish payment-failed
    PS->>IS: Publish payment-failed
    OS->>OS: Update Order (Status: FAILED)
    IS->>IS: Release Stock Reservation
```

---

## ⚡ Distributed Caching

Distributed caching is implemented using **Redis** to minimize database load and improve latency.

*   **Shopping Cart**: Fully cached in Redis for sub-millisecond access. Cache is automatically invalidated upon order completion.
*   **Product Catalog**: High-demand products are cached, with TTL-based eviction and manual eviction on updates.
*   **User Sessions**: JWT metadata and frequent user lookups are optimized via Redis.

---

---

## 🚀 API Documentation

All requests should be routed through the **API Gateway** on port `8080`.

### 👤 User Service
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users/register` | Register a new user | Public |
| `POST` | `/api/users/login` | Login and receive JWT | Public |
| `GET` | `/api/users` | List all users | ROLE_ADMIN |

### 📦 Product Service
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Get all products (Paginated) | Public |
| `GET` | `/api/products/{id}` | Get product details | Public |
| `POST` | `/api/products` | Create a new product | ROLE_ADMIN |
| `DELETE` | `/api/products/{id}` | Remove a product | ROLE_ADMIN |

### 🛒 Cart Service
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cart` | View personal shopping cart | ROLE_CUSTOMER |
| `POST` | `/api/cart/add` | Add product to cart | ROLE_CUSTOMER |
| `DELETE` | `/api/cart/{productId}` | Remove item from cart | ROLE_CUSTOMER |
| `POST` | `/api/cart/checkout` | Trigger order checkout | ROLE_CUSTOMER |

### 📋 Order Service
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/orders` | Force-create an order (Saga) | ROLE_CUSTOMER |

### 🏭 Inventory Service (Internal/Admin)
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/inventory/add` | Add stock for a product | ROLE_ADMIN |
| `POST` | `/api/inventory/reserve/{orderId}` | Reserve stock for order | Internal |
| `POST` | `/api/inventory/confirm/{orderId}` | Finalize stock deduction | Internal |
| `POST` | `/api/inventory/release/{orderId}` | Rollback stock reservation | Internal |

### 💳 Payment Service (Internal)
| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/payment/process` | Mock payment processing | Internal |

---

## 🛠️ Detailed Request Specifications

### 🛒 Add to Cart
**Endpoint:** `POST /api/cart/add`
```json
{
  "productId": "uuid-here",
  "quantity": 2
}
```

### 📋 Create Order (Direct)
**Endpoint:** `POST /api/orders`
```json
{
  "items": [
    {
      "productId": "uuid-1",
      "quantity": 1,
      "price": 299.99
    }
  ]
}
```

---

## 🔐 Mandatory Headers (Internal Trust)

When the Gateway forwards requests, it injects the following headers. Downstream services trust these implicitly via `GatewayHeaderFilter`.

*   `X-User-Id`: The UUID of the authenticated user.
*   `X-User-Role`: The assigned role (e.g., `ROLE_CUSTOMER`, `ROLE_ADMIN`).
*   `X-User-Email`: The user's registered email.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.2+ |
| **Gateway** | Spring Cloud Gateway |
| **Persistence** | PostgreSQL |
| **Messaging** | Apache Kafka |
| **Caching** | Redis |
| **Security** | Spring Security & JJWT |
| **Build Tool** | Maven |
