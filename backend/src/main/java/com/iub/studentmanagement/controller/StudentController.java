package com.iub.studentmanagement.controller;

import com.iub.studentmanagement.dto.StudentRequestDTO;
import com.iub.studentmanagement.dto.StudentResponseDTO;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for handling student-related HTTP requests.
 * Provides endpoints for CRUD operations on student resources.
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*") // This will be configured properly in production
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * GET /api/students - Retrieve all students
     * 
     * @return ResponseEntity containing a list of all students as DTOs
     */
    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        List<StudentResponseDTO> studentDTOs = students.stream()
                .map(StudentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(studentDTOs);
    }

    /**
     * GET /api/students/{id} - Retrieve a specific student by ID
     * 
     * @param id the ID of the student to retrieve
     * @return ResponseEntity containing the student DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        StudentResponseDTO studentDTO = StudentResponseDTO.fromEntity(student);
        return ResponseEntity.ok(studentDTO);
    }

    /**
     * POST /api/students - Create a new student
     * 
     * @param studentRequestDTO the student data to create
     * @return ResponseEntity containing the created student DTO with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO studentRequestDTO) {
        // Convert DTO to entity
        Student student = new Student(
            studentRequestDTO.getName(),
            studentRequestDTO.getEmail(),
            studentRequestDTO.getDepartment()
        );
        
        // Create student and convert back to response DTO
        Student createdStudent = studentService.createStudent(student);
        StudentResponseDTO responseDTO = StudentResponseDTO.fromEntity(createdStudent);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * PUT /api/students/{id} - Update an existing student
     * 
     * @param id the ID of the student to update
     * @param studentRequestDTO the updated student data
     * @return ResponseEntity containing the updated student DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDTO studentRequestDTO) {
        // Convert DTO to entity
        Student studentDetails = new Student(
            studentRequestDTO.getName(),
            studentRequestDTO.getEmail(),
            studentRequestDTO.getDepartment()
        );
        
        // Update student and convert back to response DTO
        Student updatedStudent = studentService.updateStudent(id, studentDetails);
        StudentResponseDTO responseDTO = StudentResponseDTO.fromEntity(updatedStudent);
        
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * DELETE /api/students/{id} - Delete a student
     * 
     * @param id the ID of the student to delete
     * @return ResponseEntity with no content and status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/students/department/{department} - Retrieve students by department
     * 
     * @param department the department to filter by
     * @return ResponseEntity containing a list of students in the specified department as DTOs
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByDepartment(@PathVariable String department) {
        List<Student> students = studentService.getStudentsByDepartment(department);
        List<StudentResponseDTO> studentDTOs = students.stream()
                .map(StudentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(studentDTOs);
    }
}