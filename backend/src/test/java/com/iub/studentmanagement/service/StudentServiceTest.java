package com.iub.studentmanagement.service;

import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
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
}