# Enterprise Employee & Leave Management System (EHR System)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-green?style=for-the-badge&logo=spring" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Docker-enabled-2496ED?style=for-the-badge&logo=docker" alt="Docker"/>
  <img src="https://img.shields.io/badge/JWT-Auth-purple?style=for-the-badge" alt="JWT"/>
  <img src="https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger" alt="Swagger"/>
</p>

> **Production-grade Spring Boot 3.x REST API** for managing employees, departments, leaves, attendance, and salaries with JWT authentication and Role-Based Access Control (RBAC).

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features)
- [API Endpoints (40+)](#-api-endpoints-40)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [Docker Deployment](#-docker-deployment)
- [Running Tests](#-running-tests)
- [Swagger UI](#-swagger-ui)
- [Project Structure](#-project-structure)

---

## 🎯 Overview

The **Enterprise HRM System** is a full-featured Human Resource Management REST API built with enterprise-grade patterns:

| Feature | Implementation |
|---|---|
| Authentication | JWT Access Token + Server-side Refresh Token |
| Authorization | Spring Security RBAC (ADMIN, HR, EMPLOYEE) |
| Data Access | Spring Data JPA + Hibernate (MySQL) |
| API Docs | Swagger / OpenAPI 3 |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5 + Mockito |
| Error Handling | Global Exception Handler + Custom Exceptions |
| Response Format | Uniform `ApiResponse<T>` wrapper |

---

## 🛠 Tech Stack

```
Java 21               Spring Boot 3.2.5      Spring MVC
Spring Data JPA        Hibernate ORM          MySQL 8
Spring Security        JWT (JJWT 0.12.5)     BCrypt (strength=12)
Lombok                 Swagger/OpenAPI 3      Maven
Docker                 Docker Compose         JUnit 5 + Mockito
SLF4J + Logback        Bean Validation        H2 (test)
```

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT                              │
│              (Postman / Frontend / Mobile)                  │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP Request (Bearer JWT)
┌─────────────────────────▼───────────────────────────────────┐
│              JwtAuthenticationFilter                        │
│    Extracts JWT → Validates → Sets SecurityContext         │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│              @RestController Layer                          │
│  AuthController | EmployeeController | LeaveController     │
│  DepartmentController | AttendanceController | Salary...    │
│                                                             │
│  Responsibilities: Input validation, HTTP mapping,         │
│  ResponseEntity building, @PreAuthorize RBAC               │
└─────────────────────────┬───────────────────────────────────┘
                          │ Calls service methods
┌─────────────────────────▼───────────────────────────────────┐
│              Service Interface + Implementation             │
│  AuthService | EmployeeService | LeaveService | ...        │
│                                                             │
│  Responsibilities: Business logic, validation,             │
│  Entity↔DTO mapping, @Transactional management             │
└─────────────────────────┬───────────────────────────────────┘
                          │ Calls repository methods
┌─────────────────────────▼───────────────────────────────────┐
│              @Repository Layer (Spring Data JPA)            │
│  JpaRepository | Custom @Query | Pageable                  │
│                                                             │
│  Responsibilities: CRUD, custom queries, pagination        │
└─────────────────────────┬───────────────────────────────────┘
                          │ JDBC/Hibernate
┌─────────────────────────▼───────────────────────────────────┐
│                    MySQL 8 Database                         │
│  10 tables | Foreign keys | Indexes | Constraints          │
└─────────────────────────────────────────────────────────────┘
```

**Cross-Cutting Concerns:**
- `GlobalExceptionHandler` → catches all exceptions, returns structured errors
- `BaseEntity` → audit fields on all entities (createdAt, updatedAt)
- `ApiResponse<T>` → uniform response envelope
- SLF4J → structured logging throughout

---

## ✨ Features

### 🔐 Authentication & Security
- User registration with BCrypt password hashing (strength=12)
- JWT login → access token (15 min) + refresh token (7 days, server-side)
- Token refresh without re-login
- Logout invalidates refresh token
- Role-Based Access Control: `ADMIN`, `HR`, `EMPLOYEE`
- Spring Security filter chain with stateless sessions

### 👤 Employee Module
- Full CRUD with soft delete (TERMINATED status)
- Pagination, sorting, multi-field search (name, email, designation, dept, status)
- Department mapping (ManyToOne)
- Employee code format validation (EMP-001)
- Unique email and employee code enforcement

### 🏢 Department Module
- Full CRUD with soft delete
- One-to-Many with employees (bidirectional)
- Employee count computed field
- Paginated employee listing per department
- Business rule: can't delete department with active employees

### 🌴 Leave Module
- Leave type management (Annual, Sick, Maternity, etc.)
- Leave application with automatic working day calculation
- Overlap detection (prevents double booking)
- Leave balance tracking per employee per year
- Approve/Reject with atomic balance deduction (one transaction)
- Balance restoration on approved leave cancellation
- Leave history with pagination

### ⏰ Attendance Module
- Daily check-in / check-out
- Auto work hours calculation
- Late detection (after 9:30 AM = LATE status)
- Half-day detection (< 4 hours = HALF_DAY)
- Monthly attendance report
- Monthly summary (present/late/absent/leave days count)

### 💰 Salary Module
- Salary structure with salary revision support
- Components: Basic + HRA + Allowances - Deductions = Net Salary
- Auto net salary calculation via @PrePersist
- Payslip generation with attendance integration
- Payslip history per employee

---

## 🔗 API Endpoints (40+)

### Authentication
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register new user |
| POST | `/api/v1/auth/login` | Public | Login, get tokens |
| POST | `/api/v1/auth/refresh-token` | Public | Refresh access token |
| POST | `/api/v1/auth/logout` | Authenticated | Logout, invalidate token |

### Departments
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v1/departments` | ADMIN,HR | Create department |
| GET | `/api/v1/departments` | All | Get all departments |
| GET | `/api/v1/departments/{id}` | All | Get by ID |
| PUT | `/api/v1/departments/{id}` | ADMIN,HR | Update |
| DELETE | `/api/v1/departments/{id}` | ADMIN | Soft delete |
| GET | `/api/v1/departments/{id}/employees` | All | Get dept employees (paginated) |

### Employees
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v1/employees` | ADMIN,HR | Create employee |
| GET | `/api/v1/employees?page=0&size=10&sortBy=firstName` | ADMIN,HR | Paginated list |
| GET | `/api/v1/employees/{id}` | ADMIN,HR | Get by ID |
| GET | `/api/v1/employees/code/{empCode}` | ADMIN,HR | Get by code |
| GET | `/api/v1/employees/search?query=john&departmentId=1&status=ACTIVE` | ADMIN,HR | Search |
| PUT | `/api/v1/employees/{id}` | ADMIN,HR | Update |
| DELETE | `/api/v1/employees/{id}` | ADMIN | Soft delete |

### Leave Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/v1/leaves/types` | All | Get leave types |
| POST | `/api/v1/leaves/types` | ADMIN | Create leave type |
| POST | `/api/v1/leaves/apply` | All | Apply for leave |
| GET | `/api/v1/leaves?page=0&size=10` | ADMIN,HR | All leaves |
| GET | `/api/v1/leaves/{id}` | All | Get by ID |
| GET | `/api/v1/leaves/employee/{employeeId}` | All | Employee's leaves |
| PUT | `/api/v1/leaves/{id}/approve` | ADMIN,HR | Approve leave |
| PUT | `/api/v1/leaves/{id}/reject` | ADMIN,HR | Reject leave |
| PUT | `/api/v1/leaves/{id}/cancel` | All | Cancel leave |
| GET | `/api/v1/leaves/balance/{employeeId}?year=2024` | All | Leave balance |
| POST | `/api/v1/leaves/balance/initialize/{employeeId}` | ADMIN,HR | Init balance |

### Attendance
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v1/attendance/checkin` | All | Check in |
| PUT | `/api/v1/attendance/checkout/{id}` | All | Check out |
| GET | `/api/v1/attendance/today/{employeeId}` | All | Today's record |
| GET | `/api/v1/attendance/{id}` | ADMIN,HR | By ID |
| GET | `/api/v1/attendance/employee/{employeeId}` | All | History (paginated) |
| GET | `/api/v1/attendance/monthly?employeeId=1&year=2024&month=7` | All | Monthly |
| GET | `/api/v1/attendance/summary?employeeId=1&year=2024&month=7` | All | Monthly summary |

### Salary
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/v1/salary/structure` | ADMIN | Create salary structure |
| GET | `/api/v1/salary/structure/{employeeId}` | ADMIN | Current structure |
| GET | `/api/v1/salary/structure/{employeeId}/history` | ADMIN | Salary history |
| PUT | `/api/v1/salary/structure/{id}` | ADMIN | Update structure |
| POST | `/api/v1/salary/payslip/generate` | ADMIN | Generate payslip |
| GET | `/api/v1/salary/payslip/{employeeId}` | ADMIN | Employee payslips |
| GET | `/api/v1/salary/payslip/detail/{id}` | ADMIN | Payslip by ID |

---

## 🗄 Database Schema

```
roles                   users                    refresh_tokens
────────────            ────────────             ──────────────
id (PK)                 id (PK)                  id (PK)
name (ENUM)             first_name               token (UNIQUE)
                        last_name                user_id (FK→users)
                        email (UNIQUE)           expiry_date
                        password
                        role_id (FK→roles)
                        enabled

departments             employees
───────────             ─────────
id (PK)                 id (PK)
name                    emp_code (UNIQUE)
code (UNIQUE)           first_name, last_name
description             email (UNIQUE)
manager_name            phone, designation
active                  joining_date
                        status (ENUM)
                        department_id (FK→departments)
                        user_id (FK→users)
                        salary

leave_types             leave_balances           leave_requests
───────────             ──────────────           ──────────────
id (PK)                 id (PK)                  id (PK)
name (UNIQUE)           employee_id (FK)         employee_id (FK)
max_days_per_year       leave_type_id (FK)       leave_type_id (FK)
paid                    year                     start_date, end_date
active                  total_days               number_of_days
                        used_days                reason
                        remaining_days           status (ENUM)
                        [UNIQUE: emp+type+year]  approved_by (FK→users)

attendances             salary_structures        payslips
───────────             ─────────────────        ────────
id (PK)                 id (PK)                  id (PK)
employee_id (FK)        employee_id (FK)         employee_id (FK)
attendance_date         basic_salary             salary_structure_id (FK)
check_in                hra                      month, year
check_out               allowances               basic_salary, hra ...
work_hours              deductions               net_salary
status (ENUM)           net_salary               working_days, present_days
[UNIQUE: emp+date]      effective_date           generated_at
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+
- Docker & Docker Compose (optional)

### Local Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-org/enterprise-hrm.git
cd enterprise-hrm

# 2. Create MySQL database
mysql -u root -p
CREATE DATABASE enterprise_hrm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 3. Configure database credentials
# Edit src/main/resources/application.yml:
#   spring.datasource.username: your_user
#   spring.datasource.password: your_password

# 4. Build and run
mvn clean package -DskipTests
java -jar target/enterprise-hrm-1.0.0.jar

# OR using Maven Spring Boot plugin
mvn spring-boot:run
```

The application starts at: `http://localhost:8080`

---

## 🐳 Docker Deployment

```bash
# Build and start all services
docker-compose up -d --build

# View logs
docker-compose logs -f hrm-app

# Stop everything
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v
```

### Environment Variables
Create a `.env` file in the project root:
```env
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_USER=hrm_user
MYSQL_PASSWORD=your_password
JWT_SECRET=your_64_char_hex_secret_key_here
```

### Service URLs
| Service | URL |
|---|---|
| Application | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Health Check | http://localhost:8080/actuator/health |
| MySQL | localhost:3307 |

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EmployeeServiceTest

# Run with coverage report
mvn test jacoco:report

# Test output
./target/surefire-reports/     # JUnit XML reports
./target/site/jacoco/          # Code coverage HTML report
```

---

## 📖 Swagger UI

1. Start the application
2. Navigate to: `http://localhost:8080/swagger-ui.html`
3. Click **Authorize** (top right)
4. Call `POST /api/v1/auth/login` to get a JWT token
5. Enter the token in the Authorize dialog: `Bearer <your-token>`
6. All endpoints are now accessible

---

## 📁 Project Structure

```
enterprise-hrm/
├── src/main/java/com/enterprise/hrm/
│   ├── EhrApplication.java          # Main class
│   ├── common/                      # Shared utilities
│   │   ├── ApiResponse.java
│   │   ├── PageResponse.java
│   │   └── BaseEntity.java
│   ├── config/                      # Spring configurations
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── exception/                   # Custom exceptions + handler
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── BusinessException.java
│   │   ├── DuplicateResourceException.java
│   │   └── UnauthorizedException.java
│   ├── security/                    # JWT security
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   ├── auth/                        # Authentication module
│   │   ├── controller/AuthController.java
│   │   ├── dto/                     # Request/Response DTOs
│   │   ├── entity/                  # User, Role, RefreshToken
│   │   ├── repository/
│   │   └── service/
│   ├── department/                  # Department module
│   ├── employee/                    # Employee module
│   ├── leave/                       # Leave module
│   ├── attendance/                  # Attendance module
│   └── salary/                      # Salary module
├── src/main/resources/
│   ├── application.yml
│   └── application-docker.yml
├── src/test/java/
│   ├── auth/AuthServiceTest.java
│   ├── employee/EmployeeServiceTest.java
│   └── leave/LeaveServiceTest.java
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
└── pom.xml
```

---

## 📬 Postman Collection

Import the collection from `docs/Enterprise-HRM-Postman-Collection.json`.

Pre-configured variables:
- `base_url` = `http://localhost:8080`
- `access_token` — auto-set after login

---

## 📄 License

Apache License 2.0 — See [LICENSE](LICENSE) file.

---

## 👨‍💻 Author

Built as an enterprise-grade, interview-ready Spring Boot project demonstrating production patterns including layered architecture, JWT security, JPA relationships, pagination, global exception handling, and Docker containerization.
