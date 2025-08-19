<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    Object user = session.getAttribute("authUser");
    if (user != null) {
        response.sendRedirect(path + "/home");
        return;
    } else {
        response.sendRedirect(path + "/login");
        return;
    }
%>
