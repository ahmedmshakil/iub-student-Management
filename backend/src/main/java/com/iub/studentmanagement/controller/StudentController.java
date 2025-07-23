package com.iub.studentmanagement.controller;

import com.iub.studentmanagement.dto.StudentRequestDTO;
import com.iub.studentmanagement.dto.StudentResponseDTO;
import com.iub.studentmanagement.exception.DuplicateEmailException;
import com.iub.studentmanagement.exception.GlobalExceptionHandler.ErrorResponse;
import com.iub.studentmanagement.exception.StudentNotFoundException;
import com.iub.studentmanagement.exception.ValidationException;
import com.iub.studentmanagement.model.Student;
import com.iub.studentmanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Student Management", description = "APIs for managing student records")
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
    @Operation(
        summary = "Get all students",
        description = "Retrieves a list of all students in the system. Returns an empty list if no students exist.",
        tags = {"Student Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved student list",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = StudentResponseDTO.class)),
                examples = {
                    @ExampleObject(
                        name = "typical-response",
                        summary = "Typical response with multiple students",
                        value = "[{\"id\":1,\"name\":\"John Doe\",\"email\":\"john.doe@iub.edu\",\"department\":\"Computer Science\",\"createdAt\":\"2023-01-15T10:30:00\",\"updatedAt\":\"2023-01-15T10:30:00\"},{\"id\":2,\"name\":\"Jane Smith\",\"email\":\"jane.smith@iub.edu\",\"department\":\"Mathematics\",\"createdAt\":\"2023-01-16T09:15:00\",\"updatedAt\":\"2023-01-16T09:15:00\"}]"
                    ),
                    @ExampleObject(
                        name = "empty-response",
                        summary = "Empty response when no students exist",
                        value = "[]"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students\"}"
                    )
                }
            )
        )
    })
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
    @Operation(
        summary = "Get student by ID",
        description = "Retrieves a specific student by their unique identifier. Returns detailed information about the student.",
        tags = {"Student Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved student details",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StudentResponseDTO.class),
                examples = {
                    @ExampleObject(
                        name = "student-example",
                        summary = "Example student response",
                        value = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john.doe@iub.edu\",\"department\":\"Computer Science\",\"createdAt\":\"2023-01-15T10:30:00\",\"updatedAt\":\"2023-01-15T10:30:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Student not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "not-found-example",
                        summary = "Student not found response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Student with ID 999 not found\",\"path\":\"/api/students/999\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students/1\"}"
                    )
                }
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudentById(
            @Parameter(description = "ID of the student to retrieve", required = true)
            @PathVariable Long id) {
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
    @Operation(
        summary = "Create a new student",
        description = "Creates a new student record with the provided information. The email must be unique across all students.",
        tags = {"Student Management"},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Student information to create a new record",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StudentRequestDTO.class),
                examples = {
                    @ExampleObject(
                        name = "valid-student",
                        summary = "Valid student creation request",
                        value = "{\"name\":\"John Doe\",\"email\":\"john.doe@iub.edu\",\"department\":\"Computer Science\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Student successfully created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StudentResponseDTO.class),
                examples = {
                    @ExampleObject(
                        name = "created-student",
                        summary = "Created student response",
                        value = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john.doe@iub.edu\",\"department\":\"Computer Science\",\"createdAt\":\"2023-01-15T10:30:00\",\"updatedAt\":\"2023-01-15T10:30:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "validation-error",
                        summary = "Validation error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation failed for object 'studentRequestDTO'\",\"path\":\"/api/students\",\"fieldErrors\":{\"name\":\"Name is required\",\"email\":\"Email should be valid\"}}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "duplicate-email",
                        summary = "Duplicate email error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Student with email 'john.doe@iub.edu' already exists\",\"path\":\"/api/students\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students\"}"
                    )
                }
            )
        )
    })
    @PostMapping
    public ResponseEntity<StudentResponseDTO> createStudent(
            @Parameter(description = "Student information for creation", required = true)
            @Valid @RequestBody StudentRequestDTO studentRequestDTO) {
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
    @Operation(
        summary = "Update an existing student",
        description = "Updates a student record with the provided information. All fields will be updated with the new values.",
        tags = {"Student Management"},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated student information",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StudentRequestDTO.class),
                examples = {
                    @ExampleObject(
                        name = "update-student",
                        summary = "Student update request example",
                        value = "{\"name\":\"John Smith\",\"email\":\"john.smith@iub.edu\",\"department\":\"Data Science\"}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Student successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StudentResponseDTO.class),
                examples = {
                    @ExampleObject(
                        name = "updated-student",
                        summary = "Updated student response",
                        value = "{\"id\":1,\"name\":\"John Smith\",\"email\":\"john.smith@iub.edu\",\"department\":\"Data Science\",\"createdAt\":\"2023-01-15T10:30:00\",\"updatedAt\":\"2023-01-20T14:45:00\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid input data",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "validation-error",
                        summary = "Validation error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Validation failed for object 'studentRequestDTO'\",\"path\":\"/api/students/1\",\"fieldErrors\":{\"name\":\"Name is required\",\"email\":\"Email should be valid\"}}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Student not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "not-found-example",
                        summary = "Student not found response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Student with ID 999 not found\",\"path\":\"/api/students/999\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "duplicate-email",
                        summary = "Duplicate email error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Student with email 'john.smith@iub.edu' already exists\",\"path\":\"/api/students/1\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students/1\"}"
                    )
                }
            )
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> updateStudent(
            @Parameter(description = "ID of the student to update", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Updated student information", required = true)
            @Valid @RequestBody StudentRequestDTO studentRequestDTO) {
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
    @Operation(
        summary = "Delete a student",
        description = "Deletes a student record by their unique identifier. This operation cannot be undone.",
        tags = {"Student Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Student successfully deleted",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Student not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "not-found-example",
                        summary = "Student not found response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Student with ID 999 not found\",\"path\":\"/api/students/999\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students/1\"}"
                    )
                }
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "ID of the student to delete", required = true)
            @PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/students/department/{department} - Retrieve students by department
     * 
     * @param department the department to filter by
     * @return ResponseEntity containing a list of students in the specified department as DTOs
     */
    @Operation(
        summary = "Get students by department",
        description = "Retrieves all students belonging to a specific department. Returns an empty list if no students are found in the specified department.",
        tags = {"Student Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved students by department",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = StudentResponseDTO.class)),
                examples = {
                    @ExampleObject(
                        name = "department-students",
                        summary = "Students in Computer Science department",
                        value = "[{\"id\":1,\"name\":\"John Doe\",\"email\":\"john.doe@iub.edu\",\"department\":\"Computer Science\",\"createdAt\":\"2023-01-15T10:30:00\",\"updatedAt\":\"2023-01-15T10:30:00\"},{\"id\":3,\"name\":\"Alice Johnson\",\"email\":\"alice.johnson@iub.edu\",\"department\":\"Computer Science\",\"createdAt\":\"2023-01-18T11:20:00\",\"updatedAt\":\"2023-01-18T11:20:00\"}]"
                    ),
                    @ExampleObject(
                        name = "empty-department",
                        summary = "No students in the specified department",
                        value = "[]"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "server-error",
                        summary = "Internal server error response",
                        value = "{\"timestamp\":\"2023-01-15T10:30:00\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"An unexpected error occurred\",\"path\":\"/api/students/department/Computer%20Science\"}"
                    )
                }
            )
        )
    })
    @GetMapping("/department/{department}")
    public ResponseEntity<List<StudentResponseDTO>> getStudentsByDepartment(
            @Parameter(description = "Department name to filter students by", required = true)
            @PathVariable String department) {
        List<Student> students = studentService.getStudentsByDepartment(department);
        List<StudentResponseDTO> studentDTOs = students.stream()
                .map(StudentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(studentDTOs);
    }
}