package com.studentmanagement.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.CompletionException;

/**
 * Global exception handler for the application
 */
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler() {}

    public static synchronized GlobalExceptionHandler getInstance() {
        if (instance == null) {
            instance = new GlobalExceptionHandler();
        }
        return instance;
    }

    /**
     * Handle exceptions with appropriate user feedback
     */
    public void handleException(Throwable throwable) {
        handleException(throwable, "An unexpected error occurred");
    }

    /**
     * Handle exceptions with custom message
     */
    public void handleException(Throwable throwable, String defaultMessage) {
        if (throwable == null) {
            return;
        }

        // Unwrap CompletionException
        Throwable actualException = throwable;
        if (throwable instanceof CompletionException && throwable.getCause() != null) {
            actualException = throwable.getCause();
        }

        logger.error("Exception occurred: {}", actualException.getClass().getSimpleName(), actualException);

        String userMessage = getUserFriendlyMessage(actualException, defaultMessage);
        Alert.AlertType alertType = getAlertType(actualException);

        // Show alert on JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle("System Notification");
            alert.setHeaderText(getErrorTitle(actualException));
            alert.setContentText(userMessage);
            alert.showAndWait();
        });
    }

    /**
     * Get user-friendly error message
     */
    private String getUserFriendlyMessage(Throwable exception, String defaultMessage) {
        if (exception instanceof ValidationException) {
            return exception.getMessage();
        } else if (exception instanceof AuthenticationException) {
            return exception.getMessage();
        } else if (exception instanceof DatabaseConnectionException) {
            return "Database connection failed. Please ensure XAMPP MySQL is running and try again.";
        } else if (exception instanceof SQLException) {
            return handleSQLException((SQLException) exception);
        } else if (exception instanceof SQLTimeoutException) {
            return "Database operation timed out. Please check your connection and try again.";
        } else if (exception instanceof IllegalArgumentException) {
            return "Invalid input provided. Please check your data and try again.";
        } else if (exception instanceof IllegalStateException) {
            return "System is in an invalid state. Please restart the application.";
        } else if (exception instanceof SecurityException) {
            return "Access denied. You don't have permission to perform this operation.";
        } else if (exception instanceof OutOfMemoryError) {
            return "System is running low on memory. Please close other applications and try again.";
        } else if (exception instanceof ClassNotFoundException) {
            return "Required system component not found. Please reinstall the application.";
        } else if (exception instanceof java.io.FileNotFoundException) {
            return "Required file not found. Please check the installation.";
        } else if (exception instanceof java.io.IOException) {
            return "File operation failed. Please check file permissions and disk space.";
        } else if (exception instanceof NumberFormatException) {
            return "Invalid number format. Please enter a valid number.";
        } else if (exception instanceof java.util.concurrent.TimeoutException) {
            return "Operation timed out. Please try again.";
        } else if (exception instanceof InterruptedException) {
            return "Operation was interrupted. Please try again.";
        } else {
            logger.warn("Unhandled exception type: {}", exception.getClass().getName());
            return defaultMessage + "\n\nDetails: " + exception.getMessage();
        }
    }

    /**
     * Handle SQL exceptions with specific messages
     */
    private String handleSQLException(SQLException sqlException) {
        int errorCode = sqlException.getErrorCode();
        String sqlState = sqlException.getSQLState();
        
        switch (errorCode) {
            case 1045: // Access denied
                return "Database access denied. Please check database credentials.";
            case 1049: // Unknown database
                return "Database not found. Please run the database setup script.";
            case 1146: // Table doesn't exist
                return "Database table not found. Please run the database setup script.";
            case 1062: // Duplicate entry
                return "Duplicate entry detected. The record already exists.";
            case 1452: // Foreign key constraint
                return "Data integrity error. Related record not found.";
            case 2003: // Can't connect to MySQL server
                return "Cannot connect to database server. Please ensure XAMPP MySQL is running.";
            case 2006: // MySQL server has gone away
                return "Database connection lost. Please try again.";
            default:
                if (sqlState != null) {
                    switch (sqlState.substring(0, 2)) {
                        case "08": // Connection exception
                            return "Database connection error. Please check your network connection.";
                        case "23": // Integrity constraint violation
                            return "Data integrity error. Please check your input data.";
                        case "42": // Syntax error or access rule violation
                            return "Database query error. Please contact support.";
                        default:
                            break;
                    }
                }
                return "Database error occurred: " + sqlException.getMessage();
        }
    }

    /**
     * Get appropriate alert type for exception
     */
    private Alert.AlertType getAlertType(Throwable exception) {
        if (exception instanceof ValidationException || 
            exception instanceof IllegalArgumentException ||
            exception instanceof NumberFormatException) {
            return Alert.AlertType.WARNING;
        } else if (exception instanceof AuthenticationException ||
                   exception instanceof SecurityException) {
            return Alert.AlertType.WARNING;
        } else if (exception instanceof DatabaseConnectionException ||
                   exception instanceof SQLException ||
                   exception instanceof ClassNotFoundException) {
            return Alert.AlertType.ERROR;
        } else {
            return Alert.AlertType.ERROR;
        }
    }

    /**
     * Get error title based on exception type
     */
    private String getErrorTitle(Throwable exception) {
        if (exception instanceof ValidationException) {
            return "Validation Error";
        } else if (exception instanceof AuthenticationException) {
            return "Authentication Error";
        } else if (exception instanceof DatabaseConnectionException ||
                   exception instanceof SQLException) {
            return "Database Error";
        } else if (exception instanceof SecurityException) {
            return "Security Error";
        } else if (exception instanceof java.io.IOException) {
            return "File Operation Error";
        } else if (exception instanceof OutOfMemoryError) {
            return "Memory Error";
        } else {
            return "System Error";
        }
    }

    /**
     * Handle critical system errors that require application shutdown
     */
    public void handleCriticalError(Throwable exception) {
        logger.error("Critical system error occurred", exception);
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Critical System Error");
            alert.setHeaderText("The application encountered a critical error and must be closed.");
            alert.setContentText("Error: " + exception.getMessage() + 
                               "\n\nPlease check the logs and restart the application.");
            
            alert.showAndWait();
            Platform.exit();
        });
    }

    /**
     * Log exception with context information
     */
    public void logException(Throwable exception, String context) {
        logger.error("Exception in context '{}': {}", context, exception.getMessage(), exception);
    }

    /**
     * Log exception with additional details
     */
    public void logExceptionWithDetails(Throwable exception, String context, Object... details) {
        StringBuilder detailsStr = new StringBuilder();
        for (int i = 0; i < details.length; i += 2) {
            if (i + 1 < details.length) {
                detailsStr.append(details[i]).append("=").append(details[i + 1]).append(", ");
            }
        }
        
        logger.error("Exception in context '{}' with details [{}]: {}", 
                    context, detailsStr.toString(), exception.getMessage(), exception);
    }

    /**
     * Check if an exception is recoverable
     */
    public boolean isRecoverableException(Throwable exception) {
        return exception instanceof ValidationException ||
               exception instanceof AuthenticationException ||
               exception instanceof IllegalArgumentException ||
               exception instanceof NumberFormatException ||
               (exception instanceof SQLException && 
                ((SQLException) exception).getErrorCode() != 2003 && // Can't connect
                ((SQLException) exception).getErrorCode() != 1049);   // Unknown database
    }

    /**
     * Create user-friendly error dialog
     */
    public void showUserFriendlyError(String title, String message, String details) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(message);
            
            if (details != null && !details.trim().isEmpty()) {
                alert.getDialogPane().setExpandableContent(
                    new javafx.scene.control.TextArea(details)
                );
            }
            
            alert.showAndWait();
        });
    }

    /**
     * Show success notification
     */
    public void showSuccessNotification(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Show warning notification
     */
    public void showWarningNotification(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}