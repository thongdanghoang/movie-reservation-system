---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
inputDocuments: ['prd.md', 'architecture.md']
---

# movie-reservation-system - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for movie-reservation-system, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Guest can view "Now Playing" movies list.
FR2: Guest can view showtimes for a selected movie.
FR3: Guest can view Real-Time Seat Map (Available/Held/Sold).
FR4: Guest can select an "Available" seat, triggering Atomic Lock.
FR5: System must reject selection if "Held" or "Sold" (Fail-Fast).
FR6: Guest receives visual feedback (< 200ms) of Lock Success/Failure.
FR7: Guest can enter email/phone for Guest Checkout.
FR8: System enforces Hold Timer (e.g., 5 mins).
FR9: System auto-releases seat if timer expires.
FR10: Guest can submit Credit Card payment.
FR11: System generates Reservation ID + QR Code on success.
FR12: System sends Confirmation Email with Ticket.
FR13: Admin can view Dashboard (Sold vs. Held counters).
FR14: Admin can search transactions by ID/Email.
FR15: Admin can trigger manual Email Resend.

### NonFunctional Requirements

NFR1: Performance - Latency: Booking transactions < 500ms (P99).
NFR2: Performance - Throughput: 1,000 Concurrent Users on Seat Map.
NFR3: Performance - Real-Time: Map status updates < 100ms.
NFR4: Security - Data Minimization: No raw credit card storage.
NFR5: Security - Rate Limiting: Max 10 req/sec per IP.
NFR6: Reliability - Immutability: "Sold" state is final/immutable.
NFR7: Reliability - Consistency: ACID compliance for all inventory state changes.
NFR8: Compliance - GDPR/CCPA: "Right to be Forgotten".
NFR9: Compliance - Auditability: Immutable logs for all state transitions.

### Additional Requirements

- Tech Stack: Quarkus 3.31+ (Java 25) (Reactive Reservation, Imperative Admin), Next.js 15 (Frontend).
- Infrastructure: Monorepo structure defined (docker-compose for local dev).
- Database: Postgres (Transactional), Mongo (Audit).
- Authentication: Keycloak (OIDC) via Phantom Token Pattern.
- Concurrency Strategy: Pessimistic Locking (SELECT ... FOR UPDATE NOWAIT).
- Frontend Requirement: Seat Map must be SVG based.
- Frontend State: TanStack Query (Server State) + Zustand (Client State).
- Real-Time: WebSockets required for seat updates.
- API Strategy: Code-First OpenAPI.
- Implementation Constraint: "Reactive Law" - NEVER introduce blocking code in reservation-service.
- Architecture Constraint: Admin Service must use API to access Reservation DB (no direct DB access).

### FR Coverage Map

FR1: Epic 1 - Guest View Movies
FR2: Epic 1 - Guest View Showtimes
FR3: Epic 1 - Real-Time Seat Map
FR4: Epic 2 - Atomic Lock
FR5: Epic 2 - Fail-Fast Rejection
FR6: Epic 2 - Lock Feedback
FR7: Epic 2 - Guest Checkout
FR8: Epic 2 - Hold Timer
FR9: Epic 2 - Auto-Release
FR10: Epic 2 - Payment
FR11: Epic 2 - Reservation ID
FR12: Epic 2 - Email Confirmation
FR13: Epic 3 - Admin Dashboard
FR14: Epic 3 - Transaction Search
FR15: Epic 3 - Email Resend

## Epic List

### Epic 1: The Visual Marketplace (Discovery & Real-Time Availability)

Guests can find movies and trust that the seat map they see is live and accurate (no "ghost" availability).
**FRs covered:** FR1, FR2, FR3

#### Story 1.1: Guest View Now Playing Movies

As a Guest,
I want to see a list of movies currently playing,
So that I can decide what to watch.

**Acceptance Criteria:**

**Given** I am on the landing page
**Then** I see a grid of "Now Playing" movies
**And** Each movie card shows Title, Poster, and Genre
**When** I click a movie card
**Then** I am navigated to the Movie Detail page

#### Story 1.2: Guest View Showtimes

As a Guest,
I want to see available showtimes for a selected movie,
So that I can plan my visit.

**Acceptance Criteria:**

**Given** I am on a Movie Detail page
**Then** I see a list of showtimes for the current date
**When** I view the list
**Then** Showtimes in the past are hidden or visually disabled
**When** I click a showtime
**Then** I am navigated to the Booking Page for that specific showtime

#### Story 1.3: Real-Time Seat Map Visualization

As a Guest,
I want to see the live status of every seat (Available, Held, Sold),
So that I don't try to book a taken seat.

**Acceptance Criteria:**

**Given** I am on the Booking Page
**Then** I see the SVG layout of the theater seats
**And** Seats are color-coded: Green (Available), Orange (Held), Red (Sold)
**When** Another user selects a seat
**Then** My view updates to show that seat as "Held" within 100ms
**And** I can zoom and pan the seat map for better visibility

### Epic 2: The Atomic Transaction (Lock, Pay, Confirm)

Guests can secure a specific seat, complete payment, and receive proof of purchase with zero risk of double-booking.
**FRs covered:** FR4, FR5, FR6, FR7, FR8, FR9, FR10, FR11, FR12

#### Story 2.1: Atomic Seat Lock (The "Hold")

As a Guest,
I want to temporarily hold a seat by clicking it,
So that no one else can take it while I pay.

**Acceptance Criteria:**

**Given** I click an "Available" (Green) seat
**When** The system processes the request (Pessimistic Lock)
**Then** The seat status changes to "Held" (Orange) for all users
**And** I see a timer start (5:00)
**When** Two users click the same seat simultaneously
**Then** Only one succeeds, and the other receives a "Seat Taken" message in < 200ms

#### Story 2.2: Guest Checkout Input

As a Guest,
I want to enter my email and payment details,
So that I can buy the held seat.

**Acceptance Criteria:**

**Given** I have a held seat
**Then** I see the checkout form requesting Email and Phone
**When** I enter an invalid email
**Then** The system prevents submission
**When** The hold timer expires
**Then** The form is disabled and I am redirected/notified that the hold was lost

#### Story 2.3: Payment Processing (Simulated)

As a Guest,
I want to pay for my reservation,
So that my booking is finalized.

**Acceptance Criteria:**

**Given** I submit the checkout form with valid details
**When** I click "Pay Now"
**Then** The system processes the payment (Mock Provider)
**And** On success, the seat status changes to "Sold" (Red)
**And** The hold timer stops

#### Story 2.4: Ticket Fulfillment

As a Guest,
I want to receive my ticket,
So that I have proof of purchase.

**Acceptance Criteria:**

**Given** I have successfully paid
**Then** I am shown a "Booking Confirmed" page
**And** I see a QR Code containing my signed Reservation ID
**And** I receive an email with the transaction details and QR Code within 5 seconds

#### Story 2.5: Auto-Release Expired Holds

As a System,
I want to release seats that haven't been paid for,
So that others can buy them.

**Acceptance Criteria:**

**Given** A reservation remains "Held" for > 5 minutes
**When** The background job/scheduler runs
**Then** The system reverts the status to "Available"
**And** A WebSocket update is broadcast to turn the seat Green for all users

### Epic 3: Operational Oversight (Admin Watchtower)

Admins can monitor the high-concurrency event and resolve booking issues.
**FRs covered:** FR13, FR14, FR15

#### Story 3.1: Admin Real-Time Dashboard

As an Admin,
I want to see live sales counters,
So that I know how the event is performing.

**Acceptance Criteria:**

**Given** I access the /admin dashboard
**Then** I see "Total Seats", "Sold", "Held", and "Available" counters
**And** The counters update in real-time (or near real-time)
**When** I monitor the dashboard during a sale
**Then** I can visually verify if the system is healthy (e.g., no stuck "Held" counts)

#### Story 3.2: Transaction Search

As an Admin,
I want to find a booking by email or ID,
So that I can help a guest who lost their ticket.

**Acceptance Criteria:**

**Given** I am on the Admin Dashboard
**Then** I see a search bar
**When** I enter an email address or Reservation ID
**Then** I see the matching booking details
**And** Details include Status, Seat, Movie, and Timestamp

#### Story 3.3: Manual Email Resend

As an Admin,
I want to resend a confirmation email,
So that a guest receives their missing ticket.

**Acceptance Criteria:**

**Given** I am viewing a specific booking
**Then** I see a "Resend Email" button
**When** I click it
**Then** The system triggers the email notification service again
**And** I receive a success toast message


