package com.studentmanagement.util;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.studentmanagement.model.ExamResult;
import com.studentmanagement.model.User;
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
 * Utility class for generating PDF reports with proper formatting
 */
public class PDFReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PDFReportGenerator.class);
    
    // Application color scheme
    private static final DeviceRgb BURGUNDY = new DeviceRgb(153, 27, 27);
    private static final DeviceRgb LILAC = new DeviceRgb(196, 181, 253);
    private static final DeviceRgb DARK_GREY = new DeviceRgb(55, 65, 81);
    private static final DeviceRgb LIGHT_GREY = new DeviceRgb(243, 244, 246);
    
    private final ExecutorService executorService;

    public PDFReportGenerator() {
        this.executorService = Executors.newFixedThreadPool(3);
    }

    /**
     * Generate comprehensive student report
     */
    public CompletableFuture<String> generateStudentReport(User student, List<ExamResult> examResults, 
                                                          List<com.studentmanagement.model.Subject> subjects) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = String.format("Student_Report_%s_%s.pdf", 
                    student.getAdmissionNumber(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                
                String filePath = getReportsDirectory() + File.separator + fileName;
                
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add report content
                addStudentReportHeader(document, student);
                addStudentProfileSection(document, student, subjects);
                addPerformanceSummarySection(document, examResults);
                addDetailedResultsSection(document, examResults);
                addSubjectAnalysisSection(document, examResults);
                addRecommendationsSection(document, examResults);
                addReportFooter(document);

                document.close();
                
                logger.info("Student report generated successfully: {}", filePath);
                return filePath;
                
            } catch (Exception e) {
                logger.error("Failed to generate student report", e);
                throw new RuntimeException("Failed to generate student report", e);
            }
        }, executorService);
    }

    /**
     * Generate examination ranking report
     */
    public CompletableFuture<String> generateExamRankingReport(String examName, List<ExamResult> rankedResults) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = String.format("Exam_Ranking_%s_%s.pdf", 
                    examName.replaceAll("[^a-zA-Z0-9]", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                
                String filePath = getReportsDirectory() + File.separator + fileName;
                
                PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Add report content
                addExamRankingHeader(document, examName);
                addExamStatistics(document, rankedResults);
                addRankingTable(document, rankedResults);
                addPerformanceAnalysis(document, rankedResults);
                addReportFooter(document);

                document.close();
                
                logger.info("Exam ranking report generated successfully: {}", filePath);
                return filePath;
                
            } catch (Exception e) {
                logger.error("Failed to generate exam ranking report", e);
                throw new RuntimeException("Failed to generate exam ranking report", e);
            }
        }, executorService);
    }

    private void addStudentReportHeader(Document document, User student) {
        // School header
        Paragraph schoolName = new Paragraph("STUDENT MANAGEMENT LEARNING SYSTEM")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(BURGUNDY)
            .setMarginBottom(5);
        document.add(schoolName);

        // Report title
        Paragraph title = new Paragraph("STUDENT ACADEMIC PERFORMANCE REPORT")
            .setFontSize(16)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY)
            .setMarginBottom(20);
        document.add(title);

        // Report metadata
        Table metaTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        metaTable.addCell(createStyledCell("Student Name:", true));
        metaTable.addCell(createStyledCell(student.getFullName(), false));
        
        metaTable.addCell(createStyledCell("Admission Number:", true));
        metaTable.addCell(createStyledCell(student.getAdmissionNumber(), false));
        
        metaTable.addCell(createStyledCell("Report Generated:", true));
        metaTable.addCell(createStyledCell(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm")), 
            false));

        document.add(metaTable);
    }

    private void addStudentProfileSection(Document document, User student, List<com.studentmanagement.model.Subject> subjects) {
        document.add(new Paragraph("ENROLLED SUBJECTS").setBold().setFontSize(14).setMarginTop(10));
        
        if (subjects.isEmpty()) {
            document.add(new Paragraph("No subjects enrolled yet.").setItalic().setMarginBottom(20));
            return;
        }

        // Group subjects by category
        Map<com.studentmanagement.model.Subject.Category, List<com.studentmanagement.model.Subject>> subjectsByCategory = 
            subjects.stream().collect(Collectors.groupingBy(com.studentmanagement.model.Subject::getCategory));

        Table subjectsTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        for (com.studentmanagement.model.Subject.Category category : com.studentmanagement.model.Subject.Category.values()) {
            List<com.studentmanagement.model.Subject> categorySubjects = subjectsByCategory.get(category);
            if (categorySubjects != null && !categorySubjects.isEmpty()) {
                subjectsTable.addCell(createStyledCell(getCategoryDisplayName(category), true)
                    .setBackgroundColor(LIGHT_GREY));
                
                String subjectNames = categorySubjects.stream()
                    .map(com.studentmanagement.model.Subject::getSubjectName)
                    .collect(Collectors.joining(", "));
                
                subjectsTable.addCell(createStyledCell(subjectNames, false));
            }
        }

        document.add(subjectsTable);
    }

    private void addPerformanceSummarySection(Document document, List<ExamResult> examResults) {
        document.add(new Paragraph("PERFORMANCE SUMMARY").setBold().setFontSize(14).setMarginTop(10));
        
        if (examResults.isEmpty()) {
            document.add(new Paragraph("No examination results available.").setItalic().setMarginBottom(20));
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

        double passRate = totalExams > 0 ? (double) passingExams / totalExams * 100 : 0.0;

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        // Header
        summaryTable.addHeaderCell(createHeaderCell("Total Exams"));
        summaryTable.addHeaderCell(createHeaderCell("Average Score"));
        summaryTable.addHeaderCell(createHeaderCell("Overall Grade"));
        summaryTable.addHeaderCell(createHeaderCell("Pass Rate"));

        // Data
        summaryTable.addCell(createStyledCell(String.valueOf(totalExams), false).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(createStyledCell(String.format("%.1f%%", averageMarks), false).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(createStyledCell(ExamResult.calculateGrade(BigDecimal.valueOf(averageMarks)), false).setTextAlignment(TextAlignment.CENTER));
        summaryTable.addCell(createStyledCell(String.format("%.1f%%", passRate), false).setTextAlignment(TextAlignment.CENTER));

        document.add(summaryTable);
    }

    private void addDetailedResultsSection(Document document, List<ExamResult> examResults) {
        document.add(new Paragraph("DETAILED EXAMINATION RESULTS").setBold().setFontSize(14).setMarginTop(10));
        
        if (examResults.isEmpty()) {
            return;
        }

        Table resultsTable = new Table(UnitValue.createPercentArray(new float[]{20, 25, 15, 10, 10, 10, 10}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        // Headers
        String[] headers = {"Subject", "Exam Name", "Type", "Marks", "Grade", "Position", "Date"};
        for (String header : headers) {
            resultsTable.addHeaderCell(createHeaderCell(header));
        }

        // Data rows
        for (ExamResult result : examResults) {
            resultsTable.addCell(createStyledCell(
                result.getExamination().getSubject().getSubjectName(), false));
            resultsTable.addCell(createStyledCell(
                result.getExamination().getExamName(), false));
            resultsTable.addCell(createStyledCell(
                result.getExamination().getExamType().name(), false));
            resultsTable.addCell(createStyledCell(
                result.getMarksObtained().toString(), false).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(createStyledCell(
                result.getGrade(), false).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(createStyledCell(
                String.valueOf(result.getPosition()), false).setTextAlignment(TextAlignment.CENTER));
            resultsTable.addCell(createStyledCell(
                result.getExamination().getExamDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false));
        }

        document.add(resultsTable);
    }

    private void addSubjectAnalysisSection(Document document, List<ExamResult> examResults) {
        document.add(new Paragraph("SUBJECT-WISE PERFORMANCE ANALYSIS").setBold().setFontSize(14).setMarginTop(10));
        
        Map<String, List<ExamResult>> resultsBySubject = examResults.stream()
            .collect(Collectors.groupingBy(
                result -> result.getExamination().getSubject().getSubjectName()
            ));

        Table analysisTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        // Headers
        analysisTable.addHeaderCell(createHeaderCell("Subject"));
        analysisTable.addHeaderCell(createHeaderCell("Exams Taken"));
        analysisTable.addHeaderCell(createHeaderCell("Average Score"));
        analysisTable.addHeaderCell(createHeaderCell("Average Grade"));

        // Data
        for (Map.Entry<String, List<ExamResult>> entry : resultsBySubject.entrySet()) {
            String subjectName = entry.getKey();
            List<ExamResult> subjectResults = entry.getValue();
            
            double subjectAverage = subjectResults.stream()
                .mapToDouble(result -> result.getMarksObtained().doubleValue())
                .average()
                .orElse(0.0);

            analysisTable.addCell(createStyledCell(subjectName, false));
            analysisTable.addCell(createStyledCell(String.valueOf(subjectResults.size()), false).setTextAlignment(TextAlignment.CENTER));
            analysisTable.addCell(createStyledCell(String.format("%.1f%%", subjectAverage), false).setTextAlignment(TextAlignment.CENTER));
            analysisTable.addCell(createStyledCell(ExamResult.calculateGrade(BigDecimal.valueOf(subjectAverage)), false).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(analysisTable);
    }

    private void addRecommendationsSection(Document document, List<ExamResult> examResults) {
        document.add(new Paragraph("RECOMMENDATIONS").setBold().setFontSize(14).setMarginTop(10));
        
        if (examResults.isEmpty()) {
            document.add(new Paragraph("Take more examinations to receive personalized recommendations.").setItalic());
            return;
        }

        double averageMarks = examResults.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .average()
            .orElse(0.0);

        // Generate recommendations based on performance
        if (averageMarks >= 80) {
            document.add(new Paragraph("• Excellent performance! Continue with current study methods."));
            document.add(new Paragraph("• Consider taking on leadership roles in group studies."));
            document.add(new Paragraph("• Explore advanced topics in your strongest subjects."));
        } else if (averageMarks >= 70) {
            document.add(new Paragraph("• Very good performance with room for improvement."));
            document.add(new Paragraph("• Focus on consistent study habits and time management."));
            document.add(new Paragraph("• Seek additional practice in challenging subjects."));
        } else if (averageMarks >= 60) {
            document.add(new Paragraph("• Good foundation with potential for significant improvement."));
            document.add(new Paragraph("• Consider forming study groups with high-performing peers."));
            document.add(new Paragraph("• Allocate more time to revision and practice."));
        } else if (averageMarks >= 50) {
            document.add(new Paragraph("• Average performance requires focused improvement strategies."));
            document.add(new Paragraph("• Seek additional support from teachers and tutors."));
            document.add(new Paragraph("• Develop a structured study schedule."));
        } else {
            document.add(new Paragraph("• Performance needs immediate attention and support."));
            document.add(new Paragraph("• Meet with teachers to develop an improvement plan."));
            document.add(new Paragraph("• Consider additional tutoring and study support."));
        }

        document.add(new Paragraph("\n"));
    }

    private void addExamRankingHeader(Document document, String examName) {
        Paragraph title = new Paragraph("EXAMINATION RANKING REPORT")
            .setFontSize(18)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(BURGUNDY);
        document.add(title);

        Paragraph examTitle = new Paragraph("Examination: " + examName)
            .setFontSize(14)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY)
            .setMarginBottom(20);
        document.add(examTitle);
    }

    private void addExamStatistics(Document document, List<ExamResult> results) {
        if (results.isEmpty()) {
            return;
        }

        double average = results.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .average()
            .orElse(0.0);

        double highest = results.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .max()
            .orElse(0.0);

        double lowest = results.stream()
            .mapToDouble(result -> result.getMarksObtained().doubleValue())
            .min()
            .orElse(0.0);

        long passingCount = results.stream()
            .mapToLong(result -> result.isPassing() ? 1 : 0)
            .sum();

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        statsTable.addHeaderCell(createHeaderCell("Total Students"));
        statsTable.addHeaderCell(createHeaderCell("Class Average"));
        statsTable.addHeaderCell(createHeaderCell("Highest Score"));
        statsTable.addHeaderCell(createHeaderCell("Pass Rate"));

        statsTable.addCell(createStyledCell(String.valueOf(results.size()), false).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(createStyledCell(String.format("%.1f%%", average), false).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(createStyledCell(String.format("%.1f%%", highest), false).setTextAlignment(TextAlignment.CENTER));
        statsTable.addCell(createStyledCell(String.format("%.1f%%", (double) passingCount / results.size() * 100), false).setTextAlignment(TextAlignment.CENTER));

        document.add(statsTable);
    }

    private void addRankingTable(Document document, List<ExamResult> rankedResults) {
        Table rankingTable = new Table(UnitValue.createPercentArray(new float[]{10, 30, 20, 15, 15, 10}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);

        // Headers
        String[] headers = {"Rank", "Student Name", "Admission No.", "Marks", "Grade", "Percentage"};
        for (String header : headers) {
            rankingTable.addHeaderCell(createHeaderCell(header));
        }

        // Data
        for (int i = 0; i < rankedResults.size(); i++) {
            ExamResult result = rankedResults.get(i);
            
            rankingTable.addCell(createStyledCell(String.valueOf(i + 1), false).setTextAlignment(TextAlignment.CENTER));
            rankingTable.addCell(createStyledCell(
                result.getStudent() != null ? result.getStudent().getFullName() : "N/A", false));
            rankingTable.addCell(createStyledCell(
                result.getStudent() != null ? result.getStudent().getAdmissionNumber() : "N/A", false));
            rankingTable.addCell(createStyledCell(
                result.getMarksObtained().toString(), false).setTextAlignment(TextAlignment.CENTER));
            rankingTable.addCell(createStyledCell(
                result.getGrade(), false).setTextAlignment(TextAlignment.CENTER));
            rankingTable.addCell(createStyledCell(
                String.format("%.1f%%", result.getPercentage()), false).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(rankingTable);
    }

    private void addPerformanceAnalysis(Document document, List<ExamResult> results) {
        document.add(new Paragraph("PERFORMANCE ANALYSIS").setBold().setFontSize(14).setMarginTop(10));

        // Grade distribution
        Map<String, Long> gradeDistribution = results.stream()
            .collect(Collectors.groupingBy(ExamResult::getGrade, Collectors.counting()));

        document.add(new Paragraph("Grade Distribution:").setBold().setMarginTop(10));
        
        gradeDistribution.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByKey())
            .forEach(entry -> {
                double percentage = (double) entry.getValue() / results.size() * 100;
                document.add(new Paragraph(String.format("Grade %s: %d students (%.1f%%)", 
                    entry.getKey(), entry.getValue(), percentage))
                    .setMarginLeft(20));
            });
    }

    private void addReportFooter(Document document) {
        document.add(new Paragraph("\n"));
        
        Paragraph footer = new Paragraph("This report was generated automatically by the Student Management Learning System.")
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY)
            .setItalic()
            .setMarginTop(30);
        
        document.add(footer);
        
        Paragraph timestamp = new Paragraph("Generated on: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss")))
            .setFontSize(8)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DARK_GREY);
        
        document.add(timestamp);
    }

    private Cell createStyledCell(String content, boolean isBold) {
        Paragraph paragraph = new Paragraph(content);
        if (isBold) {
            paragraph.setBold();
        }
        return new Cell().add(paragraph).setPadding(8);
    }

    private Cell createHeaderCell(String content) {
        return new Cell().add(new Paragraph(content).setBold())
            .setBackgroundColor(BURGUNDY)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(10);
    }

    private String getCategoryDisplayName(com.studentmanagement.model.Subject.Category category) {
        switch (category) {
            case COMPULSORY: return "Compulsory Subjects";
            case STEM_SCIENCE: return "STEM Sciences";
            case HUMANITIES: return "Humanities";
            case TECHNICAL: return "Technical Subjects";
            default: return category.name();
        }
    }

    private String getReportsDirectory() {
        String userHome = System.getProperty("user.home");
        String reportsDir = userHome + File.separator + "Downloads" + File.separator + "StudentReports";
        
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return reportsDir;
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