<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>คำขอลงทะเบียน</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/approve-request.css?v=1"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-approveRequest">
<c:set var="uiPage" value="approveRequest"/>
<%@ include file="shared/navigation.jspf" %>
<main class="request-shell"><a class="back-link" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">← ย้อนกลับ</a>
<h1>คำขอลงทะเบียน</h1><p>ตรวจสอบและอนุมัติผู้ที่ต้องการเข้าร่วมคอร์สของคุณ</p><p class="course-name"><c:out value="${course.courseName}"/></p>
<c:if test="${not empty requestSuccess}"><p class="notice success" role="status"><c:out value="${requestSuccess}"/></p></c:if>
<c:if test="${not empty requestError}"><p class="notice error" role="alert"><c:out value="${requestError}"/></p></c:if>
<section class="capacity"><h2>♙ ผู้เรียนที่ได้รับการอนุมัติแล้ว</h2><div><progress value="${approvedCount}" max="${course.maxMember}" aria-label="จำนวนผู้เรียนที่อนุมัติแล้ว"></progress><strong>${approvedCount} / <c:out value="${course.maxMember}"/></strong></div></section>
<c:if test="${not canAccept}"><p class="notice">ไม่สามารถรับผู้เรียนเพิ่มได้ เนื่องจากคอร์สเต็มหรือเริ่มเรียนแล้ว</p></c:if>
<section class="request-list" aria-label="คำขอรออนุมัติ"><c:choose><c:when test="${empty requests}"><p class="empty">ไม่มีคำขอที่รออนุมัติ</p></c:when><c:otherwise><c:forEach items="${requests}" var="request">
<article class="request-card"><div class="student-avatar"><c:choose><c:when test="${not empty request.member.imgProfile}"><img src="${pageContext.request.contextPath}/member/${request.member.memberId}/profile-image" alt="รูปผู้เรียน"></c:when><c:otherwise>♙</c:otherwise></c:choose></div>
<div class="student-info"><h2><c:out value="${request.member.firstname}"/> <c:out value="${request.member.surname}"/> <span class="badge">รออนุมัติ</span></h2><p><c:out value="${request.member.email}"/></p>
<p>★ <c:out value="${learnerRatings[request.member.memberId]}"/></p>
<p>เคยเรียนจบแล้ว <strong><c:out value="${completedCourseCounts[request.member.memberId]}"/></strong> คอร์ส</p></div>
<form class="actions" method="post" action="${pageContext.request.contextPath}/approveRequest"><input type="hidden" name="courseId" value="${course.courseId}"><input type="hidden" name="requestId" value="${request.registerCourseId}"><button class="reject" name="action" value="reject" type="submit">✕ ปฏิเสธ</button><button class="accept" name="action" value="accept" type="submit" <c:if test="${not canAccept}">disabled</c:if>>✓ ยอมรับ</button></form>
</article></c:forEach></c:otherwise></c:choose></section></main></body></html>
