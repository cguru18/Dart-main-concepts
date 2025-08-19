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
 * Controller for the login screen
 */
public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private VBox loginContainer;
    @FXML private TextField admissionNumberField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private AuthenticationService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();
        
        setupUI();
        setupEventHandlers();
        
        logger.debug("Login controller initialized");
    }

    private void setupUI() {
        // Hide error label and loading indicator initially
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        
        // Set focus to admission number field
        Platform.runLater(() -> admissionNumberField.requestFocus());
        
        // Add CSS classes
        loginContainer.getStyleClass().add("auth-form");
        admissionNumberField.getStyleClass().add("custom-text-field");
        passwordField.getStyleClass().add("custom-password-field");
        loginButton.getStyleClass().add("primary-button");
        signupButton.getStyleClass().add("secondary-button");
        errorLabel.getStyleClass().add("error-message");
        loadingIndicator.getStyleClass().add("loading-spinner");
    }

    private void setupEventHandlers() {
        // Login button action
        loginButton.setOnAction(event -> handleLogin());
        
        // Signup button action
        signupButton.setOnAction(event -> handleSignup());
        
        // Enter key handlers
        admissionNumberField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> handleLogin());
        
        // Input validation
        admissionNumberField.textProperty().addListener((obs, oldText, newText) -> {
            clearError();
            if (newText != null) {
                // Convert to uppercase for consistency
                if (!newText.equals(newText.toUpperCase())) {
                    admissionNumberField.setText(newText.toUpperCase());
                }
            }
        });
        
        passwordField.textProperty().addListener((obs, oldText, newText) -> clearError());
    }

    @FXML
    private void handleLogin() {
        String admissionNumber = admissionNumberField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (admissionNumber.isEmpty()) {
            showError("Please enter your admission number");
            admissionNumberField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }

        // Show loading state
        setLoadingState(true);

        // Authenticate user asynchronously
        CompletableFuture<User> authFuture = authService.authenticateUser(admissionNumber, password);
        
        authFuture.whenCompleteAsync((user, throwable) -> {
            Platform.runLater(() -> {
                setLoadingState(false);
                
                if (throwable != null) {
                    logger.warn("Authentication failed for user: {}", admissionNumber);
                    showError(throwable.getMessage());
                    passwordField.clear();
                    passwordField.requestFocus();
                } else {
                    logger.info("User authenticated successfully: {}", user.getAdmissionNumber());
                    clearError();
                    navigateToHomePage(user);
                }
            });
        });
    }

    @FXML
    private void handleSignup() {
        try {
            SceneManager.getInstance().showSignupScene();
        } catch (Exception e) {
            logger.error("Failed to navigate to signup page", e);
            showError("Failed to load signup page. Please try again.");
        }
    }

    private void navigateToHomePage(User user) {
        try {
            switch (user.getRole()) {
                case STUDENT:
                    SceneManager.getInstance().showStudentDashboard(user);
                    break;
                case TEACHER:
                    SceneManager.getInstance().showTeacherDashboard(user);
                    break;
                case ADMIN:
                    SceneManager.getInstance().showAdminDashboard(user);
                    break;
                default:
                    logger.error("Unknown user role: {}", user.getRole());
                    showError("Invalid user role. Please contact administrator.");
                    return;
            }
            
            logger.info("Navigated to dashboard for user: {}", user.getAdmissionNumber());
        } catch (Exception e) {
            logger.error("Failed to navigate to dashboard", e);
            showError("Failed to load dashboard. Please try again.");
        }
    }

    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        signupButton.setDisable(loading);
        admissionNumberField.setDisable(loading);
        passwordField.setDisable(loading);
        
        if (loading) {
            loginButton.setText("Signing in...");
        } else {
            loginButton.setText("Sign In");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Auto-hide error after 5 seconds
        Task<Void> hideTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(5000);
                return null;
            }
        };
        
        hideTask.setOnSucceeded(e -> Platform.runLater(this::clearError));
        new Thread(hideTask).start();
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    public void cleanup() {
        if (authService != null) {
            authService.shutdown();
        }
    }
}