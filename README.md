# IUB Student Management System

A full-stack web application for managing student records at IUB (Independent University, Bangladesh). Built with Spring Boot backend and Vue.js frontend.

## Technology Stack

### Backend
- **Java 21** with Spring Boot 3.2.1
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Maven** for dependency management
- **SpringDoc OpenAPI** for API documentation
- **TestContainers** for integration testing

### Frontend
- **Vue 3** with Composition API
- **Vuetify 3** for Material Design UI components
- **Vue Router 4** for navigation
- **Axios** for HTTP requests
- **Vite** for build tooling
- **Vitest** for unit testing
- **Cypress** for E2E testing

## Project Structure

```
├── backend/                    # Spring Boot backend
│   ├── src/main/java/com/iub/studentmanagement/
│   │   ├── controller/         # REST controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Custom exceptions
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Data repositories
│   │   └── service/           # Business logic
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                   # Vue.js frontend
│   ├── src/
│   │   ├── components/        # Reusable Vue components
│   │   ├── router/           # Vue Router configuration
│   │   ├── services/         # API service layer
│   │   ├── views/            # Page components
│   │   ├── App.vue           # Main app component
│   │   └── main.js           # App entry point
│   ├── package.json
│   └── vite.config.js
└── .kiro/specs/               # Feature specifications
    └── iub-student-management/
        ├── requirements.md
        ├── design.md
        └── tasks.md
```

## Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **PostgreSQL 15** or higher
- **Maven 3.8** or higher

## Database Setup

1. Install PostgreSQL and create a database:
```sql
CREATE DATABASE iub_student_management;
CREATE USER postgres WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE iub_student_management TO postgres;
```

2. Update database configuration in `backend/src/main/resources/application.properties` if needed.

## Getting Started

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Install dependencies and compile:
```bash
mvn clean compile
```

3. Run the application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:3000`

## API Documentation

Once the backend is running, you can access the Swagger UI documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## Available Scripts

### Backend
- `mvn spring-boot:run` - Start the development server
- `mvn test` - Run unit tests
- `mvn clean package` - Build the application

### Frontend
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run test:unit` - Run unit tests
- `npm run test:e2e` - Run E2E tests

## Features

- ✅ **Student Management**: Create, read, update, and delete student records
- ✅ **Responsive UI**: Works on desktop, tablet, and mobile devices
- ✅ **Data Validation**: Client-side and server-side validation
- ✅ **Error Handling**: Comprehensive error handling with user feedback
- ✅ **API Documentation**: Auto-generated Swagger documentation
- ✅ **Testing**: Unit and integration tests for both frontend and backend


