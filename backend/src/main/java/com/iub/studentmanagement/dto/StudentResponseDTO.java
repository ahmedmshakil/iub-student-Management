package com.iub.studentmanagement.dto;

import com.iub.studentmanagement.model.Student;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for student responses.
 * Contains all student data to be returned to the client.
 */
public class StudentResponseDTO {
    
    private Long id;
    private String name;
    private String email;
    private String department;
    private LocalDateTime createdAt;
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