<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:667eea,100:764ba2&height=200&section=header&text=Enterprise%20HRM%20System&fontSize=48&fontColor=ffffff&fontAlignY=38&desc=Production-Grade%20Spring%20Boot%203.x%20REST%20API&descAlignY=58&descColor=e0e0ff" width="100%"/>

<br/>

[![Java](https://img.shields.io/badge/Java-21-FF6B35?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

<br/>

[![GitHub stars](https://img.shields.io/github/stars/utkarsh-raj32/Enterprise?style=social)](https://github.com/utkarsh-raj32/Enterprise/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/utkarsh-raj32/Enterprise?style=social)](https://github.com/utkarsh-raj32/Enterprise/network/members)
[![GitHub issues](https://img.shields.io/github/issues/utkarsh-raj32/Enterprise?style=social)](https://github.com/utkarsh-raj32/Enterprise/issues)

<br/>

> 🚀 A **full-stack enterprise-grade Human Resource Management REST API** built with Java 21 and Spring Boot 3.x.
> Covers **42 REST endpoints** across 5 business modules with JWT authentication, RBAC, Docker containerization, and production-ready patterns.

<br/>

[🔍 Explore API Docs](#-api-endpoints) · [⚡ Quick Start](#-quick-start) · [🐳 Docker Deploy](#-docker-deployment) · [🧪 Run Tests](#-testing) · [📬 Postman Collection](#-postman-collection)

</div>

---

## 📌 Table of Contents

- [✨ Features](#-features)
- [🏗 Architecture](#-architecture)
- [🛠 Tech Stack](#-tech-stack)
- [📐 Project Structure](#-project-structure)
- [🗄 Database Schema](#-database-schema)
- [🔗 API Endpoints (42+)](#-api-endpoints-42)
- [⚡ Quick Start](#-quick-start)
- [🐳 Docker Deployment](#-docker-deployment)
- [🧪 Testing](#-testing)
- [📬 Postman Collection](#-postman-collection)
- [🔐 Security](#-security)
- [🎯 Design Patterns](#-design-patterns)

---

## ✨ Features

<table>
<tr>
<td width="50%">

### 🔐 Authentication & Security
- ✅ JWT Access Token (15 min TTL)
- ✅ Server-side Refresh Token (7 days)
- ✅ BCrypt Password Hashing (strength 12)
- ✅ Role-Based Access Control (RBAC)
- ✅ Stateless Session Architecture
- ✅ Spring Security Filter Chain

### 👤 Employee Management
- ✅ Full CRUD with **Soft Delete**
- ✅ Pagination, Sorting, Multi-field Search
- ✅ Employee Code Pattern Validation
- ✅ Department Mapping (ManyToOne)
- ✅ Unique Email & Code Enforcement

### 🏢 Department Management
- ✅ Full CRUD with Soft Delete
- ✅ Employee Count (Computed Field)
- ✅ Paginated Employee Listing
- ✅ Business Rule: Block Delete if has Active Employees

</td>
<td width="50%">

### 🌴 Leave Management
- ✅ Configurable Leave Types (Paid/Unpaid)
- ✅ Per-Employee Per-Year Balance Tracking
- ✅ **Overlap Detection** (prevents double booking)
- ✅ **Atomic Approval** (balance + status in 1 transaction)
- ✅ Balance Restoration on Cancellation
- ✅ Leave State Machine (PENDING → APPROVED/REJECTED/CANCELLED)

### ⏰ Attendance Tracking
- ✅ Daily Check-In / Check-Out
- ✅ **Automatic Work Hours Calculation**
- ✅ Late Detection (after 9:30 AM)
- ✅ Half-Day Detection (< 4 hours)
- ✅ Monthly Reports & Summary Dashboard

### 💰 Salary & Payroll
- ✅ Salary Structure with Revision History
- ✅ Auto Net Salary Calculation (@PrePersist)
- ✅ Payslip Generation with Attendance Integration
- ✅ Complete Payroll History per Employee

</td>
</tr>
</table>

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                                   │
│              Postman  /  Swagger UI  /  Frontend App                  │
└────────────────────────────┬─────────────────────────────────────────┘
                             │  HTTP + Bearer JWT
┌────────────────────────────▼─────────────────────────────────────────┐
│                  JWT AUTHENTICATION FILTER                            │
│         Extracts → Validates → Sets SecurityContextHolder            │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────────────────┐
│                    CONTROLLER LAYER  (@RestController)                │
│   AuthController  │  EmployeeController  │  DepartmentController    │
│   LeaveController │  AttendanceController │  SalaryController        │
│                                                                       │
│   ➤ Input Validation (@Valid)   ➤ RBAC (@PreAuthorize)              │
│   ➤ HTTP Mapping                ➤ ResponseEntity Building            │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────────────────┐
│                    SERVICE LAYER  (@Service)                          │
│   AuthServiceImpl  │  EmployeeServiceImpl  │  LeaveServiceImpl       │
│   DeptServiceImpl  │  AttendanceServiceImpl│  SalaryServiceImpl      │
│                                                                       │
│   ➤ Business Logic       ➤ @Transactional Management                │
│   ➤ Entity ↔ DTO Mapping ➤ Exception Throwing                       │
└────────────────────────────┬─────────────────────────────────────────┘
                             │
┌────────────────────────────▼─────────────────────────────────────────┐
│                  REPOSITORY LAYER  (Spring Data JPA)                  │
│   JpaRepository  │  Custom @Query (JPQL)  │  Pageable               │
│   JOIN FETCH (N+1 Prevention)  │  @EntityGraph                       │
└────────────────────────────┬─────────────────────────────────────────┘
                             │  Hibernate ORM
┌────────────────────────────▼─────────────────────────────────────────┐
│                        MySQL 8 DATABASE                               │
│         10 Tables  │  Foreign Keys  │  Indexes  │  Constraints       │
└──────────────────────────────────────────────────────────────────────┘

  Cross-Cutting:  GlobalExceptionHandler │ BaseEntity (Auditing) │ SLF4J Logging
```

### Architecture Principles

| Principle | Implementation |
|---|---|
| **Layered Architecture** | Controller → Service → Repository → DB |
| **Dependency Inversion** | All controllers depend on service *interfaces* |
| **DTO Pattern** | Entities never exposed to API clients |
| **Soft Delete** | Status flags preserve historical data |
| **ACID Transactions** | Approve leave = update status + deduct balance atomically |
| **N+1 Prevention** | JOIN FETCH in all list queries |
| **Uniform Response** | `ApiResponse<T>` wraps every response |
| **Stateless Auth** | JWT carries identity; no server sessions |

---

## 🛠 Tech Stack

<div align="center">

| Layer | Technology |
|---|---|
| **Language** | Java 21 (LTS) — Records, Pattern Matching, Virtual Threads-ready |
| **Framework** | Spring Boot 3.2.5 — Spring MVC, Data JPA, Security |
| **ORM** | Hibernate 6.x via Spring Data JPA |
| **Database** | MySQL 8.0 — InnoDB, UTF-8mb4 |
| **Auth** | Spring Security 6 + JJWT 0.12.5 (HS256 + BCrypt) |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Build** | Apache Maven 3.9 |
| **Boilerplate** | Lombok (Builder, @Slf4j, @RequiredArgsConstructor) |
| **Validation** | Jakarta Bean Validation (Hibernate Validator) |
| **Testing** | JUnit 5 + Mockito + AssertJ (BDD-style) |
| **Container** | Docker (multi-stage build) + Docker Compose |
| **Logging** | SLF4J + Logback |

</div>

---

## 📐 Project Structure

```
enterprise-hrm/
│
├── 📄 pom.xml                          # Maven dependencies
├── 🐳 Dockerfile                       # Multi-stage JDK→JRE build
├── 🐳 docker-compose.yml              # MySQL + App orchestration
├── 📋 README.md
│
├── src/main/java/com/enterprise/hrm/
│   │
│   ├── 🚀 EhrApplication.java          # @SpringBootApplication entry point
│   │
│   ├── common/                         # Shared utilities
│   │   ├── ApiResponse.java            # Generic response wrapper ApiResponse<T>
│   │   ├── PageResponse.java           # Pagination response
│   │   └── BaseEntity.java            # @MappedSuperclass with audit fields
│   │
│   ├── config/                         # Configuration classes
│   │   ├── SecurityConfig.java         # Spring Security filter chain
│   │   └── SwaggerConfig.java          # OpenAPI + JWT bearer config
│   │
│   ├── exception/                      # Exception handling
│   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   │   ├── ResourceNotFoundException   # 404
│   │   ├── BusinessException           # 400 business rule violation
│   │   ├── DuplicateResourceException  # 409 Conflict
│   │   └── UnauthorizedException       # 401
│   │
│   ├── security/                       # JWT security
│   │   ├── JwtService.java             # Token generation & validation
│   │   ├── JwtAuthenticationFilter.java# OncePerRequestFilter
│   │   └── UserDetailsServiceImpl.java # Spring Security integration
│   │
│   ├── auth/                           # 🔐 Authentication module
│   │   ├── controller/AuthController
│   │   ├── dto/                        # LoginRequest, RegisterRequest, AuthResponse
│   │   ├── entity/                     # User, Role, RefreshToken
│   │   ├── repository/
│   │   └── service/                    # AuthService + RefreshTokenService
│   │
│   ├── department/                     # 🏢 Department module
│   ├── employee/                       # 👤 Employee module
│   ├── leave/                          # 🌴 Leave module
│   ├── attendance/                     # ⏰ Attendance module
│   └── salary/                         # 💰 Salary module
│
├── src/main/resources/
│   ├── application.yml                 # Main configuration
│   └── application-docker.yml         # Docker profile overrides
│
├── src/test/java/
│   ├── auth/AuthServiceTest.java       # 3 unit tests
│   ├── employee/EmployeeServiceTest.java # 5 unit tests
│   └── leave/LeaveServiceTest.java    # 5 unit tests
│
└── docs/
    └── Enterprise-HRM-Postman-Collection.json
```

---

## 🗄 Database Schema

```
┌─────────────┐       ┌──────────────┐       ┌──────────────────┐
│    roles    │       │    users     │       │  refresh_tokens  │
├─────────────┤       ├──────────────┤       ├──────────────────┤
│ id (PK)     │◄──────│ role_id (FK) │◄──────│ user_id (FK)     │
│ name        │       │ email (UK)   │       │ token (UK)       │
└─────────────┘       │ password     │       │ expiry_date      │
                      └──────┬───────┘       └──────────────────┘
                             │
                    ┌────────▼────────┐
                    │   employees     │
                    ├─────────────────┤       ┌──────────────────┐
                    │ id (PK)         │       │   departments    │
                    │ emp_code (UK)   │       ├──────────────────┤
                    │ email (UK)      │◄──────│ id (PK)          │
                    │ status (ENUM)   │       │ code (UK)        │
                    │ department_id   │       │ active           │
                    └────────┬────────┘       └──────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
┌─────────▼──────┐  ┌────────▼───────┐  ┌──────▼──────────┐
│ leave_requests │  │  attendances   │  │salary_structures│
├────────────────┤  ├────────────────┤  ├─────────────────┤
│ start_date     │  │ attendance_date│  │ basic_salary    │
│ end_date       │  │ check_in       │  │ hra, allowances │
│ status (ENUM)  │  │ check_out      │  │ net_salary      │
│ approved_by FK │  │ work_hours     │  │ effective_date  │
└────────────────┘  └────────────────┘  └────────┬────────┘
                                                  │
┌──────────────────┐  ┌─────────────────┐  ┌─────▼───────┐
│ leave_balances   │  │   leave_types   │  │  payslips   │
├──────────────────┤  ├─────────────────┤  ├─────────────┤
│ employee_id (FK) │  │ name (UK)       │  │ month, year │
│ leave_type_id FK │  │ max_days_year   │  │ net_salary  │
│ year, used_days  │  │ paid (bool)     │  │ present_days│
│ remaining_days   │  └─────────────────┘  └─────────────┘
└──────────────────┘
```

**10 Tables** · **20+ Foreign Key Constraints** · **12 Unique Indexes**

---

## 🔗 API Endpoints (42+)

<details>
<summary><b>🔐 Authentication (4 endpoints)</b></summary>

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register new user |
| `POST` | `/api/v1/auth/login` | Public | Login → get JWT tokens |
| `POST` | `/api/v1/auth/refresh-token` | Public | Refresh access token |
| `POST` | `/api/v1/auth/logout` | Bearer | Logout, invalidate refresh token |

</details>

<details>
<summary><b>🏢 Departments (6 endpoints)</b></summary>

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/departments` | ADMIN, HR | Create department |
| `GET` | `/api/v1/departments` | All | Get all (filter: `?activeOnly=true`) |
| `GET` | `/api/v1/departments/{id}` | All | Get by ID |
| `PUT` | `/api/v1/departments/{id}` | ADMIN, HR | Update department |
| `DELETE` | `/api/v1/departments/{id}` | ADMIN | Soft delete |
| `GET` | `/api/v1/departments/{id}/employees` | All | Paginated employees |

</details>

<details>
<summary><b>👤 Employees (7 endpoints)</b></summary>

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/employees` | ADMIN, HR | Create employee |
| `GET` | `/api/v1/employees` | ADMIN, HR | Paginated list with sort |
| `GET` | `/api/v1/employees/{id}` | ADMIN, HR | Get by ID |
| `GET` | `/api/v1/employees/code/{empCode}` | ADMIN, HR | Get by employee code |
| `GET` | `/api/v1/employees/search` | ADMIN, HR | Search (query, dept, status) |
| `PUT` | `/api/v1/employees/{id}` | ADMIN, HR | Update employee |
| `DELETE` | `/api/v1/employees/{id}` | ADMIN | Soft delete (→ TERMINATED) |

</details>

<details>
<summary><b>🌴 Leave Management (11 endpoints)</b></summary>

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/leaves/types` | All | Get all leave types |
| `POST` | `/api/v1/leaves/types` | ADMIN | Create leave type |
| `POST` | `/api/v1/leaves/apply` | All | Apply for leave |
| `GET` | `/api/v1/leaves` | ADMIN, HR | All leaves (paginated) |
| `GET` | `/api/v1/leaves/{id}` | All | Get leave by ID |
| `GET` | `/api/v1/leaves/employee/{id}` | All | Employee's leave history |
| `PUT` | `/api/v1/leaves/{id}/approve` | ADMIN, HR | ✅ Approve leave |
| `PUT` | `/api/v1/leaves/{id}/reject` | ADMIN, HR | ❌ Reject leave |
| `PUT` | `/api/v1/leaves/{id}/cancel` | All | 🚫 Cancel leave |
| `GET` | `/api/v1/leaves/balance/{empId}` | All | Get leave balance |
| `POST` | `/api/v1/leaves/balance/initialize/{id}` | ADMIN, HR | Init yearly balance |

</details>

<details>
<summary><b>⏰ Attendance (7 endpoints)</b></summary>

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/attendance/checkin` | All | Employee check-in |
| `PUT` | `/api/v1/attendance/checkout/{id}` | All | Employee check-out |
| `GET` | `/api/v1/attendance/today/{empId}` | All | Today's record |
| `GET` | `/api/v1/attendance/{id}` | ADMIN, HR | Get by ID |
| `GET` | `/api/v1/attendance/employee/{id}` | All | History (paginated) |
| `GET` | `/api/v1/attendance/monthly` | All | Monthly records |
| `GET` | `/api/v1/attendance/summary` | All | Monthly summary |

</details>

<details>
<summary><b>💰 Salary (7 endpoints)</b></summary>

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/salary/structure` | ADMIN | Create salary structure |
| `GET` | `/api/v1/salary/structure/{empId}` | ADMIN | Current structure |
| `GET` | `/api/v1/salary/structure/{empId}/history` | ADMIN | Salary history |
| `PUT` | `/api/v1/salary/structure/{id}` | ADMIN | Update structure |
| `POST` | `/api/v1/salary/payslip/generate` | ADMIN | Generate payslip |
| `GET` | `/api/v1/salary/payslip/{empId}` | ADMIN | All payslips |
| `GET` | `/api/v1/salary/payslip/detail/{id}` | ADMIN | Payslip by ID |

</details>

---

## ⚡ Quick Start

### 🐳 Option 1 — Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/utkarsh-raj32/Enterprise.git
cd Enterprise

# Start everything with one command
docker-compose up -d --build

# View logs
docker-compose logs -f hrm-app
```

| Service | URL |
|---|---|
| 🌐 Swagger UI | http://localhost:8080/swagger-ui.html |
| ❤️ Health Check | http://localhost:8080/actuator/health |
| 🗄 MySQL | localhost:3307 |

---

### ☕ Option 2 — Local Setup

**Prerequisites:** Java 21+, Maven 3.9+, MySQL 8

```bash
# 1. Create the database
mysql -u root -p -e "
  CREATE DATABASE enterprise_hrm CHARACTER SET utf8mb4;
  CREATE USER 'hrm_user'@'localhost' IDENTIFIED BY 'hrm_password';
  GRANT ALL PRIVILEGES ON enterprise_hrm.* TO 'hrm_user'@'localhost';
  FLUSH PRIVILEGES;
"

# 2. Run the application (tables auto-created by Hibernate)
mvn spring-boot:run
```

### 🔑 First Steps After Starting

```bash
# Step 1 — Register Admin
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Admin","lastName":"User","email":"admin@hrm.com","password":"Admin@123","role":"ADMIN"}'

# Step 2 — Login & get token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hrm.com","password":"Admin@123"}'

# Step 3 — Use token for protected endpoints
curl -X GET http://localhost:8080/api/v1/employees \
  -H "Authorization: Bearer <your_access_token>"
```

---

## 🐳 Docker Deployment

```yaml
# docker-compose.yml creates:
#  ✅ MySQL 8 with named volume (data persists)
#  ✅ Spring Boot app (depends on healthy MySQL)
#  ✅ Custom bridge network for container communication
#  ✅ Health checks for both services
#  ✅ Resource limits (768MB RAM, 1 CPU)
```

```bash
docker-compose up -d --build    # Start
docker-compose logs -f hrm-app  # Logs
docker-compose down             # Stop
docker-compose down -v          # Stop + wipe database
```

**Environment variables** (create `.env` file):
```env
MYSQL_ROOT_PASSWORD=strong_root_password
MYSQL_USER=hrm_user
MYSQL_PASSWORD=strong_password
JWT_SECRET=your_64_character_hex_secret_here
```

---

## 🧪 Testing

```bash
# Run all unit tests
mvn test

# Run specific module tests
mvn test -Dtest=EmployeeServiceTest
mvn test -Dtest=LeaveServiceTest
mvn test -Dtest=AuthServiceTest

# Run with coverage report
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

### Test Coverage

| Test Class | Tests | Coverage |
|---|---|---|
| `AuthServiceTest` | 3 tests | Register, Duplicate Email, Login |
| `EmployeeServiceTest` | 5 tests | Create, Duplicate, Dept Not Found, Get, Soft Delete |
| `LeaveServiceTest` | 5 tests | Apply, Date Validation, Balance Check, Overlap, Not Found |

**Framework:** JUnit 5 + Mockito BDD-style (`given/when/then`) + AssertJ fluent assertions

---

## 📬 Postman Collection

Import `docs/Enterprise-HRM-Postman-Collection.json` into Postman.

**Auto-Token Feature:** The Login request automatically saves the JWT to `{{access_token}}` — all other requests use it instantly.

**Collection Variables:**

| Variable | Value | Description |
|---|---|---|
| `base_url` | `http://localhost:8080` | API base URL |
| `access_token` | *auto-set on login* | JWT Bearer token |
| `refresh_token` | *auto-set on login* | Refresh token |
| `employee_id` | *auto-set on create* | Last created employee |
| `department_id` | *auto-set on create* | Last created department |

---

## 🔐 Security

### JWT Token Flow

```
Client Login Request
        │
        ▼
AuthenticationManager.authenticate()
        │
        ▼
BCryptPasswordEncoder.matches()  ──► Invalid → 401 Unauthorized
        │ Valid
        ▼
JwtService.generateToken()  ──►  Access Token (15 min, HS256)
        +
RefreshTokenService.create()  ►  Refresh Token (7 days, DB-stored)
        │
        ▼
AuthResponse { accessToken, refreshToken, tokenType: "Bearer" }
```

### Role Permissions Matrix

| Endpoint Group | EMPLOYEE | HR | ADMIN |
|---|---|---|---|
| Auth (register/login) | ✅ | ✅ | ✅ |
| View Employees | ❌ | ✅ | ✅ |
| Create/Edit Employees | ❌ | ✅ | ✅ |
| Delete Employees | ❌ | ❌ | ✅ |
| Apply Leave | ✅ | ✅ | ✅ |
| Approve/Reject Leave | ❌ | ✅ | ✅ |
| View Attendance | ✅ (own) | ✅ | ✅ |
| Salary & Payslips | ❌ | ❌ | ✅ |

---

## 🎯 Design Patterns

| Pattern | Where Used |
|---|---|
| **Layered Architecture** | Controller → Service → Repository → DB |
| **DTO Pattern** | All request/response objects separate from entities |
| **Repository Pattern** | Spring Data JPA interfaces abstract all DB access |
| **Strategy Pattern** | Service interfaces with swappable implementations |
| **Factory Method** | `ApiResponse.success()` / `ApiResponse.error()` |
| **Proxy Pattern** | Spring AOP wraps `@Transactional`, `@PreAuthorize` |
| **Chain of Responsibility** | Spring Security filter chain |
| **Singleton** | All Spring beans (default scope) |
| **Template Method** | `OncePerRequestFilter.doFilterInternal()` |
| **Observer** | JPA `@PrePersist`, `@PreUpdate` lifecycle hooks |

---

## 📊 Project Stats

<div align="center">

| Metric | Count |
|---|---|
| 📁 Total Files | 91 |
| ☕ Java Source Files | 76 |
| 🔗 REST Endpoints | 42 |
| 🗄 Database Tables | 10 |
| 🧪 Unit Tests | 13 |
| 📦 Maven Dependencies | 18 |
| 📝 Lines of Code | 8,372+ |

</div>

---

## 🤝 Contributing

```bash
# Fork the repository
# Create your feature branch
git checkout -b feature/AmazingFeature

# Commit your changes
git commit -m 'feat: Add AmazingFeature'

# Push to the branch
git push origin feature/AmazingFeature

# Open a Pull Request
```

---

## 📄 License

Distributed under the **Apache License 2.0**. See [`LICENSE`](LICENSE) for more information.

---

<div align="center">

### ⭐ If this project helped you, give it a star!

**Built with ❤️ using Spring Boot 3.x, Java 21, and enterprise-grade patterns**

*Ready for technical interviews · Production-ready architecture · Fully documented*

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:667eea,100:764ba2&height=100&section=footer" width="100%"/>

</div>
