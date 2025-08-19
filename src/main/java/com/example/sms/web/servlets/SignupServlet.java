package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.UserDao;
import com.example.sms.model.Role;
import com.example.sms.model.User;
import com.example.sms.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "SignupServlet", urlPatterns = "/signup")
public class SignupServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(SignupServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/signup.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String admission = req.getParameter("admission");
        String email = req.getParameter("email");
        String fullName = req.getParameter("fullName");
        String password = req.getParameter("password");

        if (admission == null || email == null || fullName == null || password == null || password.length() < 8) {
            req.setAttribute("error", "All fields are required. Password must be at least 8 characters.");
            doGet(req, resp);
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        UserDao userDao = new UserDao(ds);

        try {
            Optional<User> existing = userDao.findByAdmission(admission);
            if (existing.isPresent()) {
                req.setAttribute("error", "Admission number already exists.");
                doGet(req, resp);
                return;
            }
            User user = new User();
            user.setAdmissionNumber(admission);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(Role.STUDENT);
            long id = userDao.create(user);
            user.setId(id);
            req.getSession().setAttribute("authUser", user);
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SQLException e) {
            log.error("Error during signup", e);
            resp.sendError(500);
        }
    }
}
