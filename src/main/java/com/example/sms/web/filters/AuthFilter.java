package com.example.sms.web.filters;

import com.example.sms.model.Role;
import com.example.sms.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of("/login", "/signup", "/assets/", "/index.jsp");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith) || path.equals("/");
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            res.sendRedirect(ctx + "/login");
            return;
        }

        // Admin path protection
        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
            res.sendError(403);
            return;
        }

        chain.doFilter(request, response);
    }
}
