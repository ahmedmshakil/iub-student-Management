package com.iub.studentmanagement.exception;

/**
 * Exception thrown when a student is not found in the system.
 */
public class StudentNotFoundException extends RuntimeException {
    
    public StudentNotFoundException(String message) {
        super(message);
    }
    
    public StudentNotFoundException(Long id) {
        super("Student not found with id: " + id);
    }
}