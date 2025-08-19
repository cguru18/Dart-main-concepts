package com.studentmanagement.controller;

import com.studentmanagement.model.User;
import com.studentmanagement.service.AuthenticationService;
import com.studentmanagement.util.SceneManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the signup/registration screen
 */
public class SignupController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    @FXML private VBox signupContainer;
    @FXML private TextField admissionNumberField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Button backToLoginButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();
        
        setupUI();
        setupEventHandlers();
        setupValidation();
        
        logger.debug("Signup controller initialized");
    }

    private void setupUI() {
        // Hide labels and loading indicator initially
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        
        // Set focus to admission number field
        Platform.runLater(() -> admissionNumberField.requestFocus());
        
        // Add CSS classes
        signupContainer.getStyleClass().add("auth-form");
        admissionNumberField.getStyleClass().add("custom-text-field");
        emailField.getStyleClass().add("custom-text-field");
        fullNameField.getStyleClass().add("custom-text-field");
        passwordField.getStyleClass().add("custom-password-field");
        confirmPasswordField.getStyleClass().add("custom-password-field");
        signupButton.getStyleClass().add("primary-button");
        backToLoginButton.getStyleClass().add("secondary-button");
        errorLabel.getStyleClass().add("error-message");
        successLabel.getStyleClass().add("success-message");
        loadingIndicator.getStyleClass().add("loading-spinner");
    }

    private void setupEventHandlers() {
        // Signup button action
        signupButton.setOnAction(event -> handleSignup());
        
        // Back to login button action
        backToLoginButton.setOnAction(event -> handleBackToLogin());
        
        // Enter key navigation
        admissionNumberField.setOnAction(event -> emailField.requestFocus());
        emailField.setOnAction(event -> fullNameField.requestFocus());
        fullNameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(event -> handleSignup());
    }

    private void setupValidation() {
        // Real-time validation
        admissionNumberField.textProperty().addListener((obs, oldText, newText) -> {
            clearMessages();
            if (newText != null) {
                // Convert to uppercase for consistency
                if (!newText.equals(newText.toUpperCase())) {
                    admissionNumberField.setText(newText.toUpperCase());
                }
                validateAdmissionNumber(newText);
            }
        });

        emailField.textProperty().addListener((obs, oldText, newText) -> {
            clearMessages();
            if (newText != null && !newText.trim().isEmpty()) {
                validateEmail(newText);
            }
        });

        fullNameField.textProperty().addListener((obs, oldText, newText) -> {
            clearMessages();
            if (newText != null && !newText.trim().isEmpty()) {
                validateFullName(newText);
            }
        });

        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            clearMessages();
            if (newText != null && !newText.isEmpty()) {
                validatePassword(newText);
            }
        });

        confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            clearMessages();
            if (newText != null && !newText.isEmpty()) {
                validatePasswordConfirmation(newText);
            }
        });
    }

    @FXML
    private void handleSignup() {
        if (!validateAllFields()) {
            return;
        }

        String admissionNumber = admissionNumberField.getText().trim().toUpperCase();
        String email = emailField.getText().trim().toLowerCase();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();

        // Show loading state
        setLoadingState(true);

        // Register user asynchronously
        CompletableFuture<User> registrationFuture = authService.registerStudent(
            admissionNumber, email, fullName, password
        );

        registrationFuture.whenCompleteAsync((user, throwable) -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                
                if (throwable != null) {
                    logger.warn("Registration failed for admission number: {}", admissionNumber);
                    showError(throwable.getMessage());
                } else {
                    logger.info("Student registered successfully: {}", user.getAdmissionNumber());
                    showSuccess("Registration successful! You can now sign in with your credentials.");
                    
                    // Auto-navigate to login after 2 seconds
                    Task<Void> navigateTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            Thread.sleep(2000);
                            return null;
                        }
                    };
                    
                    navigateTask.setOnSucceeded(e -> Platform.runLater(this::handleBackToLogin));
                    new Thread(navigateTask).start();
                }
            });
        });
    }

    @FXML
    private void handleBackToLogin() {
        try {
            SceneManager.getInstance().showLoginScene();
        } catch (Exception e) {
            logger.error("Failed to navigate to login page", e);
            showError("Failed to load login page. Please try again.");
        }
    }

    private boolean validateAllFields() {
        String admissionNumber = admissionNumberField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Check if all fields are filled
        if (admissionNumber.isEmpty() || email.isEmpty() || fullName.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return false;
        }

        // Validate individual fields
        if (!validateAdmissionNumber(admissionNumber)) return false;
        if (!validateEmail(email)) return false;
        if (!validateFullName(fullName)) return false;
        if (!validatePassword(password)) return false;
        if (!validatePasswordConfirmation(confirmPassword)) return false;

        return true;
    }

    private boolean validateAdmissionNumber(String admissionNumber) {
        if (admissionNumber.length() < 6 || admissionNumber.length() > 20) {
            admissionNumberField.getStyleClass().add("field-error");
            return false;
        }
        
        if (!admissionNumber.matches("^[A-Z0-9]+$")) {
            admissionNumberField.getStyleClass().add("field-error");
            return false;
        }
        
        admissionNumberField.getStyleClass().removeAll("field-error");
        admissionNumberField.getStyleClass().add("field-success");
        return true;
    }

    private boolean validateEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        
        if (!email.matches(emailPattern)) {
            emailField.getStyleClass().add("field-error");
            return false;
        }
        
        emailField.getStyleClass().removeAll("field-error");
        emailField.getStyleClass().add("field-success");
        return true;
    }

    private boolean validateFullName(String fullName) {
        if (fullName.length() < 2) {
            fullNameField.getStyleClass().add("field-error");
            return false;
        }
        
        fullNameField.getStyleClass().removeAll("field-error");
        fullNameField.getStyleClass().add("field-success");
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.length() < 8) {
            passwordField.getStyleClass().add("field-error");
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            passwordField.getStyleClass().add("field-error");
            return false;
        }
        
        passwordField.getStyleClass().removeAll("field-error");
        passwordField.getStyleClass().add("field-success");
        return true;
    }

    private boolean validatePasswordConfirmation(String confirmPassword) {
        String password = passwordField.getText();
        
        if (!confirmPassword.equals(password)) {
            confirmPasswordField.getStyleClass().add("field-error");
            return false;
        }
        
        confirmPasswordField.getStyleClass().removeAll("field-error");
        confirmPasswordField.getStyleClass().add("field-success");
        return true;
    }

    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        signupButton.setDisable(loading);
        backToLoginButton.setDisable(loading);
        admissionNumberField.setDisable(loading);
        emailField.setDisable(loading);
        fullNameField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        
        if (loading) {
            signupButton.setText("Creating Account...");
        } else {
            signupButton.setText("Create Account");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
        
        // Auto-hide error after 5 seconds
        Task<Void> hideTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(5000);
                return null;
            }
        };
        
        hideTask.setOnSucceeded(e -> Platform.runLater(this::clearMessages));
        new Thread(hideTask).start();
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        errorLabel.setText("");
        successLabel.setText("");
        
        // Clear field styling
        admissionNumberField.getStyleClass().removeAll("field-error", "field-success");
        emailField.getStyleClass().removeAll("field-error", "field-success");
        fullNameField.getStyleClass().removeAll("field-error", "field-success");
        passwordField.getStyleClass().removeAll("field-error", "field-success");
        confirmPasswordField.getStyleClass().removeAll("field-error", "field-success");
    }

    public void cleanup() {
        if (authService != null) {
            authService.shutdown();
        }
    }
}