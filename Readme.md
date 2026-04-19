# Working Method Architecture — Auth Service, User Service & Course Service

> How the **Auth Service**, **User Service**, and **Course Service** communicate and work together inside the LMS microservice ecosystem.

---

## Table of Contents

- [System Overview](#system-overview)
- [High-Level Architecture](#high-level-architecture)
- [Service Responsibilities](#service-responsibilities)
- [Communication Patterns](#communication-patterns)
   - [Synchronous — OpenFeign (REST)](#1-synchronous--openfeign-rest)
   - [Asynchronous — Apache Kafka](#2-asynchronous--apache-kafka)
- [Registration Flow (Signup)](#registration-flow-signup)
- [Login Flow](#login-flow)
- [Course Creation & Enrollment Flow](#course-creation--enrollment-flow)
- [Service Discovery — Eureka](#service-discovery--eureka)
- [Technology Summary](#technology-summary)
- [File Structure Reference](#file-structure-reference)
- [Quick Start Guide](#quick-start-guide)

---

## System Overview

The LMS platform follows a **microservices architecture** with an **event-driven** layer. Three core services handle identity, user management, and course management:

| Service | Port | Spring Name | Purpose |
|---------|------|-------------|---------|
| **Auth Service** | `8081` | `Auth-Service` | Authentication (signup, login, JWT) |
| **User Service** | `8082` | `USER-SERVICE` | User CRUD, data persistence (PostgreSQL) |
| **Course Service** | `8082` | `course-service` | Course management, enrollment (PostgreSQL, MinIO) |
| **Eureka Server** | `8761` | `EurekaServer` | Service discovery & registry |

---

## High-Level Architecture

```mermaid
graph TB
    subgraph Client
        C["Client / Frontend / Postman"]
    end

    subgraph Infrastructure
        EU["Eureka Server :8761"]
        KF["Apache Kafka :9092"]
        ZK["Zookeeper :2181"]
        DB[("PostgreSQL :5432 (lms_db)")]
        CDB[("PostgreSQL :5433 (coursedb)")]
        MN["MinIO :9000"]
    end

    subgraph Auth-Service-8081
        AC["AuthController"]
        AS["AuthService"]
        JW["JwtUtil"]
        PE["PasswordEncoder BCrypt"]
        KP1["KafkaProducerService"]
        UC["UserClient OpenFeign"]
    end

    subgraph User-Service-8082
        UCT["UserController"]
        US["UserService"]
        UR["UserRepository JPA"]
        KC1["KafkaConsumer"]
    end

    subgraph Course-Service-8082
        CC["CourseController"]
        CS["CourseService"]
        CK["KafkaProducer/Consumer"]
        MS["MinioService"]
        CR["CourseRepository"]
        ER["EnrollmentRepository"]
        JF["JwtFilter"]
    end

    C -->|HTTP| AC
    C -->|HTTP| CC

    AC --> AS
    AS --> JW
    AS --> PE
    AS --> UC
    AS --> KP1

    UC -->|Feign REST Call| UCT
    UCT --> US
    US --> UR
    UR --> DB

    CC --> JF
    JF -->|Verify| CS
    CS --> CR
    CS --> ER
    CS --> MS
    CS --> CK

    KP1 -->|Publish user-created| KF
    CK -->|Publish course events| KF
    KC1 -->|Consume events| KF
    CK -->|Consume user-created| KF

    CR --> CDB
    ER --> CDB
    MS --> MN

    AC -.->|Register| EU
    CC -.->|Register| EU
    UCT -.->|Register| EU

    style C fill:#4A90D9,stroke:#2C5F8A,color:#fff
    style EU fill:#50C878,stroke:#2E8B57,color:#fff
    style KF fill:#FF8C42,stroke:#CC6F35,color:#fff
    style DB fill:#9B59B6,stroke:#7D3C98,color:#fff
    style CDB fill:#9B59B6,stroke:#7D3C98,color:#fff
    style MN fill:#E67E22,stroke:#D35400,color:#fff
    style AC fill:#3498DB,stroke:#2980B9,color:#fff
    style CC fill:#E74C3C,stroke:#C0392B,color:#fff
    style UCT fill:#F39C12,stroke:#E67E22,color:#fff
```

---

## Service Responsibilities

### Auth Service (Port 8081)

| Component | Class | Responsibility |
|-----------|-------|----------------|
| Controller | `AuthController` | Exposes `/auth/signup` and `/auth/login` endpoints |
| Service | `AuthService` | Orchestrates registration & login logic |
| Feign Client | `UserClient` | Synchronous HTTP calls to User Service |
| JWT Utility | `JwtUtil` | Generates & validates JWT tokens (HS256) |
| Security | `SecurityConfig` | Permits `/auth/**`, BCrypt password encoder |
| Kafka Producer | `KafkaProducerService` | Publishes `user-created` events |

### User Service (Port 8082)

| Component | Class | Responsibility |
|-----------|-------|----------------|
| Controller | `UserController` | Exposes `POST /users` and `GET /users/email/{email}` |
| Service | `UserService` | Creates users, looks up by email |
| Repository | `UserRepository` | JPA repository for `users` table |
| Model | `User` | Entity with `id`, `name`, `email`, `password`, `role`, `provider` |
| Kafka Consumer | `KafkaConsumer` | Listens to `user-created` topic (group: `user-group`) |

### Course Service (Port 8082)

| Component | Class | Responsibility |
|-----------|-------|----------------|
| Controller | `CourseController` | Exposes `/courses`, `/courses/{id}/enroll` endpoints |
| Service | `CourseService` | Manages courses, enrollments, Minio uploads |
| Security | `JwtFilter` | Validates JWT tokens for protected routes |
| Minio Service | `MinioService` | Uploads files to MinIO object storage |
| Repositories | `CourseRepository`, `EnrollmentRepository` | JPA repositories for courses and enrollments |
| Kafka Producer | `KafkaProducerService` | Publishes `course-enrollment` and `course-created` events |
| Kafka Consumer | `KafkaConsumerService` | Consumes `user-created` events |

---

## Communication Patterns

The two services communicate using **two patterns** simultaneously:

### 1. Synchronous — OpenFeign (REST)

Auth Service uses **Spring Cloud OpenFeign** to make direct HTTP calls to User Service. Eureka handles service discovery so Auth Service resolves `USER-SERVICE` by name (no hardcoded URLs).

```mermaid
sequenceDiagram
    participant AS as Auth Service
    participant EU as Eureka Server
    participant US as User Service

    AS->>EU: Where is USER-SERVICE?
    EU-->>AS: localhost:8082
    AS->>US: POST /users (RegisterRequest)
    US-->>AS: User (created)
    AS->>US: GET /users/email/{email}
    US-->>AS: User (found)
```

**Feign Interface (`UserClient.java`):**

```java
@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @PostMapping("/users")
    UserResponse createUser(@RequestBody RegisterRequest request);

    @GetMapping("/users/email/{email}")
    UserResponse getByEmail(@PathVariable("email") String email);
}
```

### 2. Asynchronous — Apache Kafka

After a successful signup, Auth Service publishes a `user-created` event to the Kafka topic. User Service consumes this event for post-registration tasks (logging, notifications, etc.).

```mermaid
sequenceDiagram
    participant AS as Auth Service (Producer)
    participant KF as Kafka Broker (Topic: user-created)
    participant US as User Service (Consumer: user-group)

    AS->>KF: Publish event (user email)
    KF-->>US: Deliver message
    US->>US: Process event (log / notify / etc.)
```

**Producer (`KafkaProducerService.java`):**

```java
@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserCreatedEvent(String email) {
        kafkaTemplate.send("user-created", email);
    }
}
```

**Consumer (`KafkaConsumer.java`):**

```java
@Service
public class KafkaConsumer {
    @KafkaListener(topics = "user-created", groupId = "user-group")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}
```

---

## Registration Flow (Signup)

Complete step-by-step flow when a user registers:

```mermaid
sequenceDiagram
    actor U as User
    participant AC as AuthController
    participant AS as AuthService
    participant PE as BCryptEncoder
    participant FC as UserClient (Feign)
    participant UCT as UserController
    participant US as UserService
    participant DB as PostgreSQL
    participant KP as KafkaProducer
    participant KF as Kafka Broker
    participant KC as KafkaConsumer

    U->>AC: POST /auth/signup<br/>{name, email, password}
    AC->>AS: signup(RegisterRequest)

    Note over AS,PE: Step 1 — Hash the password
    AS->>PE: encode(rawPassword)
    PE-->>AS: hashedPassword

    Note over AS: Step 2 — Set default role = "STUDENT"

    Note over AS,FC: Step 3 — Create user via Feign (sync)
    AS->>FC: createUser(RegisterRequest)
    FC->>UCT: POST /users<br/>{name, email, hashedPassword, role}
    UCT->>US: create(RegisterRequest)
    US->>DB: INSERT INTO users
    DB-->>US: User entity
    US-->>UCT: User
    UCT-->>FC: User
    FC-->>AS: UserResponse

    Note over AS,KP: Step 4 — Fire Kafka event (async)
    AS->>KP: sendUserCreatedEvent(email)
    KP->>KF: Publish to "user-created"
    KF-->>KC: Deliver event
    KC->>KC: Log message

    AS-->>AC: "User Registered"
    AC-->>U: 200 OK — "User Registered"
```

---

## Login Flow

Complete step-by-step flow when a user logs in:

```mermaid
sequenceDiagram
    actor U as User
    participant AC as AuthController
    participant AS as AuthService
    participant FC as UserClient (Feign)
    participant UCT as UserController
    participant US as UserService
    participant DB as PostgreSQL
    participant PE as BCryptEncoder
    participant JW as JwtUtil

    U->>AC: POST /auth/login<br/>{email, password}
    AC->>AS: login(LoginRequest)

    Note over AS,FC: Step 1 — Fetch user by email (sync Feign)
    AS->>FC: getByEmail(email)
    FC->>UCT: GET /users/email/{email}
    UCT->>US: getByEmail(email)
    US->>DB: SELECT * FROM users WHERE email = ?
    DB-->>US: User entity
    US-->>UCT: User
    UCT-->>FC: User
    FC-->>AS: UserResponse (email, hashedPassword)

    Note over AS,PE: Step 2 — Verify password
    AS->>PE: matches(rawPassword, hashedPassword)
    PE-->>AS: true / false

    alt Password mismatch
        AS-->>AC: RuntimeException("Invalid credentials")
        AC-->>U: 401 Unauthorized
    end

    Note over AS,JW: Step 3 — Generate JWT token
    AS->>JW: generateToken(email)
    JW-->>AS: JWT token (HS256, 24h expiry)

    AS-->>AC: JWT token string
    AC-->>U: 200 OK — JWT token
```

---

## Course Creation & Enrollment Flow

Complete step-by-step flow when a teacher creates a course and student enrolls:

```mermaid
sequenceDiagram
    actor T as Teacher
    participant CC as CourseController
    participant CS as CourseService
    participant MS as MinioService
    participant CR as CourseRepository
    participant KP as KafkaProducer
    participant KF as Kafka Broker
    participant MN as MinIO

    T->>CC: POST /courses<br/>{title, desc, image, video, material}<br/>Auth: Bearer JWT_TEACHER
    CC->>CS: create(course, files, teacherId)
    
    Note over CS,MN: Upload files to MinIO
    CS->>MS: upload(image, imageBucket)
    MS->>MN: Store file
    MN-->>MS: imageUrl
    MS-->>CS: imageUrl
    
    CS->>MS: upload(video, videoBucket)
    MS->>MN: Store file
    MN-->>MS: videoUrl
    MS-->>CS: videoUrl
    
    CS->>MS: upload(material, materialBucket)
    MS->>MN: Store file
    MN-->>MS: materialUrl
    MS-->>CS: materialUrl

    Note over CS,KF: Save and publish event
    CS->>CR: save(course)
    CR-->>CS: Course (with ID)
    CS->>KP: publishCourseCreatedEvent()
    KP->>KF: Send to course-created topic
    CS-->>CC: Course
    CC-->>T: 200 OK

    actor S as Student
    participant ENC as EnrollController
    participant ES as EnrollmentService
    participant ER as EnrollmentRepository
    participant KP2 as KafkaProducer
    participant KF2 as Kafka Broker

    S->>ENC: POST /courses/{courseId}/enroll<br/>Auth: Bearer JWT_STUDENT
    ENC->>ES: enroll(studentId, courseId)
    ES->>ER: save(enrollment)
    ER-->>ES: Enrollment (with ID)
    ES->>KP2: publishEnrollmentEvent()
    KP2->>KF2: Send to course-enrollment topic
    ES-->>ENC: Enrollment
    ENC-->>S: 200 OK
```

---

## Service Discovery — Eureka

All three services register with **Eureka Server** on startup. Services discover each other dynamically through Eureka:

```mermaid
graph LR
    EU["Eureka Server :8761"]
    AS["Auth Service :8081"]
    US["User Service :8082"]
    CS["Course Service :8082"]

    AS -->|Registers as Auth-Service| EU
    US -->|Registers as USER-SERVICE| EU
    CS -->|Registers as course-service| EU
    
    AS -->|Discovers USER-SERVICE| EU
    CS -->|Listens to Kafka| KB["Kafka Broker"]
    US -->|Listens to Kafka| KB

    style EU fill:#50C878,stroke:#2E8B57,color:#fff
    style AS fill:#3498DB,stroke:#2980B9,color:#fff
    style US fill:#F39C12,stroke:#E67E22,color:#fff
    style CS fill:#E74C3C,stroke:#C0392B,color:#fff
    style KB fill:#FF8C42,stroke:#CC6F35,color:#fff
```

**Configuration**:

| Property | Auth Service | User Service | Course Service |
|----------|-------------|-------------|-----------------|
| `spring.application.name` | `Auth-Service` | `USER-SERVICE` | `course-service` |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka` | `http://localhost:8761/eureka` | `http://localhost:8761/eureka` |

---

## Technology Summary

```mermaid
mindmap
  root((LMS Microservices))
    Auth Service
      Spring Boot
      Spring Security
      BCrypt Encoder
      JWT HS256
      OpenFeign Client
      Kafka Producer
    User Service
      Spring Boot
      Spring Data JPA
      PostgreSQL
      Kafka Consumer
    Course Service
      Spring Boot
      Spring Data JPA
      PostgreSQL
      JWT Filter
      MinIO Client
      Kafka Producer/Consumer
    Infrastructure
      Eureka Server
      Apache Kafka
      Zookeeper
      PostgreSQL (2 instances)
      MinIO
      Docker Compose
```

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot |
| Service Discovery | Netflix Eureka |
| Inter-Service Calls | Spring Cloud OpenFeign |
| Async Messaging | Apache Kafka |
| Authentication | JWT (HS256) via `jjwt` |
| Password Hashing | BCrypt (`BCryptPasswordEncoder`) |
| Database | PostgreSQL (`lms_db`, `coursedb`) |
| ORM | Spring Data JPA / Hibernate |
| File Storage | MinIO (S3-compatible) |
| Containerization | Docker Compose |

---

## File Structure Reference

```
microservice-lms-system-springboot/
├── docker-compose.yml                    # Kafka, Zookeeper, PostgreSQL (2x), MinIO
├── htos-e2e.postman_collection.json      # Postman collection with all tests
├── Readme.md                             # Main architecture document
├── COURSE_SERVICE_README.md              # Course service detailed guide
├── EurekaServer/                         # Service Discovery (port 8761)
│   └── src/main/java/site/shazan/EurekaServer/
│       └── EurekaServerApplication.java
│
├── AuthService/                          # Authentication (port 8081)
│   └── src/main/java/site/shazan/AuthService/
│       ├── Controller/
│       │   └── AuthController.java       # /auth/signup, /auth/login
│       ├── Service/
│       │   ├── AuthService.java          # Signup & login orchestration
│       │   └── KafkaProducerService.java # Publishes 'user-created' events
│       ├── Dtos/
│       ├── repo/
│       │   └── UserClient.java           # Feign client → USER-SERVICE
│       ├── utils/
│       │   └── JwtUtil.java              # JWT generate & extract
│       └── config/
│           └── SecurityConfig.java       # BCrypt, permitAll /auth/**
│
├── UserService/                          # User Management (port 8082)
│   └── src/main/java/site/shazan/UserService/
│       ├── controller/
│       │   └── UserController.java       # POST /users, GET /users/email/{email}
│       ├── service/
│       │   └── UserService.java          # create(), getByEmail()
│       ├── dtos/
│       ├── models/
│       │   └── User.java                 # JPA entity (users table)
│       ├── repo/
│       │   └── UserRepository.java       # JPA repository
│       └── kafka/
│           └── KafkaConsumer.java        # Listens to 'user-created' topic
│
└── course/                               # Course Management (port 8082)
    ├── src/main/resources/
    │   └── application.yaml              # Kafka, JWT, MinIO config
    └── src/main/java/site/shazan/course/
        ├── controller/
        │   └── CourseController.java     # POST /courses, POST /courses/{id}/enroll
        ├── service/
        │   ├── CourseService.java        # create(), enroll(), getAll()
        │   ├── MinioService.java         # File upload to MinIO
        │   └── KafkaProducerService.java # Publishes enrollment/course events
        ├── models/
        │   ├── Course.java               # JPA entity (courses table)
        │   └── Enrollment.java           # JPA entity (enrollments table)
        ├── repo/
        │   ├── CourseRepository.java     # JPA repository for courses
        │   └── EnrollmentRepository.java # JPA repository for enrollments
        ├── kafka/
        │   ├── KafkaProducerService.java
        │   └── KafkaConsumerService.java
        └── Security/
            ├── JwtFilter.java            # JWT validation filter
            └── SecurityConfig.java       # Security configuration
```

---

## Quick Start Guide

### Prerequisites
- Java 11+
- Maven 3.8+
- Docker & Docker Compose
- Postman (optional)

### Step 1: Clone & Navigate
```bash
git clone <repo>
cd microservice-lms-system-springboot
```

### Step 2: Start Infrastructure
```bash
docker-compose up -d

# Verify all services
docker ps

# Check logs
docker logs kafka
docker logs postgres_lms
docker logs postgres_course
docker logs minio
```

### Step 3: Start Services (in order)
```bash
# Terminal 1: Eureka Server
cd EurekaServer
mvn spring-boot:run

# Terminal 2: Auth Service
cd AuthService
mvn spring-boot:run

# Terminal 3: User Service
cd UserService
mvn spring-boot:run

# Terminal 4: Course Service
cd course
mvn spring-boot:run
```

### Step 4: Verify Eureka
Navigate to: `http://localhost:8761`

Should show 3 services registered:
- Auth-Service (8081)
- USER-SERVICE (8082)
- course-service (8082)

### Step 5: Create MinIO Buckets
```bash
# Access MinIO console
# URL: http://localhost:9001
# Username: minioadmin
# Password: minioadmin

# Create buckets: images, videos, materials
```

### Step 6: Import Postman Collection
1. Open Postman
2. Click "Import"
3. Select: `htos-e2e.postman_collection.json`
4. Run E2E Flow tests in order

### Testing Workflow

**Collection Workflow**:
```
E2E Flow
  1) Signup (creates student)
  2) Login (gets JWT token)
  3) Get User By Email

User Service Account Creation
  1) Login as Admin
  2) Create Teacher Account
  3) Get Teacher By Email

Course Service E2E
  1) Login as Teacher
  2) Create Course (with files)
  3) Get All Courses
  4) Student Login
  5) Student Enroll in Course

Course Authorization Tests
  - Student Cannot Create Course (should fail)

Negative Cases
  - Login with wrong password (should fail)
```

---

## Running Each Component
