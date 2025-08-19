package com.studentmanagement.controller;

import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.dao.SubjectDAO;
import com.studentmanagement.dao.ExaminationDAO;
import com.studentmanagement.dao.ExamResultDAO;
import com.studentmanagement.model.User;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.Examination;
import com.studentmanagement.model.ExamResult;
import com.studentmanagement.service.AuthenticationService;
import com.studentmanagement.service.ReportService;
import com.studentmanagement.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the admin dashboard
 */
public class AdminDashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    // Navigation
    @FXML private VBox sidebar;
    @FXML private Button overviewButton;
    @FXML private Button usersButton;
    @FXML private Button subjectsButton;
    @FXML private Button examsButton;
    @FXML private Button reportsButton;
    @FXML private Button logoutButton;

    // Header
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;

    // Main Content
    @FXML private StackPane contentArea;
    
    // Overview Section
    @FXML private VBox overviewSection;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label totalExamsLabel;

    // User Management Section
    @FXML private VBox userManagementSection;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userNameColumn;
    @FXML private TableColumn<User, String> userAdmissionColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userStatusColumn;
    
    // Create User Form
    @FXML private TextField newUserAdmissionField;
    @FXML private TextField newUserEmailField;
    @FXML private TextField newUserNameField;
    @FXML private ComboBox<User.Role> newUserRoleComboBox;
    @FXML private PasswordField newUserPasswordField;
    @FXML private Button createUserButton;

    // System Reports Section
    @FXML private VBox reportsSection;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private Button generateSystemReportButton;
    @FXML private ProgressIndicator reportProgress;

    private User currentUser;
    private UserDAO userDAO;
    private SubjectDAO subjectDAO;
    private ExaminationDAO examinationDAO;
    private ExamResultDAO examResultDAO;
    private AuthenticationService authService;
    private ReportService reportService;
    
    private ObservableList<User> users;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isAdmin()) {
            logger.error("Invalid user access to admin dashboard");
            SceneManager.getInstance().logout();
            return;
        }

        // Initialize DAOs and services
        userDAO = new UserDAO();
        subjectDAO = new SubjectDAO();
        examinationDAO = new ExaminationDAO();
        examResultDAO = new ExamResultDAO();
        authService = new AuthenticationService();
        reportService = new ReportService();

        setupUI();
        setupEventHandlers();
        setupTableColumns();
        loadData();
        
        logger.debug("Admin dashboard controller initialized for user: {}", currentUser.getAdmissionNumber());
    }

    private void setupUI() {
        // Initialize collections
        users = FXCollections.observableArrayList();
        
        // Set user info
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        roleLabel.setText("Administrator Portal");
        
        // Setup combo boxes
        newUserRoleComboBox.setItems(FXCollections.observableArrayList(User.Role.values()));
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
            "All Students Performance",
            "Subject-wise Analysis", 
            "Examination Statistics",
            "User Activity Report"
        ));
        
        // Hide progress indicator
        reportProgress.setVisible(false);
        
        // Apply CSS classes
        sidebar.getStyleClass().add("sidebar");
        contentArea.getStyleClass().add("content-area");
        overviewSection.getStyleClass().add("card");
        userManagementSection.getStyleClass().add("card");
        reportsSection.getStyleClass().add("card");
        
        // Show overview by default
        showOverviewSection();
    }

    private void setupEventHandlers() {
        overviewButton.setOnAction(event -> showOverviewSection());
        usersButton.setOnAction(event -> showUserManagementSection());
        subjectsButton.setOnAction(event -> showSubjectsSection());
        examsButton.setOnAction(event -> showExamsSection());
        reportsButton.setOnAction(event -> showReportsSection());
        logoutButton.setOnAction(event -> handleLogout());
        
        createUserButton.setOnAction(event -> createNewUser());
        generateSystemReportButton.setOnAction(event -> generateSystemReport());
    }

    private void setupTableColumns() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userAdmissionColumn.setCellValueFactory(new PropertyValueFactory<>("admissionNumber"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().name()));
        userStatusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));

        // Set table data
        usersTable.setItems(users);
        
        // Add context menu for user management
        ContextMenu userContextMenu = new ContextMenu();
        MenuItem editUser = new MenuItem("Edit User");
        MenuItem deactivateUser = new MenuItem("Deactivate User");
        MenuItem resetPassword = new MenuItem("Reset Password");
        
        editUser.setOnAction(event -> editSelectedUser());
        deactivateUser.setOnAction(event -> deactivateSelectedUser());
        resetPassword.setOnAction(event -> resetUserPassword());
        
        userContextMenu.getItems().addAll(editUser, deactivateUser, resetPassword);
        usersTable.setContextMenu(userContextMenu);
    }

    private void loadData() {
        // Load all users
        CompletableFuture.runAsync(() -> {
            try {
                List<User> allUsers = userDAO.findAll();
                Platform.runLater(() -> {
                    users.setAll(allUsers);
                    updateStatistics();
                });
            } catch (SQLException e) {
                logger.error("Failed to load users", e);
                Platform.runLater(() -> showAlert("Error", "Failed to load users", Alert.AlertType.ERROR));
            }
        });
    }

    private void updateStatistics() {
        CompletableFuture.runAsync(() -> {
            try {
                List<User> students = userDAO.findByRole(User.Role.STUDENT);
                List<User> teachers = userDAO.findByRole(User.Role.TEACHER);
                List<Subject> subjects = subjectDAO.findAll();
                List<Examination> examinations = examinationDAO.findAll();
                
                Platform.runLater(() -> {
                    totalStudentsLabel.setText(String.valueOf(students.size()));
                    totalTeachersLabel.setText(String.valueOf(teachers.size()));
                    totalSubjectsLabel.setText(String.valueOf(subjects.size()));
                    totalExamsLabel.setText(String.valueOf(examinations.size()));
                });
                
            } catch (SQLException e) {
                logger.error("Failed to update statistics", e);
            }
        });
    }

    @FXML
    private void createNewUser() {
        try {
            String admission = newUserAdmissionField.getText().trim().toUpperCase();
            String email = newUserEmailField.getText().trim().toLowerCase();
            String name = newUserNameField.getText().trim();
            String password = newUserPasswordField.getText();
            User.Role role = newUserRoleComboBox.getValue();

            if (admission.isEmpty() || email.isEmpty() || name.isEmpty() || 
                password.isEmpty() || role == null) {
                showAlert("Validation Error", "Please fill in all fields", Alert.AlertType.WARNING);
                return;
            }

            createUserButton.setDisable(true);
            createUserButton.setText("Creating...");

            CompletableFuture<User> createFuture;
            
            if (role == User.Role.TEACHER) {
                createFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return authService.createTeacherUser(admission, email, name, password);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (role == User.Role.ADMIN) {
                createFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return authService.createAdminUser(admission, email, name, password);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                createFuture = authService.registerStudent(admission, email, name, password);
            }

            createFuture.whenCompleteAsync((user, throwable) -> {
                Platform.runLater(() -> {
                    createUserButton.setDisable(false);
                    createUserButton.setText("Create User");
                    
                    if (throwable != null) {
                        logger.error("Failed to create user", throwable);
                        showAlert("Error", "Failed to create user: " + throwable.getMessage(), Alert.AlertType.ERROR);
                    } else {
                        users.add(user);
                        clearUserForm();
                        showAlert("Success", "User created successfully!", Alert.AlertType.INFORMATION);
                        updateStatistics();
                    }
                });
            });

        } catch (Exception e) {
            logger.error("Error creating user", e);
            showAlert("Error", "Failed to create user", Alert.AlertType.ERROR);
        }
    }

    private void clearUserForm() {
        newUserAdmissionField.clear();
        newUserEmailField.clear();
        newUserNameField.clear();
        newUserPasswordField.clear();
        newUserRoleComboBox.setValue(null);
    }

    private void editSelectedUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "Please select a user to edit", Alert.AlertType.WARNING);
            return;
        }
        
        // TODO: Implement user editing dialog
        showAlert("Info", "User editing feature coming soon!", Alert.AlertType.INFORMATION);
    }

    private void deactivateSelectedUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "Please select a user to deactivate", Alert.AlertType.WARNING);
            return;
        }

        boolean confirmed = SceneManager.getInstance().showConfirmation(
            "Deactivate User",
            "Are you sure you want to deactivate " + selectedUser.getFullName() + "?"
        );

        if (confirmed) {
            CompletableFuture.runAsync(() -> {
                try {
                    boolean deactivated = userDAO.delete(selectedUser.getId());
                    Platform.runLater(() -> {
                        if (deactivated) {
                            users.remove(selectedUser);
                            showAlert("Success", "User deactivated successfully", Alert.AlertType.INFORMATION);
                            updateStatistics();
                        } else {
                            showAlert("Error", "Failed to deactivate user", Alert.AlertType.ERROR);
                        }
                    });
                } catch (SQLException e) {
                    logger.error("Failed to deactivate user", e);
                    Platform.runLater(() -> showAlert("Error", "Database error occurred", Alert.AlertType.ERROR));
                }
            });
        }
    }

    private void resetUserPassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Selection Error", "Please select a user to reset password", Alert.AlertType.WARNING);
            return;
        }
        
        // TODO: Implement password reset functionality
        showAlert("Info", "Password reset feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void generateSystemReport() {
        String reportType = reportTypeComboBox.getValue();
        if (reportType == null) {
            showAlert("Selection Error", "Please select a report type", Alert.AlertType.WARNING);
            return;
        }

        reportProgress.setVisible(true);
        generateSystemReportButton.setDisable(true);
        generateSystemReportButton.setText("Generating...");

        // TODO: Implement different report types
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000); // Simulate report generation
                Platform.runLater(() -> {
                    reportProgress.setVisible(false);
                    generateSystemReportButton.setDisable(false);
                    generateSystemReportButton.setText("Generate Report");
                    showAlert("Success", "System report generated successfully!", Alert.AlertType.INFORMATION);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    reportProgress.setVisible(false);
                    generateSystemReportButton.setDisable(false);
                    generateSystemReportButton.setText("Generate Report");
                    showAlert("Error", "Report generation was interrupted", Alert.AlertType.ERROR);
                });
            }
        });
    }

    @FXML
    private void showOverviewSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(overviewSection);
        updateSidebarSelection(overviewButton);
    }

    @FXML
    private void showUserManagementSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(userManagementSection);
        updateSidebarSelection(usersButton);
    }

    @FXML
    private void showSubjectsSection() {
        // TODO: Implement subjects management view
        showAlert("Info", "Subjects management feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showExamsSection() {
        // TODO: Implement exams management view
        showAlert("Info", "Exams management feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showReportsSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(reportsSection);
        updateSidebarSelection(reportsButton);
    }

    private void updateSidebarSelection(Button selectedButton) {
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("selected");
            }
        });
        selectedButton.getStyleClass().add("selected");
    }

    @FXML
    private void handleLogout() {
        boolean confirmed = SceneManager.getInstance().showConfirmation(
            "Logout Confirmation",
            "Are you sure you want to logout?"
        );
        
        if (confirmed) {
            logger.info("Admin logging out: {}", currentUser.getAdmissionNumber());
            SceneManager.getInstance().logout();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        SceneManager.getInstance().showAlert(title, message, type);
    }

    public void cleanup() {
        if (authService != null) {
            authService.shutdown();
        }
        if (reportService != null) {
            reportService.shutdown();
        }
    }
}