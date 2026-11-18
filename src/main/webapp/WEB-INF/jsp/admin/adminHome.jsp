<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>หน้าหลักผู้ดูแลระบบ</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+Thai:wght@400;500;600;700&display=swap">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; background: #f6f9fc; color: #183047; font-family: "Noto Sans Thai", Tahoma, sans-serif; }
        header { padding: 24px; background: white; border-bottom: 1px solid #e1eaf1; font-weight: 700; }
        .brand { max-width: 1100px; margin: auto; color: #0d67ba; }
        main { max-width: 1100px; margin: auto; padding: 56px 24px; }
        h1 { color: #0d67ba; font-size: clamp(1.6rem, 4vw, 2.2rem); }
        .subtitle { color: #66788a; line-height: 1.7; }
        .menu { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 24px; margin-top: 36px; }
        .menu a { display: flex; min-height: 170px; align-items: center; justify-content: center; padding: 28px; border: 1px solid #dce8f3; border-radius: 16px; background: white; color: #0d67ba; text-align: center; text-decoration: none; font-weight: 600; line-height: 1.8; box-shadow: 0 6px 20px #214c6f0d; }
        .menu a:hover { background: #eaf3ff; border-color: #0d67ba; }
        .menu a:focus-visible { outline: 3px solid #0d67ba; outline-offset: 4px; }
        @media (max-width: 720px) { .menu { grid-template-columns: 1fr; } .menu a { min-height: 110px; } }
    </style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-adminHome">
<%@ include file="navbar.jspf" %>


<main>
    <h1>ยินดีต้อนรับ ผู้ดูแลระบบ</h1>
    <p class="subtitle">บัญชี <c:out value="${sessionScope.adminUsername}"/> · เลือกรายการที่ต้องการจัดการ</p>
    <nav class="menu" aria-label="เมนูผู้ดูแลระบบ">
        <a href="${pageContext.request.contextPath}/admin/listCategory">จัดการหมวดหมู่</a>
        <a href="${pageContext.request.contextPath}/admin/listReportStudent">รายการรายงานที่แจ้งเรื่องผู้เรียน</a>
        <a href="${pageContext.request.contextPath}/admin/listReportTeacher">รายการรายงานที่แจ้งเรื่องผู้สอน</a>
    </nav>
</main>
</body>
</html>
