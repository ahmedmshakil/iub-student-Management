package com.iub.studentmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for student creation and update requests.
 * Contains validation annotations to ensure data integrity.
 */
public class StudentRequestDTO {
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department cannot exceed 50 characters")
    private String department;
    
    // Default constructor
    public StudentRequestDTO() {
    }
    
    // Constructor with fields
    public StudentRequestDTO(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "StudentRequestDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}