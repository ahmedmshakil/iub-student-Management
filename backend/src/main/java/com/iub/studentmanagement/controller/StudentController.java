package com.iub.studentmanagement.controller;

import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
     * @return ResponseEntity containing a list of all students
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/students/{id} - Retrieve a specific student by ID
     * 
     * @param id the ID of the student to retrieve
     * @return ResponseEntity containing the student if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    /**
     * POST /api/students - Create a new student
     * 
     * @param student the student data to create
     * @return ResponseEntity containing the created student with status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    /**
     * PUT /api/students/{id} - Update an existing student
     * 
     * @param id the ID of the student to update
     * @param student the updated student data
     * @return ResponseEntity containing the updated student
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(id, student);
        return ResponseEntity.ok(updatedStudent);
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
     * @return ResponseEntity containing a list of students in the specified department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Student>> getStudentsByDepartment(@PathVariable String department) {
        List<Student> students = studentService.getStudentsByDepartment(department);
        return ResponseEntity.ok(students);
    }
}