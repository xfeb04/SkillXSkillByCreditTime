<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>จัดการหมวดหมู่</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<style>
.ui-listCategory .course-grid { grid-template-columns:repeat(5,minmax(0,1fr)); gap:16px; }
.ui-listCategory .course-card { min-width:0; padding:20px; }
.ui-listCategory .category-description { overflow-wrap:anywhere; }
.ui-listCategory .card-actions { margin-top:auto; padding-top:20px; }
@media(max-width:1100px) { .ui-listCategory .course-grid { grid-template-columns:repeat(3,minmax(0,1fr)); } }
@media(max-width:850px) { .ui-listCategory .course-grid { grid-template-columns:repeat(2,minmax(0,1fr)); } }
@media(max-width:560px) { .ui-listCategory .course-grid { grid-template-columns:1fr; } }
.ui-listCategory .card-actions .delete-category-button { background:#dc2626; border-color:#dc2626; color:#fff; }
.ui-listCategory .card-actions .delete-category-button:hover { background:#b91c1c; border-color:#b91c1c; color:#fff; }
</style>
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-listCategory">
<%@ include file="navbar.jspf" %>
<main class="course-shell">
<a href="${pageContext.request.contextPath}/admin/adminHome">← กลับหน้าหลักผู้ดูแลระบบ</a>
<div class="page-heading"><h1>จัดการหมวดหมู่</h1><a class="primary-button" href="${pageContext.request.contextPath}/admin/addCategory">+ เพิ่มหมวดหมู่</a></div>
<p class="category-delete-note">หมายเหตุ: หากมีทักษะหรือคอร์สอยู่ในหมวดหมู่ จะไม่สามารถลบหมวดหมู่นั้นได้</p>

<c:if test="${empty categories}"><p class="empty-state">ยังไม่มีหมวดหมู่ กดเพิ่มหมวดหมู่เพื่อเริ่มต้น</p></c:if>
<section class="course-grid" style="margin-top:28px" aria-label="รายการหมวดหมู่">
<c:forEach items="${categories}" var="category">
<article class="course-card"><h2><c:out value="${category.categoryName}"/></h2><p class="category-description"><c:out value="${empty category.cateDescription ? 'ไม่มีรายละเอียด' : category.cateDescription}"/></p>
<div class="card-actions"><a class="detail-button" href="${pageContext.request.contextPath}/admin/editCategory/${category.categoryId}">แก้ไข</a>
<c:if test="${not categoryInUse[category.categoryId]}"><a class="detail-button delete-category-button" href="${pageContext.request.contextPath}/admin/deleteCategory/${category.categoryId}">ลบ</a></c:if></div></article>
</c:forEach></section>

</main><c:set var="resultMessage" value="${not empty categoryError ? categoryError : categorySuccess}"/>
<c:set var="resultIsError" value="${not empty categoryError}"/>
<%@ include file="../shared/result-dialog.jspf" %>
</body></html>
