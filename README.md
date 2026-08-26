# Vitrine 3D

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-blue?logo=postgresql)
![GraalVM](https://img.shields.io/badge/GraalVM-Native_Image-purple)

Multi-tenant SaaS platform for 3D printing professionals to showcase products and receive orders via WhatsApp.

---

## Overview

Vitrine 3D lets small 3D printing businesses create a public storefront without technical knowledge. Each store gets a shareable profile with a product catalog, custom branding, and a WhatsApp-based purchase flow.

The platform supports two layout modes driven by the store's business type:

- **LOJA** — product grid with prices (physical goods, accessories)
- **SERVICOS** — service portfolio without prices (real estate agents, general services)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 (Gradle) |
| Database | PostgreSQL · Flyway migrations |
| Cache / Rate Limiting | Redis (Upstash) |
| File Storage | Cloudflare R2 (S3-compatible) |
| Auth | JWT (access + refresh token rotation, HTTP-only cookies) |
| Build | GraalVM Native Image (AOT) |
| CI/CD | GitHub Actions → GHCR |
| Hosting | Render (managed containers) |

---

## Features

### Storefront (public)
- Public store profiles via SEO-friendly slug (`/loja/minha-loja-3d`)
- Slug history — old slugs redirect seamlessly to the current one
- Product catalog with dynamic attribute filtering (color, material, size — defined per store)
- WhatsApp click tracking for analytics
- Store theme customization (font, cover color, theme key)
- Featured products section

### Store Management
- Registration with email verification (24-hour token)
- Logo, cover image and up to 3 promotional images (Cloudflare R2)
- Custom product types (e.g. "Shirt", "Action Figure") scoped per store
- Dynamic attribute definitions per product type (TEXT, NUMBER, ENUM, BOOLEAN, URL)
- Stock tracking per product
- Affiliate profile — links to external product pages with click tracking

### Subscriptions
- Automatic 30-day trial created on registration
- Plans: FREE · BASIC · PRO with configurable limits per plan
- Admin can extend trials, change plans, or override status manually

### PDV — Point of Sale
Offline-first system designed for brick-and-mortar stores operating without stable internet.

- Sync by unique `offline_id` — no duplicate sales on reconnect
- Employee management with PIN-based login at the register
- Customer registration with credit/debt tracking (fiado)
- Cash flow ledger (income and expense, categorized)
- Sale items snapshot — product name and price preserved even after product deletion
- Payment methods: Cash · PIX · Debit card · Credit card · Credit account

### Admin Panel
- List, activate/deactivate and delete any store
- Override subscription plan, status and trial dates
- Platform-wide stats

### Auth & Security
- Dual auth: store owners and admin users in separate tables with independent flows
- JWT access token + rotating refresh token (HTTP-only cookie)
- Rate limiting on public endpoints via Redis sliding window

---

## Database

Five Flyway migrations — `validate` mode in production (no destructive auto-DDL):

| Migration | Description |
|---|---|
| V1 | Core schema: stores, products, auth |
| V2 | PDV system |
| V3 | Subscriptions and plan limits |
| V4 | Layout mode (LOJA / SERVICOS) |
| V5 | Schema simplification |

---

## Getting Started

**Requirements:** Java 21, Docker

```bash
# Start PostgreSQL + Redis
docker compose up -d

# Run the application
./gradlew bootRun
```

API base: `http://localhost:8080`  
Health check: `GET /api/health`  
Swagger UI: `GET /swagger-ui/index.html`

---

## Environment Variables

| Variable | Description |
|---|---|
| `DATABASE_URL` | PostgreSQL JDBC URL |
| `REDIS_URL` | Redis connection URL |
| `REDIS_PASSWORD` | Redis password |
| `JWT_SECRET` | JWT signing secret (min 32 chars) |
| `STORAGE_ENDPOINT` | S3-compatible endpoint (R2) |
| `STORAGE_BUCKET` | Bucket name |
| `STORAGE_ACCESS_KEY` | Storage access key |
| `STORAGE_SECRET_KEY` | Storage secret key |
| `ADMIN_EMAIL` | Admin user email *(default: admin@vitrine3d.com)* |
| `ADMIN_PASSWORD` | Admin user password |
| `COOKIE_SECURE` | `true` for HTTPS environments |
| `COOKIE_SAME_SITE` | `None` for cross-origin frontends |
| `APP_KEEP_ALIVE_ENABLED` | `true` to prevent serverless cold starts |

---

## Key API Endpoints

### Public
```
GET  /api/users/{slug}                        Store profile
GET  /api/products/store/{id}/public          Product catalog
GET  /api/products/store/{id}/search          Filtered search
GET  /api/products/store/{id}/featured        Featured products
POST /api/products/{id}/whatsapp-click        Track click
GET  /api/locations/states                    Brazilian states
GET  /api/locations/states/{id}/cities        Cities by state
```

### Auth
```
POST /api/auth/login                          Login → role: STORE_OWNER | ADMIN
POST /api/auth/refresh                        Refresh access token
POST /api/auth/logout
GET  /api/auth/verify-email?token=...
POST /api/auth/resend-verification
POST /api/users/register
```

### Store (authenticated)
```
GET    /api/users/me                          Own profile
PATCH  /api/users/me                          Update settings + layout
POST   /api/users/me/logo                     Upload logo
POST   /api/users/me/cover-image              Upload cover
DELETE /api/users/me/cover-image
POST   /api/users/me/promo-images             Upload promo images (max 3)
```

### Catalog (authenticated)
```
GET    /api/products/store/{id}               Own products
POST   /api/products                          Create product
PUT    /api/products/{id}                     Update product
DELETE /api/products/{id}
GET    /api/products/store/{id}/product-types   Product types
POST   /api/product-types                     Create product type
PUT    /api/product-types/{id}
DELETE /api/product-types/{id}
```

### PDV (authenticated)
```
POST /api/pdv/sales/sync                      Sync offline sales batch
GET  /api/pdv/sales                           Sales list
GET  /api/pdv/customers                       Customers
POST /api/pdv/customers
GET  /api/pdv/employees                       Employees
POST /api/pdv/employees
POST /api/pdv/cash-flows/sync                 Sync cash flow batch
GET  /api/pdv/customer-credits                Credits (fiado)
POST /api/pdv/customer-credits/{id}/payments  Register payment
```

### Admin (ADMIN role required)
```
GET    /api/admin/stores                      All stores (paginated)
PATCH  /api/admin/stores/{id}/toggle-active
DELETE /api/admin/stores/{id}
PUT    /api/admin/stores/{id}/subscription
POST   /api/admin/stores/{id}/subscription/extend
GET    /api/admin/stats
```

---

## Deployment

The application compiles to a GraalVM native image and is published to GHCR via GitHub Actions. Render pulls the image and runs it as a managed container.

```bash
# Build and push (manual)
docker build -t ghcr.io/<user>/vitrine3d:latest .
docker push ghcr.io/<user>/vitrine3d:latest
```

Staging environment mirrors production using a separate Neon database branch and Upstash Redis instance.
