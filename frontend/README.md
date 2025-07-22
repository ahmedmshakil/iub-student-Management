# IUB Student Management System - Frontend

This is the frontend application for the IUB Student Management System built with Vue 3, Vuetify 3, and Vite.

## Features

- **Student List**: View all students in a data table with search and pagination
- **Add Student**: Create new student records with form validation
- **Edit Student**: Update existing student information
- **View Student**: Display detailed student information
- **Delete Student**: Remove students with confirmation dialog
- **Responsive Design**: Works on desktop, tablet, and mobile devices

## Tech Stack

- **Vue 3** with Composition API
- **Vuetify 3** for Material Design components
- **Vue Router 4** for navigation
- **Axios** for HTTP requests
- **Vite** for fast development and building

## Prerequisites

- Node.js 16+ and npm
- Backend API running on http://localhost:8080

## Installation

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

The application will be available at http://localhost:3000

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run test:unit` - Run unit tests with Vitest
- `npm run test:e2e` - Run end-to-end tests with Cypress
- `npm run test:e2e:ci` - Run E2E tests in CI mode

## Project Structure

```
src/
├── views/           # Page components
│   ├── StudentList.vue
│   ├── StudentForm.vue
│   └── StudentDetail.vue
├── services/        # API services
│   └── studentService.js
├── router/          # Route configuration
│   └── routes.js
├── App.vue          # Main app component
└── main.js          # App entry point
```

## API Integration

The frontend communicates with the Spring Boot backend through REST API calls:

- `GET /api/students` - Get all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create new student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

## Development Notes

- The app uses Vuetify 3 for consistent Material Design UI
- Form validation is implemented both client-side and server-side
- Global notification system for user feedback
- Responsive design with mobile-first approach
- API proxy configured in Vite for development