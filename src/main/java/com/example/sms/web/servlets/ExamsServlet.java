package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.ExamDao;
import com.example.sms.dao.ScoreDao;
import com.example.sms.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ExamsServlet", urlPatterns = "/exams")
public class ExamsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        ExamDao examDao = new ExamDao(ds);
        ScoreDao scoreDao = new ScoreDao(ds);
        try {
            req.setAttribute("exams", examDao.listAll());
            // Per exam totals could be fetched asynchronously if needed
        } catch (SQLException e) {
            resp.sendError(500);
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/exams.jsp").forward(req, resp);
    }
}
