<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>เข้าสู่ระบบ</title>
    <style>
        :root { --blue: #0d67ba; --blue-dark: #07579f; --ink: #183047; --muted: #66788a; --line: #e1eaf1; }
        * { box-sizing: border-box; } body { margin: 0; min-width: 320px; color: var(--ink); font-family: "Noto Sans Thai", Tahoma, sans-serif; }
        .login-page { display: flex; min-height: 100vh; flex-direction: column; padding: 22px; background: radial-gradient(circle at 50% 30%, #fff 0, #f6faff 40%, #e7f2fb 100%); }.page-actions { display: flex; width: 100%; max-width: 1060px; justify-content: space-between; margin: 0 auto; font-size: .86rem; font-weight: 600; }.back-link, .admin-link { color: var(--blue); text-decoration: none; }.back-link:hover, .admin-link:hover { text-decoration: underline; }
        .login-content { width: min(100%, 390px); margin: auto; padding: 30px 0 38px; text-align: center; }.brand-lockup { display: inline-flex; align-items: center; gap: 8px; color: var(--ink); font-size: .92rem; font-weight: 700; }.brand-mark { display: grid; width: 27px; height: 27px; place-items: center; border-radius: 8px; background: var(--blue); color: #fff; font-size: .83rem; }.login-content h1 { margin: 17px 0 7px; font-size: clamp(1.65rem, 5vw, 2rem); line-height: 1.35; }.subtitle { margin: 0 0 25px; color: var(--muted); font-size: .86rem; line-height: 1.65; }
        .login-card { padding: 27px; border: 1px solid #e7edf3; border-radius: 13px; background: rgba(255, 255, 255, .94); box-shadow: 0 14px 35px rgba(40, 80, 115, .11); text-align: left; }.login-card label { display: block; margin: 0 0 7px; color: #3d5365; font-size: .85rem; font-weight: 600; }.field-wrap { display: flex; height: 45px; align-items: center; gap: 9px; margin-bottom: 5px; border-radius: 7px; padding: 0 12px; background: #f0f3f6; color: #8ca0b1; }.field-wrap:has(input.has-error) { outline: 1px solid #c62828; background: #fff5f5; }.field-wrap span { font-size: .9rem; }.field-wrap input { width: 100%; border: 0; outline: 0; background: transparent; color: var(--ink); font: inherit; font-size: .82rem; }.field-wrap input::placeholder { color: #8a9aaa; }.field-error, .authentication-error { margin: 0 0 14px; color: #c62828 !important; font-size: .68rem; font-weight: 500; }.authentication-error { margin-top: 2px; }.success-message { margin: 0 0 14px; color: #25824e; font-size: .72rem; font-weight: 500; }.login-button, .register-button { display: block; width: 100%; border-radius: 7px; padding: 10px 14px; font: inherit; font-size: .86rem; font-weight: 600; text-align: center; text-decoration: none; }.login-button { border: 0; color: #fff; background: var(--blue); cursor: pointer; }.login-button:hover { background: var(--blue-dark); }.login-button span { margin-left: 5px; font-size: 1rem; }.divider { display: flex; align-items: center; gap: 10px; margin: 20px 0; color: #9aa8b5; font-size: .72rem; }.divider::before, .divider::after { height: 1px; flex: 1; background: #e6ecf1; content: ""; }.register-button { border: 1px solid #ccd7e1; color: var(--blue); background: #edf1f5; }.register-button:hover { border-color: var(--blue); background: #e5f1fd; }
        @media (max-width: 480px) { .login-page { padding: 18px 15px; }.page-actions { font-size: .78rem; }.login-content { padding-top: 42px; }.login-card { padding: 24px; } }
    </style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-loginMember">
    <main class="login-page">
        <header class="page-actions">
            <a href="${pageContext.request.contextPath}/searchCourse" class="back-link">← กลับหน้าหลัก</a>
            <a href="${pageContext.request.contextPath}/admin/loginAdmin" class="admin-link">เข้าสู่ระบบผู้ดูแล</a>
        </header>

        <section class="login-content" aria-labelledby="login-title">
            <h1 id="login-title">เข้าสู่ระบบ</h1>
            <p class="subtitle">แลกเปลี่ยนทักษะด้วยเครดิตเวลา<br></p>

            <form class="login-card" action="${pageContext.request.contextPath}/loginMember" method="post" novalidate data-login-form>
                <label for="username">ชื่อผู้ใช้</label>
                <div class="field-wrap">
                    <span aria-hidden="true"></span>
                    <input id="username" name="username" type="text" value="<c:out value='${username}'/>" autocomplete="username" placeholder="กรอกชื่อผู้ใช้ของคุณ" aria-describedby="username-error" required>
                </div>
                <p id="username-error" class="field-error" data-error-for="username" <c:if test="${empty usernameError}">hidden</c:if>><c:out value="${usernameError}"/></p>

                <label for="password">รหัสผ่าน</label>
                <div class="field-wrap">
                    <span aria-hidden="true"></span>
                    <input id="password" name="password" type="password" autocomplete="current-password" placeholder="กรอกรหัสผ่านของคุณ" aria-describedby="password-error" required>
                </div>
                <p id="password-error" class="field-error" data-error-for="password" <c:if test="${empty passwordError}">hidden</c:if>><c:out value="${passwordError}"/></p>
                <p class="authentication-error" data-authentication-error <c:if test="${empty authenticationError}">hidden</c:if>><c:out value="${authenticationError}"/></p>
                <p class="success-message" <c:if test="${empty success}">hidden</c:if>><c:out value="${success}"/></p>

                <button type="submit" class="login-button">เข้าสู่ระบบ <span aria-hidden="true">→</span></button>
                <div class="divider"><span>หรือ</span></div>
                <a href="${pageContext.request.contextPath}/registerMember" class="register-button">สมัครสมาชิก</a>
            </form>
        </section>
    </main>
    <script>
        <c:if test="${not empty success}">
            window.appAlert('ลงทะเบียนเสร็จแล้ว');
        </c:if>
        document.querySelectorAll('[data-login-form]').forEach((form) => {
            const username = form.querySelector('[name="username"]'); const password = form.querySelector('[name="password"]');
            const usernameError = form.querySelector('[data-error-for="username"]'); const passwordError = form.querySelector('[data-error-for="password"]');
            const authenticationError = form.querySelector('[data-authentication-error]');
            const showError = (input, element, message) => { input.classList.add('has-error'); element.textContent = message; element.hidden = false; };
            const clearError = (input, element) => { input.classList.remove('has-error'); element.textContent = ''; element.hidden = true; };
            [username, password].forEach((input) => input.addEventListener('input', () => { clearError(input, input === username ? usernameError : passwordError); if (authenticationError) authenticationError.hidden = true; }));
            form.addEventListener('submit', (event) => { clearError(username, usernameError); clearError(password, passwordError); if (authenticationError) authenticationError.hidden = true; let firstInvalid = null; if (!username.value.trim()) { showError(username, usernameError, 'กรุณากรอกชื่อผู้ใช้'); firstInvalid = username; } if (!password.value) { showError(password, passwordError, 'กรุณากรอกรหัสผ่าน'); firstInvalid ??= password; } if (firstInvalid) { event.preventDefault(); firstInvalid.focus(); } });
        });
    </script>
</body>
</html>
