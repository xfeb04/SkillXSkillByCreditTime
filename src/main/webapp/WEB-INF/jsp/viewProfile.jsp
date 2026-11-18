<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>โปรไฟล์ของฉัน</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/view-profile.css?v=2">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-viewProfile">
    <c:set var="uiPage" value="viewProfile"/>
<%@ include file="shared/navigation.jspf" %>

    <main class="page-shell">
        <section class="profile-card">
            <div class="profile-image"><c:choose><c:when test="${profileHasImage}"><img src="${pageContext.request.contextPath}${profileImageUrl}" alt="รูปโปรไฟล์ของ ${profile.firstname}"></c:when><c:otherwise><span><c:out value="${profileInitial}"/></span></c:otherwise></c:choose></div>
            <div class="profile-info">
                <h1><c:out value="${profile.firstname}"/> <c:out value="${profile.surname}"/></h1>
                <p class="rating">★ <strong><c:out value="${rating}"/></strong> / 5.00</p>
                <c:if test="${not empty profile.bio}"><p class="bio"><c:out value="${profile.bio}"/></p></c:if>
                <div class="contact-info"><span>✉ <c:out value="${profile.email}"/></span><span>⌕ <c:out value="${profile.phoneNumber}"/></span></div>
            </div>
            <c:if test="${isOwnProfile}"><a class="edit-button" href="${pageContext.request.contextPath}/editProfile">✎ แก้ไขข้อมูลส่วนตัว</a></c:if>
            
        </section>
        <section class="profile-section profile-reviews" aria-labelledby="profile-reviews-title">
            <div class="section-heading"><h2 id="profile-reviews-title">รีวิวจากผู้เรียน</h2>
                <div id="review-controls" hidden><button type="button" id="reviews-prev" aria-label="เลื่อนรีวิวไปทางซ้าย" aria-controls="profile-review-track">←</button><button type="button" id="reviews-next" aria-label="เลื่อนรีวิวไปทางขวา" aria-controls="profile-review-track">→</button></div>
            </div>
            <c:choose><c:when test="${empty reviews}"><p class="profile-review-empty">ยังไม่มีความคิดเห็นจากผู้เรียน</p></c:when><c:otherwise>
            <div id="profile-review-track" class="profile-review-track" tabindex="0" role="region" aria-label="รายการรีวิว เลื่อนซ้ายหรือขวาเพื่ออ่านเพิ่มเติม">
                <c:forEach items="${reviews}" var="review">
                <article class="profile-review-card">
                    <div class="profile-review-top"><span class="profile-review-stars" aria-label="${review.rating} จาก 5 ดาว"><c:forEach begin="1" end="5" var="star"><span class="${star le review.ratingValue ? 'filled' : ''}" aria-hidden="true">★</span></c:forEach></span><span class="profile-review-date"><c:out value="${review.reviewDate}"/></span></div>
                    <p class="profile-review-comment"><c:out value="${empty review.comment ? 'ไม่ได้ระบุความคิดเห็น' : review.comment}"/></p>
                </article>
                </c:forEach>
            </div>
            </c:otherwise></c:choose>
        </section>


        <c:if test="${not empty skills}">
            <section class="profile-section">
                <div class="section-heading"><h2>ทักษะและความชำนาญ</h2></div>
                <div class="skill-grid">
                    <c:forEach items="${skills}" var="skill" end="5">
                        <article class="skill-card">
                            <c:if test="${not empty skill.skillImg}">
                                <img class="skill-image" src="${pageContext.request.contextPath}/skill/${skill.skillId}/image" alt="รูปทักษะ <c:out value='${skill.category.categoryName}'/>" loading="lazy">
                            </c:if>
                            <div class="skill-content">
                                <h3><c:out value="${skill.category.categoryName}"/></h3>
                                <p class="skill-description-scroll" tabindex="0" role="region" aria-label="คำอธิบายทักษะ"><c:out value="${skill.skillDescription}"/></p>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty courses}">
            <section class="profile-section">
                <div class="section-heading"><h2>คอร์สที่กำลังเปิดรับลงทะเบียน</h2></div>
                <div class="course-grid">
                    <c:forEach items="${courses}" var="course" end="5">
                        <article class="course-card"><div class="course-tags"><span><c:out value="${course.categoryName}"/></span><small>${course.online ? 'Online' : 'Offline'}</small></div><h3><c:out value="${course.courseName}"/></h3><p>ผู้เรียนที่อนุมัติแล้ว <strong><c:out value="${course.approvedMemberCount}"/> / <c:out value="${course.maxMember}"/></strong> คน</p><a href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}">ดูรายละเอียด</a></article>
                    </c:forEach>
                </div>
            </section>
        </c:if>
    </main>
<c:set var="resultMessage" value="${not empty profileError ? profileError : profileSuccess}"/>
<c:set var="resultIsError" value="${not empty profileError}"/>
<%@ include file="shared/result-dialog.jspf" %>
<script src="${pageContext.request.contextPath}/js/profile-reviews.js?v=1"></script>
</body>
</html>
