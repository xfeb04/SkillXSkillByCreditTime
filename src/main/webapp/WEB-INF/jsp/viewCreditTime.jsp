<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>เครดิตเวลา</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/credit-time.css?v=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-viewCreditTime">
    <c:set var="uiPage" value="viewCreditTime"/>
<%@ include file="shared/navigation.jspf" %>
    <main class="page-shell">
        <a class="back-link" href="${pageContext.request.contextPath}/searchCourse">← กลับไปหน้าค้นหาคอร์ส</a>
        <section class="balance-card">
            <p>เครดิตเวลาคงเหลือ</p>
            <h1>${creditBalance} <small>เครดิต</small></h1>
            <a class="history-button" href="${pageContext.request.contextPath}/viewCreditTransaction">ดูประวัติเครดิต</a>
        </section>
        <section class="explanation-card credit-guide">
            <h2>เครดิตเวลาคืออะไร?</h2>
            <p>เครดิตเวลาเป็นหน่วยที่ใช้แลกเปลี่ยนการเรียนรู้ในระบบ คุณสามารถใช้เครดิตสมัครเรียน รับเครดิตจากการสอน หรือรับเครดิตจากการเรียนคอร์สที่ผู้สอนสนับสนุน โดยจำนวนเครดิตขึ้นอยู่กับประเภทและเงื่อนไขของคอร์ส</p>
            <h3>เครดิตใช้กับคอร์สแต่ละประเภทอย่างไร?</h3>
            <dl>
                <dt>จ่ายเครดิตปกติ</dt>
                <dd>ผู้เรียนจ่ายเครดิตตามที่ผู้สอนกำหนด ระบบพักเครดิตไว้เมื่อสมัคร และตัดออกเมื่อผู้สอนอนุมัติ ผู้สอนได้รับเครดิตเมื่อสอนเสร็จ</dd>
                <dt>ลงทะเบียนฟรี</dt>
                <dd>ผู้เรียนไม่เสียเครดิต ระบบให้ผู้สอน <strong>2 เครดิตต่อผู้เรียนที่ได้รับอนุมัติ 1 คน</strong> โดยแสดงเป็นรอรับหลังอนุมัติ และได้รับจริงเมื่อสอนเสร็จ</dd>
                <dt>จ่ายเครดิตให้ผู้เรียน</dt>
                <dd>ผู้สอนเป็นผู้สนับสนุนเครดิต ระบบพักเครดิตตั้งแต่สร้างคอร์สตาม <strong>จำนวนที่เปิดรับ × เครดิตต่อคน</strong> เมื่ออนุมัติจะจัดสรรเครดิตให้ผู้เรียนแต่ละคน เมื่อคอร์สเปลี่ยนเป็น “กำลังดำเนินการ” ระบบคืนเครดิตส่วนที่ไม่ได้ใช้ให้ผู้สอน ส่วนเครดิตที่จัดสรรให้ผู้เรียนยังเป็นรอรับ และผู้เรียนได้รับจริงเมื่อสอนเสร็จ</dd>
            </dl>
            <h3>สถานะเครดิตหมายถึงอะไร?</h3>
            <ul>
                <li><strong>เครดิตคงเหลือ:</strong> เครดิตที่นำไปใช้ได้</li>
                <li><strong>พักเครดิตไว้:</strong> เครดิตที่กันไว้ชั่วคราว ยังนำไปใช้กับรายการอื่นไม่ได้</li>
                <li><strong>จ่ายแล้ว:</strong> เครดิตที่ตัดออกจากฝั่งผู้จ่ายแล้ว ผู้รับอาจยังอยู่ระหว่างรอรับ</li>
                <li><strong>รอรับเครดิต:</strong> เครดิตที่จะได้รับเมื่อคอร์สสอนเสร็จ</li>
                <li><strong>ได้รับแล้ว:</strong> เครดิตที่เพิ่มเข้ายอดคงเหลือแล้ว</li>
            </ul>
            <h3>การคืนเครดิต</h3>
            <p>สำหรับคอร์สปกติ หากผู้เรียนยกเลิกขณะรออนุมัติ ผู้สอนปฏิเสธ หรือคำขอไม่ได้รับอนุมัติก่อนเริ่มคอร์ส ระบบจะคืนเครดิตที่พักไว้ให้ผู้เรียน</p>
        </section>
    </main>
</body>
</html>
