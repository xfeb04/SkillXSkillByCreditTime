<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>รีวิวคอร์ส</title>
<style>
.review-shell{max-width:850px}.review-panel{background:white;border:1px solid #e1eaf1;border-radius:16px;padding:28px;margin:24px 0}.teacher{display:flex;align-items:center;gap:14px}.teacher img{width:64px;height:64px;border-radius:50%;object-fit:cover}.review-panel h2{text-align:center}.stars{display:flex;justify-content:center;gap:12px;border:0;margin:24px 0}.stars legend{text-align:center}.stars label{cursor:pointer;font-size:2.2rem;color:#9aa6b2}.stars input:checked+span{color:#e6a600}.stars input{accent-color:#e6a600}.review-panel textarea{display:block;width:100%;min-height:160px;background:#f3f4f6;border:1px solid #d8e0e7;border-radius:12px;padding:16px;font:inherit;margin-top:10px}.field-error{color:#c62828;font-size:.85rem}.review-panel .card-actions{margin-top:28px}.review-panel .card-actions>*{flex:1}.course-description{white-space:pre-wrap;overflow-wrap:anywhere}
</style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-reviewCourse">
<c:set var="uiPage" value="reviewCourse"/>
<%@ include file="shared/navigation.jspf" %>
<main class="course-shell review-shell">
<a href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">← กลับไปหน้ารายละเอียดคอร์ส</a>
<h1>รีวิวคอร์ส</h1>
<section class="review-panel review-course-summary" aria-labelledby="review-course-name">
<p class="review-summary-heading">รายละเอียดคอร์ส</p>
<h2 id="review-course-name"><c:out value="${course.courseName}"/></h2>
<div class="review-course-badges"><span><c:out value="${course.categoryName}"/></span><span>${course.online ? 'Online' : 'Offline'}</span><span><c:out value="${course.courseType}"/></span></div>
<p class="course-description"><c:out value="${course.courseDescription}"/></p>
<dl class="review-course-facts">
<div><dt>วันเริ่มเรียนครั้งแรก</dt><dd><c:choose><c:when test="${not empty course.schedules}"><c:out value="${course.schedules[0].date}"/></c:when><c:otherwise>ไม่ระบุ</c:otherwise></c:choose></dd></div>
<div><dt>เครดิตของคอร์ส</dt><dd><c:out value="${course.creditSummary}"/></dd></div>
</dl>
<div class="teacher review-teacher">
<div class="review-teacher-avatar"><span aria-hidden="true"><c:out value="${course.teacherInitial}"/></span><img src="${pageContext.request.contextPath}/member/${teacherId}/profile-image" alt="รูปผู้สอน" onerror="this.hidden=true"></div>
<div><small>ผู้สอน</small><strong><c:out value="${course.teacherName}"/></strong></div>
</div>
</section>
<form id="review-form" class="review-panel" action="${pageContext.request.contextPath}/reviewCourse" method="post" novalidate>
<input type="hidden" name="registrationId" value="${registrationId}">
<h2>บอกความรู้สึกของคุณต่อคอร์สนี้</h2>
<fieldset class="stars"><legend>คะแนนความพึงพอใจ</legend>
<c:forEach begin="1" end="5" var="score"><label><input type="radio" name="rating" value="${score}" aria-label="${score} ดาว" aria-describedby="rating-error" required <c:if test="${rating eq score}">checked</c:if>><span aria-hidden="true">★</span></label></c:forEach>
</fieldset>
<p id="rating-summary" class="rating-summary" aria-live="polite">เลือกคะแนน 1–5 ดาว</p>
<p id="rating-error" class="field-error" role="alert" <c:if test="${empty ratingError}">hidden</c:if>><c:out value="${ratingError}"/></p>
<label for="comment">ความคิดเห็นเพิ่มเติม (ไม่บังคับ ไม่เกิน 255 ตัวอักษร)</label>
<textarea id="comment" name="comment" maxlength="255" aria-describedby="comment-error" placeholder="แชร์ประสบการณ์การเรียนของคุณ..."><c:out value="${comment}"/></textarea>
<p id="comment-error" class="field-error" role="alert" <c:if test="${empty commentError}">hidden</c:if>><c:out value="${commentError}"/></p>
<div class="card-actions"><button class="primary-button" type="submit">ส่งรีวิว</button><a class="detail-button" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">ยกเลิก</a></div>
</form></main>
<script>
const form = document.getElementById('review-form');
const ratingInputs = [...form.querySelectorAll('input[name="rating"]')];
function paintRating() {
 const selected = Number(form.querySelector('input[name="rating"]:checked')?.value || 0);
 ratingInputs.forEach(input => input.nextElementSibling.classList.toggle('filled', Number(input.value) <= selected));
 document.getElementById('rating-summary').textContent = selected ? selected + ' จาก 5 ดาว' : 'เลือกคะแนน 1–5 ดาว';
 if (selected) document.getElementById('rating-error').hidden = true;
}
ratingInputs.forEach(input => input.addEventListener('change', paintRating));
paintRating();
form.addEventListener('submit', function(event) {
 const rating = form.querySelector('input[name="rating"]:checked');
 const comment = document.getElementById('comment');
 const ratingError = document.getElementById('rating-error');
 const commentError = document.getElementById('comment-error');
 ratingError.hidden = !!rating;
 ratingError.textContent = rating ? '' : 'กรุณากรอกคะแนนให้ถูกต้อง';
 commentError.hidden = comment.value.length <= 255;
 commentError.textContent = commentError.hidden ? '' : 'ความคิดเห็นต้องไม่เกิน 255 ตัวอักษร';
 if (!rating || !commentError.hidden) {
   event.preventDefault();
   (!rating ? form.querySelector('input[name="rating"]') : comment).focus();
 }
});
</script></body></html>
