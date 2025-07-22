package com.iub.studentmanagement.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation errors occur during student operations.
 * This exception can hold multiple validation errors for different fields.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    /**
     * Creates a new ValidationException with a general message.
     * 
     * @param message The general validation error message
     */
    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    /**
     * Creates a new ValidationException with a general message and a map of field-specific errors.
     * 
     * @param message The general validation error message
     * @param errors Map of field names to error messages
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
    
    /**
     * Creates a new ValidationException with a single field error.
     * 
     * @param field The field name that has the validation error
     * @param errorMessage The specific error message for the field
     */
    public ValidationException(String field, String errorMessage) {
        super("Validation failed for field: " + field);
        this.errors = new HashMap<>();
        this.errors.put(field, errorMessage);
    }
    
    /**
     * Gets the map of validation errors.
     * 
     * @return Map of field names to error messages
     */
    public Map<String, String> getErrors() {
        return errors;
    }
    
    /**
     * Adds a field error to the existing errors map.
     * 
     * @param field The field name
     * @param errorMessage The error message
     */
    public void addError(String field, String errorMessage) {
        this.errors.put(field, errorMessage);
    }
    
    /**
     * Checks if there are any validation errors.
     * 
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}