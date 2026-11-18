<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ประวัติเครดิต</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/credit-time.css?v=4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/member-navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-viewCreditTransaction">
    <c:set var="uiPage" value="viewCreditTransaction"/>
<%@ include file="shared/navigation.jspf" %>
    <main class="page-shell">
        <a class="back-link" href="${pageContext.request.contextPath}/viewCreditTime">← กลับไปหน้าเครดิตเวลา</a>
        <section class="credit-overview" aria-label="ยอดเครดิตของฉัน">
            <div class="credit-total"><h2>เครดิตคงเหลือ</h2><p>${creditBalance} <small>เครดิต</small></p></div>
            <div class="credit-total frozen-total"><h2>เครดิตที่พักไว้</h2><p>${creditFrozen} <small>เครดิต</small></p><span>รอการอนุมัติหรือดำเนินการตามเงื่อนไขคอร์ส</span></div>
        </section>
        <section class="transaction-section">
            <h1>ประวัติเครดิต</h1>
            <nav class="transaction-tabs" aria-label="ประเภทประวัติเครดิต">
                <a class="${activeTab eq 'spent' and activeStatus eq 'completed' ? 'active' : ''}" href="${pageContext.request.contextPath}/viewCreditTransaction?tab=spent&amp;status=completed" <c:if test="${activeTab eq 'spent' and activeStatus eq 'completed'}">aria-current="page"</c:if>>ใช้เครดิตแล้ว</a>
                <a class="${activeTab eq 'spent' and activeStatus eq 'pending' ? 'active' : ''}" href="${pageContext.request.contextPath}/viewCreditTransaction?tab=spent&amp;status=pending" <c:if test="${activeTab eq 'spent' and activeStatus eq 'pending'}">aria-current="page"</c:if>>เครดิตที่พักไว้</a>
                <a class="${activeTab eq 'received' and activeStatus eq 'pending' ? 'active' : ''}" href="${pageContext.request.contextPath}/viewCreditTransaction?tab=received&amp;status=pending" <c:if test="${activeTab eq 'received' and activeStatus eq 'pending'}">aria-current="page"</c:if>>รอรับเครดิต</a>
                <a class="${activeTab eq 'received' and activeStatus eq 'completed' ? 'active' : ''}" href="${pageContext.request.contextPath}/viewCreditTransaction?tab=received&amp;status=completed" <c:if test="${activeTab eq 'received' and activeStatus eq 'completed'}">aria-current="page"</c:if>>ได้รับเครดิตแล้ว</a>
            </nav>
            <c:if test="${activeTab eq 'received' and activeStatus eq 'pending'}">
                <p class="pending-credit-note">เครดิตรายการนี้ยังไม่เข้ายอดคงเหลือของคุณ โดยจะได้รับเมื่อสอนเสร็จตามเงื่อนไขคอร์ส</p>
            </c:if>
            <c:choose>
                <c:when test="${empty transactions}"><div class="empty-state"><c:out value="${emptyMessage}"/></div></c:when>
                <c:otherwise>
                    <div class="transaction-list">
                        <c:forEach items="${transactions}" var="transaction">
                            <article class="transaction-item">
                                <div><strong><c:out value="${transactionLabel}"/> · <c:out value="${transaction.course.courseName}"/></strong><p><c:out value="${transaction.tranDescription}"/></p><small><c:out value="${transaction.creditTranDateDisplay}"/></small></div>
                                <div class="transaction-summary"><strong><c:out value="${transaction.amount}"/> เครดิต</strong><span><c:out value="${transaction.creditTranStatus}"/></span></div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</body>
</html>
