package com.iub.studentmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iub.studentmanagement.dto.StudentRequestDTO;
import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.exception.ValidationException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@DisplayName("Student Controller Integration Tests")
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Student student1;
    private Student student2;
    private List<Student> studentList;
    private StudentRequestDTO validStudentRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        // Set up test students
        student1 = new Student("John Doe", "john.doe@example.com", "Computer Science");
        student1.setId(1L);
        
        student2 = new Student("Jane Smith", "jane.smith@example.com", "Mathematics");
        student2.setId(2L);
        
        studentList = Arrays.asList(student1, student2);
        
        // Set up valid request DTO
        validStudentRequest = new StudentRequestDTO("New Student", "new.student@example.com", "Physics");
    }

    @Nested
    @DisplayName("GET /api/students - Get All Students")
    class GetAllStudentsTests {
        
        @Test
        @DisplayName("Should return all students when students exist")
        void getAllStudents_ShouldReturnAllStudents() throws Exception {
            when(studentService.getAllStudents()).thenReturn(studentList);

            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].name", is("John Doe")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].name", is("Jane Smith")));

            verify(studentService).getAllStudents();
        }

        @Test
        @DisplayName("Should return empty array when no students exist")
        void getAllStudents_WithNoStudents_ShouldReturnEmptyArray() throws Exception {
            when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(studentService).getAllStudents();
        }
        
        @Test
        @DisplayName("Should handle server error gracefully")
        void getAllStudents_WithServerError_ShouldReturn500() throws Exception {
            when(studentService.getAllStudents()).thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status", is(500)))
                    .andExpect(jsonPath("$.error", is("Internal Server Error")));

            verify(studentService).getAllStudents();
        }
    }

    @Nested
    @DisplayName("GET /api/students/{id} - Get Student By ID")
    class GetStudentByIdTests {
        
        @Test
        @DisplayName("Should return student when ID exists")
        void getStudentById_WithValidId_ShouldReturnStudent() throws Exception {
            when(studentService.getStudentById(1L)).thenReturn(student1);

            mockMvc.perform(get("/api/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("John Doe")))
                    .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                    .andExpect(jsonPath("$.department", is("Computer Science")));

            verify(studentService).getStudentById(1L);
        }

        @Test
        @DisplayName("Should return 404 when ID doesn't exist")
        void getStudentById_WithInvalidId_ShouldReturn404() throws Exception {
            when(studentService.getStudentById(99L)).thenThrow(new StudentNotFoundException(99L));

            mockMvc.perform(get("/api/students/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 99")));

            verify(studentService).getStudentById(99L);
        }
        
        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void getStudentById_WithInvalidIdFormat_ShouldReturn400() throws Exception {
            mockMvc.perform(get("/api/students/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("should be of type")));
        }
    }

    @Nested
    @DisplayName("GET /api/students/department/{department} - Get Students By Department")
    class GetStudentsByDepartmentTests {
        
        @Test
        @DisplayName("Should return filtered students when department exists")
        void getStudentsByDepartment_WithExistingDepartment_ShouldReturnFilteredStudents() throws Exception {
            List<Student> csStudents = Arrays.asList(student1);
            
            when(studentService.getStudentsByDepartment("Computer Science")).thenReturn(csStudents);

            mockMvc.perform(get("/api/students/department/Computer Science"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].department", is("Computer Science")));

            verify(studentService).getStudentsByDepartment("Computer Science");
        }
        
        @Test
        @DisplayName("Should return empty array when department has no students")
        void getStudentsByDepartment_WithNonExistingDepartment_ShouldReturnEmptyArray() throws Exception {
            when(studentService.getStudentsByDepartment("History")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/students/department/History"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(studentService).getStudentsByDepartment("History");
        }
        
        @Test
        @DisplayName("Should handle URL encoded department names")
        void getStudentsByDepartment_WithEncodedDepartmentName_ShouldDecodeCorrectly() throws Exception {
            List<Student> csStudents = Arrays.asList(student1);
            
            when(studentService.getStudentsByDepartment("Computer Science")).thenReturn(csStudents);

            mockMvc.perform(get("/api/students/department/Computer%20Science"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].department", is("Computer Science")));

            verify(studentService).getStudentsByDepartment("Computer Science");
        }
    }

    @Nested
    @DisplayName("POST /api/students - Create Student")
    class CreateStudentTests {
        
        @Test
        @DisplayName("Should create student with valid data")
        void createStudent_WithValidData_ShouldReturnCreatedStudent() throws Exception {
            Student savedStudent = new Student(
                validStudentRequest.getName(), 
                validStudentRequest.getEmail(), 
                validStudentRequest.getDepartment()
            );
            savedStudent.setId(3L);

            when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validStudentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(3)))
                    .andExpect(jsonPath("$.name", is("New Student")))
                    .andExpect(jsonPath("$.email", is("new.student@example.com")))
                    .andExpect(jsonPath("$.department", is("Physics")));

            verify(studentService).createStudent(any(Student.class));
        }

        @Test
        @DisplayName("Should return 409 with duplicate email")
        void createStudent_WithDuplicateEmail_ShouldReturn409() throws Exception {
            StudentRequestDTO duplicateEmailRequest = new StudentRequestDTO(
                "Duplicate Student", 
                "john.doe@example.com", 
                "Chemistry"
            );

            when(studentService.createStudent(any(Student.class)))
                    .thenThrow(new DuplicateEmailException("john.doe@example.com"));

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")))
                    .andExpect(jsonPath("$.message", containsString("already exists")));

            verify(studentService).createStudent(any(Student.class));
        }
        
        @Test
        @DisplayName("Should return 400 with invalid email format")
        void createStudent_WithInvalidEmail_ShouldReturn400() throws Exception {
            StudentRequestDTO invalidEmailRequest = new StudentRequestDTO(
                "Invalid Email", 
                "not-an-email", 
                "Chemistry"
            );

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.email", containsString("valid")));
        }
        
        @Test
        @DisplayName("Should return 400 with missing required fields")
        void createStudent_WithMissingFields_ShouldReturn400() throws Exception {
            StudentRequestDTO incompleteRequest = new StudentRequestDTO();
            incompleteRequest.setName("Incomplete Student");
            // Missing email and department

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.department", notNullValue()));
        }
        
        @Test
        @DisplayName("Should return 400 with malformed JSON")
        void createStudent_WithMalformedJson_ShouldReturn400() throws Exception {
            String malformedJson = "{\"name\": \"Malformed JSON\", \"email\": \"missing-closing-quote@example.com, \"department\": \"Physics\"}";

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id} - Update Student")
    class UpdateStudentTests {
        
        @Test
        @DisplayName("Should update student with valid data")
        void updateStudent_WithValidData_ShouldReturnUpdatedStudent() throws Exception {
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated John", 
                "john.doe@example.com", 
                "Data Science"
            );
            
            Student updatedStudent = new Student(
                updateRequest.getName(), 
                updateRequest.getEmail(), 
                updateRequest.getDepartment()
            );
            updatedStudent.setId(1L);

            when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(updatedStudent);

            mockMvc.perform(put("/api/students/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Updated John")))
                    .andExpect(jsonPath("$.department", is("Data Science")));

            verify(studentService).updateStudent(eq(1L), any(Student.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent student")
        void updateStudent_WithInvalidId_ShouldReturn404() throws Exception {
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated Student", 
                "updated@example.com", 
                "Chemistry"
            );

            when(studentService.updateStudent(eq(99L), any(Student.class)))
                    .thenThrow(new StudentNotFoundException(99L));

            mockMvc.perform(put("/api/students/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 99")));

            verify(studentService).updateStudent(eq(99L), any(Student.class));
        }
        
        @Test
        @DisplayName("Should return 409 when updating with duplicate email")
        void updateStudent_WithDuplicateEmail_ShouldReturn409() throws Exception {
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated Student", 
                "jane.smith@example.com", // This email belongs to student2
                "Chemistry"
            );

            when(studentService.updateStudent(eq(1L), any(Student.class)))
                    .thenThrow(new DuplicateEmailException("jane.smith@example.com"));

            mockMvc.perform(put("/api/students/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")))
                    .andExpect(jsonPath("$.message", containsString("already exists")));

            verify(studentService).updateStudent(eq(1L), any(Student.class));
        }
        
        @Test
        @DisplayName("Should return 400 with invalid data")
        void updateStudent_WithInvalidData_ShouldReturn400() throws Exception {
            StudentRequestDTO invalidRequest = new StudentRequestDTO(
                "", // Empty name
                "invalid-email", // Invalid email
                "" // Empty department
            );

            mockMvc.perform(put("/api/students/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.name", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.department", notNullValue()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id} - Delete Student")
    class DeleteStudentTests {
        
        @Test
        @DisplayName("Should return 204 when deleting existing student")
        void deleteStudent_WithValidId_ShouldReturn204() throws Exception {
            doNothing().when(studentService).deleteStudent(1L);

            mockMvc.perform(delete("/api/students/1"))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(studentService).deleteStudent(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent student")
        void deleteStudent_WithInvalidId_ShouldReturn404() throws Exception {
            doThrow(new StudentNotFoundException(99L)).when(studentService).deleteStudent(99L);

            mockMvc.perform(delete("/api/students/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 99")));

            verify(studentService).deleteStudent(99L);
        }
        
        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void deleteStudent_WithInvalidIdFormat_ShouldReturn400() throws Exception {
            mockMvc.perform(delete("/api/students/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("should be of type")));
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle unsupported HTTP method")
        void handleUnsupportedHttpMethod_ShouldReturn405() throws Exception {
            mockMvc.perform(patch("/api/students/1"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.status", is(405)))
                    .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                    .andExpect(jsonPath("$.message", containsString("not supported")));
        }
        
        @Test
        @DisplayName("Should handle unsupported media type")
        void handleUnsupportedMediaType_ShouldReturn415() throws Exception {
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("This is plain text"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status", is(415)))
                    .andExpect(jsonPath("$.error", is("Unsupported Media Type")));
        }
        
        @Test
        @DisplayName("Should handle validation exception from service layer")
        void handleValidationException_ShouldReturn400() throws Exception {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "Invalid email format");
            errors.put("name", "Name is required");
            
            ValidationException validationException = new ValidationException("Validation failed", errors);
            when(studentService.createStudent(any(Student.class))).thenThrow(validationException);

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validStudentRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", is("Validation failed")))
                    .andExpect(jsonPath("$.fieldErrors.email", is("Invalid email format")))
                    .andExpect(jsonPath("$.fieldErrors.name", is("Name is required")));
        }
        
        @Test
        @DisplayName("Should handle data integrity violation")
        void handleDataIntegrityViolation_ShouldReturn409() throws Exception {
            when(studentService.createStudent(any(Student.class)))
                    .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validStudentRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")));
        }
    }
}