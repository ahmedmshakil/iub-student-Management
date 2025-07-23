package com.iub.studentmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iub.studentmanagement.dto.StudentRequestDTO;
import com.iub.studentmanagement.dto.StudentResponseDTO;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.repository.StudentRepository;
import com.iub.studentmanagement.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the StudentController.
 * Uses @SpringBootTest to load the entire application context.
 * Tests the full request-response cycle including database operations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Student Controller Integration Tests")
public class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    private Student student1;
    private Student student2;
    private StudentRequestDTO validStudentRequest;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        studentRepository.deleteAll();
        
        // Set up test students
        student1 = new Student("John Doe", "john.doe@example.com", "Computer Science");
        student2 = new Student("Jane Smith", "jane.smith@example.com", "Mathematics");
        
        // Save students to database
        student1 = studentRepository.save(student1);
        student2 = studentRepository.save(student2);
        
        // Set up valid request DTO
        validStudentRequest = new StudentRequestDTO("New Student", "new.student@example.com", "Physics");
    }

    @Nested
    @DisplayName("GET /api/students - Get All Students")
    class GetAllStudentsTests {
        
        @Test
        @DisplayName("Should return all students when students exist")
        void getAllStudents_ShouldReturnAllStudents() throws Exception {
            // When & Then
            MvcResult result = mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("John Doe")))
                    .andExpect(jsonPath("$[1].name", is("Jane Smith")))
                    .andReturn();
            
            // Verify response can be deserialized to DTOs
            String content = result.getResponse().getContentAsString();
            List<?> students = objectMapper.readValue(content, List.class);
            assertEquals(2, students.size());
        }

        @Test
        @DisplayName("Should return empty array when no students exist")
        void getAllStudents_WithNoStudents_ShouldReturnEmptyArray() throws Exception {
            // Given
            studentRepository.deleteAll();
            
            // When & Then
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/students/{id} - Get Student By ID")
    class GetStudentByIdTests {
        
        @Test
        @DisplayName("Should return student when ID exists")
        void getStudentById_WithValidId_ShouldReturnStudent() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/{id}", student1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(student1.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("John Doe")))
                    .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                    .andExpect(jsonPath("$.department", is("Computer Science")))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()));
        }

        @Test
        @DisplayName("Should return 404 when ID doesn't exist")
        void getStudentById_WithInvalidId_ShouldReturn404() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 999")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
        
        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void getStudentById_WithInvalidIdFormat_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("should be of type")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
    }

    @Nested
    @DisplayName("GET /api/students/department/{department} - Get Students By Department")
    class GetStudentsByDepartmentTests {
        
        @Test
        @DisplayName("Should return filtered students when department exists")
        void getStudentsByDepartment_WithExistingDepartment_ShouldReturnFilteredStudents() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/department/{department}", "Computer Science"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name", is("John Doe")))
                    .andExpect(jsonPath("$[0].department", is("Computer Science")));
        }
        
        @Test
        @DisplayName("Should return empty array when department has no students")
        void getStudentsByDepartment_WithNonExistingDepartment_ShouldReturnEmptyArray() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/department/{department}", "History"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
        
        @Test
        @DisplayName("Should handle URL encoded department names")
        void getStudentsByDepartment_WithEncodedDepartmentName_ShouldDecodeCorrectly() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/students/department/{department}", "Computer Science"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].department", is("Computer Science")));
        }
    }

    @Nested
    @DisplayName("POST /api/students - Create Student")
    class CreateStudentTests {
        
        @Test
        @DisplayName("Should create student with valid data")
        void createStudent_WithValidData_ShouldReturnCreatedStudent() throws Exception {
            // When & Then
            MvcResult result = mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validStudentRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name", is("New Student")))
                    .andExpect(jsonPath("$.email", is("new.student@example.com")))
                    .andExpect(jsonPath("$.department", is("Physics")))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()))
                    .andReturn();
            
            // Verify student was actually saved to database
            String content = result.getResponse().getContentAsString();
            StudentResponseDTO responseDTO = objectMapper.readValue(content, StudentResponseDTO.class);
            
            assertTrue(studentRepository.existsById(responseDTO.getId()));
            assertEquals(3, studentRepository.count());
        }

        @Test
        @DisplayName("Should return 409 with duplicate email")
        void createStudent_WithDuplicateEmail_ShouldReturn409() throws Exception {
            // Given
            StudentRequestDTO duplicateEmailRequest = new StudentRequestDTO(
                "Duplicate Student", 
                "john.doe@example.com", // This email already exists
                "Chemistry"
            );

            // When & Then
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")))
                    .andExpect(jsonPath("$.message", containsString("already exists")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify no new student was added
            assertEquals(2, studentRepository.count());
        }
        
        @Test
        @DisplayName("Should return 400 with invalid email format")
        void createStudent_WithInvalidEmail_ShouldReturn400() throws Exception {
            // Given
            StudentRequestDTO invalidEmailRequest = new StudentRequestDTO(
                "Invalid Email", 
                "not-an-email", 
                "Chemistry"
            );

            // When & Then
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify no new student was added
            assertEquals(2, studentRepository.count());
        }
        
        @Test
        @DisplayName("Should return 400 with missing required fields")
        void createStudent_WithMissingFields_ShouldReturn400() throws Exception {
            // Given
            StudentRequestDTO incompleteRequest = new StudentRequestDTO();
            incompleteRequest.setName("Incomplete Student");
            // Missing email and department

            // When & Then
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.department", notNullValue()))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify no new student was added
            assertEquals(2, studentRepository.count());
        }
        
        @Test
        @DisplayName("Should return 400 with malformed JSON")
        void createStudent_WithMalformedJson_ShouldReturn400() throws Exception {
            // Given
            String malformedJson = "{\"name\": \"Malformed JSON\", \"email\": \"missing-closing-quote@example.com, \"department\": \"Physics\"}";

            // When & Then
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("Malformed JSON")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify no new student was added
            assertEquals(2, studentRepository.count());
        }
    }

    @Nested
    @DisplayName("PUT /api/students/{id} - Update Student")
    class UpdateStudentTests {
        
        @Test
        @DisplayName("Should update student with valid data")
        void updateStudent_WithValidData_ShouldReturnUpdatedStudent() throws Exception {
            // Given
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated John", 
                "john.updated@example.com", 
                "Data Science"
            );
            
            // When & Then
            MvcResult result = mockMvc.perform(put("/api/students/{id}", student1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(student1.getId().intValue())))
                    .andExpect(jsonPath("$.name", is("Updated John")))
                    .andExpect(jsonPath("$.email", is("john.updated@example.com")))
                    .andExpect(jsonPath("$.department", is("Data Science")))
                    .andExpect(jsonPath("$.createdAt", notNullValue()))
                    .andExpect(jsonPath("$.updatedAt", notNullValue()))
                    .andReturn();
            
            // Verify student was actually updated in database
            Student updatedStudent = studentRepository.findById(student1.getId()).orElseThrow();
            assertEquals("Updated John", updatedStudent.getName());
            assertEquals("john.updated@example.com", updatedStudent.getEmail());
            assertEquals("Data Science", updatedStudent.getDepartment());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent student")
        void updateStudent_WithInvalidId_ShouldReturn404() throws Exception {
            // Given
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated Student", 
                "updated@example.com", 
                "Chemistry"
            );

            // When & Then
            mockMvc.perform(put("/api/students/{id}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 999")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify database remains unchanged
            assertEquals(2, studentRepository.count());
            Student unchangedStudent = studentRepository.findById(student1.getId()).orElseThrow();
            assertEquals("John Doe", unchangedStudent.getName());
        }
        
        @Test
        @DisplayName("Should return 409 when updating with duplicate email")
        void updateStudent_WithDuplicateEmail_ShouldReturn409() throws Exception {
            // Given
            StudentRequestDTO updateRequest = new StudentRequestDTO(
                "Updated Student", 
                "jane.smith@example.com", // This email belongs to student2
                "Chemistry"
            );

            // When & Then
            mockMvc.perform(put("/api/students/{id}", student1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)))
                    .andExpect(jsonPath("$.error", is("Conflict")))
                    .andExpect(jsonPath("$.message", containsString("already exists")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify student remains unchanged
            Student unchangedStudent = studentRepository.findById(student1.getId()).orElseThrow();
            assertEquals("John Doe", unchangedStudent.getName());
            assertEquals("john.doe@example.com", unchangedStudent.getEmail());
        }
        
        @Test
        @DisplayName("Should return 400 with invalid data")
        void updateStudent_WithInvalidData_ShouldReturn400() throws Exception {
            // Given
            StudentRequestDTO invalidRequest = new StudentRequestDTO(
                "", // Empty name
                "invalid-email", // Invalid email
                "" // Empty department
            );

            // When & Then
            mockMvc.perform(put("/api/students/{id}", student1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.fieldErrors.name", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                    .andExpect(jsonPath("$.fieldErrors.department", notNullValue()))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify student remains unchanged
            Student unchangedStudent = studentRepository.findById(student1.getId()).orElseThrow();
            assertEquals("John Doe", unchangedStudent.getName());
        }
    }

    @Nested
    @DisplayName("DELETE /api/students/{id} - Delete Student")
    class DeleteStudentTests {
        
        @Test
        @DisplayName("Should return 204 when deleting existing student")
        void deleteStudent_WithValidId_ShouldReturn204() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/students/{id}", student1.getId()))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
            
            // Verify student was actually deleted from database
            assertFalse(studentRepository.existsById(student1.getId()));
            assertEquals(1, studentRepository.count());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent student")
        void deleteStudent_WithInvalidId_ShouldReturn404() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/students/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")))
                    .andExpect(jsonPath("$.message", containsString("Student not found with id: 999")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify database remains unchanged
            assertEquals(2, studentRepository.count());
        }
        
        @Test
        @DisplayName("Should return 400 when ID is not a number")
        void deleteStudent_WithInvalidIdFormat_ShouldReturn400() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/students/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.error", is("Bad Request")))
                    .andExpect(jsonPath("$.message", containsString("should be of type")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify database remains unchanged
            assertEquals(2, studentRepository.count());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle unsupported HTTP method")
        void handleUnsupportedHttpMethod_ShouldReturn405() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/students/{id}", student1.getId()))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.status", is(405)))
                    .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                    .andExpect(jsonPath("$.message", containsString("not supported")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }
        
        @Test
        @DisplayName("Should handle unsupported media type")
        void handleUnsupportedMediaType_ShouldReturn415() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/students")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("This is plain text"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status", is(415)))
                    .andExpect(jsonPath("$.error", is("Unsupported Media Type")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
            
            // Verify no new student was added
            assertEquals(2, studentRepository.count());
        }
        
        @Test
        @DisplayName("Should handle invalid URL path")
        void handleInvalidUrlPath_ShouldReturn404() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/invalid-path"))
                    .andExpect(status().isNotFound());
        }
    }
}