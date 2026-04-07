# E-Commerce Backend API

A production-ready RESTful backend for an e-commerce platform, built with **Spring Boot 3**.  
Covers the full shopping flow — from authentication and product browsing to cart management, order processing, payments, and reviews.

> 🚀 **Live demo:** `https://bikun1-ecommerce-backend.up.railway.app`  
> 📄 **Swagger UI:** `https://bikun1-ecommerce-backend.up.railway.app/swagger-ui/index.html`

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Database Design](#database-design)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Environment Variables](#environment-variables)
  - [Run with Docker](#run-with-docker)
  - [Run Locally](#run-locally)
- [API Reference](#api-reference)
- [Caching Strategy](#caching-strategy)
- [Security](#security)
- [Running Tests](#running-tests)
- [Project Status](#project-status)

---

## Features

### Authentication & Authorization
| Feature | Status |
|---|---|
| Register | ✅ |
| Login with JWT | ✅ |
| Refresh token | ✅ |
| Logout | ✅ |
| Role-based access control (USER / ADMIN / MODERATOR) | ✅ |
| Update profile | ✅ |
| Change password | ✅ |
| Forgot password | 🔜 Planned |

### Product Management
| Feature | Status |
|---|---|
| Create / Update / Delete product (Admin) | ✅ |
| View product list & detail | ✅ |
| Search product | ✅ |
| Filter by category, price range, rating | ✅ |
| Pagination | ✅ |
| Product images (Cloudinary) | ✅ |
| Soft delete (`is_deleted`) | ✅ |

### Category Management
| Feature | Status |
|---|---|
| Create / Update / Delete category (Admin) | ✅ |
| View all categories | ✅ |

### Shopping Cart
| Feature | Status |
|---|---|
| Add to cart | ✅ |
| Update item quantity | ✅ |
| Remove item | ✅ |
| View cart | ✅ |
| Auto-calculate total price | ✅ |

### Order System
| Feature | Status |
|---|---|
| Create order | ✅ |
| Order status lifecycle (`PENDING → PAID → SHIPPED → COMPLETED / CANCELLED`) | ✅ |
| View order history | ✅ |
| Admin can update order status | ✅ |
| Stock validation before placing order | ✅ |
| Transactional order creation | ✅ |

### Payment
| Feature | Status |
|---|---|
| Mock payment gateway | ✅ |
| Save payment status | ✅ |
| Store transaction ID | ✅ |

### Reviews & Ratings
| Feature | Status |
|---|---|
| Users can review a product | ✅ |
| One review per user per product (enforced by DB unique constraint) | ✅ |
| Rating from 1 to 5 | ✅ |
| Average rating per product | ✅ |

### Admin & Analytics
| Feature | Status |
|---|---|
| Revenue statistics by month | ✅ |
| Top selling products | ✅ |
| Dashboard overview | ✅ |
| Email notifications (order confirmation) | ✅ |

### Infrastructure
| Feature | Status |
|---|---|
| Redis caching for product list (TTL 10 min) | ✅ |
| Swagger / OpenAPI documentation | ✅ |
| Docker + Docker Compose | ✅ |
| Restore soft-deleted accounts | ✅ |
| Unit testing | ✅ |
| Cloud deployment (Railway) | ✅ |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Authentication | Spring Security + JWT |
| Database | MySQL |
| Caching | Redis + Spring Cache |
| Image Storage | Cloudinary |
| Email | Resend |
| Documentation | Swagger (SpringDoc OpenAPI) |
| Containerization | Docker + Docker Compose |
| Deployment | Railway |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

---

## Architecture Overview

```
Client
  │
  ▼
Spring Security Filter (JWT validation)
  │
  ▼
Controller Layer      (REST endpoints, request/response mapping)
  │
  ▼
Service Layer         (business logic, @Cacheable / @CacheEvict)
  │
  ├──────────→ Redis  (product list cache, TTL 10 min)
  │
  ▼
Repository Layer      (Spring Data JPA)
  │
  ▼
MySQL Database
```

---

## Database Design

### `users`
```sql
users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  UNIQUE NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100),
    enabled     BOOLEAN      DEFAULT TRUE,
    is_deleted  BOOLEAN      DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
)
```

### `user_roles`
```sql
user_roles (
    user_id  BIGINT NOT NULL,
    role     VARCHAR(20) NOT NULL,   -- ROLE_USER | ROLE_ADMIN | ROLE_MODERATOR
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
)
```

### `refresh_tokens`
```sql
refresh_tokens (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    token       VARCHAR(600) UNIQUE NOT NULL,
    user_id     BIGINT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN   NOT NULL DEFAULT FALSE,
    is_deleted  BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
)
```

### `categories`
```sql
categories (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(500),
    is_deleted  BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL
)
```

### `products`
```sql
products (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       DECIMAL(10,2) NOT NULL,
    stock       INT NOT NULL,
    rating      DOUBLE,
    category_id BIGINT,
    is_deleted  BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
)
```

### `images`
```sql
images (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    url        VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL
)
```

### `carts` & `cart_items`
```sql
carts (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT UNIQUE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

cart_items (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    UNIQUE (cart_id, product_id),
    FOREIGN KEY (cart_id)    REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

### `orders` & `order_items`
```sql
orders (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT NOT NULL,
    total_price    DECIMAL(10,2) NOT NULL,
    status         ENUM('PENDING','PAID','SHIPPED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    payment_status ENUM('PENDING','SUCCESS','FAILED'),
    transaction_id VARCHAR(100),
    is_deleted     BOOLEAN   DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

order_items (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id   BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    price      DECIMAL(10,2) NOT NULL,
    quantity   INT NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

### `reviews`
```sql
reviews (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    is_deleted BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (user_id, product_id),
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Environment Variables

The project uses separate env files depending on how you run it:

| File | Used by |
|---|---|
| `.env.local` | `mvn spring-boot:run` (local dev) |
| `.env.docker` | `docker compose up` |

Create the appropriate file(s) in the project root using the template below.  
**Do not commit either file to version control.**

```env
MAIL_FROM=your_sender@example.com
RESEND_API_KEY=your_resend_api_key
RESEND_API_BASE_URL=https://api.resend.com

SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ecommerce
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=ecommerce

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your_jwt_secret_key

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

> For `.env.docker`, set `SPRING_DATASOURCE_URL` to point at the Docker service name (e.g. `jdbc:mysql://mysql:3306/ecommerce`) and `REDIS_HOST` to `redis`.

### Run with Docker

```bash
docker compose --env-file .env.docker up --build
```

The application starts at `http://localhost:8080`.  
Redis runs on port `6379`, MySQL on port `3306`.

### Run Locally

1. Ensure MySQL and Redis are running on your machine.
2. Create `.env.local` with your local credentials (see template above).
3. Start the application:

```bash
mvn spring-boot:run
```

---

## API Reference

Interactive API documentation is available via Swagger UI:

```
# Local
http://localhost:8080/swagger-ui/index.html

# Production
https://bikun1-ecommerce-backend.up.railway.app/swagger-ui/index.html
```

### Quick Reference

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new account |
| POST | `/api/auth/login` | Public | Login, receive JWT |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | USER | Logout |
| POST | `/api/auth/change-password` | USER | Change password |
| GET | `/api/users` | ADMIN | Get all users |
| GET | `/api/users/{id}` | USER / ADMIN | Get user by id |
| PATCH | `/api/users/{id}` | ADMIN | Update user |
| DELETE | `/api/users/{id}` | ADMIN | Soft-delete user |
| PATCH | `/api/users/{id}/restore` | ADMIN | Restore deleted user |
| GET | `/api/products` | Public | Get product list *(cached)* |
| GET | `/api/products/{id}` | Public | Get product detail |
| POST | `/api/products` | ADMIN | Create product |
| PATCH | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Soft-delete product |
| PATCH | `/api/products/{id}/restore` | ADMIN | Restore deleted product |
| GET | `/api/categories` | Public | Get all categories |
| GET | `/api/categories/{id}` | Public | Get category by id |
| POST | `/api/categories` | ADMIN | Create category |
| PATCH | `/api/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/categories/{id}` | ADMIN | Soft-delete category |
| PATCH | `/api/categories/{id}/restore` | ADMIN | Restore deleted category |
| GET | `/api/cart` | USER | View cart |
| POST | `/api/cart/items` | USER | Add item to cart |
| PATCH | `/api/cart/items/{cartItemId}` | USER | Update item quantity |
| DELETE | `/api/cart/items/{cartItemId}` | USER | Remove item from cart |
| POST | `/api/orders` | USER | Place an order |
| GET | `/api/orders` | USER | View order history |
| GET | `/api/orders/{id}` | USER | Get order by id |
| POST | `/api/orders/{id}/pay` | USER | Pay for order |
| PATCH | `/api/orders/{id}/status` | ADMIN | Update order status |
| POST | `/api/products/{productId}/reviews` | USER | Submit a review |
| GET | `/api/products/{productId}/reviews` | Public | Get reviews for a product |
| POST | `/api/uploads/image` | ADMIN | Upload product image |
| GET | `/api/admin/statistics/revenue-by-month` | ADMIN | Revenue by month |
| GET | `/api/admin/statistics/top-selling-products` | ADMIN | Top selling products |
| GET | `/api/admin/statistics/dashboard` | ADMIN | Dashboard overview |

---

## Caching Strategy

The product list endpoint (`GET /api/products`) is cached in Redis using Spring Cache.

| Property | Value |
|---|---|
| Cache name | `products` |
| Default TTL | 10 minutes |
| Cache key | composite of `methodName + page + size + sortBy + sortDir + filter` |
| Eviction trigger | Create, update, delete, or restore a product (`allEntries = true`) |
| Read annotation | `@Cacheable(cacheNames = "products", key = "...")` |
| Write annotation | `@CacheEvict(cacheNames = "products", allEntries = true)` |

Each unique combination of pagination, sort, and filter parameters is stored as a **separate cache entry**. When any product is modified, all entries are evicted at once via `allEntries = true`.

**Flow:**

```
GET /api/products?page=0&size=10&sortBy=price&minPrice=100
  │
  ├── Cache HIT  (key matches) → return data from Redis immediately
  │
  └── Cache MISS → query DB → store result under composite key → return data
```

Cache name, TTL, and serialization settings are configured in `RedisCacheConfig`.

---

## Security

- All write and user-specific endpoints require a valid **JWT Bearer token** in the `Authorization` header.
- Access tokens are short-lived (default **15 minutes**); use `/api/auth/refresh` to obtain a new one.
- Refresh tokens are valid for **7 days** (`604800000 ms`).
- Refresh token is stored as an **HttpOnly, Secure cookie** — never exposed to JavaScript.
- Refresh tokens are tracked in the database and can be explicitly revoked on logout.
- Passwords are hashed with **BCrypt** before storage — raw passwords are never persisted.
- Role-based access is enforced at the method level via `@PreAuthorize`. Three roles are supported: `ROLE_USER`, `ROLE_ADMIN`, and `ROLE_MODERATOR`.
- Soft delete is used for users and products — data is never permanently removed, only flagged with `is_deleted = true`.

---

## Running Tests

```bash
mvn test
```

The test suite covers service-layer business logic and repository interactions using **JUnit 5** and **Mockito**.

---

## Project Status

| Area | Status |
|---|---|
| Core REST API | ✅ Complete |
| JWT Authentication & RBAC | ✅ Complete |
| Redis Caching | ✅ Complete |
| Order & Payment Flow | ✅ Complete |
| Reviews & Ratings | ✅ Complete |
| Admin Analytics | ✅ Complete |
| Email Notifications | ✅ Complete |
| Docker Support | ✅ Complete |
| Swagger Documentation | ✅ Complete |
| Unit Tests | ✅ Complete |
| Forgot Password | 🔜 Planned |
| Cloud Deployment (Railway) | ✅ Complete |
