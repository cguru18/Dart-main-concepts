package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.ExamDao;
import com.example.sms.dao.ScoreDao;
import com.example.sms.model.Exam;
import com.example.sms.model.Subject;
import com.example.sms.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "ExamDetailsServlet", urlPatterns = "/exam")
public class ExamDetailsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        ExamDao examDao = new ExamDao(ds);
        ScoreDao scoreDao = new ScoreDao(ds);
        List<Exam> exams;
        try {
            exams = examDao.listAll();
        } catch (SQLException e) {
            resp.sendError(500);
            return;
        }
        req.setAttribute("exams", exams);

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                long examId = Long.parseLong(idParam);
                Optional<Exam> examOpt = examDao.findById(examId);
                if (examOpt.isPresent()) {
                    User u = (User) req.getSession().getAttribute("authUser");
                    Map<Subject, Integer> myScores = scoreDao.scoresBySubjectForUser(examId, u.getId());
                    Map<Subject, Integer> counts = scoreDao.studentCountBySubject(examId);
                    Map<Subject, Integer> positions = new EnumMap<>(Subject.class);
                    for (Subject s : Subject.values()) {
                        if (myScores.containsKey(s)) {
                            positions.put(s, scoreDao.positionForUserBySubject(examId, u.getId(), s));
                        }
                    }
                    req.setAttribute("selectedExam", examOpt.get());
                    req.setAttribute("myScores", myScores);
                    req.setAttribute("subjectCounts", counts);
                    req.setAttribute("positions", positions);
                }
            } catch (Exception ignored) {
                // ignore parse or SQL error and just render page without details
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/exam.jsp").forward(req, resp);
    }
}