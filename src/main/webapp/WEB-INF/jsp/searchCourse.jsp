<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ค้นหาคอร์ส</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/search-course.css?v=teacher-image-2">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-searchCourse">
    <c:set var="uiPage" value="searchCourse"/>
<%@ include file="shared/navigation.jspf" %>

    <main class="page-shell">
        <section class="hero">
            
            <h1>ค้นหาคอร์สที่น่าสนใจ</h1>
            

            <form id="search-form" class="search-bar" action="${pageContext.request.contextPath}/searchCourse" method="get" novalidate>
                <label class="search-field" for="keyword">
                    <span aria-hidden="true">⌕</span>
                    <input id="keyword" name="keyword" value="<c:out value='${keyword}'/>" placeholder="ค้นหาทักษะหรือคอร์สที่อยากเรียน...">
                </label>
                <button type="submit">ค้นหา</button>
            </form>
        </section>

        <section class="filter-panel" aria-label="ตัวกรองคอร์ส">
            <form id="filter-form" action="${pageContext.request.contextPath}/searchCourse" method="get">
                <input type="hidden" name="keyword" value="<c:out value='${keyword}'/>">
                <div class="filter-row category-row">
                    <span class="filter-label"><span aria-hidden="true">⌂</span> หมวดหมู่คอร์ส</span>
                    <select name="categoryId" onchange="this.form.requestSubmit()" aria-label="เลือกหมวดหมู่คอร์ส">
                        <option value="">ทั้งหมด</option>
                        <c:forEach items="${categories}" var="category">
                            <option value="${category.categoryId}" <c:if test="${category.categoryId eq categoryId}">selected</c:if>><c:out value="${category.categoryName}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="filter-row">
                    <label class="filter-label" for="delivery"><span aria-hidden="true">◉</span> รูปแบบการเรียน</label>
                    <select id="delivery" name="delivery" onchange="this.form.requestSubmit()">
                        <option value="all" ${delivery eq 'all' ? 'selected' : ''}>ทั้งหมด</option>
                        <option value="online" ${delivery eq 'online' ? 'selected' : ''}>Online</option>
                        <option value="offline" ${delivery eq 'offline' ? 'selected' : ''}>Offline</option>
                    </select>
                </div>

                <div class="filter-row course-type-row">
                    <label class="filter-label" for="courseType"><span aria-hidden="true">✦</span> ประเภทคอร์ส</label>
                    <select id="courseType" name="courseType" onchange="this.form.requestSubmit()">
                        <option value="all" ${courseType eq 'all' ? 'selected' : ''}>ทั้งหมด</option>
                        <option value="normal" ${courseType eq 'normal' ? 'selected' : ''}>ปกติ</option>
                        <option value="free" ${courseType eq 'free' ? 'selected' : ''}>เรียนฟรี</option>
                        <option value="teacher-paid" ${courseType eq 'teacher-paid' ? 'selected' : ''}>ผู้สอนจ่ายให้ผู้เรียน</option>
                    </select>
                </div>
            </form>
        </section>

        <section class="course-section" aria-live="polite">
            <div class="course-heading">
                <h2>คอร์สที่เปิดรับสมัคร</h2>
                <span>${courses.size()} คอร์ส</span>
            </div>
            <c:choose>
                <c:when test="${empty courses}">
                    <div class="empty-state">ยังไม่พบคอร์สที่ตรงกับการค้นหา ลองเปลี่ยนคำค้นหาหรือตัวกรองอีกครั้ง</div>
                </c:when>
                <c:otherwise>
                    <div class="course-grid">
                        <c:forEach items="${courses}" var="course">
                            <article class="course-card">
                                <div class="card-meta">
                                    <span class="category-tag"><c:out value="${course.categoryName}"/></span>
                                    <span class="delivery-tag">${course.online ? '◉ Online' : '⌖ Offline'}</span>
                                </div>
                                <h3><c:out value="${course.courseName}"/></h3>
                                <p class="description"><c:out value="${course.courseDescription}"/></p>
                                <div class="course-benefits">
                                    <span class="type-badge ${course.courseTypeKey}"><c:out value="${course.courseTypeLabel}"/></span>
                                    <span class="credit-label"><c:out value="${course.creditLabel}"/></span>
                                </div>
                                <c:choose>
                                    <c:when test="${not empty sessionScope.memberId}">
                                        <a class="teacher" href="${pageContext.request.contextPath}/viewProfile/${course.teacherId}" aria-label="ดูโปรไฟล์ผู้สอน ${course.teacherName}">
                                            <div class="avatar"><c:out value="${course.teacherInitial}"/><img src="${pageContext.request.contextPath}/member/${course.teacherId}/profile-image" alt="" loading="lazy" onerror="this.hidden=true"></div>
                                            <div><strong><c:out value="${course.teacherName}"/></strong><small>★ <c:out value="${course.teacherRating}"/></small></div>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="teacher">
                                            <div class="avatar"><c:out value="${course.teacherInitial}"/><img src="${pageContext.request.contextPath}/member/${course.teacherId}/profile-image" alt="" loading="lazy" onerror="this.hidden=true"></div>
                                            <div><strong><c:out value="${course.teacherName}"/></strong><small>★ <c:out value="${course.teacherRating}"/></small></div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                <div class="course-actions">
                                    <a class="detail-button" href="${pageContext.request.contextPath}/viewCourseDetail/${course.courseId}" aria-label="ดูรายละเอียดคอร์ส ${course.courseName}">ดูรายละเอียด</a>
                                    <c:if test="${not empty sessionScope.memberId and sessionScope.memberId ne course.teacherId and not registeredCourseIds.contains(course.courseId)}">
                                        <form class="register-form" action="${pageContext.request.contextPath}/registerCourse/${course.courseId}" method="post">
                                            <button class="register-button" type="submit">ลงทะเบียน</button>
                                        </form>
                                    </c:if>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
    <c:if test="${not empty registrationSuccess or not empty registrationError}">
        <span hidden data-app-alert><c:out value="${not empty registrationError ? registrationError : registrationSuccess}"/></span>
    </c:if>
    <script>
        (() => {
            const keywordInput = document.getElementById('keyword');
            const allowedKeyword = /^[A-Za-z0-9\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E59 ]*$/;
            for (const id of ['search-form', 'filter-form']) {
                document.getElementById(id).addEventListener('submit', event => {
                    const input = event.target.elements.namedItem('keyword');
                    if (input.value.length <= 255 && allowedKeyword.test(input.value)) return;
                    event.preventDefault();
                    keywordInput.focus();
                    window.appAlert('กรุณากรอกคำค้นหาให้ถูกต้อง โดยใช้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข และช่องว่าง ความยาวไม่เกิน 255 ตัวอักษร');
                });
            }
        })();
    </script>
</body>
</html>
