# Learning Management System (LMS)

## Overview
This project is a Learning Management System (LMS) designed to provide an online education platform similar to Udemy. It allows users to create, manage, purchase, and consume courses through a scalable and distributed system.

The system is built using a microservices architecture combined with an event-driven approach, enabling independent services to communicate efficiently and scale as needed.

## Key Features
- User authentication (email/password and OAuth2 with Google)
- Role-based access control (Admin, Teacher, Student)
- Course creation and management
- Course purchasing and enrollment
- Media upload and storage
- Notification system (email)
- Event-driven communication between services

## User Roles

### Admin
- Manage users (create, update, delete)
- Assign roles

### Teacher
- Create and manage courses
- Upload course content

### Student
- Register and login
- Purchase courses
- Access enrolled courses

## Architecture

### Style
- Microservices Architecture
- Event-Driven Architecture

### Services
- API Gateway
- Auth Service
- User Service
- Course Service
- Payment Service
- Media Service
- Notification Service

### Communication
- REST APIs (synchronous)
- Kafka (asynchronous event streaming)

## Event-Driven Flow (Example)

1. A student purchases a course  
2. Payment Service emits a `PaymentCompleted` event  
3. Other services react:
   - Enrollment Service enrolls the student  
   - Notification Service sends confirmation email  
   - Dashboard updates automatically  

## Technology Stack
- Backend: Spring Boot
- Messaging: Apache Kafka
- Storage: MinIO
- Database: PostgreSQL / MySQL
- Authentication: JWT + OAuth2

## Getting Started

### Prerequisites
- Java (JDK 17+)
- Docker
- Kafka
- MinIO
- Maven or Gradle

### Run the System
1. Clone the repository
2. Start infrastructure services (Kafka, MinIO, Database)
3. Run each microservice
4. Access the system via API Gateway

## Documentation
For detailed technical specifications, including APIs, database schema, and system design, see:

- SRS Document: [Open](./srs.md)

Or refer to the full SRS content here:
:contentReference[oaicite:0]{index=0}

## Future Enhancements
- Course reviews and ratings
- Live classes (WebRTC)
- AI-based recommendations
- Mobile application support

## License
This project is intended for educational and development purposes.