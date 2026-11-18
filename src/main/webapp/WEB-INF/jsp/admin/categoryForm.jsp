<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>${mode eq 'add' ? 'เพิ่มหมวดหมู่' : mode eq 'edit' ? 'แก้ไขหมวดหมู่' : 'ลบหมวดหมู่'}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/list-course.css?v=1">
<style>.category-form{max-width:700px;background:white;border:1px solid #e1eaf1;border-radius:16px;padding:28px;margin-top:24px}label{display:block;margin:18px 0 8px}input,textarea{width:100%;padding:12px;font:inherit;border:1px solid #ccd8e2;border-radius:8px}textarea{min-height:160px}input[readonly],textarea[readonly]{background:#f1f3f5;color:#66788a}.field-error{color:#c62828;font-size:.85rem}.hint{color:#66788a;font-size:.85rem}button:disabled{opacity:.5;cursor:not-allowed}</style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-categoryForm">
<%@ include file="navbar.jspf" %>
<main class="course-shell">
<a href="${pageContext.request.contextPath}/admin/listCategory">← กลับรายการหมวดหมู่</a>
<h1>${mode eq 'add' ? 'เพิ่มหมวดหมู่' : mode eq 'edit' ? 'แก้ไขหมวดหมู่' : 'ลบหมวดหมู่'}</h1>

<c:url var="action" value="/admin/saveCategory"/>
<c:if test="${mode eq 'add'}"><c:url var="action" value="/admin/addCategory"/></c:if>
<c:if test="${mode eq 'delete'}"><c:url var="action" value="/admin/deleteCategory/${category.categoryId}"/></c:if>
<form id="category-form" class="category-form" action="${action}" method="post" data-mode="${mode}" novalidate>
<c:if test="${not empty category.categoryId}"><input type="hidden" name="categoryId" value="${category.categoryId}"></c:if>
<label for="categoryName">ชื่อหมวดหมู่</label>
<input id="categoryName" name="categoryName" value="<c:out value='${category.categoryName}'/>" aria-describedby="name-error" <c:if test="${mode eq 'delete'}">readonly</c:if> required minlength="2" maxlength="50">
<p id="name-error" class="field-error" role="alert" <c:if test="${empty errors.categoryName}">hidden</c:if>><c:out value="${errors.categoryName}"/></p>
<label for="cateDescription">รายละเอียด</label>
<textarea id="cateDescription" name="cateDescription" required maxlength="255" aria-describedby="description-error" <c:if test="${mode eq 'delete'}">readonly</c:if>><c:out value="${category.cateDescription}"/></textarea>
<p id="description-error" class="field-error" role="alert" <c:if test="${empty errors.cateDescription}">hidden</c:if>><c:out value="${errors.cateDescription}"/></p>
<c:choose><c:when test="${mode eq 'delete'}">
<p class="error-message">หากมีคอร์สหรือทักษะอยู่ในหมวดหมู่นี้ จะไม่สามารถลบหมวดหมู่ได้</p>
<c:if test="${inUse}"><p role="alert">หมวดหมู่นี้กำลังถูกใช้งาน จึงไม่สามารถลบได้</p></c:if>
</c:when><c:otherwise><p class="hint">ชื่อ: ภาษาไทยหรืออังกฤษและช่องว่าง 2–50 ตัวอักษร ห้ามซ้ำ<br>รายละเอียด: ไม่เกิน 255 ตัวอักษร รองรับภาษาไทย อังกฤษ ตัวเลข ช่องว่าง และ . , / # -</p></c:otherwise></c:choose>
<div class="card-actions"><button type="submit" <c:if test="${mode eq 'delete' and inUse}">disabled</c:if>>${mode eq 'delete' ? 'ยืนยันลบหมวดหมู่' : 'บันทึก'}</button><a class="detail-button" href="${pageContext.request.contextPath}/admin/listCategory">ยกเลิก</a></div>
</form></main><script src="${pageContext.request.contextPath}/js/category-form.js"></script><c:set var="resultMessage" value="${not empty categoryError ? categoryError : categorySuccess}"/>
<c:set var="resultIsError" value="${not empty categoryError}"/>
<%@ include file="../shared/result-dialog.jspf" %>
</body></html>
