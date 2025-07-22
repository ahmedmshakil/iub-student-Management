package com.iub.studentmanagement.exception;

/**
 * Exception thrown when attempting to create or update a student with an email
 * that already exists in the system.
 */
public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String email) {
        super("Student with email '" + email + "' already exists");
    }
}