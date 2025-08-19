package com.studentmanagement.service;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.exception.AuthenticationException;
import com.studentmanagement.exception.ValidationException;
import com.studentmanagement.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Service for handling user authentication and registration
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UserDAO userDAO;
    private final ExecutorService executorService;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern ADMISSION_NUMBER_PATTERN = Pattern.compile(
        "^[A-Z0-9]{6,20}$"
    );
    private static final int MIN_PASSWORD_LENGTH = 8;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Register a new student user
     */
    public CompletableFuture<User> registerStudent(String admissionNumber, String email, 
                                                  String fullName, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateRegistrationData(admissionNumber, email, fullName, password);
                
                // Check if user already exists
                if (userDAO.existsByAdmissionNumber(admissionNumber)) {
                    throw new ValidationException("Admission number already exists: " + admissionNumber);
                }
                
                if (userDAO.existsByEmail(email)) {
                    throw new ValidationException("Email already exists: " + email);
                }
                
                // Hash password
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
                
                // Create user
                User user = new User(admissionNumber, email, fullName, hashedPassword, User.Role.STUDENT);
                User savedUser = userDAO.save(user);
                
                logger.info("Student registered successfully: {}", admissionNumber);
                return savedUser;
                
            } catch (SQLException e) {
                logger.error("Database error during student registration", e);
                throw new RuntimeException("Registration failed due to database error", e);
            } catch (ValidationException e) {
                logger.warn("Validation error during registration: {}", e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Authenticate user login
     */
    public CompletableFuture<User> authenticateUser(String admissionNumber, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (admissionNumber == null || admissionNumber.trim().isEmpty()) {
                    throw new AuthenticationException("Admission number is required");
                }
                
                if (password == null || password.trim().isEmpty()) {
                    throw new AuthenticationException("Password is required");
                }
                
                Optional<User> userOpt = userDAO.findByAdmissionNumber(admissionNumber.trim().toUpperCase());
                
                if (userOpt.isEmpty()) {
                    throw new AuthenticationException("Invalid admission number or password");
                }
                
                User user = userOpt.get();
                
                if (!user.isActive()) {
                    throw new AuthenticationException("Account is deactivated. Please contact administrator.");
                }
                
                if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                    throw new AuthenticationException("Invalid admission number or password");
                }
                
                logger.info("User authenticated successfully: {}", admissionNumber);
                return user;
                
            } catch (SQLException e) {
                logger.error("Database error during authentication", e);
                throw new RuntimeException("Authentication failed due to database error", e);
            } catch (AuthenticationException e) {
                logger.warn("Authentication failed for user: {}", admissionNumber);
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Change user password
     */
    public CompletableFuture<Boolean> changePassword(int userId, String currentPassword, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validatePassword(newPassword);
                
                Optional<User> userOpt = userDAO.findById(userId);
                if (userOpt.isEmpty()) {
                    throw new AuthenticationException("User not found");
                }
                
                User user = userOpt.get();
                
                if (!BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
                    throw new AuthenticationException("Current password is incorrect");
                }
                
                String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                user.setPasswordHash(hashedNewPassword);
                
                userDAO.update(user);
                
                logger.info("Password changed successfully for user: {}", user.getAdmissionNumber());
                return true;
                
            } catch (SQLException e) {
                logger.error("Database error during password change", e);
                throw new RuntimeException("Password change failed due to database error", e);
            } catch (AuthenticationException | ValidationException e) {
                logger.warn("Password change failed: {}", e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Validate registration data
     */
    private void validateRegistrationData(String admissionNumber, String email, 
                                        String fullName, String password) throws ValidationException {
        // Validate admission number
        if (admissionNumber == null || admissionNumber.trim().isEmpty()) {
            throw new ValidationException("Admission number is required");
        }
        
        if (!ADMISSION_NUMBER_PATTERN.matcher(admissionNumber.trim().toUpperCase()).matches()) {
            throw new ValidationException("Invalid admission number format. Must be 6-20 alphanumeric characters");
        }
        
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches()) {
            throw new ValidationException("Invalid email format");
        }
        
        // Validate full name
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        
        if (fullName.trim().length() < 2) {
            throw new ValidationException("Full name must be at least 2 characters long");
        }
        
        // Validate password
        validatePassword(password);
    }

    /**
     * Validate password strength
     */
    private void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter) {
            throw new ValidationException("Password must contain at least one letter");
        }
        
        if (!hasDigit) {
            throw new ValidationException("Password must contain at least one digit");
        }
    }

    /**
     * Create admin user (for system initialization)
     */
    public User createAdminUser(String admissionNumber, String email, String fullName, String password) 
            throws ValidationException, SQLException {
        validateRegistrationData(admissionNumber, email, fullName, password);
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User admin = new User(admissionNumber, email, fullName, hashedPassword, User.Role.ADMIN);
        
        return userDAO.save(admin);
    }

    /**
     * Create teacher user
     */
    public User createTeacherUser(String admissionNumber, String email, String fullName, String password) 
            throws ValidationException, SQLException {
        validateRegistrationData(admissionNumber, email, fullName, password);
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User teacher = new User(admissionNumber, email, fullName, hashedPassword, User.Role.TEACHER);
        
        return userDAO.save(teacher);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}