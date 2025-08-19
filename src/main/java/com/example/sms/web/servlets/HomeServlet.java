package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.ExamDao;
import com.example.sms.dao.SubjectSelectionDao;
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
import java.util.List;
import java.util.Set;

@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        SubjectSelectionDao selDao = new SubjectSelectionDao(ds);
        ExamDao examDao = new ExamDao(ds);
        try {
            Set<Subject> selected = selDao.getSubjectsForUser(user.getId());
            req.setAttribute("selectedSubjects", selected);
            req.setAttribute("exams", examDao.listAll());
        } catch (SQLException e) {
            req.setAttribute("error", "Failed to load data");
        }
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
