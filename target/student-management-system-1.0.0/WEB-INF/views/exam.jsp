<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Exam Details"/>
<div class="card">
    <h3>Examination Details</h3>
    <form method="get" action="${pageContext.request.contextPath}/exam" class="flex items-center" style="gap:8px;">
        <label for="examId">Select exam</label>
        <select id="examId" name="id">
            <c:forEach var="e" items="${exams}">
                <option value="${e.id}" <c:if test="${selectedExam != null && selectedExam.id == e.id}">selected</c:if>>${e.name} (T${e.term} ${e.year})</option>
            </c:forEach>
        </select>
        <button class="primary" type="submit">View</button>
    </form>
</div>

<c:if test="${selectedExam != null}">
<div class="card" style="margin-top:12px;">
    <h4>${selectedExam.name} — Term ${selectedExam.term}, ${selectedExam.year}</h4>
    <table class="table">
        <thead>
        <tr>
            <th>Subject</th>
            <th>Your Marks</th>
            <th>Total Students</th>
            <th>Your Position</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${myScores}">
            <tr>
                <td>${s.key.displayName}</td>
                <td>${s.value}</td>
                <td><c:out value="${subjectCounts[s.key]}"/></td>
                <td><c:out value="${positions[s.key]}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</c:if>
</jsp:body>