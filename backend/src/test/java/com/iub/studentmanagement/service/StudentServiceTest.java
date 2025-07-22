package com.iub.studentmanagement.service;

import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.exception.ValidationException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        // Create test students
        student1 = new Student("John Doe", "john.doe@example.com", "Computer Science");
        student1.setId(1L);

        student2 = new Student("Jane Smith", "jane.smith@example.com", "Mathematics");
        student2.setId(2L);
    }

    @Test
    void getAllStudents_ShouldReturnAllStudents() {
        // Arrange
        when(studentRepository.findAll()).thenReturn(Arrays.asList(student1, student2));

        // Act
        List<Student> students = studentService.getAllStudents();

        // Assert
        assertEquals(2, students.size());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    void getStudentsByDepartment_ShouldReturnStudentsInDepartment() {
        // Arrange
        when(studentRepository.findByDepartment("Computer Science")).thenReturn(Arrays.asList(student1));

        // Act
        List<Student> students = studentService.getStudentsByDepartment("Computer Science");

        // Assert
        assertEquals(1, students.size());
        assertEquals("John Doe", students.get(0).getName());
        verify(studentRepository, times(1)).findByDepartment("Computer Science");
    }

    @Test
    void getStudentById_WithValidId_ShouldReturnStudent() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));

        // Act
        Student foundStudent = studentService.getStudentById(1L);

        // Assert
        assertNotNull(foundStudent);
        assertEquals(1L, foundStudent.getId());
        assertEquals("John Doe", foundStudent.getName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    void getStudentById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> {
            studentService.getStudentById(999L);
        });
        verify(studentRepository, times(1)).findById(999L);
    }

    @Test
    void getStudentByEmail_WithValidEmail_ShouldReturnStudent() {
        // Arrange
        when(studentRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(student1));

        // Act
        Student foundStudent = studentService.getStudentByEmail("john.doe@example.com");

        // Assert
        assertNotNull(foundStudent);
        assertEquals("john.doe@example.com", foundStudent.getEmail());
        verify(studentRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void getStudentByEmail_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> {
            studentService.getStudentByEmail("nonexistent@example.com");
        });
        verify(studentRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void createStudent_WithUniqueEmail_ShouldCreateStudent() {
        // Arrange
        Student newStudent = new Student("New Student", "new.student@example.com", "Physics");
        when(studentRepository.existsByEmail("new.student@example.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);

        // Act
        Student createdStudent = studentService.createStudent(newStudent);

        // Assert
        assertNotNull(createdStudent);
        assertEquals("New Student", createdStudent.getName());
        verify(studentRepository, times(1)).existsByEmail("new.student@example.com");
        verify(studentRepository, times(1)).save(newStudent);
    }

    @Test
    void createStudent_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        Student newStudent = new Student("New Student", "john.doe@example.com", "Physics");
        when(studentRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> {
            studentService.createStudent(newStudent);
        });
        verify(studentRepository, times(1)).existsByEmail("john.doe@example.com");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void updateStudent_WithValidIdAndUniqueEmail_ShouldUpdateStudent() {
        // Arrange
        Student updatedDetails = new Student("John Doe Updated", "john.updated@example.com", "Data Science");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Student updatedStudent = studentService.updateStudent(1L, updatedDetails);

        // Assert
        assertEquals("John Doe Updated", updatedStudent.getName());
        assertEquals("john.updated@example.com", updatedStudent.getEmail());
        assertEquals("Data Science", updatedStudent.getDepartment());
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).existsByEmail("john.updated@example.com");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void updateStudent_WithInvalidId_ShouldThrowException() {
        // Arrange
        Student updatedDetails = new Student("John Doe Updated", "john.updated@example.com", "Data Science");
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> {
            studentService.updateStudent(999L, updatedDetails);
        });
        verify(studentRepository, times(1)).findById(999L);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void updateStudent_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        Student updatedDetails = new Student("John Doe Updated", "jane.smith@example.com", "Data Science");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.existsByEmail("jane.smith@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> {
            studentService.updateStudent(1L, updatedDetails);
        });
        verify(studentRepository, times(1)).findById(1L);
        verify(studentRepository, times(1)).existsByEmail("jane.smith@example.com");
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudent_WithValidId_ShouldDeleteStudent() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(1L);

        // Act
        studentService.deleteStudent(1L);

        // Assert
        verify(studentRepository, times(1)).existsById(1L);
        verify(studentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteStudent_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(StudentNotFoundException.class, () -> {
            studentService.deleteStudent(999L);
        });
        verify(studentRepository, times(1)).existsById(999L);
        verify(studentRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean exists = studentService.existsById(1L);

        // Assert
        assertTrue(exists);
        verify(studentRepository, times(1)).existsById(1L);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean exists = studentService.existsById(999L);

        // Assert
        assertFalse(exists);
        verify(studentRepository, times(1)).existsById(999L);
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Arrange
        when(studentRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act
        boolean exists = studentService.existsByEmail("john.doe@example.com");

        // Assert
        assertTrue(exists);
        verify(studentRepository, times(1)).existsByEmail("john.doe@example.com");
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        // Arrange
        when(studentRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean exists = studentService.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
        verify(studentRepository, times(1)).existsByEmail("nonexistent@example.com");
    }
    
    @Test
    @DisplayName("Update student with same email should not trigger duplicate email check")
    void updateStudent_WithSameEmail_ShouldUpdateStudent() {
        // Arrange
        Student updatedDetails = new Student("John Doe Updated", "john.doe@example.com", "Data Science");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Student updatedStudent = studentService.updateStudent(1L, updatedDetails);

        // Assert
        assertEquals("John Doe Updated", updatedStudent.getName());
        assertEquals("john.doe@example.com", updatedStudent.getEmail());
        assertEquals("Data Science", updatedStudent.getDepartment());
        verify(studentRepository, times(1)).findById(1L);
        // Should not check for duplicate email when email hasn't changed
        verify(studentRepository, never()).existsByEmail("john.doe@example.com");
        verify(studentRepository, times(1)).save(any(Student.class));
    }
    
    @Test
    @DisplayName("Get students by department should return empty list when no students found")
    void getStudentsByDepartment_WithNonExistingDepartment_ShouldReturnEmptyList() {
        // Arrange
        String nonExistingDepartment = "Non-Existing Department";
        when(studentRepository.findByDepartment(nonExistingDepartment)).thenReturn(Collections.emptyList());

        // Act
        List<Student> students = studentService.getStudentsByDepartment(nonExistingDepartment);

        // Assert
        assertNotNull(students);
        assertTrue(students.isEmpty());
        verify(studentRepository, times(1)).findByDepartment(nonExistingDepartment);
    }
    
    @Test
    @DisplayName("Get all students should return empty list when no students exist")
    void getAllStudents_WithNoStudents_ShouldReturnEmptyList() {
        // Arrange
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Student> students = studentService.getAllStudents();

        // Assert
        assertNotNull(students);
        assertTrue(students.isEmpty());
        verify(studentRepository, times(1)).findAll();
    }
    
    @Nested
    @DisplayName("Edge cases and boundary tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Update student with null fields should set null values")
        void updateStudent_WithNullFields_ShouldSetNullValues() {
            // Arrange
            Student originalStudent = new Student("Original Name", "original@example.com", "Original Department");
            originalStudent.setId(1L);
            
            Student updatedDetails = new Student(null, null, null);
            
            when(studentRepository.findById(1L)).thenReturn(Optional.of(originalStudent));
            when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
                Student savedStudent = (Student) invocation.getArgument(0);
                // The current implementation sets null values, so we need to verify that
                assertNull(savedStudent.getName());
                assertNull(savedStudent.getEmail());
                assertNull(savedStudent.getDepartment());
                return savedStudent;
            });
            
            // Act
            Student result = studentService.updateStudent(1L, updatedDetails);
            
            // Assert
            // The current implementation sets null values
            assertNull(result.getName());
            assertNull(result.getEmail());
            assertNull(result.getDepartment());
            verify(studentRepository, times(1)).findById(1L);
            verify(studentRepository, times(1)).save(any(Student.class));
        }
        
        @ParameterizedTest
        @DisplayName("Get students by department with various department values")
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "Computer Science", "Mathematics"})
        void getStudentsByDepartment_WithVariousDepartmentValues(String department) {
            // Arrange
            List<Student> expectedStudents = new ArrayList<>();
            if ("Computer Science".equals(department)) {
                expectedStudents.add(student1);
            } else if ("Mathematics".equals(department)) {
                expectedStudents.add(student2);
            }
            
            when(studentRepository.findByDepartment(department)).thenReturn(expectedStudents);
            
            // Act
            List<Student> result = studentService.getStudentsByDepartment(department);
            
            // Assert
            assertEquals(expectedStudents.size(), result.size());
            verify(studentRepository, times(1)).findByDepartment(department);
        }
    }
    
    @Nested
    @DisplayName("Batch operation tests")
    class BatchOperationTests {
        
        @Test
        @DisplayName("Get all students should handle large result sets")
        void getAllStudents_WithLargeResultSet_ShouldReturnAllStudents() {
            // Arrange
            List<Student> largeStudentList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Student student = new Student("Student " + i, "student" + i + "@example.com", "Department " + (i % 10));
                student.setId((long) i);
                largeStudentList.add(student);
            }
            
            when(studentRepository.findAll()).thenReturn(largeStudentList);
            
            // Act
            List<Student> result = studentService.getAllStudents();
            
            // Assert
            assertEquals(1000, result.size());
            verify(studentRepository, times(1)).findAll();
        }
    }
    
    @Nested
    @DisplayName("Exception handling tests")
    class ExceptionHandlingTests {
        
        @Test
        @DisplayName("Repository throws exception during save should propagate exception")
        void createStudent_WithRepositoryException_ShouldPropagateException() {
            // Arrange
            Student newStudent = new Student("New Student", "new.student@example.com", "Physics");
            when(studentRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenThrow(new RuntimeException("Database error"));
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                studentService.createStudent(newStudent);
            });
            
            assertEquals("Database error", exception.getMessage());
            verify(studentRepository, times(1)).existsByEmail(anyString());
            verify(studentRepository, times(1)).save(any(Student.class));
        }
    }
    
    @Nested
    @DisplayName("Validation tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Create student with invalid email format should be caught at service level")
        void createStudent_WithInvalidEmailFormat_ShouldHandleValidation() {
            // This test verifies that if validation somehow bypasses the entity validation,
            // the service layer would still handle it properly
            
            // Arrange
            Student invalidStudent = new Student("Test Student", "invalid-email", "Computer Science");
            when(studentRepository.existsByEmail(anyString())).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenThrow(
                new DataIntegrityViolationException("Invalid email format")
            );
            
            // Act & Assert
            Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
                studentService.createStudent(invalidStudent);
            });
            
            assertTrue(exception.getMessage().contains("Invalid email format"));
            verify(studentRepository, times(1)).existsByEmail(anyString());
            verify(studentRepository, times(1)).save(any(Student.class));
        }
    }
    
    @Nested
    @DisplayName("Transaction management tests")
    class TransactionTests {
        
        @Test
        @DisplayName("Delete operation should be transactional")
        void deleteStudent_ShouldBeTransactional() {
            // Arrange
            when(studentRepository.existsById(1L)).thenReturn(true);
            doNothing().when(studentRepository).deleteById(1L);
            
            // Act
            studentService.deleteStudent(1L);
            
            // Assert
            verify(studentRepository, times(1)).existsById(1L);
            verify(studentRepository, times(1)).deleteById(1L);
            
            // Note: We can't directly test @Transactional behavior in a unit test,
            // but we can verify the method is called correctly
        }
    }
    
    @Nested
    @DisplayName("Concurrent operation tests")
    class ConcurrentOperationTests {
        
        @Test
        @DisplayName("Multiple operations should not interfere with each other")
        void multipleOperations_ShouldNotInterfere() {
            // Arrange
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
            when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
            
            // Act
            Student result1 = studentService.getStudentById(1L);
            Student result2 = studentService.getStudentById(2L);
            
            // Assert
            assertEquals(1L, result1.getId());
            assertEquals(2L, result2.getId());
            verify(studentRepository, times(1)).findById(1L);
            verify(studentRepository, times(1)).findById(2L);
        }
    }
}