<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Exams"/>
<div class="card">
    <h3>Examination Results</h3>
    <table class="table">
        <thead>
        <tr>
            <th>Exam</th>
            <th>Term</th>
            <th>Year</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="e" items="${exams}">
            <tr>
                <td>${e.name}</td>
                <td>${e.term}</td>
                <td>${e.year}</td>
                <td><a class="badge" href="${pageContext.request.contextPath}/report.pdf?examId=${e.id}">Generate PDF</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</jsp:body>
