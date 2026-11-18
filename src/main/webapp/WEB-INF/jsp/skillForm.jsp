<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${title}"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/skill-form.css?v=3">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-skillForm">
    <c:set var="uiPage" value="skillForm"/>
<%@ include file="shared/navigation.jspf" %>
    <main class="page-shell">
        <section class="page-heading"><h1><c:out value="${title}"/></h1><p>ระบุรายละเอียดทักษะใหม่ของคุณเพื่อร่วมกิจกรรมแลกเปลี่ยนเครดิตเวลากับชุมชน</p></section>
        <form class="skill-form" action="${pageContext.request.contextPath}${formAction}" method="post" enctype="multipart/form-data">
            <label for="categoryId">หมวดหมู่ทักษะ</label><select id="categoryId" name="categoryId" required><option value="">กรุณาเลือกหมวดหมู่</option><c:forEach items="${categories}" var="category"><option value="${category.categoryId}" <c:if test="${category.categoryId eq categoryId}">selected</c:if>><c:out value="${category.categoryName}"/></option></c:forEach></select><p class="field-error"><c:out value="${errors['categoryId']}"/></p>
            <label for="skillDescription">ประสบการณ์</label><textarea id="skillDescription" name="skillDescription" maxlength="255" placeholder="กรุณาระบุรายละเอียดหรือประสบการณ์เกี่ยวกับทักษะนี้" required><c:out value="${skillDescription}"/></textarea><p class="hint">รายละเอียดไม่เกิน 255 ตัวอักษร</p><p class="field-error"><c:out value="${errors['skillDescription']}"/></p>
            <label for="skillImg">รูปภาพประกอบ <em>(ไม่บังคับ)</em></label>
            <div class="skill-image-upload" id="skill-image-upload">
                <p data-image-help>เลือกรูปภาพประกอบทักษะของคุณ</p>
                <p class="hint" data-image-help>JPG JPEG PNG ขนาดไม่เกิน 5MB</p>
                <input id="skillImg" name="skillImg" type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png" aria-describedby="skill-image-error">
                <input id="removeSkillImg" name="removeSkillImg" type="hidden" value="${removeSkillImg ? 'true' : 'false'}">
                <button type="button" id="choose-skill-image" hidden>เลือกรูปภาพ</button>
                <img id="skill-image-preview" alt="ภาพตัวอย่างทักษะ" <c:choose><c:when test="${hasExistingImage and not removeSkillImg}">src="${pageContext.request.contextPath}/skill/${skillId}/image"</c:when><c:otherwise>hidden</c:otherwise></c:choose>>
                <button type="button" id="remove-skill-image" hidden>ลบรูปภาพ</button>
                <p id="skill-image-removal" class="hint" hidden>รูปภาพจะถูกลบเมื่อกดบันทึก</p>
            </div>
            <p id="skill-image-error" class="field-error" role="alert"><c:out value="${errors['skillImg']}"/></p>
            <div class="actions"><a class="cancel-button" href="${pageContext.request.contextPath}/listSkill">ยกเลิก</a><button type="submit"><c:out value="${isEdit ? 'บันทึกการแก้ไข' : 'บันทึกทักษะ'}"/></button></div>
        </form>
    </main>
<script src="${pageContext.request.contextPath}/js/skill-form.js?v=1"></script>
</body>
</html>
