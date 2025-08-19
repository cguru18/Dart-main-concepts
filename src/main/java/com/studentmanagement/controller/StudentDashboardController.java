package com.studentmanagement.controller;

import com.studentmanagement.model.ExamResult;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.User;
import com.studentmanagement.service.StudentService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Controller for the student dashboard
 */
public class StudentDashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(StudentDashboardController.class);

    // Navigation
    @FXML private VBox sidebar;
    @FXML private Button profileButton;
    @FXML private Button subjectsButton;
    @FXML private Button examsButton;
    @FXML private Button reportsButton;
    @FXML private Button logoutButton;

    // Header
    @FXML private Label welcomeLabel;
    @FXML private Label admissionLabel;

    // Main Content Area
    @FXML private StackPane contentArea;
    
    // Profile Section
    @FXML private VBox profileSection;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileAdmissionLabel;
    @FXML private GridPane subjectSelectionGrid;
    @FXML private Label subjectCountLabel;
    @FXML private Button saveSubjectsButton;

    // Exam Results Section
    @FXML private VBox examSection;
    @FXML private TableView<ExamResult> examResultsTable;
    @FXML private TableColumn<ExamResult, String> subjectColumn;
    @FXML private TableColumn<ExamResult, String> examNameColumn;
    @FXML private TableColumn<ExamResult, String> examTypeColumn;
    @FXML private TableColumn<ExamResult, BigDecimal> marksColumn;
    @FXML private TableColumn<ExamResult, String> gradeColumn;
    @FXML private TableColumn<ExamResult, Integer> positionColumn;
    @FXML private TableColumn<ExamResult, String> dateColumn;

    // Statistics
    @FXML private Label totalSubjectsLabel;
    @FXML private Label averageMarksLabel;
    @FXML private Label overallGradeLabel;
    @FXML private Label totalExamsLabel;

    // Report Generation
    @FXML private Button generateReportButton;
    @FXML private ProgressIndicator reportProgress;

    private StudentService studentService;
    private User currentUser;
    private ObservableList<Subject> availableSubjects;
    private ObservableList<Subject> enrolledSubjects;
    private ObservableList<ExamResult> examResults;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentService = new StudentService();
        currentUser = SceneManager.getInstance().getCurrentUser();
        
        if (currentUser == null || !currentUser.isStudent()) {
            logger.error("Invalid user access to student dashboard");
            SceneManager.getInstance().logout();
            return;
        }

        setupUI();
        setupEventHandlers();
        setupTableColumns();
        loadUserData();
        
        logger.debug("Student dashboard controller initialized for user: {}", currentUser.getAdmissionNumber());
    }

    private void setupUI() {
        // Initialize collections
        availableSubjects = FXCollections.observableArrayList();
        enrolledSubjects = FXCollections.observableArrayList();
        examResults = FXCollections.observableArrayList();
        
        // Set user info in header
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        admissionLabel.setText("Admission: " + currentUser.getAdmissionNumber());
        
        // Hide progress indicator initially
        reportProgress.setVisible(false);
        
        // Apply CSS classes
        sidebar.getStyleClass().add("sidebar");
        contentArea.getStyleClass().add("content-area");
        profileSection.getStyleClass().add("card");
        examSection.getStyleClass().add("card");
        
        // Set initial view to profile
        showProfileSection();
    }

    private void setupEventHandlers() {
        profileButton.setOnAction(event -> showProfileSection());
        subjectsButton.setOnAction(event -> showSubjectsSection());
        examsButton.setOnAction(event -> showExamSection());
        reportsButton.setOnAction(event -> generateReport());
        logoutButton.setOnAction(event -> handleLogout());
        
        saveSubjectsButton.setOnAction(event -> saveSubjectSelection());
        generateReportButton.setOnAction(event -> generateReport());
    }

    private void setupTableColumns() {
        subjectColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getExamination() != null && result.getExamination().getSubject() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    result.getExamination().getSubject().getSubjectName()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        examNameColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getExamination() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    result.getExamination().getExamName()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        examTypeColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getExamination() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    result.getExamination().getExamType().getDisplayName()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        marksColumn.setCellValueFactory(new PropertyValueFactory<>("marksObtained"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        dateColumn.setCellValueFactory(cellData -> {
            ExamResult result = cellData.getValue();
            if (result.getExamination() != null && result.getExamination().getExamDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    result.getExamination().getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Set table data
        examResultsTable.setItems(examResults);
        
        // Style grade column based on grade value
        gradeColumn.setCellFactory(column -> new TableCell<ExamResult, String>() {
            @Override
            protected void updateItem(String grade, boolean empty) {
                super.updateItem(grade, empty);
                
                if (empty || grade == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(grade);
                    
                    // Apply grade-specific styling
                    getStyleClass().removeAll("grade-a", "grade-b", "grade-c", "grade-d", "grade-e");
                    
                    if (grade.startsWith("A")) {
                        getStyleClass().add("grade-a");
                    } else if (grade.startsWith("B")) {
                        getStyleClass().add("grade-b");
                    } else if (grade.startsWith("C")) {
                        getStyleClass().add("grade-c");
                    } else if (grade.startsWith("D")) {
                        getStyleClass().add("grade-d");
                    } else {
                        getStyleClass().add("grade-e");
                    }
                }
            }
        });
    }

    private void loadUserData() {
        // Load student subjects
        CompletableFuture<List<Subject>> subjectsFuture = studentService.getStudentSubjects(currentUser.getId());
        
        subjectsFuture.whenCompleteAsync((subjects, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    logger.error("Failed to load student subjects", throwable);
                    showAlert("Error", "Failed to load your subjects. Please try again.", Alert.AlertType.ERROR);
                } else {
                    enrolledSubjects.setAll(subjects);
                    updateSubjectDisplay();
                    loadAvailableSubjects();
                }
            });
        });

        // Load exam results
        CompletableFuture<List<ExamResult>> resultsFuture = studentService.getStudentExamResults(currentUser.getId());
        
        resultsFuture.whenCompleteAsync((results, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    logger.error("Failed to load exam results", throwable);
                    showAlert("Error", "Failed to load exam results. Please try again.", Alert.AlertType.ERROR);
                } else {
                    examResults.setAll(results);
                    updateStatistics();
                }
            });
        });
    }

    private void loadAvailableSubjects() {
        CompletableFuture<List<Subject>> availableFuture = studentService.getAvailableSubjectsForStudent(currentUser.getId());
        
        availableFuture.whenCompleteAsync((subjects, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    logger.error("Failed to load available subjects", throwable);
                } else {
                    availableSubjects.setAll(subjects);
                    buildSubjectSelectionUI();
                }
            });
        });
    }

    private void buildSubjectSelectionUI() {
        subjectSelectionGrid.getChildren().clear();
        
        // Group subjects by category
        Map<Subject.Category, List<Subject>> subjectsByCategory = availableSubjects.stream()
            .collect(Collectors.groupingBy(Subject::getCategory));

        int row = 0;
        
        // Add each category
        for (Subject.Category category : Subject.Category.values()) {
            List<Subject> categorySubjects = subjectsByCategory.get(category);
            if (categorySubjects == null || categorySubjects.isEmpty()) {
                continue;
            }

            // Category header
            Label categoryLabel = new Label(getCategoryDisplayName(category));
            categoryLabel.getStyleClass().add("category-title");
            subjectSelectionGrid.add(categoryLabel, 0, row++, 2, 1);

            // Add subjects for this category
            for (Subject subject : categorySubjects) {
                CheckBox subjectCheckbox = new CheckBox(subject.getSubjectName() + " (" + subject.getSubjectCode() + ")");
                subjectCheckbox.getStyleClass().add("subject-checkbox");
                subjectCheckbox.setUserData(subject);
                
                // Check if already enrolled
                boolean isEnrolled = enrolledSubjects.stream()
                    .anyMatch(enrolled -> enrolled.getId() == subject.getId());
                subjectCheckbox.setSelected(isEnrolled);
                
                // Add validation
                subjectCheckbox.setOnAction(event -> validateSubjectSelection());
                
                subjectSelectionGrid.add(subjectCheckbox, 0, row++, 2, 1);
            }
            
            // Add spacing between categories
            Region spacer = new Region();
            spacer.setPrefHeight(10);
            subjectSelectionGrid.add(spacer, 0, row++, 2, 1);
        }
        
        updateSubjectCount();
    }

    private String getCategoryDisplayName(Subject.Category category) {
        switch (category) {
            case COMPULSORY: return "Compulsory Subjects (Required)";
            case STEM_SCIENCE: return "STEM Sciences (Choose up to 2)";
            case HUMANITIES: return "Humanities (Choose up to 2)";
            case TECHNICAL: return "Technical Subjects (Choose up to 2)";
            default: return category.name();
        }
    }

    private void validateSubjectSelection() {
        Map<Subject.Category, Long> selectedCounts = getSelectedSubjects().stream()
            .collect(Collectors.groupingBy(Subject::getCategory, Collectors.counting()));

        long stemCount = selectedCounts.getOrDefault(Subject.Category.STEM_SCIENCE, 0L);
        long humanitiesCount = selectedCounts.getOrDefault(Subject.Category.HUMANITIES, 0L);
        long technicalCount = selectedCounts.getOrDefault(Subject.Category.TECHNICAL, 0L);
        
        // Disable checkboxes if limits are reached
        subjectSelectionGrid.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                CheckBox checkbox = (CheckBox) node;
                Subject subject = (Subject) checkbox.getUserData();
                
                if (subject != null && !checkbox.isSelected()) {
                    boolean shouldDisable = false;
                    
                    if (subject.isStemScience() && stemCount >= 2) {
                        shouldDisable = true;
                    } else if (subject.isHumanities() && humanitiesCount >= 2) {
                        shouldDisable = true;
                    } else if (subject.isTechnical() && technicalCount >= 2) {
                        shouldDisable = true;
                    }
                    
                    checkbox.setDisable(shouldDisable);
                }
            }
        });
        
        updateSubjectCount();
    }

    private List<Subject> getSelectedSubjects() {
        return subjectSelectionGrid.getChildren().stream()
            .filter(node -> node instanceof CheckBox)
            .map(node -> (CheckBox) node)
            .filter(CheckBox::isSelected)
            .map(checkbox -> (Subject) checkbox.getUserData())
            .filter(subject -> subject != null)
            .collect(Collectors.toList());
    }

    private void updateSubjectCount() {
        List<Subject> selected = getSelectedSubjects();
        int totalSelected = selected.size() + 3; // +3 for compulsory subjects
        
        subjectCountLabel.setText("Selected: " + totalSelected + "/8 subjects");
        
        boolean isValid = totalSelected >= 7 && totalSelected <= 8;
        saveSubjectsButton.setDisable(!isValid);
        
        if (totalSelected < 7) {
            subjectCountLabel.getStyleClass().removeAll("success-message");
            subjectCountLabel.getStyleClass().add("error-message");
        } else if (totalSelected > 8) {
            subjectCountLabel.getStyleClass().removeAll("success-message");
            subjectCountLabel.getStyleClass().add("error-message");
        } else {
            subjectCountLabel.getStyleClass().removeAll("error-message");
            subjectCountLabel.getStyleClass().add("success-message");
        }
    }

    private void saveSubjectSelection() {
        List<Integer> selectedSubjectIds = getSelectedSubjects().stream()
            .map(Subject::getId)
            .collect(Collectors.toList());

        saveSubjectsButton.setDisable(true);
        saveSubjectsButton.setText("Saving...");

        CompletableFuture<Boolean> enrollmentFuture = studentService.enrollStudentInSubjects(
            currentUser.getId(), selectedSubjectIds
        );

        enrollmentFuture.whenCompleteAsync((success, throwable) -> {
            Platform.runLater(() -> {
                saveSubjectsButton.setDisable(false);
                saveSubjectsButton.setText("Save Subject Selection");
                
                if (throwable != null) {
                    logger.error("Failed to save subject selection", throwable);
                    showAlert("Error", "Failed to save subject selection: " + throwable.getMessage(), Alert.AlertType.ERROR);
                } else if (success) {
                    showAlert("Success", "Subject selection saved successfully!", Alert.AlertType.INFORMATION);
                    loadUserData(); // Refresh data
                } else {
                    showAlert("Warning", "Some subjects could not be saved. Please try again.", Alert.AlertType.WARNING);
                }
            });
        });
    }

    private void updateSubjectDisplay() {
        // Update profile section
        profileNameLabel.setText(currentUser.getFullName());
        profileEmailLabel.setText(currentUser.getEmail());
        profileAdmissionLabel.setText(currentUser.getAdmissionNumber());
        
        // Update statistics
        totalSubjectsLabel.setText(String.valueOf(enrolledSubjects.size()));
    }

    private void updateStatistics() {
        if (examResults.isEmpty()) {
            averageMarksLabel.setText("No exams yet");
            overallGradeLabel.setText("N/A");
            totalExamsLabel.setText("0");
            return;
        }

        // Calculate average marks
        double average = examResults.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .average()
            .orElse(0.0);

        averageMarksLabel.setText(String.format("%.1f%%", average));
        overallGradeLabel.setText(ExamResult.calculateGrade(BigDecimal.valueOf(average)));
        totalExamsLabel.setText(String.valueOf(examResults.size()));
    }

    @FXML
    private void showProfileSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(profileSection);
        updateSidebarSelection(profileButton);
    }

    @FXML
    private void showSubjectsSection() {
        // This would show a detailed subject management view
        showProfileSection(); // For now, redirect to profile
    }

    @FXML
    private void showExamSection() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(examSection);
        updateSidebarSelection(examsButton);
    }

    @FXML
    private void generateReport() {
        reportProgress.setVisible(true);
        generateReportButton.setDisable(true);
        generateReportButton.setText("Generating...");

        // Simulate report generation (implement PDF generation here)
        Task<Void> reportTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000); // Simulate processing time
                return null;
            }
        };

        reportTask.setOnSucceeded(event -> Platform.runLater(() -> {
            reportProgress.setVisible(false);
            generateReportButton.setDisable(false);
            generateReportButton.setText("Generate Report");
            showAlert("Success", "Report generated successfully!", Alert.AlertType.INFORMATION);
        }));

        reportTask.setOnFailed(event -> Platform.runLater(() -> {
            reportProgress.setVisible(false);
            generateReportButton.setDisable(false);
            generateReportButton.setText("Generate Report");
            showAlert("Error", "Failed to generate report. Please try again.", Alert.AlertType.ERROR);
        }));

        new Thread(reportTask).start();
    }

    private void updateSidebarSelection(Button selectedButton) {
        // Clear all selections
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("selected");
            }
        });
        
        // Add selection to current button
        selectedButton.getStyleClass().add("selected");
    }

    @FXML
    private void handleLogout() {
        boolean confirmed = SceneManager.getInstance().showConfirmation(
            "Logout Confirmation",
            "Are you sure you want to logout?"
        );
        
        if (confirmed) {
            logger.info("User logging out: {}", currentUser.getAdmissionNumber());
            SceneManager.getInstance().logout();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        SceneManager.getInstance().showAlert(title, message, type);
    }

    public void cleanup() {
        if (studentService != null) {
            studentService.shutdown();
        }
    }
}