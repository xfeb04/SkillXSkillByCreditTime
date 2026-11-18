<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>รายชื่อผู้เรียน</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-register-student.css?v=1"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-listRegisterStudent">
<c:set var="uiPage" value="listRegisterStudent"/>
<%@ include file="shared/navigation.jspf" %>
<main class="student-shell">
<a class="back-link" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">← ย้อนกลับ</a>
<header class="page-heading"><h1>รายชื่อผู้เรียน</h1><h2><c:out value="${course.courseName}"/></h2></header>
<c:if test="${not empty reportSuccess}"><p role="status"><c:out value="${reportSuccess}"/></p></c:if>
<section class="student-list" aria-label="รายชื่อผู้เรียนที่อนุมัติแล้ว">
<c:choose><c:when test="${empty students}"><p class="empty-state">ยังไม่มีผู้เรียนที่ได้รับการอนุมัติ</p></c:when><c:otherwise>
<c:forEach items="${students}" var="registration"><article class="student-card">
<div class="student-avatar"><c:choose><c:when test="${not empty registration.member.imgProfile}"><img src="${pageContext.request.contextPath}/member/${registration.member.memberId}/profile-image" alt="รูปโปรไฟล์ผู้เรียน" loading="lazy"></c:when><c:otherwise><span aria-label="ไม่มีรูปโปรไฟล์">♙</span></c:otherwise></c:choose></div>
<div class="student-info"><h3><c:out value="${registration.member.firstname}"/> <c:out value="${registration.member.surname}"/> <span class="badge"><c:out value="${registration.registerStatus}"/></span></h3><p><c:out value="${registration.member.email}"/></p></div>
<a class="report-button" href="${pageContext.request.contextPath}/reportStudent?registerCourseId=${registration.registerCourseId}" aria-label="รายงานผู้เรียน <c:out value='${registration.member.firstname}'/> <c:out value='${registration.member.surname}'/>">▤ รายงาน</a>
</article></c:forEach></c:otherwise></c:choose></section></main>
<c:if test="${not empty reportSuccess}">
    <script>window.appAlert('ส่งรายงานผู้เรียนสำเร็จ');</script>
</c:if>
</body></html>
