package com.studentmanagement.exception;

/**
 * Custom exception for database connection issues
 */
public class DatabaseConnectionException extends RuntimeException {
    
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }
}