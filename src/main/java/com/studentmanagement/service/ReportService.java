package com.studentmanagement.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.studentmanagement.model.ExamResult;
import com.studentmanagement.model.Subject;
import com.studentmanagement.model.User;
import com.studentmanagement.service.StudentService.StudentPerformanceSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service for generating PDF reports
 */
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    private final StudentService studentService;
    private final ExecutorService executorService;
    
    // Color scheme matching the application
    private static final DeviceRgb BURGUNDY = new DeviceRgb(153, 27, 27);
    private static final DeviceRgb LILAC = new DeviceRgb(196, 181, 253);
    private static final DeviceRgb DARK_GREY = new DeviceRgb(55, 65, 81);

    public ReportService() {
        this.studentService = new StudentService();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * Generate comprehensive student performance report
     */
    public CompletableFuture<String> generateStudentPerformanceReport(User student, List<ExamResult> examResults) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = generateStudentReportFileName(student);
                String filePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;
                
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add header
                addReportHeader(document, student);
                
                // Add student information
                addStudentInformation(document, student);
                
                // Add performance summary
                addPerformanceSummary(document, examResults);
                
                // Add detailed results table
                addDetailedResultsTable(document, examResults);
                
                // Add subject performance breakdown
                addSubjectPerformanceBreakdown(document, examResults);
                
                // Add footer
                addReportFooter(document);

                document.close();
                
                logger.info("Student performance report generated: {}", filePath);
                return filePath;
                
            } catch (FileNotFoundException e) {
                logger.error("Failed to create PDF report file", e);
                throw new RuntimeException("Failed to generate report: Unable to create file", e);
            } catch (Exception e) {
                logger.error("Error generating student performance report", e);
                throw new RuntimeException("Failed to generate report", e);
            }
        }, executorService);
    }

    /**
     * Generate class performance report for teachers
     */
    public CompletableFuture<String> generateClassPerformanceReport(List<ExamResult> classResults, String className) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = "Class_Performance_Report_" + className.replaceAll("[^a-zA-Z0-9]", "_") + 
                                "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                String filePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;
                
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add header
                addClassReportHeader(document, className);
                
                // Add class statistics
                addClassStatistics(document, classResults);
                
                // Add detailed class results
                addClassResultsTable(document, classResults);
                
                // Add footer
                addReportFooter(document);

                document.close();
                
                logger.info("Class performance report generated: {}", filePath);
                return filePath;
                
            } catch (Exception e) {
                logger.error("Error generating class performance report", e);
                throw new RuntimeException("Failed to generate class report", e);
            }
        }, executorService);
    }

    private void addReportHeader(Document document, User student) {
        Paragraph header = new Paragraph("STUDENT PERFORMANCE REPORT")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(BURGUNDY);
        
        document.add(header);
        
        Paragraph schoolName = new Paragraph("Student Management Learning System")
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY);
        
        document.add(schoolName);
        
        Paragraph reportDate = new Paragraph("Generated on: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm")))
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        
        document.add(reportDate);
    }

    private void addStudentInformation(Document document, User student) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(100));
        
        infoTable.addHeaderCell(new Cell().add(new Paragraph("Student Information").setBold())
            .setBackgroundColor(LILAC).setTextAlignment(TextAlignment.CENTER).setColspan(2));
        
        infoTable.addCell(new Cell().add(new Paragraph("Full Name:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(student.getFullName())));
        
        infoTable.addCell(new Cell().add(new Paragraph("Admission Number:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(student.getAdmissionNumber())));
        
        infoTable.addCell(new Cell().add(new Paragraph("Email:").setBold()));
        infoTable.addCell(new Cell().add(new Paragraph(student.getEmail())));
        
        document.add(infoTable);
        document.add(new Paragraph("\n"));
    }

    private void addPerformanceSummary(Document document, List<ExamResult> examResults) {
        if (examResults.isEmpty()) {
            document.add(new Paragraph("No examination results available.").setItalic());
            return;
        }

        // Calculate statistics
        double averageMarks = examResults.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .average()
            .orElse(0.0);

        long totalExams = examResults.size();
        long passingExams = examResults.stream()
            .mapToLong(result -> result.isPassing() ? 1 : 0)
            .sum();

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
            .setWidth(UnitValue.createPercentValue(100));
        
        summaryTable.addHeaderCell(new Cell().add(new Paragraph("Performance Summary").setBold())
            .setBackgroundColor(BURGUNDY).setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER).setColspan(4));
        
        summaryTable.addCell(new Cell().add(new Paragraph("Total Exams").setBold()).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph("Average Marks").setBold()).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph("Overall Grade").setBold()).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph("Pass Rate").setBold()).setTextAlignment(TextAlignment.CENTER));
        
        summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalExams))).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", averageMarks))).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph(ExamResult.calculateGrade(BigDecimal.valueOf(averageMarks)))).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", (double) passingExams / totalExams * 100))).setTextAlignment(TextAlignment.CENTER));
        
        document.add(summaryTable);
        document.add(new Paragraph("\n"));
    }

    private void addDetailedResultsTable(Document document, List<ExamResult> examResults) {
        if (examResults.isEmpty()) {
            return;
        }

        Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{20, 25, 15, 10, 10, 10, 10}))
            .setWidth(UnitValue.createPercentValue(100));
        
        resultsTable.addHeaderCell(new Cell().add(new Paragraph("Detailed Examination Results").setBold())
            .setBackgroundColor(DARK_GREY).setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER).setColspan(7));

        // Add column headers
        String[] headers = {"Subject", "Exam Name", "Type", "Marks", "Grade", "Position", "Date"};
        for (String header : headers) {
            resultsTable.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                .setBackgroundColor(LILAC).setTextAlignment(TextAlignment.CENTER));
        }

        // Add data rows
        for (ExamResult result : examResults) {
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getSubject().getSubjectName())));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getExamName())));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getExamType().name())));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getMarksObtained().toString())).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getGrade())).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(new Cell().add(new Paragraph(
                String.valueOf(result.getPosition()))).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
        }

        document.add(resultsTable);
        document.add(new Paragraph("\n"));
    }

    private void addSubjectPerformanceBreakdown(Document document, List<ExamResult> examResults) {
        Map<String, List<ExamResult>> resultsBySubject = examResults.stream()
            .collect(Collectors.groupingBy(
                result -> result.getExamination().getSubject().getSubjectName()
            ));

        document.add(new Paragraph("Subject Performance Breakdown").setBold().setFontSize(16));

        for (Map.Entry<String, List<ExamResult>> entry : resultsBySubject.entrySet()) {
            String subjectName = entry.getKey();
            List<ExamResult> subjectResults = entry.getValue();

            double subjectAverage = subjectResults.stream()
                .mapToDouble(result -> result.getMarksObtained().doubleValue())
                .average()
                .orElse(0.0);

            Paragraph subjectSummary = new Paragraph(String.format(
                "%s: %.1f%% average (%s) - %d exams",
                subjectName, subjectAverage, 
                ExamResult.calculateGrade(BigDecimal.valueOf(subjectAverage)),
                subjectResults.size()
            )).setMarginLeft(20);

            document.add(subjectSummary);
        }
    }

    private void addClassReportHeader(Document document, String className) {
        Paragraph header = new Paragraph("CLASS PERFORMANCE REPORT")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(BURGUNDY);
        
        document.add(header);
        
        Paragraph classInfo = new Paragraph("Class: " + className)
            .setFontSize(16)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY);
        
        document.add(classInfo);
    }

    private void addClassStatistics(Document document, List<ExamResult> classResults) {
        if (classResults.isEmpty()) {
            document.add(new Paragraph("No examination results available for this class.").setItalic());
            return;
        }

        double classAverage = classResults.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .average()
            .orElse(0.0);

        long totalStudents = classResults.stream()
            .map(ExamResult::getStudentId)
            .distinct()
            .count();

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
            .setWidth(UnitValue.createPercentValue(100));
        
        statsTable.addHeaderCell(new Cell().add(new Paragraph("Class Statistics").setBold())
            .setBackgroundColor(BURGUNDY).setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER).setColspan(4));
        
        statsTable.addCell(new Cell().add(new Paragraph("Total Students")).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph("Total Exams")).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph("Class Average")).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph("Overall Grade")).setTextAlignment(TextAlignment.CENTER));
        
        statsTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalStudents))).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph(String.valueOf(classResults.size()))).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", classAverage))).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(new Cell().add(new Paragraph(ExamResult.calculateGrade(BigDecimal.valueOf(classAverage)))).setTextAlignment(TextAlignment.CENTER));
        
        document.add(statsTable);
        document.add(new Paragraph("\n"));
    }

    private void addClassResultsTable(Document document, List<ExamResult> classResults) {
        if (classResults.isEmpty()) {
            return;
        }

        Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{20, 15, 20, 15, 10, 10, 10}))
            .setWidth(UnitValue.createPercentValue(100));
        
        resultsTable.addHeaderCell(new Cell().add(new Paragraph("Class Examination Results").setBold())
            .setBackgroundColor(DARK_GREY).setFontColor(ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER).setColspan(7));

        // Add column headers
        String[] headers = {"Student Name", "Admission", "Subject", "Exam", "Marks", "Grade", "Position"};
        for (String header : headers) {
            resultsTable.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                .setBackgroundColor(LILAC).setTextAlignment(TextAlignment.CENTER));
        }

        // Add data rows
        for (ExamResult result : classResults) {
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getStudent() != null ? result.getStudent().getFullName() : "N/A")));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getStudent() != null ? result.getStudent().getAdmissionNumber() : "N/A")));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getSubject().getSubjectName())));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getExamination().getExamName())));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getMarksObtained().toString())).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(new Cell().add(new Paragraph(
                result.getGrade())).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(new Cell().add(new Paragraph(
                String.valueOf(result.getPosition()))).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(resultsTable);
    }

    private void addReportFooter(Document document) {
        Paragraph footer = new Paragraph("\n\nThis report was generated automatically by the Student Management Learning System.")
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY)
            .setItalic();
        
        document.add(footer);
        
        Paragraph timestamp = new Paragraph("Report generated on: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss")))
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY);
        
        document.add(timestamp);
    }

    private String generateStudentReportFileName(User student) {
        return "Student_Report_" + student.getAdmissionNumber() + "_" + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
    }

    /**
     * Generate subject-wise performance report
     */
    public CompletableFuture<String> generateSubjectReport(int subjectId, List<ExamResult> subjectResults) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String subjectName = subjectResults.isEmpty() ? "Unknown" : 
                    subjectResults.get(0).getExamination().getSubject().getSubjectName();
                
                String fileName = "Subject_Report_" + subjectName.replaceAll("[^a-zA-Z0-9]", "_") + 
                                "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                String filePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;
                
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add subject report content
                addSubjectReportHeader(document, subjectName);
                addSubjectStatistics(document, subjectResults);
                addSubjectResultsTable(document, subjectResults);
                addReportFooter(document);

                document.close();
                
                logger.info("Subject performance report generated: {}", filePath);
                return filePath;
                
            } catch (Exception e) {
                logger.error("Error generating subject performance report", e);
                throw new RuntimeException("Failed to generate subject report", e);
            }
        }, executorService);
    }

    private void addSubjectReportHeader(Document document, String subjectName) {
        Paragraph header = new Paragraph("SUBJECT PERFORMANCE REPORT")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(BURGUNDY);
        
        document.add(header);
        
        Paragraph subjectTitle = new Paragraph("Subject: " + subjectName)
            .setFontSize(16)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY);
        
        document.add(subjectTitle);
    }

    private void addSubjectStatistics(Document document, List<ExamResult> subjectResults) {
        // Implementation for subject-specific statistics
        if (!subjectResults.isEmpty()) {
            double average = subjectResults.stream()
                .mapToDouble(result -> result.getMarksObtained().doubleValue())
                .average()
                .orElse(0.0);

            Paragraph stats = new Paragraph(String.format(
                "Subject Average: %.1f%% | Total Students: %d | Overall Grade: %s",
                average, subjectResults.size(), ExamResult.calculateGrade(BigDecimal.valueOf(average))
            )).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);

            document.add(stats);
        }
    }

    private void addSubjectResultsTable(Document document, List<ExamResult> subjectResults) {
        // Implementation for subject results table
        addClassResultsTable(document, subjectResults); // Reuse class results table format
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