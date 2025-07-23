package com.iub.studentmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
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

    @BeforeEach
    void setUp() {
        student1 = new Student("John Doe", "john.doe@example.com", "Computer Science");
        student1.setId(1L);
        
        student2 = new Student("Jane Smith", "jane.smith@example.com", "Mathematics");
        student2.setId(2L);
        
        studentList = Arrays.asList(student1, student2);
    }

    @Test
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
    void getStudentById_WithInvalidId_ShouldReturn404() throws Exception {
        when(studentService.getStudentById(99L)).thenThrow(new StudentNotFoundException(99L));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound());

        verify(studentService).getStudentById(99L);
    }

    @Test
    void createStudent_WithValidData_ShouldReturnCreatedStudent() throws Exception {
        Student newStudent = new Student("New Student", "new.student@example.com", "Physics");
        Student savedStudent = new Student("New Student", "new.student@example.com", "Physics");
        savedStudent.setId(3L);

        when(studentService.createStudent(any(Student.class))).thenReturn(savedStudent);

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Student")))
                .andExpect(jsonPath("$.email", is("new.student@example.com")))
                .andExpect(jsonPath("$.department", is("Physics")));

        verify(studentService).createStudent(any(Student.class));
    }

    @Test
    void createStudent_WithDuplicateEmail_ShouldReturn400() throws Exception {
        Student newStudent = new Student("Duplicate Email", "john.doe@example.com", "Chemistry");

        when(studentService.createStudent(any(Student.class)))
                .thenThrow(new DuplicateEmailException("john.doe@example.com"));

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isBadRequest());

        verify(studentService).createStudent(any(Student.class));
    }

    @Test
    void updateStudent_WithValidData_ShouldReturnUpdatedStudent() throws Exception {
        Student updatedStudent = new Student("Updated John", "john.doe@example.com", "Data Science");
        updatedStudent.setId(1L);

        when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(updatedStudent);

        mockMvc.perform(put("/api/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated John")))
                .andExpect(jsonPath("$.department", is("Data Science")));

        verify(studentService).updateStudent(eq(1L), any(Student.class));
    }

    @Test
    void updateStudent_WithInvalidId_ShouldReturn404() throws Exception {
        Student updatedStudent = new Student("Updated Student", "updated@example.com", "Chemistry");

        when(studentService.updateStudent(eq(99L), any(Student.class)))
                .thenThrow(new StudentNotFoundException(99L));

        mockMvc.perform(put("/api/students/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedStudent)))
                .andExpect(status().isNotFound());

        verify(studentService).updateStudent(eq(99L), any(Student.class));
    }

    @Test
    void deleteStudent_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(1L);
    }

    @Test
    void deleteStudent_WithInvalidId_ShouldReturn404() throws Exception {
        doThrow(new StudentNotFoundException(99L)).when(studentService).deleteStudent(99L);

        mockMvc.perform(delete("/api/students/99"))
                .andExpect(status().isNotFound());

        verify(studentService).deleteStudent(99L);
    }

    @Test
    void getStudentsByDepartment_ShouldReturnFilteredStudents() throws Exception {
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
}