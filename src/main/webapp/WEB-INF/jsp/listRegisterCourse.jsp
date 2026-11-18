<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>รายการคอร์สที่ลงทะเบียนไว้</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-listRegisterCourse">
<c:set var="uiPage" value="listRegisterCourse"/>
<%@ include file="shared/navigation.jspf" %>
<main class="course-shell">
<div class="page-heading"><div><h1>รายการคอร์สที่ลงทะเบียนไว้</h1></div></div>
<c:if test="${not empty registrationSuccess}"><p class="success-message" role="status"><c:out value="${registrationSuccess}"/></p></c:if>
<c:if test="${not empty registrationError}"><p class="error-message" role="alert"><c:out value="${registrationError}"/></p></c:if>
<nav class="course-tabs" aria-label="สถานะการเรียน">
<c:url var="tabUrl" value="/listRegisterCourse"><c:param name="tab" value="registered"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'registered' ? 'active' : ''}" <c:if test="${activeTab eq 'registered'}">aria-current="page"</c:if>>คอร์สที่ลงทะเบียน</a>
<c:url var="tabUrl" value="/listRegisterCourse"><c:param name="tab" value="approved"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'approved' ? 'active' : ''}" <c:if test="${activeTab eq 'approved'}">aria-current="page"</c:if>>คอร์สที่ได้รับอนุมัติแล้ว</a>
<c:url var="tabUrl" value="/listRegisterCourse"><c:param name="tab" value="ongoing"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'ongoing' ? 'active' : ''}" <c:if test="${activeTab eq 'ongoing'}">aria-current="page"</c:if>>คอร์สที่กำลังเรียน</a>
<c:url var="tabUrl" value="/listRegisterCourse"><c:param name="tab" value="completed"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'completed' ? 'active' : ''}" <c:if test="${activeTab eq 'completed'}">aria-current="page"</c:if>>คอร์สที่เรียนเสร็จสิ้น</a>
<c:url var="tabUrl" value="/listRegisterCourse"><c:param name="tab" value="cancelled"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'cancelled' ? 'active' : ''}" <c:if test="${activeTab eq 'cancelled'}">aria-current="page"</c:if>>ยกเลิก / ถูกปฏิเสธ</a>
</nav>
<c:choose><c:when test="${empty courses}"><p class="empty-state">ยังไม่มีคอร์สในสถานะนี้</p></c:when><c:otherwise><section class="course-grid">
<c:forEach items="${courses}" var="course"><article class="course-card"><div class="card-meta"><span class="category"><c:out value="${course.categoryName}"/></span><span>${course.online ? 'Online' : 'Offline'}</span></div>
<h2><c:out value="${course.courseName}"/></h2><p class="description"><c:out value="${course.courseDescription}"/></p>
<p class="credit"><span class="type"><c:out value="${course.courseType}"/></span> <c:choose><c:when test="${course.courseType eq 'เรียนฟรี'}">ไม่มีค่าเครดิต</c:when><c:when test="${course.courseType eq 'ผู้สอนจ่ายให้ผู้เรียน'}">ให้ผู้เรียน <c:out value="${course.creditPerCourse}"/> เครดิต/คน</c:when><c:otherwise>ใช้ <c:out value="${course.creditPerCourse}"/> เครดิต</c:otherwise></c:choose></p>
<div class="course-info"><p>สถานะการลงทะเบียน: <c:out value="${course.registrationStatus}"/></p><p>เริ่มเรียน <c:out value="${course.startDate}"/></p><p>ผู้เรียนที่อนุมัติแล้ว <c:out value="${course.approvedMemberCount}"/> / <c:out value="${course.maxMember}"/> คน</p></div>
<div class="card-actions"><a class="detail-button" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">ดูรายละเอียด</a>
</div>
</article></c:forEach></section></c:otherwise></c:choose></main></body></html>
