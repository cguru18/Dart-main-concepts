package com.studentmanagement.exception;

/**
 * Custom exception for validation errors
 */
public class ValidationException extends Exception {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}