package com.iub.studentmanagement.repository;

import com.iub.studentmanagement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the StudentRepository using H2 in-memory database.
 * Tests CRUD operations and custom query methods.
 */
@DataJpaTest
@DisplayName("Student Repository Tests")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student student1;
    private Student student2;
    private Student student3;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        studentRepository.deleteAll();

        // Create test students
        student1 = new Student("John Doe", "john.doe@example.com", "Computer Science");
        student2 = new Student("Jane Smith", "jane.smith@example.com", "Mathematics");
        student3 = new Student("Bob Johnson", "bob.johnson@example.com", "Computer Science");

        // Save test students
        student1 = studentRepository.save(student1);
        student2 = studentRepository.save(student2);
        student3 = studentRepository.save(student3);
    }

    @Test
    @DisplayName("Should find all students")
    void shouldFindAllStudents() {
        // When
        List<Student> students = studentRepository.findAll();

        // Then
        assertEquals(3, students.size(), "Should find all 3 students");
        assertTrue(students.stream().anyMatch(s -> s.getEmail().equals("john.doe@example.com")));
        assertTrue(students.stream().anyMatch(s -> s.getEmail().equals("jane.smith@example.com")));
        assertTrue(students.stream().anyMatch(s -> s.getEmail().equals("bob.johnson@example.com")));
    }

    @Test
    @DisplayName("Should find student by ID")
    void shouldFindStudentById() {
        // When
        Optional<Student> foundStudent = studentRepository.findById(student1.getId());

        // Then
        assertTrue(foundStudent.isPresent(), "Student should be found by ID");
        assertEquals("John Doe", foundStudent.get().getName());
        assertEquals("john.doe@example.com", foundStudent.get().getEmail());
    }

    @Test
    @DisplayName("Should not find student with non-existent ID")
    void shouldNotFindStudentWithNonExistentId() {
        // When
        Optional<Student> foundStudent = studentRepository.findById(999L);

        // Then
        assertTrue(foundStudent.isEmpty(), "Student should not be found with non-existent ID");
    }

    @Test
    @DisplayName("Should save new student")
    void shouldSaveNewStudent() {
        // Given
        Student newStudent = new Student("Alice Brown", "alice.brown@example.com", "Physics");

        // When
        Student savedStudent = studentRepository.save(newStudent);

        // Then
        assertNotNull(savedStudent.getId(), "Saved student should have an ID");
        assertEquals("Alice Brown", savedStudent.getName());
        assertEquals("alice.brown@example.com", savedStudent.getEmail());
        assertEquals("Physics", savedStudent.getDepartment());
        assertNotNull(savedStudent.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(savedStudent.getUpdatedAt(), "Updated timestamp should be set");

        // Verify it was actually saved to the database
        Optional<Student> foundStudent = studentRepository.findById(savedStudent.getId());
        assertTrue(foundStudent.isPresent(), "Student should be found in the database");
    }

    @Test
    @DisplayName("Should update existing student")
    void shouldUpdateExistingStudent() {
        // Given
        student1.setName("John Updated");
        student1.setDepartment("Information Technology");

        // When
        Student updatedStudent = studentRepository.save(student1);

        // Then
        assertEquals(student1.getId(), updatedStudent.getId(), "ID should remain the same");
        assertEquals("John Updated", updatedStudent.getName(), "Name should be updated");
        assertEquals("Information Technology", updatedStudent.getDepartment(), "Department should be updated");
        assertEquals("john.doe@example.com", updatedStudent.getEmail(), "Email should remain the same");

        // Verify it was actually updated in the database
        Optional<Student> foundStudent = studentRepository.findById(student1.getId());
        assertTrue(foundStudent.isPresent(), "Student should be found in the database");
        assertEquals("John Updated", foundStudent.get().getName());
        assertEquals("Information Technology", foundStudent.get().getDepartment());
    }

    @Test
    @DisplayName("Should delete student")
    void shouldDeleteStudent() {
        // Given
        Long studentId = student1.getId();

        // When
        studentRepository.deleteById(studentId);

        // Then
        Optional<Student> foundStudent = studentRepository.findById(studentId);
        assertTrue(foundStudent.isEmpty(), "Student should be deleted from the database");
        assertEquals(2, studentRepository.count(), "Should have 2 students remaining");
    }

    @Test
    @DisplayName("Should find students by department")
    void shouldFindStudentsByDepartment() {
        // When
        List<Student> computerScienceStudents = studentRepository.findByDepartment("Computer Science");
        List<Student> mathematicsStudents = studentRepository.findByDepartment("Mathematics");
        List<Student> physicsStudents = studentRepository.findByDepartment("Physics");

        // Then
        assertEquals(2, computerScienceStudents.size(), "Should find 2 Computer Science students");
        assertEquals(1, mathematicsStudents.size(), "Should find 1 Mathematics student");
        assertEquals(0, physicsStudents.size(), "Should find 0 Physics students");

        assertTrue(computerScienceStudents.stream()
                .anyMatch(s -> s.getEmail().equals("john.doe@example.com")));
        assertTrue(computerScienceStudents.stream()
                .anyMatch(s -> s.getEmail().equals("bob.johnson@example.com")));
        assertTrue(mathematicsStudents.stream()
                .anyMatch(s -> s.getEmail().equals("jane.smith@example.com")));
    }

    @Test
    @DisplayName("Should find student by email")
    void shouldFindStudentByEmail() {
        // When
        Optional<Student> foundStudent = studentRepository.findByEmail("jane.smith@example.com");

        // Then
        assertTrue(foundStudent.isPresent(), "Student should be found by email");
        assertEquals("Jane Smith", foundStudent.get().getName());
        assertEquals("Mathematics", foundStudent.get().getDepartment());
    }

    @Test
    @DisplayName("Should not find student with non-existent email")
    void shouldNotFindStudentWithNonExistentEmail() {
        // When
        Optional<Student> foundStudent = studentRepository.findByEmail("nonexistent@example.com");

        // Then
        assertTrue(foundStudent.isEmpty(), "Student should not be found with non-existent email");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // When
        boolean existingEmail = studentRepository.existsByEmail("john.doe@example.com");
        boolean nonExistingEmail = studentRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertTrue(existingEmail, "Should return true for existing email");
        assertFalse(nonExistingEmail, "Should return false for non-existing email");
    }
    
    @Test
    @DisplayName("Should enforce email uniqueness constraint")
    void shouldEnforceEmailUniquenessConstraint() {
        // Given
        Student duplicateEmailStudent = new Student("Another John", "john.doe@example.com", "Physics");
        
        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            studentRepository.save(duplicateEmailStudent);
            studentRepository.flush(); // Force the persistence context to flush
        }, "Should throw exception for duplicate email");
    }
    
    @Test
    @DisplayName("Should support pagination")
    void shouldSupportPagination() {
        // Given
        for (int i = 0; i < 20; i++) {
            studentRepository.save(new Student(
                "Student " + i,
                "student" + i + "@example.com",
                i % 2 == 0 ? "Computer Science" : "Mathematics"
            ));
        }
        
        // When
        Pageable firstPageWithTwoElements = PageRequest.of(0, 2);
        Page<Student> firstPage = studentRepository.findAll(firstPageWithTwoElements);
        
        Pageable secondPageWithTwoElements = PageRequest.of(1, 2);
        Page<Student> secondPage = studentRepository.findAll(secondPageWithTwoElements);
        
        // Then
        assertEquals(2, firstPage.getContent().size(), "First page should have 2 elements");
        assertEquals(2, secondPage.getContent().size(), "Second page should have 2 elements");
        assertNotEquals(
            firstPage.getContent().get(0).getId(),
            secondPage.getContent().get(0).getId(),
            "Pages should contain different students"
        );
        assertTrue(firstPage.hasNext(), "Should have next page");
        assertEquals(23, firstPage.getTotalElements(), "Total elements should be 23 (20 new + 3 from setup)");
    }
    
    @Test
    @DisplayName("Should support sorting")
    void shouldSupportSorting() {
        // Given
        studentRepository.save(new Student("Aaron Adams", "aaron@example.com", "Physics"));
        studentRepository.save(new Student("Zack Zane", "zack@example.com", "Biology"));
        
        // When
        List<Student> ascendingByName = studentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<Student> descendingByName = studentRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));
        
        // Then
        assertEquals("Aaron Adams", ascendingByName.get(0).getName(), "First student should be Aaron Adams when sorted ascending");
        assertEquals("Zack Zane", descendingByName.get(0).getName(), "First student should be Zack Zane when sorted descending");
    }
    
    @Test
    @DisplayName("Should handle batch delete operations")
    void shouldHandleBatchDeleteOperations() {
        // Given
        List<Student> computerScienceStudents = studentRepository.findByDepartment("Computer Science");
        
        // When
        studentRepository.deleteAll(computerScienceStudents);
        
        // Then
        List<Student> remainingStudents = studentRepository.findAll();
        assertEquals(1, remainingStudents.size(), "Should have 1 student remaining");
        assertEquals("jane.smith@example.com", remainingStudents.get(0).getEmail(), "Remaining student should be Jane Smith");
        assertEquals(0, studentRepository.findByDepartment("Computer Science").size(), "Should have no Computer Science students");
    }
    
    @Test
    @DisplayName("Should handle count operations")
    void shouldHandleCountOperations() {
        // When
        long totalCount = studentRepository.count();
        
        // Then
        assertEquals(3, totalCount, "Should have 3 students in total");
    }
    
    @Test
    @DisplayName("Should handle exists by ID check")
    void shouldHandleExistsByIdCheck() {
        // When
        boolean existsById = studentRepository.existsById(student1.getId());
        boolean notExistsById = studentRepository.existsById(999L);
        
        // Then
        assertTrue(existsById, "Should return true for existing ID");
        assertFalse(notExistsById, "Should return false for non-existing ID");
    }
}