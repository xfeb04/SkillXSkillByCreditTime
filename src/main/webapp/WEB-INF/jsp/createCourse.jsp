<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${empty editCourseId ? 'สร้างคอร์ส' : 'แก้ไขคอร์ส'}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/create-course.css?v=2">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<script defer src="${pageContext.request.contextPath}/js/create-course.js?v=delivery-clear-3"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-createCourse">
<c:set var="uiPage" value="createCourse"/>
<%@ include file="shared/navigation.jspf" %>
<main class="course-shell">
<h1>${empty editCourseId ? 'สร้างคอร์ส' : 'แก้ไขคอร์ส'}</h1><p class="intro">แบ่งปันทักษะของคุณกับชุมชน</p>
<c:if test="${not empty errors}"><div class="form-errors" role="alert"><strong>กรุณาตรวจสอบข้อมูล</strong><ul><c:forEach items="${errors}" var="error"><li><c:out value="${error}"/></li></c:forEach></ul></div></c:if>
<form action="${pageContext.request.contextPath}${empty editCourseId ? '/createCourse' : '/editCourse/'}${editCourseId}" method="post" id="course-form">
<section class="form-card"><h2>หมวดหมู่คอร์ส</h2>
<label for="categoryId">หมวดหมู่</label><select id="categoryId" name="categoryId" required><option value="">เลือกหมวดหมู่คอร์ส</option><c:forEach items="${categories}" var="category"><option value="${category.categoryId}" <c:if test="${category.categoryId eq form.categoryId}">selected</c:if>><c:out value="${category.categoryName}"/></option></c:forEach></select>
</section>
<section class="form-card"><h2>รายละเอียดคอร์ส</h2>
<label for="courseName">ชื่อคอร์ส </label><input id="courseName" name="courseName" required minlength="4" maxlength="100" value="<c:out value='${form.courseName}'/>" placeholder="เช่น พื้นฐานการออกแบบ UI ด้วย Figma">
<label for="courseDescription">รายละเอียดคอร์ส </label><textarea id="courseDescription" name="courseDescription" minlength="20" maxlength="255" rows="5" required placeholder="อธิบายเนื้อหาและสิ่งที่ผู้เรียนจะได้รับ"><c:out value="${form.courseDescription}"/></textarea>
<div class="two-columns"><fieldset><legend>รูปแบบการเรียน</legend><div class="choices">
<label><input type="radio" name="delivery" value="online" required <c:if test="${form.delivery eq 'online'}">checked</c:if>><span>ออนไลน์</span></label>
<label><input type="radio" name="delivery" value="offline" <c:if test="${form.delivery eq 'offline'}">checked</c:if>><span>ออฟไลน์</span></label></div></fieldset>
<div><div id="platform-field"><label for="onlinePlatform">แพลตฟอร์มออนไลน์ </label><textarea id="onlinePlatform" name="onlinePlatform" minlength="3" maxlength="255" rows="3" placeholder="ชื่อแพลตฟอร์ม ลิงก์เข้าร่วม และรายละเอียดเพิ่มเติม"><c:out value="${form.onlinePlatform}"/></textarea></div>
<div id="location-field"><label for="location">สถานที่เรียน </label><textarea id="location" name="location" minlength="3" maxlength="255" rows="3" placeholder="อาคาร ห้อง ที่อยู่ หรือลิงก์แผนที่"><c:out value="${form.location}"/></textarea></div></div></div>
<div class="two-columns"><div><label for="requiredEquip">อุปกรณ์ที่ต้องเตรียม </label><textarea id="requiredEquip" name="requiredEquip" minlength="4" maxlength="255" required rows="2" placeholder="เช่น โน้ตบุ๊ก สมุดจด"><c:out value="${form.requiredEquip}"/></textarea></div><div><label for="maxMember">จำนวนผู้เรียนสูงสุด </label><input type="number" id="maxMember" name="maxMember" min="1" max="2147483647" step="1" required value="<c:out value='${form.maxMember}'/>"></div></div>
<fieldset><legend>รูปแบบคอร์ส </legend><div class="choices">
<label><input type="radio" name="courseType" aria-describedby="course-type-help" value="ปกติ" required <c:if test="${form.courseType eq 'ปกติ'}">checked</c:if>><span>จ่ายเครดิตปกติ</span></label>
<label><input type="radio" name="courseType" aria-describedby="course-type-help" value="เรียนฟรี" <c:if test="${form.courseType eq 'เรียนฟรี'}">checked</c:if>><span>ลงทะเบียนฟรี</span></label>
<label><input type="radio" name="courseType" aria-describedby="course-type-help" value="ผู้สอนจ่ายให้ผู้เรียน" <c:if test="${form.courseType eq 'ผู้สอนจ่ายให้ผู้เรียน'}">checked</c:if>><span>จ่ายเครดิตให้ผู้เรียน</span></label></div></fieldset>
<div id="course-type-help" class="course-type-help" aria-live="polite">
<p data-course-type-help="ปกติ" hidden>ผู้เรียนจ่ายเครดิตตามจำนวนที่คุณกำหนด คุณจะได้รับเครดิตเมื่อสอนเสร็จ</p>
<p data-course-type-help="เรียนฟรี" hidden>ผู้เรียนไม่เสียเครดิต ระบบให้คุณ <strong>2 เครดิตต่อผู้เรียนที่ได้รับอนุมัติ 1 คน</strong> โดยได้รับเมื่อสอนเสร็จ</p>
<p data-course-type-help="ผู้สอนจ่ายให้ผู้เรียน" hidden>ระบบพักเครดิตของคุณไว้ตาม <strong>จำนวนที่เปิดรับ × เครดิตต่อคน</strong> เมื่อเริ่มสอน ระบบคืนเครดิตส่วนที่ไม่ได้ใช้ให้คุณ ส่วนผู้เรียนได้รับเครดิตเมื่อสอนเสร็จ</p>
</div>
<div id="credit-balance-note" data-available-credit="${availableCourseCredit}" hidden style="padding:12px 16px;border:1px solid #d6e4f0;border-radius:8px;background:#f4f8fc" aria-live="polite">มีเครดิตคงเหลือจำนวน <strong>${creditBalance}</strong> เครดิต<c:if test="${existingCourseReserve gt 0}"><br>รวมเครดิตที่พักไว้สำหรับคอร์สนี้แล้ว สามารถใช้ได้ ${availableCourseCredit} เครดิต</c:if></div>
<label for="creditPerCourse" id="credit-label">ค่าเครดิตต่อคอร์ส </label><input type="number" id="creditPerCourse" name="creditPerCourse" min="0" step="any" required aria-describedby="credit-budget-error" value="<c:out value='${form.creditPerCourse}'/>">
<p id="credit-budget-error" class="field-error" style="color:#c62828" aria-live="polite" hidden></p>
</section>
<section class="form-card"><div class="section-heading"><h2>กำหนดการสอน</h2><button type="button" id="add-day">＋ เพิ่มวันเรียน</button></div>
<div id="schedule"><c:forEach items="${schedule}" var="row" varStatus="loop"><div class="day-row"><div class="section-heading"><h3>วันเรียนที่ <span class="day-number">${loop.count}</span></h3><button type="button" class="remove-day">ลบวันเรียน</button></div><div class="three-columns">
<label>วันที่สอน<input type="date" name="teachDate" min="${today}" required value="<c:out value='${row.date}'/>"></label>
<label>เวลาเริ่ม<input type="time" name="startTime" required value="<c:out value='${row.start}'/>"></label>
<label>เวลาสิ้นสุด<input type="time" name="endTime" required value="<c:out value='${row.end}'/>"></label>
</div></div></c:forEach></div></section>
<div class="form-actions"><a href="${pageContext.request.contextPath}/listCourse">ยกเลิก</a><button type="submit">${empty editCourseId ? 'บันทึกคอร์ส' : 'บันทึกการแก้ไข'}</button></div>
</form></main></body></html>
