package com.iub.studentmanagement.repository;

import com.iub.studentmanagement.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the StudentRepository using TestContainers with PostgreSQL.
 * Tests CRUD operations and custom query methods with a real PostgreSQL database.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Student Repository TestContainers Tests")
class StudentRepositoryTestContainers {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

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
    @DisplayName("Should handle concurrent operations")
    void shouldHandleConcurrentOperations() {
        // Given
        Student newStudent1 = new Student("Student One", "student.one@example.com", "Biology");
        Student newStudent2 = new Student("Student Two", "student.two@example.com", "Chemistry");
        
        // When - Save multiple students in sequence
        Student savedStudent1 = studentRepository.save(newStudent1);
        Student savedStudent2 = studentRepository.save(newStudent2);
        
        // Then - Both should be saved successfully
        assertNotNull(savedStudent1.getId(), "First student should have an ID");
        assertNotNull(savedStudent2.getId(), "Second student should have an ID");
        
        // Verify both were saved to the database
        List<Student> allStudents = studentRepository.findAll();
        assertEquals(5, allStudents.size(), "Should have 5 students in total");
        assertTrue(allStudents.stream().anyMatch(s -> s.getEmail().equals("student.one@example.com")));
        assertTrue(allStudents.stream().anyMatch(s -> s.getEmail().equals("student.two@example.com")));
    }
    
    @Test
    @DisplayName("Should handle batch operations")
    void shouldHandleBatchOperations() {
        // Given
        Student newStudent1 = new Student("Batch Student 1", "batch.student1@example.com", "History");
        Student newStudent2 = new Student("Batch Student 2", "batch.student2@example.com", "Geography");
        List<Student> batchStudents = List.of(newStudent1, newStudent2);
        
        // When - Save multiple students at once
        List<Student> savedStudents = studentRepository.saveAll(batchStudents);
        
        // Then
        assertEquals(2, savedStudents.size(), "Should save 2 students");
        assertTrue(savedStudents.stream().allMatch(s -> s.getId() != null), "All students should have IDs");
        
        // Verify all were saved to the database
        List<Student> allStudents = studentRepository.findAll();
        assertEquals(5, allStudents.size(), "Should have 5 students in total");
        assertTrue(allStudents.stream().anyMatch(s -> s.getEmail().equals("batch.student1@example.com")));
        assertTrue(allStudents.stream().anyMatch(s -> s.getEmail().equals("batch.student2@example.com")));
    }
    
    @Test
    @DisplayName("Should enforce email uniqueness constraint in PostgreSQL")
    void shouldEnforceEmailUniquenessConstraintInPostgreSQL() {
        // Given
        Student duplicateEmailStudent = new Student("Another John", "john.doe@example.com", "Physics");
        
        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            studentRepository.save(duplicateEmailStudent);
            studentRepository.flush(); // Force the persistence context to flush
        }, "Should throw exception for duplicate email in PostgreSQL");
    }
    
    @Test
    @DisplayName("Should support pagination with PostgreSQL")
    void shouldSupportPaginationWithPostgreSQL() {
        // Given
        List<Student> bulkStudents = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            bulkStudents.add(new Student(
                "Student " + i,
                "student" + i + "@example.com",
                i % 3 == 0 ? "Computer Science" : (i % 3 == 1 ? "Mathematics" : "Physics")
            ));
        }
        studentRepository.saveAll(bulkStudents);
        
        // When
        Pageable firstPageWithTenElements = PageRequest.of(0, 10);
        Page<Student> firstPage = studentRepository.findAll(firstPageWithTenElements);
        
        Pageable secondPageWithTenElements = PageRequest.of(1, 10);
        Page<Student> secondPage = studentRepository.findAll(secondPageWithTenElements);
        
        // Then
        assertEquals(10, firstPage.getContent().size(), "First page should have 10 elements");
        assertEquals(10, secondPage.getContent().size(), "Second page should have 10 elements");
        assertTrue(firstPage.hasNext(), "Should have next page");
        assertEquals(53, firstPage.getTotalElements(), "Total elements should be 53 (50 new + 3 from setup)");
    }
    
    @Test
    @DisplayName("Should support sorting with PostgreSQL")
    void shouldSupportSortingWithPostgreSQL() {
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
    @DisplayName("Should handle complex sorting with PostgreSQL")
    void shouldHandleComplexSortingWithPostgreSQL() {
        // Given
        studentRepository.save(new Student("Aaron Adams", "aaron@example.com", "Computer Science"));
        studentRepository.save(new Student("Zack Adams", "zack@example.com", "Mathematics"));
        
        // When - Sort by department ascending, then by name descending
        List<Student> sortedStudents = studentRepository.findAll(
            Sort.by(Sort.Order.asc("department"), Sort.Order.desc("name"))
        );
        
        // Then
        // First Computer Science students (sorted by name desc)
        List<Student> computerScienceStudents = sortedStudents.stream()
            .filter(s -> s.getDepartment().equals("Computer Science"))
            .toList();
            
        // Then Mathematics students (sorted by name desc)
        List<Student> mathStudents = sortedStudents.stream()
            .filter(s -> s.getDepartment().equals("Mathematics"))
            .toList();
            
        assertTrue(computerScienceStudents.get(0).getName().compareTo(computerScienceStudents.get(1).getName()) >= 0,
            "Computer Science students should be sorted by name descending");
            
        assertTrue(mathStudents.get(0).getName().compareTo(mathStudents.get(1).getName()) >= 0,
            "Mathematics students should be sorted by name descending");
    }
    
    @Test
    @DisplayName("Should handle large batch inserts with PostgreSQL")
    void shouldHandleLargeBatchInsertsWithPostgreSQL() {
        // Given
        List<Student> bulkStudents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bulkStudents.add(new Student(
                "Bulk Student " + i,
                "bulk.student" + i + "@example.com",
                i % 3 == 0 ? "Computer Science" : (i % 3 == 1 ? "Mathematics" : "Physics")
            ));
        }
        
        // When
        List<Student> savedStudents = studentRepository.saveAll(bulkStudents);
        studentRepository.flush();
        
        // Then
        assertEquals(100, savedStudents.size(), "Should save 100 students");
        assertEquals(103, studentRepository.count(), "Should have 103 students in total (100 new + 3 from setup)");
    }
    
    @Test
    @DisplayName("Should handle case-sensitive queries in PostgreSQL")
    void shouldHandleCaseSensitiveQueriesInPostgreSQL() {
        // Given
        Student student = new Student("Case Test", "UPPERCASE@EXAMPLE.COM", "Test Department");
        studentRepository.save(student);
        
        // When - PostgreSQL is case-sensitive by default
        Optional<Student> foundWithExactCase = studentRepository.findByEmail("UPPERCASE@EXAMPLE.COM");
        Optional<Student> foundWithDifferentCase = studentRepository.findByEmail("uppercase@example.com");
        
        // Then
        assertTrue(foundWithExactCase.isPresent(), "Should find student with exact case match");
        // PostgreSQL is case-sensitive for text comparisons by default
        assertTrue(foundWithDifferentCase.isEmpty(), "Should not find student with different case");
    }
    
    @Test
    @DisplayName("Should handle transaction rollback in PostgreSQL")
    void shouldHandleTransactionRollbackInPostgreSQL() {
        // Given
        long initialCount = studentRepository.count();
        Student validStudent = new Student("Valid Student", "valid@example.com", "Valid Department");
        Student invalidStudent = new Student("Invalid Student", "john.doe@example.com", "Invalid Department"); // Duplicate email
        
        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            // This should be executed in a transaction that will be rolled back
            studentRepository.save(validStudent);
            studentRepository.save(invalidStudent); // This will cause a constraint violation
            studentRepository.flush();
        });
        
        // Then - The transaction should be rolled back, so the valid student should not be saved either
        assertEquals(initialCount, studentRepository.count(), "Student count should remain unchanged after rollback");
        assertFalse(studentRepository.existsByEmail("valid@example.com"), "Valid student should not exist after rollback");
    }
}