# CloudNest ☁️

A modern, scalable cloud storage solution that enables users to store, manage, and access their files seamlessly across multiple formats with secure authentication and flexible subscription plans.

## 🚀 Features

### Core Functionality
- **Multi-Format File Upload**: Upload and store documents, images, videos, audio files, and more without format restrictions
- **Secure Authentication**: JWT-based authentication system ensuring robust user verification and session management
- **Subscription Management**: Flexible tier-based access with integrated payment processing
- **RESTful API**: Comprehensive CRUD operations for efficient file and user management

### Security & Performance
- Token-based authentication with Clerk integration
- Secure file storage and retrieval
- Session management and user verification
- Transaction security with Razorpay payment gateway

## 🛠️ Tech Stack

### Frontend
- **React**: Modern UI framework for building responsive interfaces
- **Clerk**: User authentication and management

### Backend
- **Spring Boot**: Robust Java-based backend framework
- **RESTful APIs**: Clean API architecture for client-server communication
- **JWT**: Secure token-based authentication

### Payment Integration
- **Razorpay**: Payment gateway for subscription management and secure transactions

## 📋 Prerequisites

Before running this project, ensure you have:

- Node.js (v14 or higher)
- Java JDK (v11 or higher)
- Maven
- MySQL or PostgreSQL database
- Clerk account for authentication
- Razorpay account for payment processing


```

### File Operations
```
GET    /api/files             - Get all user files
GET    /api/files/{id}        - Get specific file
POST   /api/files/upload      - Upload new file
PUT    /api/files/{id}        - Update file metadata
DELETE /api/files/{id}        - Delete file
```

### Subscription Management
```
GET    /api/subscriptions     - Get subscription plans
POST   /api/subscriptions     - Create subscription
PUT    /api/subscriptions     - Update subscription
POST   /api/payments/verify   - Verify payment
```



## 📧 Support


---

⭐ If you find this project useful, please consider giving it a star!
