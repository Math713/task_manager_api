# Task Manager API

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-brightgreen?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-green?style=flat-square&logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=flat-square&logo=swagger)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=flat-square&logo=apachemaven)

A RESTful API for task management with JWT-based authentication, role-based access control, and full CRUD operations. Built as a personal learning project to deepen knowledge of Spring Security before applying it to a larger academic project.

---

## Features

- **JWT Authentication** — stateless auth with access token (15min) and refresh token (7 days)
- **Role-based access control** — `USER` and `ADMIN` roles with protected routes
- **Task management** — create, list, update, and delete tasks per authenticated user
- **Admin panel** — list all users and tasks, delete users with ownership guards
- **Custom exception handling** — structured `ApiError` responses for all error scenarios
- **Pagination & sorting** — all list endpoints support page, size, and sort parameters
- **OpenAPI documentation** — fully documented via Swagger UI with Bearer auth support

---

## Getting Started

### Running locally

**Requirements:** Java 21+, Maven 3.8+, PostgreSQL

```bash
git clone https://github.com/Math713/task-manager-api.git
cd task-manager-api
```

Create `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/task_manager
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your_base64_encoded_secret_here
jwt.expiration=900000
jwt.refresh-expiration=604800000
```

Create the database in PostgreSQL:

```sql
CREATE DATABASE task_manager;
```

```bash
mvn spring-boot:run
```

Swagger UI → `http://localhost:8080/swagger-ui.html`

---

## Endpoints

### Auth

| Method | Route | Description | Auth |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | Public |
| `POST` | `/auth/login` | Login and receive tokens | Public |
| `POST` | `/auth/refresh` | Refresh access token | Public |

### Tasks

| Method | Route | Description | Auth |
|---|---|---|---|
| `POST` | `/tasks` | Create a task | User |
| `GET` | `/tasks` | List your tasks (paginated) | User |
| `GET` | `/tasks/{id}` | Get task by id | User |
| `PUT` | `/tasks/{id}` | Update task | User |
| `DELETE` | `/tasks/{id}` | Delete task | User |

### Admin

| Method | Route | Description | Auth |
|---|---|---|---|
| `GET` | `/admin/users` | List all users | Admin |
| `GET` | `/admin/users/{id}` | Get user by id | Admin |
| `DELETE` | `/admin/users/{id}` | Delete user | Admin |
| `GET` | `/admin/tasks` | List all tasks (paginated) | Admin |

---

## Authentication flow

```
POST /auth/register  →  { accessToken, refreshToken }
POST /auth/login     →  { accessToken, refreshToken }

GET /tasks  →  Authorization: Bearer {accessToken}

POST /auth/refresh  →  { accessToken, refreshToken }  (when accessToken expires)
```

To authenticate in Swagger UI, click **Authorize** and enter your `accessToken`. The header is added automatically.

---

## Project structure

```
src/main/java/com/matheus/task_manager_api/
├── config/         → SecurityConfig, SwaggerConfig
├── controller/     → AuthController, TaskController, AdminController
├── dto/            → request and response records
├── entity/         → User, Task
├── enums/          → Role, TaskStatus, TaskPriority
├── exception/      → GlobalHandlerException, ApiError, custom exceptions
├── mapper/         → TaskMapper, UserMapper
├── repository/     → UserRepository, TaskRepository
├── security/       → JwtService, JwtFilter, UserDetailsServiceImpl, SecurityUtils
└── service/        → AuthService, TaskService, AdminService
```

---

## Error responses

All errors follow a consistent structure:

```json
{
  "timestamp": "2026-04-15T18:10:02.470502",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "User with id 99 not found",
  "path": "/admin/users/99",
  "fields": null
}
```

Validation errors include a `fields` map with per-field messages.

---

## Tech stack

Java 21 · Spring Boot 3 · Spring Security · JWT (JJWT 0.12.3) · Spring Data JPA · PostgreSQL · Hibernate · Lombok · SpringDoc OpenAPI · Maven

---

## Roadmap

- [ ] Unit and integration tests
- [ ] Docker + Docker Compose
- [ ] Deploy
