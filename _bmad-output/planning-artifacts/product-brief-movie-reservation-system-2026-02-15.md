---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments: ['brainstorming-session-2026-02-15.md', 'README.md']
date: 2026-02-15
author: thomas
---

# Product Brief: movie-reservation-system

## Executive Summary

The Movie Reservation System is a high-reliability booking platform designed to withstand extreme traffic spikes without compromising data integrity. Unlike traditional systems that prioritize availability at the cost of consistency (leading to double-bookings), this system acts as an "Atomic Bunker" for reservation data. Its core promise is absolute certainty: if a user receives a confirmation, the seat is guaranteed theirs, even if it means shedding excess load during peak demand.

---

## Core Vision

### Problem Statement

During high-demand events (e.g., blockbuster releases), traditional reservation systems often fail catastrophically in two ways: total system crashes due to database overload, or worse, "Double Booking" logic errors where multiple users are sold the same seat due to race conditions.

### Problem Impact

For the user, a "Double Booking" is the ultimate failure—arriving at a theater only to find their seat taken is a reputation-destroying experience. For the business, the cost of refunds, support, and loss of trust outweighs the marginal revenue of overselling.

### Why Existing Solutions Fall Short

Most current solutions attempt to handle load by adding complex caching layers (Redis) or distributed locks. Under extreme pressure, these layers often desync from the primary database, creating a "split-brain" scenario where the cache says "Available" but the database says "Booked".

### Proposed Solution

We propose a "Consistency First" architecture that relies on the Database as the single source of truth for the critical reservation action. By utilizing atomic SQL state transitions and optimistic locking, we eliminate the need for fragile distributed locks.

### Key Differentiators

- **The Atomic Bunker:** A reservation mechanism that creates a hard guarantee against double-booking at the database row level.
- **Fail-Fast Integrity:** A deliberate design choice to show a "System Busy" message rather than risk a false confirmation.
- **Circuit Breaker Architecture:** Intelligent load shedding that protects the core booking engine from total collapse.

---

## Target Users

### Primary Users: "The Fan" (Alex)
*   **Profile:** High-intent moviegoer, motivated by FOMO and event participation.
*   **The Struggle:** Anxious about securing seats for high-demand premieres. Frustrated by "spinning wheels of death" and uncertain confirmation states.
*   **The Win:** Absolute certainty. When they get a ticket, they *know* it's theirs. They value a "Sold Out" message over a false "Confirmed" message because it saves them from public embarrassment.

### Secondary Users: "The Guardian" (Theater Manager)
*   **Profile:** Operational staff managing the physical venue.
*   **The Struggle:** Dealing with angry customers who have double-booked tickets (reputation damage & conflict resolution). They are the ones who have to say "I'm sorry, your seat doesn't exist."
*   **The Win:** A boring night. No double-bookings mean no angry mobs in the lobby.

### User Journey (The "High Noon" Scenario)
1.  **The Drop:** Tickets for *Avengers 35* go on sale at 12:00 PM. Traffic spikes 1000x.
2.  **The Attempt:** Alex selects Seat F5. Thousands of others are clicking simultaneously.
3.  **The Filter:**
    *   *System Overload:* Instead of crashing, the system sheds load. Alex might see a "Queue" or "Busy" screen, but the system stays up.
    *   *Race Condition:* Someone clicks F5 milliseconds before Alex. The "Atomic Bunker" rejects Alex's request instantly.
4.  **The Truth:** Alex sees "Seat Taken" immediately (Fail Fast), allowing them to pick another seat *before* it's too late.
5.  **The Guarantee:** Alex picks G5. The lock holds. Payment processes. The QR code arrives. Alex walks into the theater knowing that seat is 100% theirs.

---

## Success Metrics

### User Success: "The Confidence Metric"
For Alex, success is the elimination of uncertainty.
*   **The "Held" Confidence:** When "Select" is clicked, visual confirmation of the lock must appear in **< 200ms**.
*   **The "Payment" Assurance:** Zero "Ghost Charges" (money taken, no ticket). 
*   **Delivery Speed:** Ticket confirmation email/QR code received within **5 seconds** of payment success.

### Business Objectives: "Yield & Integrity"
For the Theater Manager, success is maximizing yield while minimizing support chaos.
*   **Inventory Liquidity:** Maximize time seats are "Available" or "Sold". Minimize time in "Held" (limbo). A seat held by a bot or indecisive user is a seat not being sold.
*   **Support Cost Minimization:** **0% Double Bookings.** Any double booking wipes out the profit margin of the screening due to refund/support costs.

### Key Performance Indicators (KPIs)
*   **Visual Lock Latency:** 95th percentile < 200ms.
*   **Double Booking Rate:** Strictly 0.0%.
*   **Ghost Charge Rate:** Strictly 0.0%.
*   **Session Churn:** % of "Held" seats that expire without purchase (target: minimize via tighter timeouts).

---

## MVP Scope

### Core Features
*   **Real-time Atomic Seat Map:** Direct-from-database seat availability visualization. No caching layers to ensure 100% accuracy of "Available" state.
*   **The "One-Shot" Reservation:** Atomic database transaction that locks the seat and creates the reservation in a single query.
*   **Guest Checkout:** Frictionless booking flow requiring only email/phone. No account creation required for V1 to maximize throughput.
*   **Payment Simulation:** Robust handling of payment states (Success, Failure, Timeout) to verify the "Gatekeeper" logic.
*   **Ticket Delivery:** Asynchronous generation of QR code tickets sent via SMTP (simulated with Mailhog).

### Out of Scope for MVP
*   **User Authentication:** No login/signup required. Account linking will be a V2 feature via retroactive email matching.
*   **Admin Dashboard:** System management will be done via SQL/CLI tools initially.
*   **Multiple Theaters:** MVP is limited to a single cinema/screen context to validate the core booking engine.
*   **Complex Pricing:** Flat pricing for all seats in V1.

### MVP Success Criteria
*   **Zero Double Bookings** under simulated high concurrency (load testing).
*   **< 200ms** visual confirmation of seat lock.
*   **100%** of successful payments result in a ticket email.
