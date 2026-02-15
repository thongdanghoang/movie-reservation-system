---
stepsCompleted: [ 1, 2, 3, 4, 5, 6, 7, 8 ]
inputDocuments: [ 'prd.md', 'product-brief-movie-reservation-system-2026-02-15.md', 'README.md' ]
workflowType: 'architecture'
project_name: 'movie-reservation-system'
user_name: 'thomas'
lastStep: 8
status: 'complete'
completedAt: '2026-02-15'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each
architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

1. **Content Discovery:** Guest view of movies and showtimes using React/Vue SPA.
2. **Real-Time Seat Selection:** WebSocket-driven seat map; "Available", "Held", "Sold" states.
3. **Atomic Booking:** Guest checkout, hold timer enforcement, payment processing (simulated), fail-fast consistency.
4. **Fulfillment:** QR code generation, email delivery.
5. **Admin Watchtower:** Real-time dashboard, transaction search, manual resend.

**Non-Functional Requirements:**

- **Performance:** P99 booking transaction time < 500ms; Seat map updates < 100ms.
- **Reliability:** 0% double-booking tolerance; ACID compliance for inventory.
- **Security:** No raw credit card storage; Rate limiting.
- **Compliance:** GDPR/CCPA support.

**Scale & Complexity:**

- Primary domain: **Full-Stack Web (Real-time Transactional)**
- Complexity level: **High**
    - High concurrency (1,000 requests/sec).
    - Strict consistency requirements (CP system).
    - **Technology Specifics:** Quarkus Match (Reactive/Imperative), Next.js, Postgres/Mongo, Keycloak.

### Technical Constraints & Dependencies

- **Backend Stack:** Quarkus (JDK 25).
    - **Execution Model:** **Hybrid**. Reactive for high-throughput Reservation Service (leveraging unified
      configuration); Imperative for Admin/Reporting.
- **Frontend Stack:** Next.js.
- **Database:** PostgreSQL (Core transactional), MongoDB (Audit/Logs).
- **Identity:** Keycloak (OIDC).
    - **Token Strategy:** **Phantom Token Pattern**. Gateway exchanges Opaque tokens (client-side) for JWTs (internal).
- **Architecture Style:** "Atomic Bunker" - Consistency First.

### Cross-Cutting Concerns Identified

- **Concurrency Control:** Locking mechanisms, isolation levels, race condition handling.
- **Observability:** Distributed tracing, metrics, logs.
- **Error Handling:** Fail-fast responses, graceful degradation (load shedding).
- **State Management:** Synchronization between DB and Client WebSocket state.

## Starter Template Evaluation

### Primary Technology Domain

**Full-Stack Web (Real-time Transactional)** based on project requirements analysis.

### Starter Options Considered

* **Official Quarkus CLI:** The most reliable way to bootstrap Quarkus applications. Allows precise control over
  versions (3.31+ as requested) and extensions.
* **Create Next App:** The industry standard for Next.js.
* **JHipster (Quarkus Blueprint):** Rejected due to lack of granular control over versions and "Atomic Bunker"
  architecture needs.

### Selected Starter: Manual Composition (Official CLIs)

**Rationale for Selection:**
Manual composition via official CLIs provides the necessary control to enforce **Quarkus 3.31+** and **JDK 25+** while
maintaining a clean, modular architecture (Hybrid Reactive/Imperative).

**Initialization Commands:**

* **Backend (Reservation Service - Reactive):**
  ```bash
  quarkus create app com.atomicbunker:reservation-service \
    --extension='resteasy-reactive-jackson,hibernate-reactive-panache,reactive-pg-client,smallrye-openapi,smallrye-health' \
    --java=25 --maven --stream=3.31
  ```
* **Backend (Admin Service - Imperative):**
  ```bash
  quarkus create app com.atomicbunker:admin-service \
    --extension='resteasy-reactive-jackson,hibernate-orm-panache,jdbc-postgresql,smallrye-openapi,smallrye-health' \
    --java=25 --maven --stream=3.31
  ```
* **Frontend (Next.js):**
  ```bash
  npx create-next-app@latest frontend \
    --typescript --tailwind --eslint --src-dir --app --import-alias '@/*'
  ```

**Architectural Decisions Provided by Starters:**

**Language & Runtime:**

* **Backend:** Java 25+ (Latest) / Quarkus 3.31+.
* **Frontend:** TypeScript 5+ (Strict Mode).

**Styling Solution:**

* **Frontend:** Tailwind CSS v4 (if available) / v3.4.

**Build Tooling:**

* **Backend:** Maven (Standard).
* **Frontend:** Turbopack.

**Testing Framework:**

* **Backend:** JUnit 5 + RestAssured.
* **Frontend:** Vitest + React Testing Library.

**Code Organization:**

* **Monorepo Structure (Proposed):**
  ```
  /
  ├── frontend/             # Next.js App
  ├── services/
  │   ├── reservation/      # Quarkus Reactive
  │   └── admin/            # Quarkus Imperative
  ├── docker-compose.yml    # Local Dev orchestration
  └── README.md
  ```

**Development Experience:**

* **Backend:** Quarkus Dev Mode (Live coding).
* **Frontend:** Next.js Dev Server.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**

* **Database Locking:** Pessimistic via `SELECT ... FOR UPDATE NOWAIT` (Atomic Bunker req).
* **Real-Time Transport:** WebSockets (Quarkus Reactive) for bi-directional seat holds.
* **Frontend State:** TanStack Query + Zustand (Server vs Client state separation).

**Important Decisions (Shape Architecture):**

* **Migration Tool:** Flyway (SQL-first).
* **Seat Map Rendering:** SVG (Performance + Styling balance).

### Data Architecture

* **Locking Strategy:** **Pessimistic Locking**.
    * *Rationale:* Optimistic locking risks late failures under high contention. Pessimistic locking (NOWAIT) provides
      immediate feedback ("Fail Fast") at the DB level, essential for the "Atomic Bunker" guarantee.
* **Migration Tool:** **Flyway**.
    * *Rationale:* Aligns with the need for hand-tuned SQL for performance critical tables.

### Authentication & Security

* **Authentication:** **Keycloak (OIDC)** via Phantom Token Pattern.
    * *Rationale:* Decided in Context Analysis. Implementation will use Keycloak Sidecar or Gateway integration.

### API & Communication Patterns

* **Real-Time Updates:** **WebSockets**.
    * *Rationale:* Required for sub-100ms updates of seat status (Available -> Held).
* **API Definition:** **Code-First (OpenAPI)**.
    * *Rationale:* Rapid development in Quarkus using MicroProfile OpenAPI annotations.

### Frontend Architecture

* **State Management:** **TanStack Query (v5) + Zustand**.
    * *Rationale:* Explicit separation of concerns. React Query handles async server state (inventory), Zustand handles
      synchronous client state (selected seats).
* **Seat Map Rendering:** **SVG**.
    * *Rationale:* Lighter than DOM nodes, easier to style/animate than Canvas. Scalable to ~5000 seats without major
      perf hit.

### Infrastructure & Deployment

* **Local Orchestration:** **Docker Compose**.
    * *Rationale:* Unified environment for all services (Quarkus Reactive, Quarkus Imperative, Next.js, Postgres, Mongo,
      Keycloak).

### Decision Impact Analysis

**Implementation Sequence:**

1. **Project Shell:** Initialize Monorepo with Docker Compose (DBs + Keycloak).
2. **The Bunker (Reservation Service):** Implement Postgres Schema + Pessimistic Locking logic.
3. **Real-Time Layer:** Implement WebSocket server on Reservation Service.
4. **Frontend Core:** Initialize Next.js + SVG Seat Map.
5. **Integration:** Connect Frontend to WebSockets.

**Cross-Component Dependencies:**

* Frontend SVG Map depends on exact WebSocket payload structure from Reservation Service.
* Frontend SVG Map depends on exact WebSocket payload structure from Reservation Service.
* Reservation Service depends on migrations (Flyway) running before app startup.

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:**
5 areas (Execution Model, Naming, Structure, Error Handling, Communication).

### Naming Patterns

**Database Naming Conventions:**

* **Tables:** `snake_case`, plural (e.g., `reservations`, `movies`).
* **Columns:** `snake_case` (e.g., `reservation_id`, `created_at`).
* **Indexes:** `idx_{table}_{column}` (e.g., `idx_reservations_email`).

**API Naming Conventions:**

* **Endpoints:** `kebab-case`, plural resources (e.g., `/api/v1/movie-showtimes`).
* **JSON Fields:** `camelCase` (e.g., `reservationId`, `showtimeId`).
* **Headers:** `X-Custom-Header` (Kebab-Case).

**Code Naming Conventions:**

* **Java:** `PascalCase` for Classes (`ReservationService`), `camelCase` for methods/variables (`createReservation`).
* **TypeScript/React:** `PascalCase` for Components (`SeatMap`), `camelCase` for functions/hooks (`useSeatMap`).

### Structure Patterns

**Project Organization (Monorepo):**

* `/services/reservation-service`: Quarkus Reactive (The Bunker).
* `/services/admin-service`: Quarkus Imperative (The Watchtower).
* `/frontend`: Next.js App (The Experience).
* `/shared`: Proto files, API types, Shared constants.

**File Structure Patterns:**

* **Java:** Standard Maven (`src/main/java`, `src/test/java`). Co-locate related features where possible.
* **React:** Feature-based folder structure (`src/features/booking/components/SeatMap.tsx`).

### Format Patterns

**API Response Formats:**

* **Success:** HTTP 200/201 + JSON Body.
* **Error:** HTTP 4xx/5xx + JSON Body:
  ```json
  {
    "code": "SEAT_TAKEN",
    "message": "Seat F5 is no longer available.",
    "traceId": "abc-123"
  }
  ```

**Data Exchange Formats:**

* **Dates:** ISO-8601 Strings (`2026-02-15T12:00:00Z`) everywhere. No timestamps.
* **Money:** `BigDecimal` in Java, String in JSON (`"12.50"`), handled carefully in JS.

### Communication Patterns

**Event System Patterns:**

* **Internal:** Synchronous REST for critical paths (Booking).
* **External/Async:** RabbitMQ/Kafka for non-critical side effects (Email).
* **Naming:** `DomainEvent` (e.g., `ReservationCreated`, `PaymentFailed`).

**State Management Patterns:**

* **Server State (React Query):** Stale-while-revalidate default.
* **Client State (Zustand):** Minimal, synchronous UI state only.

### Process Patterns

**Error Handling Patterns:**

* **Backend:** Global `ExceptionMapper` in Quarkus to transform Exceptions -> Standard Error JSON.
* **Frontend:** Logic in separate "Service" layers, UI components handle display via Toast/Alerts.

### Enforcement Guidelines

**All AI Agents MUST:**

1. **Respect the "Reactive Law":** NEVER introduce blocking code in `reservation-service`.
2. **Follow the Folder Structure:** Do not create top-level folders outside the defined monorepo structure.
3. **Strictly Type API Responses:** No `any` in TypeScript; use generated types from OpenAPI where possible.

**Pattern Examples:**

* **Good (Reactive):**
  ```java
  public Uni<Response> create() {
      return Reservation.persist(res).map(r -> Response.ok(r).build());
  }
  ```
* **Bad (Blocking - FORBIDDEN):**
  ```java
  public Response create() {
      repository.save(res); // Blocking!
      return Response.ok().build();
  }
  ```

## Project Structure & Boundaries

### Complete Project Directory Structure

```
movie-reservation-system/
├── README.md
├── docker-compose.yml              # Orchestrates DBs, Keycloak, and Services
├── .env.example
├── .gitignore
├── services/
│   ├── reservation-service/        # [Reactive] "The Bunker"
│   │   ├── src/main/java/com/atomicbunker/reservation/
│   │   │   ├── domain/             # Entities (Reservation, Seat, Showtime)
│   │   │   │   └── Seat.java       # @Entity @Table(name="seats")
│   │   │   ├── repository/         # PanacheReactiveRepository
│   │   │   ├── service/            # Business Logic (Locking)
│   │   │   ├── resource/           # JAX-RS Reactive Endpoints
│   │   │   └── websocket/          # SeatMapSocket.java
│   │   ├── src/main/resources/
│   │   │   ├── db/migration/       # Flyway SQL (Creates tables)
│   │   │   └── application.properties
│   │   └── pom.xml
│   └── admin-service/              # [Imperative] "The Watchtower"
│       ├── src/main/java/com/atomicbunker/admin/
│       │   ├── dashboard/          # Aggregation logic
│       │   └── reporting/          # Excel/PDF exports
│       └── pom.xml
├── frontend/                       # [Next.js] "The Experience"
│   ├── src/
│   │   ├── app/
│   │   │   ├── layout.tsx          # Root Layout (Providers)
│   │   │   ├── page.tsx            # Landing Page
│   │   │   └── book/[id]/page.tsx  # Booking Page (Seat Map)
│   │   ├── features/
│   │   │   ├── seatmap/            # SVG Logic + Zustand Store
│   │   │   ├── booking/            # Checkout Form
│   │   │   └── auth/               # Keycloak Integration
│   │   ├── lib/
│   │   │   ├── socket.ts           # WebSocket Client
│   │   │   └── api.ts              # Fetch Wrapper
│   │   └── components/ui/          # Shadcn/Tailwind UI
│   ├── public/
│   ├── next.config.js
│   ├── tailwind.config.ts
│   └── package.json
└── shared/                         # Shared Types/Contracts
    └── api-types/                  # TypeScript Interfaces matching Java DTOs
```

### Architectural Boundaries

**API Boundaries:**

* **Public Edge:** Next.js Server Actions / API Routes -> Calls Internal Microservices.
* **Service-to-Service:** Admin Service calls Reservation Service via REST for inventory checks.

**Component Boundaries:**

* **Seat Map:** Strictly Isolated. Manages its own socket connection. Updates global `useBookingStore`.
* **Checkout:** Dependent on `useBookingStore`. Cannot proceed without valid `reservationId`.

**Data Boundaries:**

* **Reservation DB (Postgres):** OWNED by Reservation Service. Admin Service MUST NOT touch tables directly; must use
  API.
* **Audit DB (Mongo):** Shared write access (via services), Read access via Admin Service.

### Requirements to Structure Mapping

**Feature/Epic Mapping:**

* **Real-Time Seat Map:** `frontend/src/features/seatmap` <-> `services/reservation-service/.../websocket`
* **Atomic Booking:** `services/reservation-service/.../service/BookingService.java` (Transactional Lock)
* **Admin Dashboard:** `services/admin-service/.../dashboard` <-> `frontend/src/app/admin`

### Integration Points

**Internal Communication:**

* **Frontend -> Reservation:** WebSocket (wss://) for Map, REST (https://) for Booking.
* **Frontend -> Keycloak:** OIDC Redirects.

**Data Flow:**

1. User clicks Seat -> Frontend Socket -> Reservation Service -> DB Lock.
   **Data Flow:**
1. User clicks Seat -> Frontend Socket -> Reservation Service -> DB Lock.
2. DB Success -> Reservation Service -> Broadcast "Held" -> All Frontends update SVG.

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
High. The "Hybrid" model is complex but necessary. Next.js + Quarkus is a standard high-performance stack.

* *Conflict Resolution:* Reactive vs Imperative code is separated by service boundaries (Reservation vs Admin).

**Pattern Consistency:**
The "Reactive Law" explicitly prevents blocking code in the Reservation Service, ensuring the "Atomic Bunker" premise
holds.

### Requirements Coverage Validation ✅

**Epic/Feature Coverage:**

* **Real-Time Seat Map:** Fully supported by `seatmap` feature (Next.js) + WebSocket (Quarkus).
* **High Concurrency Booking:** Supported by `SELECT FOR UPDATE NOWAIT` (Postgres).

**Non-Functional Requirements Coverage:**

* **Performance:** Native compilation option + Reactive IO.
* **Scalability:** Stateless services (mostly) allow horizontal scaling; DB is the bottleneck (handled by Pessimistic
  Locking to fail fast).

### Implementation Readiness Validation ✅

**Decision Completeness:**
Versions Locked: Quarkus 3.31+, JDK 25, Next.js 15.

**Structure Completeness:**
Monorepo structure defined to file level for key components (Socket, Entities).

### Architecture Completeness Checklist

**✅ Requirements Analysis**

- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed

**✅ Architectural Decisions**

- [x] Critical decisions documented with versions
- [x] Technology stack fully specified (Quarkus 3.31+, JDK 25)

**✅ Project Structure**

- [x] Complete directory structure defined
- [x] Component boundaries established

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** High

**First Implementation Priority:**
Initialize the Monorepo with Docker Compose (Postgres, Mongo, Keycloak) and the Quarkus/Next.js skeletons.
