<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${course.courseName}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/view-course-detail.css?v=clean-desktop-1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-viewCourseDetail">
    <c:set var="uiPage" value="viewCourseDetail"/>
<%@ include file="shared/navigation.jspf" %>

    <main class="page-shell">
        
        <a class="back-link" href="${pageContext.request.contextPath}/searchCourse">← กลับไปหน้าค้นหา</a>

        <div class="detail-layout">
            <div class="content-column">
        <section class="course-intro" aria-labelledby="course-name">
            <div class="course-tags">
                <span class="delivery-tag">${course.online ? 'เรียนออนไลน์ · Online' : 'เรียนที่สถานที่ · Offline'}</span>
                <span class="category-tag"><c:out value="${course.categoryName}"/></span>
                <span class="type-tag ${course.courseTypeKey}"><c:out value="${course.courseType}"/></span>
            </div>
            <h1 id="course-name"><c:out value="${course.courseName}"/></h1>
            <div class="course-meta">
                <span class="teacher-meta"><span class="small-avatar course-teacher-avatar"><span aria-hidden="true"><c:out value="${course.teacherInitial}"/></span><img src="${pageContext.request.contextPath}/member/${course.teacherId}/profile-image" alt="?????????" onerror="this.hidden=true"></span> โดย <c:out value="${course.teacherName}"/></span>
                
            </div>
        </section>


                <section class="content-card" aria-labelledby="description-heading">
                    <h2 id="description-heading">รายละเอียดคอร์ส</h2>
                    <p class="course-description"><c:out value="${course.courseDescription}"/></p>
                    <div class="info-grid">
                        <article class="info-box">
                            
                            <div><h3>อุปกรณ์ที่ต้องเตรียม</h3><p><c:out value="${course.requiredEquip}"/></p></div>
                        </article>
                        <article class="info-box">
                            
                            <div><h3><c:out value="${course.accessDetailLabel}"/></h3><p><c:out value="${course.accessDetail}"/></p></div>
                        </article>
                    </div>
                </section>

                <section class="schedule-section" aria-labelledby="schedule-heading">
                    <h2 id="schedule-heading">ตารางการเรียน</h2>
                    <c:choose>
                        <c:when test="${empty course.schedules}"><div class="empty-schedule">ยังไม่มีตารางการเรียน</div></c:when>
                        <c:otherwise>
                            <div class="schedule-list">
                                <c:forEach items="${course.schedules}" var="schedule" varStatus="loop">
                                    <article class="schedule-card">
                                        <span class="calendar-date"><span><c:out value="${schedule.month}"/></span><strong><c:out value="${schedule.day}"/></strong></span>
                                        <div><strong>ครั้งที่ ${loop.count} · <c:out value="${schedule.date}"/></strong><span><c:out value="${schedule.time}"/></span></div>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>

            <aside class="side-column">
                <section class="enrollment-card">
<span class="course-status"><c:out value="${course.courseStatus}"/></span>
                    <p class="price-label">${course.courseTypeKey eq 'teacher-paid' ? 'สิทธิประโยชน์สำหรับผู้เรียน' : 'ค่าสมัครเรียน'}</p>
                    <p class="credit-summary ${course.courseTypeKey}"><c:out value="${course.creditSummary}"/></p>
                    <div class="price-rule"></div>
                    <p class="course-type"><c:out value="${course.courseType}"/></p><p class="enrollment-count">ผู้เรียนที่อนุมัติแล้ว <strong><c:out value="${course.approvedMemberCount}"/> / <c:out value="${course.maxMember}"/></strong> คน</p>
                    <c:if test="${canEditCourse}"><a class="register-button" href="${pageContext.request.contextPath}/editCourse/${course.courseId}">แก้ไขคอร์ส</a></c:if>
                    <c:if test="${canCompleteCourse}"><form method="post" action="${pageContext.request.contextPath}/completeCourse/${course.courseId}" data-confirm="ยืนยันว่าคอร์สนี้สอนเสร็จสิ้นแล้วหรือไม่?"><button class="register-button" type="submit">ยืนยันสอนเสร็จ</button></form></c:if>
                    <c:if test="${canViewStudents}">
                        <div class="owner-course-actions">
                            
                            <c:if test="${canManageRequests}">
                            <a class="register-button" href="${pageContext.request.contextPath}/approveRequest?courseId=${course.courseId}">อนุมัติคำขอลงทะเบียน</a>
                            </c:if>
                            <a class="register-button" href="${pageContext.request.contextPath}/listRegisterStudent?courseId=${course.courseId}">รายชื่อผู้เรียน</a>
                        </div>
                    </c:if>
                    <c:choose>
                        <c:when test="${canReviewCourse}">
                            <a class="register-button" href="${pageContext.request.contextPath}/reviewCourse?courseId=${course.courseId}">รีวิวคอร์ส</a>
                        </c:when>
                        <c:when test="${canRegister}">
                            <form action="${pageContext.request.contextPath}/registerCourse/${course.courseId}" method="post">
                                <button class="register-button" type="submit">ส่งคำขอลงทะเบียน</button>
                            </form>
                        </c:when>
                        <c:when test="${canCancelRegistration}">
                            <form action="${pageContext.request.contextPath}/cancelRegisterCourse/${registrationId}" method="post">
                                <button class="cancel-button" type="submit">ยกเลิกลงทะเบียน</button>
                            </form>
                        </c:when>
                        <c:when test="${alreadyReviewed}">
                            <p class="registration-status">คุณรีวิวคอร์สนี้แล้ว <strong><c:out value="${reviewScore}"/> / 5 คะแนน</strong></p>
                        </c:when>
                        <c:when test="${not empty registrationStatus}">
                            <p class="registration-status">สถานะคำขอ: <strong><c:out value="${registrationStatus}"/></strong></p>
                        </c:when>
                    </c:choose>
                    <c:if test="${canReportTeacher}"><a class="cancel-button" href="${pageContext.request.contextPath}/reportTeacher?registerCourseId=${registrationId}">รายงานผู้สอน</a></c:if>
                </section>
                <section class="teacher-card"><div class="teacher-card-person"><span class="small-avatar course-teacher-avatar"><span aria-hidden="true"><c:out value="${course.teacherInitial}"/></span><img src="${pageContext.request.contextPath}/member/${course.teacherId}/profile-image" alt="?????????" onerror="this.hidden=true"></span><strong><c:out value="${course.teacherName}"/></strong></div><a href="${pageContext.request.contextPath}/viewProfile/${course.teacherId}">ดูโปรไฟล์ผู้สอน <span aria-hidden="true">→</span></a></section>
            </aside>
        </div>
    </main>
    <c:if test="${not empty registrationSuccess}"><span hidden data-app-alert><c:out value="${registrationSuccess}"/></span></c:if>
    <c:if test="${not empty registrationError}"><span hidden data-app-alert><c:out value="${registrationError}"/></span></c:if>
<c:set var="resultMessage" value="${not empty courseError ? courseError : courseSuccess}"/>
<c:set var="resultIsError" value="${not empty courseError}"/>
<%@ include file="shared/result-dialog.jspf" %>
</body>
</html>
