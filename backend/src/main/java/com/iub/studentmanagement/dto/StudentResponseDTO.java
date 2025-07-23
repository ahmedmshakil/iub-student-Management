package com.iub.studentmanagement.dto;

import com.iub.studentmanagement.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for student responses.
 * Contains all student data to be returned to the client.
 */
@Schema(description = "Student information returned in API responses")
public class StudentResponseDTO {
    
    @Schema(description = "Unique identifier for the student", example = "1")
    private Long id;
    
    @Schema(description = "Student's full name", example = "John Doe")
    private String name;
    
    @Schema(description = "Student's email address", example = "john.doe@iub.edu")
    private String email;
    
    @Schema(description = "Student's academic department", example = "Computer Science")
    private String department;
    
    @Schema(description = "Timestamp when the student record was created", example = "2023-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the student record was last updated", example = "2023-01-20T14:45:00")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public StudentResponseDTO() {
    }
    
    // Constructor with fields
    public StudentResponseDTO(Long id, String name, String email, String department, 
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Factory method to create a StudentResponseDTO from a Student entity
     * 
     * @param student The Student entity
     * @return A new StudentResponseDTO with data from the Student entity
     */
    public static StudentResponseDTO fromEntity(Student student) {
        return new StudentResponseDTO(
            student.getId(),
            student.getName(),
            student.getEmail(),
            student.getDepartment(),
            student.getCreatedAt(),
            student.getUpdatedAt()
        );
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "StudentResponseDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}