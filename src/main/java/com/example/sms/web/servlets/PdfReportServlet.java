package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.ScoreDao;
import com.example.sms.model.User;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;

@WebServlet(name = "PdfReportServlet", urlPatterns = "/report.pdf")
public class PdfReportServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        String examIdParam = req.getParameter("examId");
        if (examIdParam == null) {
            resp.sendError(400, "examId required");
            return;
        }
        long examId = Long.parseLong(examIdParam);

        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        ScoreDao scoreDao = new ScoreDao(ds);
        try {
            Map<Long, Integer> totals = scoreDao.totalByStudentForExam(examId);
            int myTotal = totals.getOrDefault(user.getId(), 0);
            int rank = (int) totals.entrySet().stream()
                    .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .takeWhile(id -> id != user.getId())
                    .count() + 1;

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=report.pdf");
            Document document = new Document();
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            document.add(new Paragraph("Exam Summary Report", title));
            document.add(new Paragraph("Student: " + user.getFullName()));
            document.add(new Paragraph("Admission: " + user.getAdmissionNumber()));
            document.add(new Paragraph("Total Marks: " + myTotal));
            document.add(new Paragraph("Rank: " + rank + " out of " + totals.size()));
            document.close();
        } catch (Exception e) {
            resp.sendError(500);
        }
    }
}
