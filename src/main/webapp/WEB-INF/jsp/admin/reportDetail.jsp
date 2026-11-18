<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>ข้อมูลรายงาน</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1"><style>.report-layout{display:grid;grid-template-columns:minmax(0,1.2fr) minmax(0,1fr);gap:24px;margin-top:24px;align-items:start}.report-layout p{line-height:1.8;overflow-wrap:anywhere}.evidence{width:100%;max-height:450px;object-fit:contain;border-radius:12px}.multiline{white-space:pre-wrap}.report-layout h3{font-size:1rem}.danger{color:#b33333;border-color:#b33333}@media(max-width:760px){.report-layout{grid-template-columns:1fr}}</style><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-reportDetail">
<%@ include file="navbar.jspf" %>
<main class="course-shell">
<a href="${pageContext.request.contextPath}/admin/listReport${type}">← กลับรายการรายงาน</a>
<h1>รายละเอียดรายงาน</h1>
<div class="report-layout">
<section class="report-info-card" aria-labelledby="reported-course-title">
<div class="report-badges"><span><c:out value="${course.categoryName}"/></span><span>${course.online ? 'Online' : 'Offline'}</span></div>
<p class="report-label">คอร์สที่เกี่ยวข้อง</p>
<h2 id="reported-course-title"><c:out value="${course.courseName}"/></h2>
<p class="report-description multiline"><c:out value="${course.courseDescription}"/></p>
<dl class="report-facts">
<div><dt>ประเภทคอร์ส</dt><dd><c:out value="${course.courseType}"/></dd></div>
<div><dt>เครดิต</dt><dd><c:out value="${course.creditSummary}"/></dd></div>
<div><dt>สถานะคอร์ส</dt><dd><span class="report-status"><c:out value="${courseStatus}"/></span></dd></div>
<div><dt>จำนวนผู้เรียนสูงสุด</dt><dd><c:out value="${course.maxMember}"/> คน</dd></div>
<div class="report-fact-wide"><dt>ผู้สอน</dt><dd><c:out value="${course.teacherName}"/></dd></div>
<div class="report-fact-wide"><dt><c:out value="${course.accessDetailLabel}"/></dt><dd class="multiline"><c:out value="${course.accessDetail}"/></dd></div>
<div class="report-fact-wide"><dt>อุปกรณ์ที่ต้องเตรียม</dt><dd class="multiline"><c:out value="${course.requiredEquip}"/></dd></div>
</dl></section>
<aside class="report-info-card" aria-labelledby="report-info-title">
<h2 id="report-info-title">ข้อมูลรายงาน${type eq 'Student' ? 'ผู้เรียน' : 'ผู้สอน'}</h2>
<div class="reported-person"><span class="report-label">ผู้ถูกรายงาน</span><strong><c:out value="${report.name}"/></strong><span class="report-email"><c:out value="${report.email}"/></span></div>
<dl class="report-facts report-single">
<div><dt>วันที่รายงาน</dt><dd><c:out value="${report.date}"/></dd></div>
<div><dt>สาเหตุการรายงาน</dt><dd class="report-reason multiline"><c:out value="${report.reason}"/></dd></div>
</dl>
<c:if test="${not empty evidence}"><div class="report-evidence"><h3>รูปหลักฐาน</h3><img class="evidence" src="data:${evidenceMime};base64,${evidence}" alt="รูปหลักฐานประกอบรายงาน"></div></c:if>
<div class="report-footer"><a class="report-ban-link" href="${pageContext.request.contextPath}/admin/report/${report.reportId}/ban">ดำเนินการแบน</a></div>
</aside></div></main></body></html>
