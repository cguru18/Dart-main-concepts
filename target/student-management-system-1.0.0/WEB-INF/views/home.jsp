<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Home"/>
<c:if test="${not empty error}"><div class="badge" style="margin:12px 0;">${error}</div></c:if>

<div class="grid grid-3">
    <div class="card">
        <h3>Profile</h3>
        <div class="flex flex-col">
            <span>Name: ${sessionScope.authUser.fullName}</span>
            <span>Admission: ${sessionScope.authUser.admissionNumber}</span>
            <span>Email: ${sessionScope.authUser.email}</span>
            <a class="badge" href="${pageContext.request.contextPath}/subjects" style="width:max-content;margin-top:8px;">Edit Subjects</a>
        </div>
    </div>
    <div class="card">
        <h3>Subjects</h3>
        <c:forEach var="s" items="${selectedSubjects}">
            <span class="badge" style="margin-right:6px;">${s.displayName}</span>
        </c:forEach>
    </div>
    <div class="card">
        <h3>Exams</h3>
        <ul>
            <c:forEach var="e" items="${exams}">
                <li>
                    <span>${e.name} (Term ${e.term}, ${e.year})</span>
                    <a class="badge" href="${pageContext.request.contextPath}/exam?id=${e.id}" style="margin-left:8px;">View Details</a>
                    <a class="badge" href="${pageContext.request.contextPath}/report.pdf?examId=${e.id}" style="margin-left:8px;">Generate PDF</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>

<div class="card" style="margin-top:16px;">
    <h3>Requirements</h3>
    <div class="grid grid-2">
        <div>
            <h4>Functional</h4>
            <ul>
                <li>User sign-up and login</li>
                <li>Subject selection with constraints</li>
                <li>Exam results view and ranking</li>
                <li>PDF report generation</li>
                <li>Role-based dashboard</li>
            </ul>
        </div>
        <div>
            <h4>Non-Functional</h4>
            <ul>
                <li>Responsive UI for mobile and desktop</li>
                <li>Secure password storage (bcrypt)</li>
                <li>Connection pooling</li>
                <li>Graceful error handling</li>
                <li>Logging for observability</li>
            </ul>
        </div>
    </div>
</div>
</jsp:body>
