<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Admin Dashboard"/>
<div class="grid grid-2">
    <div class="card">
        <h3>Admin/Teacher Dashboard</h3>
        <ul>
            <li>Manage Exams</li>
            <li>Upload Scores</li>
            <li>View Student Rankings</li>
        </ul>
    </div>
    <div class="card">
        <h3>System Health</h3>
        <p>Connection pool active and logging enabled.</p>
    </div>
    <div class="card">
        <a class="badge" href="${pageContext.request.contextPath}/home">Back to Home</a>
    </div>
    </div>
</jsp:body>
