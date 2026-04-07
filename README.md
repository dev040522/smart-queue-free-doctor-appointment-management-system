# Smart Queue

Smart Queue is a doctor appointment and queue management project with:

- a Spring Boot backend API
- a React + Vite frontend
- an H2 database enabled by default for quick local setup

## Run Locally

Open two terminals.

### 1. Start the backend

From the project root:

```bash
sh mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080/api
```

If you want another port:

```bash
SERVER_PORT=8081 sh mvnw spring-boot:run
```

### 2. Start the frontend

In a second terminal:

```bash
cd frontend
npm run dev
```

If the backend is on port `8081`, run:

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8081/api npm run dev
```

The frontend usually runs on:

```text
http://localhost:5173
```

## Helpful URLs

- Frontend home: `http://localhost:5173`
- Admin database page: `http://localhost:5173/admin/database`
- Doctors API: `http://localhost:8080/api/doctors`
- Queue API: `http://localhost:8080/api/appointments/queue`
- H2 console: `http://localhost:8080/api/h2-console/`

## H2 Database Login

Use these values in the H2 console:

- JDBC URL: `jdbc:h2:file:./data/smart-queue`
- User Name: `sa`
- Password: leave blank

## Root Scripts

You can also run these from the project root:

```bash
npm run frontend:dev
npm run frontend:build
npm run frontend:lint
npm run backend:run
npm run backend:test
```

## Current Runtime Features

- register and login connected to backend
- patient booking with specialist -> doctor -> time slot flow
- live queue status
- patient appointment history
- doctor dashboard with live queue overview
- doctor queue manager with status updates
- admin database center for users, doctors, and appointments
# smart-queue-free-doctor-appointment-management-system
