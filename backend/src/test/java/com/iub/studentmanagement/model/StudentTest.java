package com.iub.studentmanagement.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Student entity class.
 * Tests validation constraints and entity behavior.
 */
@DisplayName("Student Entity Tests")
class StudentTest {

    private Validator validator;
    private Student student;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid student for tests
        student = new Student(
            "John Doe",
            "john.doe@example.com",
            "Computer Science"
        );
    }

    @Test
    @DisplayName("Should validate when all fields are valid")
    void shouldValidateWhenAllFieldsAreValid() {
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertTrue(violations.isEmpty(), "No violations should be found for a valid student");
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void shouldFailValidationWhenNameIsNull() {
        // Given
        student.setName(null);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when name is empty")
    void shouldFailValidationWhenNameIsEmpty() {
        // Given
        student.setName("");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Name is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when name exceeds max length")
    void shouldFailValidationWhenNameExceedsMaxLength() {
        // Given
        String longName = "A".repeat(101); // 101 characters
        student.setName(longName);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Name cannot exceed 100 characters", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when email is null")
    void shouldFailValidationWhenEmailIsNull() {
        // Given
        student.setEmail(null);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when email is empty")
    void shouldFailValidationWhenEmailIsEmpty() {
        // Given
        student.setEmail("");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Email is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when email is invalid")
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        student.setEmail("invalid-email");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when email exceeds max length")
    void shouldFailValidationWhenEmailExceedsMaxLength() {
        // Given
        String longEmail = "a".repeat(90) + "@example.com"; // More than 100 characters
        student.setEmail(longEmail);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(2, violations.size(), "Should have two violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email cannot exceed 100 characters")),
                "Should have 'Email cannot exceed 100 characters' violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email should be valid")),
                "Should have 'Email should be valid' violation");
    }

    @Test
    @DisplayName("Should fail validation when department is null")
    void shouldFailValidationWhenDepartmentIsNull() {
        // Given
        student.setDepartment(null);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Department is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when department is empty")
    void shouldFailValidationWhenDepartmentIsEmpty() {
        // Given
        student.setDepartment("");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Department is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should fail validation when department exceeds max length")
    void shouldFailValidationWhenDepartmentExceedsMaxLength() {
        // Given
        String longDepartment = "D".repeat(51); // 51 characters
        student.setDepartment(longDepartment);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(1, violations.size(), "Should have one violation");
        assertEquals("Department cannot exceed 50 characters", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Should correctly use constructor with fields")
    void shouldCorrectlyUseConstructorWithFields() {
        // Given
        String name = "Jane Smith";
        String email = "jane.smith@example.com";
        String department = "Mathematics";
        
        // When
        Student newStudent = new Student(name, email, department);
        
        // Then
        assertEquals(name, newStudent.getName());
        assertEquals(email, newStudent.getEmail());
        assertEquals(department, newStudent.getDepartment());
        assertNull(newStudent.getId());
        assertNull(newStudent.getCreatedAt());
        assertNull(newStudent.getUpdatedAt());
    }

    @Test
    @DisplayName("Should correctly use getters and setters")
    void shouldCorrectlyUseGettersAndSetters() {
        // Given
        Long id = 1L;
        String name = "Jane Smith";
        String email = "jane.smith@example.com";
        String department = "Mathematics";
        
        // When
        student.setId(id);
        student.setName(name);
        student.setEmail(email);
        student.setDepartment(department);
        
        // Then
        assertEquals(id, student.getId());
        assertEquals(name, student.getName());
        assertEquals(email, student.getEmail());
        assertEquals(department, student.getDepartment());
    }

    @Test
    @DisplayName("Should correctly implement toString method")
    void shouldCorrectlyImplementToStringMethod() {
        // Given
        student.setId(1L);
        
        // When
        String toString = student.toString();
        
        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='John Doe'"));
        assertTrue(toString.contains("email='john.doe@example.com'"));
        assertTrue(toString.contains("department='Computer Science'"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"john.doe@example.com", "jane.smith@university.edu", "test.user@domain.co.uk"})
    @DisplayName("Should validate various valid email formats")
    void shouldValidateVariousValidEmailFormats(String validEmail) {
        // Given
        student.setEmail(validEmail);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertTrue(violations.isEmpty(), "No violations should be found for valid email: " + validEmail);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "missing-at.com", "@missing-username.com", "spaces in@email.com"})
    @DisplayName("Should fail validation for various invalid email formats")
    void shouldFailValidationForVariousInvalidEmailFormats(String invalidEmail) {
        // Given
        student.setEmail(invalidEmail);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty(), "Violations should be found for invalid email: " + invalidEmail);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email should be valid")),
                "Should have 'Email should be valid' violation");
    }
    
    @Test
    @DisplayName("Should handle timestamp fields correctly")
    void shouldHandleTimestampFieldsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When - Reflection would be used in a real scenario, but for testing purposes we're just verifying the getters
        
        // Then
        assertNull(student.getCreatedAt(), "CreatedAt should be null before persistence");
        assertNull(student.getUpdatedAt(), "UpdatedAt should be null before persistence");
    }
    
    @Test
    @DisplayName("Should validate student with minimum valid data")
    void shouldValidateStudentWithMinimumValidData() {
        // Given
        Student minimalStudent = new Student(
            "A", // Minimum name length
            "a@b.c", // Minimum valid email
            "X" // Minimum department length
        );
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(minimalStudent);
        
        // Then
        assertTrue(violations.isEmpty(), "No violations should be found for minimal valid student");
    }
    
    @Test
    @DisplayName("Should validate student with maximum valid data lengths")
    void shouldValidateStudentWithMaximumValidDataLengths() {
        // Given
        Student maxLengthStudent = new Student(
            "A".repeat(100), // Maximum name length
            "a".repeat(90) + "@b.c", // Maximum email length that's still valid
            "D".repeat(50) // Maximum department length
        );
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(maxLengthStudent);
        
        // Then
        assertTrue(violations.isEmpty(), "No violations should be found for maximum length valid student");
    }
    
    @Test
    @DisplayName("Should fail validation with multiple violations")
    void shouldFailValidationWithMultipleViolations() {
        // Given
        student.setName("");
        student.setEmail("invalid-email");
        student.setDepartment("");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(3, violations.size(), "Should have three violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Name is required")),
                "Should have 'Name is required' violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email should be valid")),
                "Should have 'Email should be valid' violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Department is required")),
                "Should have 'Department is required' violation");
    }
    
    @Test
    @DisplayName("Should validate with whitespace-trimmed values")
    void shouldValidateWithWhitespaceTrimmedValues() {
        // Given
        student.setName("  John Doe  ");
        student.setEmail("  john.doe@example.com  ");
        student.setDepartment("  Computer Science  ");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertTrue(violations.isEmpty(), "No violations should be found for whitespace-trimmed values");
    }
    
    @Test
    @DisplayName("Should fail validation with blank (whitespace-only) values")
    void shouldFailValidationWithBlankValues() {
        // Given
        student.setName("   ");
        student.setDepartment("   ");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertEquals(2, violations.size(), "Should have two violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Name is required")),
                "Should have 'Name is required' violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Department is required")),
                "Should have 'Department is required' violation");
    }
}