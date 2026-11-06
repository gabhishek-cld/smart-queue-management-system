# smart-queue-management-system
A full-stack queue management platform developed using Spring Boot, React, and Vite, designed to streamline customer flow and service operations. Enables customers to generate digital tokens, allows staff to manage and call queues efficiently, and provides a real-time display board for live queue updates.
## Tech Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Auth      | JWT |
| Database  | PostgreSQL 14+|
| Real-time | Spring WebSocket (STOMP)  |
| Docs      | Swagger / SpringDoc OpenAPI |
| Testing   | JUnit 5, Mockito |
| Frontend  | React 18, Vite 8, Tailwind CSS, Axios, Recharts |

---

## Features

**Customer Kiosk** (public, no login)
- Select a service type and get a unique token, e.g. `V-001`, `C-042`
- Optional name for virtual check-in
- Live ETA based on queue depth × rolling average service time

**Staff Counter** (STAFF or ADMIN role)
- Call Next : highest-priority ticket first (VIP before normal, then FIFO)
- Complete / No Show : updates or skips the rolling service-time average
- Transfer : moves the customer to a different service queue without re-queuing

**Display Board** (public)
- Live WebSocket feed : updates the moment a counter calls a ticket
- Counter status grid, recent calls history

**Admin Dashboard** (ADMIN role)
- Today's stats: total, completed, no-shows, avg wait, avg service time
- Bar chart of tickets by hour (busiest hour)
- Per-service-type breakdown
- Add / remove counters, change status (Active / Paused / Offline) in real time

---

## Core Logic

**Priority Queue**
`PriorityBlockingQueue<Ticket>` sorted by `priorityWeight DESC` then `createdAt ASC`. VIP tickets (weight 10) always jump ahead of normal ones (weight 2).

**Pessimistic Locking**
`@Lock(PESSIMISTIC_WRITE)` on the "find next ticket" query. When two counters press Call Next simultaneously, the database locks the row so only one gets it.

**Dynamic ETA**
```
ETA = position_in_queue × avgServiceMinutes
```
`avgServiceMinutes` updates after every completed ticket using an exponential moving average (α = 0.3):
```
new_avg = 0.7 × old_avg + 0.3 × actual_minutes
```

**Auth — httpOnly cookie**
On login the backend writes the JWT into an `httpOnly` cookie. JavaScript cannot read it. On every request the browser sends it automatically. On page load the frontend calls `GET /api/auth/me` to restore the session.

---

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+ running locally

---

## Setup

### 1. Create the database

```sql
CREATE DATABASE smart_qms;
```

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```
 
> Edit `src/main/resources/application.properties.fake` and rename it to `application.properties`.

On first run `DataInitializer` seeds users, service types, and counters automatically.

- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

- App: http://localhost:5173

---

## Default accounts

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | ADMIN |
| staff1   | staff123 | STAFF |
| staff2   | staff123 | STAFF |

---

## Pages

| URL       | Access      | Description                          |
|-----------|-------------|--------------------------------------|
| `/`       | Anyone      | Customer kiosk                       |
| `/display`| Anyone      | Live display board (WebSocket)       |
| `/login`  | Anyone      | Staff / admin sign in                |
| `/staff`  | STAFF/ADMIN | Counter interface                    |
| `/admin`  | ADMIN       | Analytics + counter management       |

---

## API Reference

| Method | Path                            | Access      | Description                      |
|--------|---------------------------------|-------------|----------------------------------|
| POST   | /api/auth/login                 | Public      | Login, sets JWT cookie           |
| GET    | /api/auth/me                    | Public      | Returns session user from cookie |
| POST   | /api/auth/logout                | Public      | Clears JWT cookie                |
| GET    | /api/services                   | Public      | List service types               |
| POST   | /api/tickets                    | Public      | Generate a ticket                |
| GET    | /api/tickets/{id}               | Public      | Ticket status + ETA              |
| GET    | /api/counters                   | STAFF/ADMIN | List counters                    |
| POST   | /api/counters/{id}/next         | STAFF/ADMIN | Call next ticket                 |
| PUT    | /api/counters/{id}/complete     | STAFF/ADMIN | Mark completed                   |
| PUT    | /api/counters/{id}/no-show      | STAFF/ADMIN | Mark no-show                     |
| PUT    | /api/counters/{id}/transfer     | STAFF/ADMIN | Transfer to another service      |
| GET    | /api/admin/analytics            | ADMIN       | Today's analytics                |
| POST   | /api/admin/counters             | ADMIN       | Add counter                      |
| PUT    | /api/admin/counters/{id}/status | ADMIN       | Change counter status            |
| DELETE | /api/admin/counters/{id}        | ADMIN       | Remove counter                   |
| WS     | /ws → /topic/board              | Public      | Live display board events        |

---

## Project Structure

```
smart-qms/
├── backend/
│   └── src/main/java/com/qms/
│       ├── config/         SecurityConfig, WebSocketConfig, OpenApiConfig, DataInitializer
│       ├── security/       JwtUtil, JwtFilter, UserDetailsServiceImpl
│       ├── model/
│       │   ├── entity/     Ticket, Counter, ServiceType, AppUser
│       │   ├── enums/      TicketStatus, CounterStatus, UserRole
│       │   └── dto/        Request + Response DTOs
│       ├── repository/     JPA interfaces (TicketRepository has the pessimistic lock query)
│       ├── service/        QueueService, TicketService, CounterService, AnalyticsService, AuthService
│       ├── controller/     AuthController, TicketController, CounterController, AdminController
│       ├── exception/      GlobalExceptionHandler, ResourceNotFoundException, QueueEmptyException
│       └── util/           TokenGenerator
└── frontend/
    └── src/
        ├── api/            axiosConfig.js (withCredentials), index.js (all API calls)
        ├── context/        AuthContext.jsx (session via /auth/me)
        ├── components/     Navbar.jsx, PrivateRoute.jsx
        └── pages/          CustomerKiosk, StaffCounter, AdminDashboard, DisplayBoard, LoginPage
```

---

## Running Tests

```bash
cd backend
mvn test
```
