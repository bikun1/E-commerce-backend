## demo4

Spring Boot demo project using **Redis + Spring Cache** to cache product APIs.  
(Mô tả ngắn: Dự án Spring Boot minh họa cache danh sách sản phẩm với Redis.)

### Features
- **Product list caching**: caches `GET /api/products` responses using `@Cacheable`.
- **Cache invalidation**: clears cache on product `create`, `update`, `delete`, `restore` via `@CacheEvict(allEntries = true)`.
- **Configurable TTL**: default cache time‑to‑live is **5 minutes** in `RedisCacheConfig`.
- **RESTful API**: simple CRUD endpoints for products (và có thể mở rộng thêm cho cart/order).

### Tech stack
- **Java**: 17+ (hoặc version được cấu hình trong project)
- **Framework**: Spring Boot
- **Cache**: Spring Cache abstraction
- **Data store**: Redis
- **Build tool**: Maven

### Prerequisites
- Đã cài **Java JDK** và set `JAVA_HOME`.
- Đã cài **Maven** (`mvn -v` chạy OK trong terminal).
- Có **Docker** (khuyến khích) hoặc một Redis server riêng.
- Redis chạy trên `localhost:6379` (giá trị mặc định trong cấu hình).

### Start Redis with Docker

```bash
docker run --name demo4-redis -p 6379:6379 -d redis:7
```

Để dừng và xóa container:

```bash
docker stop demo4-redis && docker rm demo4-redis
```

### Configure Redis connection

Chỉnh trong file `src/main/resources/application.yaml`:
- `spring.data.redis.host` – host Redis (mặc định: `localhost`)
- `spring.data.redis.port` – port Redis (mặc định: `6379`)

TTL và các options cache khác được cấu hình trong class `RedisCacheConfig`.

### Run the app

Từ thư mục gốc của project:

```bash
mvn spring-boot:run
```

App mặc định chạy tại `http://localhost:8080`.

### API usage

- **Lấy danh sách sản phẩm (được cache)**  
  - **Method**: `GET`  
  - **URL**: `/api/products`  
  - Lần gọi đầu sẽ truy vấn dữ liệu thật và lưu vào cache `products`; các lần tiếp theo trong thời gian TTL sẽ trả về từ Redis.

- **Tạo / cập nhật / xóa / khôi phục sản phẩm**  
  - Các API ghi này sẽ được annotate `@CacheEvict(allEntries = true)` trên cache `products`, giúp lần `GET /api/products` tiếp theo lấy lại data mới nhất.

Ví dụ gọi nhanh bằng `curl`:

```bash
curl http://localhost:8080/api/products
```

### How caching works (high level)
- Khi `ProductServiceImpl#getAllProducts(...)` được gọi, Spring kiểm tra cache `products`.
- Nếu đã có key tương ứng trong Redis, dữ liệu trả về luôn từ cache.
- Nếu chưa có, method chạy, lấy data từ DB/service, sau đó lưu vào Redis với TTL đã config.
- Mọi thao tác ghi (create, update, delete, restore) sẽ clear toàn bộ cache `products` để tránh trả về dữ liệu cũ.

### Run tests

```bash
mvn test
```

### Notes
- Có thể điều chỉnh cache name, TTL, key generator… trong `RedisCacheConfig` và annotations ở service.
- Khi deploy production, hãy:
  - Cấu hình lại `spring.data.redis.host/port` để trỏ tới Redis thật.
  - Bật auth/SSL cho Redis (nếu có) và cấu hình kết nối an toàn.
# demo4

## Redis caching for product list

This project caches the product list endpoint (`GET /api/products`) using Spring Cache + Redis.

### What’s cached
- Cache name: `products`
- Cached method: `ProductServiceImpl#getAllProducts(...)` (already annotated with `@Cacheable`)
- Eviction: on product `create`, `update`, `delete`, `restore` (already annotated with `@CacheEvict(allEntries = true)`)
- Default TTL: 5 minutes (configured in `RedisCacheConfig`)

### Prerequisites
- Redis running on `localhost:6379` (default)

### Start Redis (Docker)

```bash
docker run --name demo4-redis -p 6379:6379 -d redis:7
```

### Configure Redis connection
Edit `src/main/resources/application.yaml`:
- `spring.data.redis.host`
- `spring.data.redis.port`

### Run the app

```bash
mvn spring-boot:run
```

