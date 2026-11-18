<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>รายการคอร์สของฉัน</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-listCourse">
<c:set var="uiPage" value="listCourse"/>
<%@ include file="shared/navigation.jspf" %>
<main class="course-shell"><div class="page-heading"><div><h1>รายการคอร์สของฉัน</h1>
</div></div>


<nav class="course-tabs" aria-label="สถานะคอร์ส"><c:url var="tabUrl" value="/listCourse"><c:param name="tab" value="created"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'created' ? 'active' : ''}" <c:if test="${activeTab eq 'created'}">aria-current="page"</c:if>>คอร์สที่สร้างแล้ว</a><c:url var="tabUrl" value="/listCourse"><c:param name="tab" value="open"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'open' ? 'active' : ''}" <c:if test="${activeTab eq 'open'}">aria-current="page"</c:if>>คอร์สที่เปิดรับสมัคร</a><c:url var="tabUrl" value="/listCourse"><c:param name="tab" value="ongoing"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'ongoing' ? 'active' : ''}" <c:if test="${activeTab eq 'ongoing'}">aria-current="page"</c:if>>คอร์สที่กำลังดำเนินการ</a><c:url var="tabUrl" value="/listCourse"><c:param name="tab" value="completed"/></c:url>
<a href="${tabUrl}" class="${activeTab eq 'completed' ? 'active' : ''}" <c:if test="${activeTab eq 'completed'}">aria-current="page"</c:if>>คอร์สที่สอนเสร็จแล้ว</a></nav>
<c:choose><c:when test="${empty courses}"><p class="empty-state">ยังไม่มีคอร์สในสถานะนี้</p></c:when><c:otherwise><section class="course-grid">
<c:forEach items="${courses}" var="course"><article class="course-card"><div class="card-meta"><span class="category"><c:out value="${course.categoryName}"/></span><span>${course.online ? 'Online' : 'Offline'}</span></div>
<h2><c:out value="${course.courseName}"/></h2><p class="description"><c:out value="${course.courseDescription}"/></p>
<p class="credit"><span class="type"><c:out value="${course.courseType}"/></span> <c:choose><c:when test="${course.courseType eq 'เรียนฟรี'}">ไม่มีค่าเครดิต</c:when><c:when test="${course.courseType eq 'ผู้สอนจ่ายให้ผู้เรียน'}">ให้ผู้เรียน <c:out value="${course.creditPerCourse}"/> เครดิต/คน</c:when><c:otherwise>ใช้ <c:out value="${course.creditPerCourse}"/> เครดิต</c:otherwise></c:choose></p>
<div class="course-info"><p>เริ่มเรียน <c:out value="${course.startDate}"/></p><p>ผู้เรียนที่อนุมัติแล้ว <c:out value="${course.approvedMemberCount}"/> / <c:out value="${course.maxMember}"/> คน</p></div>
<div class="card-actions"><a class="detail-button" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">ดูรายละเอียด</a><c:if test="${course.canOpen}"><form method="post" action="${pageContext.request.contextPath}/openCourse/${course.courseId}"><button type="submit">เปิดรับสมัคร</button></form></c:if></div>
<c:if test="${course.courseStatus eq 'เปิดรับสมัคร'}"><form class="card-actions" method="post" action="${pageContext.request.contextPath}/startCourse/${course.courseId}" data-confirm="เริ่มสอนทันทีหรือไม่? คำขอที่ยังไม่อนุมัติจะถูกยกเลิกและคืนเครดิตตามเงื่อนไข"><button type="submit" <c:if test="${course.approvedMemberCount lt 1}">disabled title="ต้องอนุมัติผู้เรียนอย่างน้อย 1 คน"</c:if>>ดำเนินการสอน</button></form><c:if test="${course.approvedMemberCount lt 1}"><p class="hint">ต้องมีผู้เรียนที่อนุมัติแล้วอย่างน้อย 1 คน</p></c:if></c:if>
<c:if test="${course.courseStatus eq 'กำลังดำเนินการ'}"><form class="card-actions" method="post" action="${pageContext.request.contextPath}/completeCourse/${course.courseId}" data-confirm="ยืนยันว่าคอร์สนี้สอนเสร็จสิ้นแล้วหรือไม่?"><button type="submit">ยืนยันสอนเสร็จ</button></form></c:if>
</article></c:forEach></section></c:otherwise></c:choose></main><c:set var="resultMessage" value="${not empty courseError ? courseError : courseSuccess}"/>
<c:set var="resultIsError" value="${not empty courseError}"/>
<%@ include file="shared/result-dialog.jspf" %>
</body></html>
