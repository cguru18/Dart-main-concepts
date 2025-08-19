package com.example.sms.web.servlets;

import com.example.sms.config.AppContextListener;
import com.example.sms.dao.SubjectSelectionDao;
import com.example.sms.model.Subject;
import com.example.sms.model.User;
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
import java.util.EnumSet;
import java.util.Set;

@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ProfileServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        Set<Subject> selected = EnumSet.noneOf(Subject.class);
        for (Subject s : Subject.values()) {
            if (req.getParameter(s.name()) != null) selected.add(s);
        }

        String validation = validateSelection(selected);
        if (validation != null) {
            req.setAttribute("error", validation);
            doGet(req, resp);
            return;
        }

        DataSource ds = (DataSource) getServletContext().getAttribute(AppContextListener.DATA_SOURCE_ATTR);
        SubjectSelectionDao dao = new SubjectSelectionDao(ds);
        try {
            dao.replaceSubjects(user.getId(), selected);
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SQLException e) {
            log.error("Saving subjects failed", e);
            resp.sendError(500);
        }
    }

    private String validateSelection(Set<Subject> subjects) {
        long compulsoryCount = subjects.stream().filter(s -> s.getCategory() == Subject.Category.COMPULSORY).count();
        if (compulsoryCount != 3) return "You must include all 3 compulsory subjects.";

        long stem = subjects.stream().filter(s -> s.getCategory() == Subject.Category.STEM).count();
        if (stem < 2 || stem > 2) return "Pick exactly 2 STEM sciences.";

        long hum = subjects.stream().filter(s -> s.getCategory() == Subject.Category.HUMANITIES).count();
        if (hum < 1 || hum > 2) return "Pick 1 or 2 Humanities.";

        long tech = subjects.stream().filter(s -> s.getCategory() == Subject.Category.TECHNICAL).count();
        if (tech < 1 || tech > 2) return "Pick 1 or 2 Technical subjects.";

        int total = subjects.size();
        if (total < 7 || total > 8) return "Total subjects must be 7 or 8.";
        return null;
    }
}
