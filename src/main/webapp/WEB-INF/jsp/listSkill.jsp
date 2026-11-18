<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>รายการทักษะของฉัน</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-skill.css?v=2">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-listSkill">
    <c:set var="uiPage" value="listSkill"/>
<%@ include file="shared/navigation.jspf" %>
    <main class="page-shell">
        <a class="back-link" href="${pageContext.request.contextPath}/viewProfile">← กลับไปหน้าโปรไฟล์</a>
        <section class="page-heading"><div><h1>รายการทักษะของฉัน</h1><p>แสดงทักษะที่คุณต้องการแบ่งปันให้กับชุมชน</p></div><a class="add-button" href="${pageContext.request.contextPath}/addSkill">＋ เพิ่มทักษะ</a></section>
        <c:choose>
            <c:when test="${empty skills}"><div class="empty-state">คุณยังไม่มีรายการทักษะ</div></c:when>
            <c:otherwise>
                <section class="skill-grid">
                    <c:forEach items="${skills}" var="skill">
                        <article class="skill-card">
                            <c:if test="${not empty skill.skillImg}"><img class="skill-image" src="${pageContext.request.contextPath}/skill/${skill.skillId}/image" alt="รูปทักษะ ${skill.category.categoryName}"></c:if>
                            <div class="skill-content"><h2><c:out value="${skill.category.categoryName}"/></h2><p class="skill-description-scroll" tabindex="0" role="region" aria-label="คำอธิบายทักษะ"><c:out value="${skill.skillDescription}"/></p><a class="edit-button" href="${pageContext.request.contextPath}/editSkill/${skill.skillId}">✎ แก้ไขทักษะ</a></div>
                        </article>
                    </c:forEach>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
    <c:if test="${not empty skillSuccess}"><span hidden data-app-alert><c:out value="${skillSuccess}"/></span></c:if>
</body>
</html>
