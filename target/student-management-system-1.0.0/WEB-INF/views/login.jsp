<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Login"/>
<div class="card" style="max-width:480px;margin:40px auto;">
    <h2>Student Login</h2>
    <c:if test="${not empty error}"><div class="badge" style="margin-bottom:12px;">${error}</div></c:if>
    <form method="post">
        <div class="flex flex-col">
            <label>Admission Number</label>
            <input type="text" name="admission" required>
        </div>
        <div class="flex flex-col" style="margin-top:10px;">
            <label>Password</label>
            <input type="password" name="password" required>
        </div>
        <div class="flex justify-between items-center" style="margin-top:16px;">
            <button class="primary" type="submit">Login</button>
            <a href="${pageContext.request.contextPath}/signup">New here? Sign up</a>
        </div>
    </form>
    <div style="margin-top:12px;font-size:0.9rem;color:#ccc;">Use on mobile or desktop; layout is responsive.</div>
    <div style="margin-top:4px;font-size:0.85rem;color:#aaa;">Non-functional: secure auth, responsive UI, accessibility basics.</div>
    </div>
</jsp:body>
