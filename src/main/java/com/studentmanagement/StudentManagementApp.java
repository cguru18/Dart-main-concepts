package com.studentmanagement;

import com.studentmanagement.controller.LoginController;
import com.studentmanagement.util.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Main JavaFX Application class for Student Management System
 */
public class StudentManagementApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(StudentManagementApp.class);
    
    private static final String APP_TITLE = "Student Management Learning System";
    private static final String APP_VERSION = "1.0.0";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Test database connection
            if (!DatabaseConnection.getInstance().testConnection()) {
                logger.error("Failed to connect to database. Please ensure XAMPP MySQL is running.");
                showDatabaseError(primaryStage);
                return;
            }
            
            // Initialize SceneManager with primary stage
            SceneManager.getInstance().setPrimaryStage(primaryStage);
            
            // Load login screen
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Apply CSS styles
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            
            // Configure primary stage
            primaryStage.setTitle(APP_TITLE + " v" + APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setMaximized(false);
            
            // Set application icon (if available)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/app-icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                logger.debug("App icon not found, using default");
            }
            
            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Application closing...");
                shutdown();
                Platform.exit();
            });
            
            // Show the stage
            primaryStage.show();
            
            // Center on screen
            primaryStage.centerOnScreen();
            
            logger.info("Student Management System started successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load application UI", e);
            showLoadError(primaryStage);
        } catch (Exception e) {
            logger.error("Unexpected error during application startup", e);
            showUnexpectedError(primaryStage);
        }
    }

    private void showDatabaseError(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/database-error.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            
            stage.setTitle("Database Connection Error");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load database error screen", e);
            Platform.exit();
        }
    }

    private void showLoadError(Stage stage) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Application Error");
        alert.setHeaderText("Failed to Load Application");
        alert.setContentText("The application failed to load properly. Please check the logs and try again.");
        alert.showAndWait();
        Platform.exit();
    }

    private void showUnexpectedError(Stage stage) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Unexpected Error");
        alert.setHeaderText("An unexpected error occurred");
        alert.setContentText("Please check the logs for more details and try restarting the application.");
        alert.showAndWait();
        Platform.exit();
    }

    private void shutdown() {
        try {
            // Shutdown database connection pool
            DatabaseConnection.getInstance().shutdown();
            logger.info("Database connections closed");
        } catch (Exception e) {
            logger.error("Error during application shutdown", e);
        }
    }

    public static void main(String[] args) {
        // Set system properties for better JavaFX performance
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.subpixeltext", "false");
        
        // Configure logging
        System.setProperty("logback.configurationFile", "logback.xml");
        
        logger.info("Starting Student Management Learning System...");
        
        // Launch JavaFX application
        launch(args);
    }
}