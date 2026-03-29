# Software Requirements Specification (SRS)
## Learning Management System (LMS)

---

## 1. Introduction

### 1.1 Purpose
This document describes the requirements for a Learning Management System (LMS) built using Spring Boot with a microservices and event-driven architecture. The system supports course creation, enrollment, and content delivery similar to Udemy.

### 1.2 Scope
The LMS will provide:
- User authentication and authorization (OAuth2 + basic auth)
- Role-based access control (Admin, Teacher, Student)
- Course creation, publishing, and purchasing
- Media storage using MinIO
- Event-driven communication using Kafka

### 1.3 Definitions
- LMS: Learning Management System
- OAuth2: Authentication protocol for secure login (e.g., Google)
- RBAC: Role-Based Access Control
- Microservices: Independent services communicating via APIs/events
- Event-driven architecture: Services communicate through events (Kafka)

---

## 2. Overall Description

### 2.1 Product Perspective
The system is a distributed application consisting of multiple microservices:
- API Gateway
- Auth Service
- User Service
- Course Service
- Payment Service
- Media Service
- Notification Service

### 2.2 User Classes
- **Admin**
  - Full system access
  - Manage users (create/update/delete)
- **Teacher**
  - Create and manage courses
- **Student**
  - Register/login
  - Purchase and consume courses

### 2.3 Assumptions
- Users have internet access
- OAuth2 providers (e.g., Google) are available
- Kafka and MinIO are properly configured

---

## 3. System Architecture

### 3.1 Architecture Style
- Microservices Architecture
- Event-Driven Architecture (Kafka)

### 3.2 Core Components
- **API Gateway**: Routes requests
- **Auth Service**: Handles authentication (JWT, OAuth2)
- **User Service**: Manages user data and roles
- **Course Service**: Handles course creation and management
- **Payment Service**: Handles transactions
- **Media Service**: Handles file storage (MinIO)
- **Notification Service**: Sends emails/events

### 3.3 Communication
- REST APIs (synchronous)
- Kafka (asynchronous events)

---

## 4. Functional Requirements

### 4.1 Authentication & Authorization

#### 4.1.1 Signup
- Students can register using email/password
- Default role: Student

#### 4.1.2 Login
- Email/password login
- OAuth2 login (Google)

#### 4.1.3 Password Management
- Forgot password
- Reset password via email token

#### 4.1.4 Role-Based Access
- Admin, Teacher, Student roles enforced via JWT

---

### 4.2 User Management

#### Admin Capabilities:
- Create users (Admin, Teacher, Student)
- Update user roles
- Delete users

#### System Behavior:
- Store user data securely
- Encrypt passwords

---

### 4.3 Course Management

#### Teacher/Admin:
- Create course
- Add:
  - Title
  - Description
  - Price
  - Category
- Upload videos and materials
- Publish/unpublish course

#### Student:
- View course catalog
- Enroll after payment
- Access purchased courses

---

### 4.4 Payment System

- Students can purchase courses
- Payment status stored
- On successful payment:
  - Emit event via Kafka
  - Grant course access

---

### 4.5 Media Management

- Upload videos/images to MinIO
- Generate secure access URLs
- Store metadata in database

---

### 4.6 Event System (Kafka)

Events include:
- User Registered
- Course Created
- Course Purchased
- Payment Completed

Services subscribe and react accordingly.

---

### 4.7 Notification System

- Send email for:
  - Registration
  - Password reset
  - Course purchase confirmation

---

## 5. Non-Functional Requirements

### 5.1 Performance
- Support concurrent users
- Fast API response (<500ms)

### 5.2 Scalability
- Microservices independently scalable

### 5.3 Security
- JWT-based authentication
- OAuth2 integration
- Password encryption (BCrypt)
- Secure API Gateway

### 5.4 Reliability
- Retry mechanisms in Kafka consumers
- Fault tolerance in services

### 5.5 Maintainability
- Modular service design
- Clean code practices

---

## 6. Database Design (High-Level)

### 6.1 User Table
- id
- name
- email
- password
- role
- provider (OAuth/local)

### 6.2 Course Table
- id
- title
- description
- price
- teacher_id
- status

### 6.3 Enrollment Table
- id
- student_id
- course_id
- payment_status

### 6.4 Media Table
- id
- course_id
- file_url
- type (video/image)

---

## 7. API Overview (Sample)

### Auth Service
- POST /auth/signup
- POST /auth/login
- POST /auth/oauth2
- POST /auth/forgot-password
- POST /auth/reset-password

### User Service
- GET /users
- POST /users
- PUT /users/{id}
- DELETE /users/{id}

### Course Service
- POST /courses
- GET /courses
- GET /courses/{id}
- PUT /courses/{id}
- DELETE /courses/{id}

---

## 8. Constraints

- Must use Spring Boot
- Must use Kafka for event streaming
- Must use MinIO for storage
- Must follow microservices architecture

---

## 9. Future Enhancements

- Course reviews and ratings
- Live classes (WebRTC)
- AI-based recommendations
- Mobile application

---

## 10. Conclusion

This system provides a scalable, secure, and modern LMS platform using microservices and event-driven architecture. It supports role-based access, course monetization, and distributed processing with Kafka and MinIO.