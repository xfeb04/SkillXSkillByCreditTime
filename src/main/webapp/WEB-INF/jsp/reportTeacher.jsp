<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>รายงานผู้สอน</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/report-student.css?v=1"><script defer src="${pageContext.request.contextPath}/js/report-student.js?v=image-picker-2?v=1"></script><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-reportTeacher">
<c:set var="uiPage" value="reportTeacher"/>
<%@ include file="shared/navigation.jspf" %>
<main class="report-shell"><a class="back" href="${pageContext.request.contextPath}/viewCourseDetail/${registration.course.courseId}">← ย้อนกลับ</a><h1>รายงานผู้สอน</h1><p>กรุณาระบุรายละเอียดพฤติกรรมที่ไม่เหมาะสมและแนบหลักฐานเพื่อประกอบการพิจารณา</p>
<form id="report-form" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/reportTeacher">
<input type="hidden" name="registerCourseId" value="${registration.registerCourseId}">
<label for="teacherName">ผู้สอนที่รายงาน</label><input id="teacherName" readonly value="<c:out value='${registration.course.teacher.firstname}'/> <c:out value='${registration.course.teacher.surname}'/>">
<label for="courseName">ชื่อคอร์ส</label><input id="courseName" readonly value="<c:out value='${registration.course.courseName}'/>">
<label for="imgReport">รูปหลักฐาน (ไม่บังคับ)</label><div id="drop-zone" class="upload-zone"><p data-upload-help>ลากไฟล์มาวางที่นี่ หรือเลือกรูปภาพ</p><p class="hint" data-upload-help>JPG JPEG PNG GIF ขนาดไม่เกิน 5MB</p><input id="imgReport" name="imgReport" type="file" accept=".jpg,.jpeg,.png,.gif" aria-describedby="image-error"><button id="choose-image" type="button" hidden>เลือกรูปภาพ</button><img id="image-preview" alt="ตัวอย่างรูปหลักฐาน" hidden><button id="remove-image" type="button" hidden>ลบรูปภาพ</button></div>
<p id="image-error" class="field-error" aria-live="polite"><c:out value="${errors.imgReport}"/></p>
<label for="reason">เหตุผลการรายงาน</label><textarea id="reason" name="reason" rows="6" maxlength="255" required aria-describedby="reason-error" placeholder="กรุณาระบุรายละเอียดพฤติกรรมที่ต้องการรายงาน"><c:out value="${reason}"/></textarea><p class="hint">ภาษาไทย ภาษาอังกฤษ ตัวเลข และช่องว่าง ไม่เกิน 255 ตัวอักษร</p><p id="reason-error" class="field-error" aria-live="polite"><c:out value="${errors.reason}"/></p>
<c:if test="${not empty errors}"><p class="hint">หากแนบรูปไว้ กรุณาเลือกไฟล์รูปภาพอีกครั้งก่อนส่งรายงาน</p></c:if>
<div class="actions"><a href="${pageContext.request.contextPath}/viewCourseDetail/${registration.course.courseId}">ยกเลิก</a><button type="submit">ส่งรายงาน</button></div></form></main></body></html>