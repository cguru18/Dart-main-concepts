package com.studentmanagement.util;

import com.studentmanagement.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for managing scene transitions and navigation
 */
public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private static SceneManager instance;
    private Stage primaryStage;
    private User currentUser;

    private SceneManager() {}

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void showLoginScene() throws IOException {
        loadScene("/fxml/login.fxml", "Login - Student Management System");
    }

    public void showSignupScene() throws IOException {
        loadScene("/fxml/signup.fxml", "Sign Up - Student Management System");
    }

    public void showStudentDashboard(User user) throws IOException {
        setCurrentUser(user);
        loadScene("/fxml/student-dashboard.fxml", "Student Dashboard - " + user.getFullName());
    }

    public void showTeacherDashboard(User user) throws IOException {
        setCurrentUser(user);
        loadScene("/fxml/teacher-dashboard.fxml", "Teacher Dashboard - " + user.getFullName());
    }

    public void showAdminDashboard(User user) throws IOException {
        setCurrentUser(user);
        loadScene("/fxml/admin-dashboard.fxml", "Admin Dashboard - " + user.getFullName());
    }

    public void showStudentProfile() throws IOException {
        if (currentUser == null || !currentUser.isStudent()) {
            throw new IllegalStateException("No student user logged in");
        }
        loadScene("/fxml/student-profile.fxml", "Student Profile - " + currentUser.getFullName());
    }

    public void showExamResults() throws IOException {
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in");
        }
        loadScene("/fxml/exam-results.fxml", "Examination Results - " + currentUser.getFullName());
    }

    public void logout() {
        try {
            setCurrentUser(null);
            showLoginScene();
            logger.info("User logged out successfully");
        } catch (IOException e) {
            logger.error("Failed to navigate to login screen during logout", e);
        }
    }

    private void loadScene(String fxmlPath, String title) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set");
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Apply CSS styles
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            
            // Center the stage after loading new scene
            primaryStage.centerOnScreen();
            
            logger.debug("Scene loaded successfully: {}", fxmlPath);
            
        } catch (Exception e) {
            logger.error("Failed to load scene: {}", fxmlPath, e);
            throw new IOException("Failed to load scene: " + fxmlPath, e);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showAlert(String title, String message, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply styling to alert
        alert.getDialogPane().getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );
        
        alert.showAndWait();
    }

    public boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply styling to alert
        alert.getDialogPane().getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );
        
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }
}