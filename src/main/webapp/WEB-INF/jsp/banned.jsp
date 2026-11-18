<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="th"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>บัญชีถูกระงับ</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/err.css">
<style>.ban-reason{white-space:pre-wrap;overflow-wrap:anywhere;text-align:left;padding:16px;background:#f6f9fc;border-radius:10px}.actions form{flex:1}.actions button{width:100%;font:inherit;cursor:pointer}</style><link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head><body class="app-ui ui-banned">
<main class="error-card"><div class="error-icon" aria-hidden="true">!</div><h1>บัญชีของคุณถูกแบน</h1>
<p>คุณถูกระงับการใช้งานถึงวันที่ <strong><c:out value="${banUntil}"/></strong></p>
<h2>สาเหตุการแบน</h2><p class="ban-reason"><c:out value="${banReason}" default="ไม่ระบุสาเหตุ"/></p>
<p>เมื่อถึงวันปลดแบนตามเวลาไทย คุณสามารถเข้าสู่ระบบอีกครั้งได้</p>
<div class="actions"><form method="post" action="${pageContext.request.contextPath}/logoutMember"><button class="button secondary" type="submit">ออกจากระบบ</button></form></div>
</main></body></html>
