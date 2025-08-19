package com.studentmanagement.controller;

import com.studentmanagement.dao.ExamResultDAO;
import com.studentmanagement.dao.ExaminationDAO;
import com.studentmanagement.dao.SubjectDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.model.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the teacher dashboard
 */
public class TeacherDashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDashboardController.class);

    // Navigation
    @FXML private VBox sidebar;
    @FXML private Button overviewButton;
    @FXML private Button examsButton;
    @FXML private Button studentsButton;
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
    @FXML private Label totalExamsLabel;
    @FXML private Label totalSubjectsLabel;
    @FXML private Label averagePerformanceLabel;

    // Exam Management Section
    @FXML private VBox examManagementSection;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private TextField examNameField;
    @FXML private ComboBox<Examination.ExamType> examTypeComboBox;
    @FXML private DatePicker examDatePicker;
    @FXML private TextField maxMarksField;
    @FXML private Button createExamButton;
    @FXML private TableView<Examination> examsTable;
    @FXML private TableColumn<Examination, String> examSubjectColumn;
    @FXML private TableColumn<Examination, String> examNameColumn;
    @FXML private TableColumn<Examination, String> examTypeColumn;
    @FXML private TableColumn<Examination, String> examDateColumn;
    @FXML private TableColumn<Examination, Integer> maxMarksColumn;

    // Student Results Section
    @FXML private VBox studentResultsSection;
    @FXML private ComboBox<Examination> examSelectionComboBox;
    @FXML private TableView<ExamResult> resultsTable;
    @FXML private TableColumn<ExamResult, String> studentNameColumn;
    @FXML private TableColumn<ExamResult, String> admissionColumn;
    @FXML private TableColumn<ExamResult, BigDecimal> marksColumn;
    @FXML private TableColumn<ExamResult, String> gradeColumn;
    @FXML private TableColumn<ExamResult, Integer> positionColumn;

    private User currentUser;
    private UserDAO userDAO;
    private SubjectDAO subjectDAO;
    private ExaminationDAO examinationDAO;
    private ExamResultDAO examResultDAO;
    private ReportService reportService;
    
    private ObservableList<Subject> subjects;
    private ObservableList<Examination> examinations;
    private ObservableList<ExamResult> examResults;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SceneManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isTeacher()) {
            logger.error("Invalid user access to teacher dashboard");
            SceneManager.getInstance().logout();
            return;
        }

        // Initialize DAOs and services
        userDAO = new UserDAO();
        subjectDAO = new SubjectDAO();
        examinationDAO = new ExaminationDAO();
        examResultDAO = new ExamResultDAO();
        reportService = new ReportService();

        setupUI();
        setupEventHandlers();
        setupTableColumns();
        loadData();
        
        logger.debug("Teacher dashboard controller initialized for user: {}", currentUser.getAdmissionNumber());
    }

    private void setupUI() {
        // Initialize collections
        subjects = FXCollections.observableArrayList();
        examinations = FXCollections.observableArrayList();
        examResults = FXCollections.observableArrayList();
        
        // Set user info
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        roleLabel.setText("Teacher Dashboard");
        
        // Setup combo boxes
        subjectComboBox.setItems(subjects);
        examTypeComboBox.setItems(FXCollections.observableArrayList(Examination.ExamType.values()));
        examSelectionComboBox.setItems(examinations);
        
        // Set default values
        maxMarksField.setText("100");
        examDatePicker.setValue(LocalDate.now().plusDays(7));
        
        // Apply CSS classes
        sidebar.getStyleClass().add("sidebar");
        contentArea.getStyleClass().add("content-area");
        overviewSection.getStyleClass().add("card");
        examManagementSection.getStyleClass().add("card");
        studentResultsSection.getStyleClass().add("card");
        
        // Show overview by default
        showOverviewSection();
    }

    private void setupEventHandlers() {
        overviewButton.setOnAction(event -> showOverviewSection());
        examsButton.setOnAction(event -> showExamManagementSection());
        studentsButton.setOnAction(event -> showStudentResultsSection());
        reportsButton.setOnAction(event -> generateClassReport());
        logoutButton.setOnAction(event -> handleLogout());
        
        createExamButton.setOnAction(event -> createExamination());
        examSelectionComboBox.setOnAction(event -> loadExamResults());
    }

    private void setupTableColumns() {
        // Exam table columns
        examSubjectColumn.setCellValueFactory(cellData -> {
            Examination exam = cellData.getValue();
            if (exam.getSubject() != null) {
                return new javafx.beans.property.SimpleStringProperty(exam.getSubject().getSubjectName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        examNameColumn.setCellValueFactory(new PropertyValueFactory<>("examName"));
        examTypeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExamType().getDisplayName()));
        examDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        maxMarksColumn.setCellValueFactory(new PropertyValueFactory<>("maxMarks"));

        // Results table columns
        studentNameColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getStudent() != null) {
                return new javafx.beans.property.SimpleStringProperty(result.getStudent().getFullName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        admissionColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getStudent() != null) {
                return new javafx.beans.property.SimpleStringProperty(result.getStudent().getAdmissionNumber());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        marksColumn.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        // Set table data
        examsTable.setItems(examinations);
        resultsTable.setItems(examResults);
    }

    private void loadData() {
        // Load subjects
        CompletableFuture.runAsync(() -> {
            try {
                List<Subject> allSubjects = subjectDAO.findAll();
                Platform.runLater(() -> subjects.setAll(allSubjects));
            } catch (SQLException e) {
                logger.error("Failed to load subjects", e);
                Platform.runLater(() -> showAlert("Error", "Failed to load subjects", Alert.AlertType.ERROR));
            }
        });

        // Load examinations
        CompletableFuture.runAsync(() -> {
            try {
                List<Examination> allExams = examinationDAO.findAll();
                Platform.runLater(() -> {
                    examinations.setAll(allExams);
                    updateStatistics();
                });
            } catch (SQLException e) {
                logger.error("Failed to load examinations", e);
                Platform.runLater(() -> showAlert("Error", "Failed to load examinations", Alert.AlertType.ERROR));
            }
        });
    }

    private void updateStatistics() {
        // Update overview statistics
        CompletableFuture.runAsync(() -> {
            try {
                List<User> students = userDAO.findByRole(User.Role.STUDENT);
                List<Subject> allSubjects = subjectDAO.findAll();
                
                Platform.runLater(() -> {
                    totalStudentsLabel.setText(String.valueOf(students.size()));
                    totalExamsLabel.setText(String.valueOf(examinations.size()));
                    totalSubjectsLabel.setText(String.valueOf(allSubjects.size()));
                    
                    // Calculate average performance (placeholder)
                    averagePerformanceLabel.setText("75.2%");
                });
                
            } catch (SQLException e) {
                logger.error("Failed to update statistics", e);
            }
        });
    }

    @FXML
    private void createExamination() {
        try {
            String examName = examNameField.getText().trim();
            Subject selectedSubject = subjectComboBox.getValue();
            Examination.ExamType examType = examTypeComboBox.getValue();
            LocalDate examDate = examDatePicker.getValue();
            String maxMarksText = maxMarksField.getText().trim();

            // Validation
            if (examName.isEmpty() || selectedSubject == null || examType == null || 
                examDate == null || maxMarksText.isEmpty()) {
                showAlert("Validation Error", "Please fill in all fields", Alert.AlertType.WARNING);
                return;
            }

            int maxMarks = Integer.parseInt(maxMarksText);
            if (maxMarks <= 0 || maxMarks > 1000) {
                showAlert("Validation Error", "Max marks must be between 1 and 1000", Alert.AlertType.WARNING);
                return;
            }

            // Create examination
            Examination exam = new Examination(examName, examType, selectedSubject.getId(), examDate, currentUser.getId());
            exam.setMaxMarks(maxMarks);

            CompletableFuture.runAsync(() -> {
                try {
                    Examination savedExam = examinationDAO.save(exam);
                    savedExam.setSubject(selectedSubject);
                    
                    Platform.runLater(() -> {
                        examinations.add(savedExam);
                        clearExamForm();
                        showAlert("Success", "Examination created successfully!", Alert.AlertType.INFORMATION);
                    });
                    
                } catch (SQLException e) {
                    logger.error("Failed to create examination", e);
                    Platform.runLater(() -> showAlert("Error", "Failed to create examination", Alert.AlertType.ERROR));
                }
            });

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Max marks must be a valid number", Alert.AlertType.WARNING);
        }
    }

    private void clearExamForm() {
        examNameField.clear();
        subjectComboBox.setValue(null);
        examTypeComboBox.setValue(null);
        examDatePicker.setValue(LocalDate.now().plusDays(7));
        maxMarksField.setText("100");
    }

    private void loadExamResults() {
        Examination selectedExam = examSelectionComboBox.getValue();
        if (selectedExam == null) {
            examResults.clear();
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<ExamResult> results = examResultDAO.getStudentRankingByExam(selectedExam.getId());
                Platform.runLater(() -> examResults.setAll(results));
            } catch (SQLException e) {
                logger.error("Failed to load exam results", e);
                Platform.runLater(() -> showAlert("Error", "Failed to load exam results", Alert.AlertType.ERROR));
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
    private void showExamManagementSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(examManagementSection);
        updateSidebarSelection(examsButton);
    }

    @FXML
    private void showStudentResultsSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(studentResultsSection);
        updateSidebarSelection(studentsButton);
    }

    @FXML
    private void generateClassReport() {
        if (examResults.isEmpty()) {
            showAlert("Warning", "No exam results available to generate report", Alert.AlertType.WARNING);
            return;
        }

        CompletableFuture<String> reportFuture = reportService.generateClassPerformanceReport(
            examResults, "Class Report"
        );

        reportFuture.whenCompleteAsync((filePath, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    logger.error("Failed to generate class report", throwable);
                    showAlert("Error", "Failed to generate report: " + throwable.getMessage(), Alert.AlertType.ERROR);
                } else {
                    showAlert("Success", "Report generated successfully!\nSaved to: " + filePath, Alert.AlertType.INFORMATION);
                }
            });
        });
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
            logger.info("Teacher logging out: {}", currentUser.getAdmissionNumber());
            SceneManager.getInstance().logout();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        SceneManager.getInstance().showAlert(title, message, type);
    }

    public void cleanup() {
        if (reportService != null) {
            reportService.shutdown();
        }
    }
}