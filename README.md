# E-Commerce Backend API

A production-ready RESTful backend for an e-commerce platform, built with **Spring Boot**.  
Covers the full shopping flow — from authentication and product browsing to cart management, order processing, payments, and reviews.

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
| Role-based access control (USER / ADMIN) | ✅ |
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
| Order status lifecycle (PENDING → PAID → SHIPPED → COMPLETED / CANCELLED) | ✅ |
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
| Email notifications (order confirmation) | ✅ |

### Infrastructure
| Feature | Status |
|---|---|
| Redis caching for product list (TTL 5 min) | ✅ |
| Swagger / OpenAPI documentation | ✅ |
| Docker + Docker Compose | ✅ |
| Restore soft-deleted accounts | ✅ |
| Unit testing | 🔜 In progress |
| Cloud deployment | 🔜 Planned |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Authentication | Spring Security + JWT |
| Database | MySQL |
| Caching | Redis + Spring Cache |
| Image storage | Cloudinary |
| Email | Spring Mail |
| Documentation | Swagger (SpringDoc OpenAPI) |
| Containerization | Docker + Docker Compose |
| Build tool | Maven |

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
  ├──────────→ Redis  (product list cache, TTL 5 min)
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
    id          BIGINT PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(255),
    role        ENUM('USER', 'ADMIN') NOT NULL,
    is_deleted  BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
)
```

### `categories`
```sql
categories (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP
)
```

### `products`
```sql
products (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       DECIMAL(10,2) NOT NULL,
    stock       INT NOT NULL,
    category_id BIGINT,
    image_url   VARCHAR(500),
    is_deleted  BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
)
```

### `carts` & `cart_items`
```sql
carts (
    id      BIGINT PRIMARY KEY,
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

cart_items (
    id         BIGINT PRIMARY KEY,
    cart_id    BIGINT,
    product_id BIGINT,
    quantity   INT NOT NULL,
    FOREIGN KEY (cart_id)    REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

### `orders` & `order_items`
```sql
orders (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT,
    total_price DECIMAL(10,2) NOT NULL,
    status      ENUM('PENDING','PAID','SHIPPED','COMPLETED','CANCELLED'),
    created_at  TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

order_items (
    id         BIGINT PRIMARY KEY,
    order_id   BIGINT,
    product_id BIGINT,
    price      DECIMAL(10,2) NOT NULL,
    quantity   INT NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

### `reviews`
```sql
reviews (
    id         BIGINT PRIMARY KEY,
    user_id    BIGINT,
    product_id BIGINT,
    rating     INT CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP,
    UNIQUE (user_id, product_id),
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
)
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Environment Variables

Create a `.env` file in the project root (or configure directly in `application.yaml`):

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000
JWT_REFRESH_EXPIRATION_MS=604800000

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### Run with Docker

```bash
docker compose up --build
```

The application starts at `http://localhost:8080`.  
Redis runs on port `6379`, MySQL on port `3306`.

### Run Locally

1. Ensure MySQL and Redis are running on your machine.
2. Update `src/main/resources/application.yaml` with your local credentials.
3. Start the application:

```bash
mvn spring-boot:run
```

---

## API Reference

Interactive API documentation is available via Swagger UI once the app is running:

```
http://localhost:8080/swagger-ui.html
```

### Quick Reference

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new account |
| POST | `/api/auth/login` | Public | Login, receive JWT |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | USER | Logout |
| GET | `/api/products` | Public | Get product list *(cached)* |
| GET | `/api/products/{id}` | Public | Get product detail |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Soft-delete product |
| GET | `/api/categories` | Public | Get all categories |
| POST | `/api/categories` | ADMIN | Create category |
| GET | `/api/cart` | USER | View cart |
| POST | `/api/cart/items` | USER | Add item to cart |
| PUT | `/api/cart/items/{id}` | USER | Update item quantity |
| DELETE | `/api/cart/items/{id}` | USER | Remove item from cart |
| POST | `/api/orders` | USER | Place an order |
| GET | `/api/orders` | USER | View order history |
| PUT | `/api/orders/{id}/status` | ADMIN | Update order status |
| POST | `/api/reviews` | USER | Submit a review |
| GET | `/api/admin/stats/revenue` | ADMIN | Revenue by month |
| GET | `/api/admin/stats/top-products` | ADMIN | Top selling products |

---

## Caching Strategy

The product list endpoint (`GET /api/products`) is cached in Redis using Spring Cache.

| Property | Value |
|---|---|
| Cache name | `products` |
| Default TTL | 5 minutes |
| Eviction trigger | create, update, delete, or restore a product |
| Read annotation | `@Cacheable("products")` |
| Write annotation | `@CacheEvict(value = "products", allEntries = true)` |

**Flow:**

```
GET /api/products
  │
  ├── Cache HIT  → return data from Redis immediately
  │
  └── Cache MISS → query DB → store result in Redis → return data
```

Cache name, TTL, and serialization settings are configured in `RedisCacheConfig`.

---

## Security

- All write and user-specific endpoints require a valid **JWT Bearer token** in the `Authorization` header.
- Access tokens are short-lived (default **24 hours**); use `/api/auth/refresh` to obtain a new one.
- Passwords are hashed with **BCrypt** before storage — raw passwords are never persisted.
- Role-based access is enforced at the method level via `@PreAuthorize("hasRole('ADMIN')")`.
- Soft delete is used for users and products — data is never permanently removed, only flagged with `is_deleted = true`.

---

## Running Tests

```bash
mvn test
```

---

## Project Status

| Area | Status |
|---|---|
| Core REST API | ✅ Complete |
| JWT authentication & RBAC | ✅ Complete |
| Redis caching | ✅ Complete |
| Order & payment flow | ✅ Complete |
| Reviews & ratings | ✅ Complete |
| Admin analytics | ✅ Complete |
| Email notifications | ✅ Complete |
| Docker support | ✅ Complete |
| Swagger documentation | ✅ Complete |
| Unit tests | 🔜 In progress |
| Forgot password | 🔜 Planned |
| Cloud deployment | 🔜 Planned |
