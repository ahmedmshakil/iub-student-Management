package com.iub.studentmanagement.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iub.studentmanagement.controller.StudentController;
import com.iub.studentmanagement.dto.StudentRequestDTO;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the GlobalExceptionHandler class.
 * Uses MockMvc to test exception handling in the context of HTTP requests.
 */
@WebMvcTest(StudentController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void handleStudentNotFoundException_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(99L)).thenThrow(new StudentNotFoundException(99L));

        // When & Then
        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Student not found with id: 99")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleDuplicateEmailException_ShouldReturnConflict() throws Exception {
        // Given
        StudentRequestDTO studentDTO = new StudentRequestDTO("Test Student", "existing@example.com", "Computer Science");
        when(studentService.createStudent(any(Student.class))).thenThrow(new DuplicateEmailException("existing@example.com"));

        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("already exists")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> errors = new HashMap<>();
        errors.put("email", "Invalid email format");
        errors.put("name", "Name is required");
        
        ValidationException validationException = new ValidationException("Validation failed", errors);
        when(studentService.updateStudent(eq(1L), any(Student.class))).thenThrow(validationException);

        StudentRequestDTO studentDTO = new StudentRequestDTO("", "invalid-email", "Computer Science");

        // When & Then
        mockMvc.perform(put("/api/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors.email", is("Email should be valid")))
                .andExpect(jsonPath("$.fieldErrors.name", is("Name is required")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequest() throws Exception {
        // Given
        StudentRequestDTO invalidStudent = new StudentRequestDTO("", "", "");

        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors", aMapWithSize(greaterThan(0))))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/students/abc"))  // 'abc' is not a valid Long ID
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("should be of type")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleHttpRequestMethodNotSupported_ShouldReturnMethodNotAllowed() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/students/1"))  // PATCH is not supported
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status", is(405)))
                .andExpect(jsonPath("$.error", is("Method Not Allowed")))
                .andExpect(jsonPath("$.message", containsString("not supported")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleHttpMediaTypeNotSupported_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.TEXT_PLAIN)
                .content("This is plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status", is(415)))
                .andExpect(jsonPath("$.error", is("Unsupported Media Type")))
                .andExpect(jsonPath("$.message", containsString("not supported")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnConflict() throws Exception {
        // Given
        StudentRequestDTO studentDTO = new StudentRequestDTO("Test Student", "test@example.com", "Computer Science");
        when(studentService.createStudent(any(Student.class))).thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(studentService.getAllStudents()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}