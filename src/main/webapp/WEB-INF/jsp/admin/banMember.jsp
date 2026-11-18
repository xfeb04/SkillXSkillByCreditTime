<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>แบนสมาชิก</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<style>.ban-layout{display:grid;grid-template-columns:280px minmax(0,1fr);gap:24px;align-items:start;margin-top:24px}.member-picture{width:100px;height:100px;object-fit:cover;border-radius:50%;margin:auto}.member-card{text-align:center}.ban-form label{display:block;margin:16px 0 8px}.ban-form input,.ban-form textarea{width:100%;padding:12px;border:1px solid #d7e0e8;border-radius:8px;font:inherit}.ban-form textarea{min-height:180px}.field-error{font-size:.85rem;color:#c62828}.ban-button{background:#b33333;border-color:#b33333}.member-card p{overflow-wrap:anywhere}@media(max-width:700px){.ban-layout{grid-template-columns:1fr}}</style><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-banMember">
<%@ include file="navbar.jspf" %>
<main class="course-shell">
<a href="${pageContext.request.contextPath}/admin/report/${report.reportId}/detail">← กลับรายละเอียดรายงาน</a><h1>แบน<c:out value="${party}"/></h1>
<p class="error-message">สมาชิกจะไม่สามารถใช้งานบัญชีได้จนถึงวันปลดแบน กรุณาตรวจสอบหลักฐานและสาเหตุก่อนยืนยัน</p>
<div class="ban-layout"><section class="course-card member-card"><img class="member-picture" src="${pageContext.request.contextPath}/member/${report.memberId}/profile-image" alt="รูปสมาชิก" onerror="this.hidden=true"><h2><c:out value="${report.name}"/></h2><p><c:out value="${report.email}"/></p><p>Member ID: <c:out value="${report.memberId}"/></p><p>วันที่สมัคร: <c:out value="${joined}"/></p></section>
<form id="ban-form" class="course-card ban-form" method="post" action="${pageContext.request.contextPath}/admin/report/${report.reportId}/${party eq 'ผู้เรียน' ? 'banStudent' : 'banTeacher'}" novalidate><h2>รายละเอียดการแบน</h2>
<label for="unbanDate">วันที่ปลดแบน</label><input type="date" id="unbanDate" name="unbanDate" min="${minDate}" value="<c:out value='${unbanDate}'/>" required aria-describedby="date-error"><p id="date-error" class="field-error" role="alert" <c:if test="${empty errors.date}">hidden</c:if>><c:out value="${errors.date}"/></p>
<label for="reason">สาเหตุการแบน</label><textarea id="reason" name="reason" required maxlength="255" aria-describedby="reason-error"><c:out value="${reason}"/></textarea><p id="reason-error" class="field-error" role="alert" <c:if test="${empty errors.reason}">hidden</c:if>><c:out value="${errors.reason}"/></p>
<p>ไม่เกิน 255 ตัวอักษร รองรับภาษาไทย อังกฤษ ตัวเลข ช่องว่าง และ . , / # -</p>
<div class="card-actions"><button class="ban-button" type="submit">ยืนยันแบนสมาชิก</button><a class="detail-button" href="${pageContext.request.contextPath}/admin/report/${report.reportId}/detail">ยกเลิก</a></div>
</form></div></main><script>
const form = document.getElementById('ban-form'), date = document.getElementById('unbanDate'), reason = document.getElementById('reason');
form.addEventListener('submit', event => {
 const d = document.getElementById('date-error'), r = document.getElementById('reason-error'), value = reason.value.trim();
 d.textContent = !date.value || !date.validity.valid ? 'กรุณาเลือกวันปลดแบนที่เป็นวันในอนาคตเท่านั้น' : '';
 r.textContent = !value ? 'กรุณากรอกสาเหตุการแบน' : value.length > 255 ? 'สาเหตุการแบนต้องไม่เกิน 255 ตัวอักษร' : !/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s.,/#-]+$/.test(value) ? 'ใช้ได้เฉพาะภาษาไทย อังกฤษ ตัวเลข ช่องว่าง และ . , / # -' : '';
 d.hidden = !d.textContent; r.hidden = !r.textContent;
 if (!d.hidden || !r.hidden) { event.preventDefault(); (!d.hidden ? date : reason).focus(); }
 else form.dataset.confirm = 'ยืนยันแบนสมาชิกนี้จนถึงวันที่ ' + window.formatBuddhistDate(date.value) + ' หรือไม่?';
});
if (!document.getElementById('date-error').hidden) date.focus(); else if (!document.getElementById('reason-error').hidden) reason.focus();
</script></body></html>
