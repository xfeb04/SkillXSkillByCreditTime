<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>เกิดข้อผิดพลาด</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/err.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-err">
<main class="error-card">
    <div class="error-icon" aria-hidden="true">!</div>
    <h1>ขออภัย เกิดข้อผิดพลาด</h1>
    <p><c:out value="${errorMessage}" default="ตอนนี้เซิร์ฟเวอร์ขัดข้อง กรุณาลองใหม่อีกครั้งครับ"/></p>
    <p class="error-detail">Error: <c:out value="${errorCode}" default="500"/> — <c:out value="${errorTitle}" default="Internal Server Error"/></p>
    <div class="actions">
        <a id="back-button" class="button secondary" href="${pageContext.request.contextPath}/searchCourse">ย้อนกลับ</a>
        <a class="button primary" href="${pageContext.request.contextPath}/searchCourse">กลับหน้าหลัก</a>
    </div>
</main>
<script>
document.getElementById('back-button').addEventListener('click', function (event) {
    if (window.history.length > 1) {
        event.preventDefault();
        window.history.back();
    }
});
</script>
</body>
</html>
