<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:directive.include file="_layout.jsp"/>
<jsp:body>
<c:set var="pageTitle" value="Subjects"/>
<div class="card">
    <h3>Subject Selection</h3>
    <form method="post" action="${pageContext.request.contextPath}/profile">
        <div class="grid grid-3">
            <div>
                <h4>Compulsory</h4>
                <label><input type="checkbox" name="MATHEMATICS" checked disabled> Mathematics</label><br>
                <label><input type="checkbox" name="ENGLISH" checked disabled> English</label><br>
                <label><input type="checkbox" name="KISWAHILI" checked disabled> Kiswahili</label>
            </div>
            <div>
                <h4>STEM (Pick 2)</h4>
                <label><input type="checkbox" name="PHYSICS"> Physics</label><br>
                <label><input type="checkbox" name="CHEMISTRY"> Chemistry</label><br>
                <label><input type="checkbox" name="BIOLOGY"> Biology</label><br>
                <label><input type="checkbox" name="COMPUTER_STUDIES"> Computer Studies</label>
            </div>
            <div>
                <h4>Humanities (1-2)</h4>
                <label><input type="checkbox" name="HISTORY_GOVERNMENT"> History &amp; Government</label><br>
                <label><input type="checkbox" name="GEOGRAPHY"> Geography</label><br>
                <label><input type="checkbox" name="BUSINESS_STUDIES"> Business Studies</label>
            </div>
            <div>
                <h4>Technical (1-2)</h4>
                <label><input type="checkbox" name="DRAWING_DESIGN"> Drawing &amp; Design</label><br>
                <label><input type="checkbox" name="ART_DESIGN"> Art &amp; Design</label><br>
                <label><input type="checkbox" name="MUSIC"> Music</label>
            </div>
        </div>
        <div class="flex justify-between items-center" style="margin-top:12px;">
            <button class="primary" type="submit">Save</button>
            <span class="badge">Total subjects must be 7 or 8</span>
        </div>
    </form>
</div>
</jsp:body>
