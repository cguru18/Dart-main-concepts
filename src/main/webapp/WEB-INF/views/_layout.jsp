<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${pageTitle}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/styles.css"/>
</head>
<body>
<div class="app-shell">
    <div class="topbar">
        <div class="brand">SMS</div>
        <div class="flex items-center">
            <c:choose>
                <c:when test="${not empty sessionScope.authUser}">
                    <span class="badge">${sessionScope.authUser.fullName}</span>
                    <form method="post" action="${pageContext.request.contextPath}/logout" style="margin:0 0 0 12px;">
                        <button class="primary" type="submit">Logout</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login">Login</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="container">
        <jsp:doBody/>
    </div>
</div>
</body>
</html>
