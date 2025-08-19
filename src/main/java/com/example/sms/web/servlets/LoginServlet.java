package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.UserDao;
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

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(LoginServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String admission = req.getParameter("admission");
        String password = req.getParameter("password");
        if (admission == null || password == null) {
            req.setAttribute("error", "Admission and password required");
            doGet(req, resp);
            return;
        }
        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        UserDao userDao = new UserDao(ds);
        try {
            Optional<User> userOpt = userDao.findByAdmission(admission);
            if (userOpt.isPresent() && PasswordUtil.verify(password, userOpt.get().getPasswordHash())) {
                req.getSession().setAttribute("authUser", userOpt.get());
                resp.sendRedirect(req.getContextPath() + "/home");
            } else {
                req.setAttribute("error", "Invalid credentials");
                doGet(req, resp);
            }
        } catch (SQLException e) {
            log.error("Login error", e);
            resp.sendError(500);
        }
    }
}
