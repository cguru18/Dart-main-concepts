<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Sign Up"/>
<div class="card" style="max-width:520px;margin:40px auto;">
    <h2>Create Account</h2>
    <c:if test="${not empty error}"><div class="badge" style="margin-bottom:12px;">${error}</div></c:if>
    <form method="post">
        <div class="grid grid-2">
            <div class="flex flex-col">
                <label>Admission Number</label>
                <input type="text" name="admission" required>
            </div>
            <div class="flex flex-col">
                <label>Email</label>
                <input type="email" name="email" required>
            </div>
            <div class="flex flex-col">
                <label>Full Names</label>
                <input type="text" name="fullName" required>
            </div>
            <div class="flex flex-col">
                <label>Password (min 8 chars)</label>
                <input type="password" name="password" minlength="8" required>
            </div>
        </div>
        <div class="flex justify-between items-center" style="margin-top:16px;">
            <button class="primary" type="submit">Create Account</button>
            <a href="${pageContext.request.contextPath}/login">Already have an account?</a>
        </div>
    </form>
</div>
</jsp:body>
