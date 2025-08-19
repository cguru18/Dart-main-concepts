package com.studentmanagement.controller;

import com.studentmanagement.util.DatabaseConnection;
import com.studentmanagement.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for database error screen
 */
public class DatabaseErrorController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseErrorController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Database error controller initialized");
    }

    @FXML
    private void retryConnection() {
        try {
            // Test database connection
            if (DatabaseConnection.getInstance().testConnection()) {
                logger.info("Database connection successful, navigating to login");
                SceneManager.getInstance().showLoginScene();
            } else {
                SceneManager.getInstance().showAlert(
                    "Connection Failed", 
                    "Still unable to connect to database. Please check XAMPP MySQL service.", 
                    Alert.AlertType.ERROR
                );
            }
        } catch (Exception e) {
            logger.error("Error during connection retry", e);
            SceneManager.getInstance().showAlert(
                "Error", 
                "Failed to test database connection: " + e.getMessage(), 
                Alert.AlertType.ERROR
            );
        }
    }

    @FXML
    private void exitApplication() {
        logger.info("User chose to exit application due to database error");
        Platform.exit();
    }
}