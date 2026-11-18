<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="th">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>สมัครสมาชิก</title>
    <style>
        :root { --blue:#0d67ba; --blue-dark:#07579f; --ink:#183047; --muted:#66788a; --line:#dfe8f0; --error:#c62828; }
        * { box-sizing:border-box; } body { margin:0; min-width:320px; background:radial-gradient(circle at 50% 20%,#fff 0,#f6faff 43%,#e7f2fb 100%); color:var(--ink); font-family:"Noto Sans Thai",Tahoma,sans-serif; }
        .page { min-height:100vh; padding:22px; }.page-actions { display:flex; max-width:760px; justify-content:space-between; margin:0 auto; font-size:.86rem; font-weight:600; }.back-link,.admin-link { color:var(--blue); text-decoration:none; }.back-link:hover,.admin-link:hover { text-decoration:underline; }.content { width:min(100%,680px); margin:0 auto; padding:22px 0 48px; }.brand { display:flex; justify-content:center; align-items:center; gap:8px; font-size:.92rem; font-weight:700; }.brand-mark { display:grid; width:27px; height:27px; place-items:center; border-radius:8px; background:var(--blue); color:#fff; font-size:.84rem; }.intro { margin:16px 0 25px; text-align:center; }.intro h1 { margin:0 0 5px; font-size:clamp(1.65rem,5vw,2rem); }.intro p { margin:0; color:var(--muted); font-size:.85rem; }
        .form-card { padding:28px; border:1px solid #e4ecf3; border-radius:14px; background:rgba(255,255,255,.96); box-shadow:0 14px 35px rgba(40,80,115,.11); }.form-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:0 16px; }.form-group { margin-bottom:17px; }.form-group.full { grid-column:1 / -1; }.form-group label { display:block; margin-bottom:7px; color:#3d5365; font-size:.84rem; font-weight:600; }.required { color:var(--error); }.control { width:100%; min-height:44px; border:1px solid #dce5ed; border-radius:8px; padding:10px 12px; color:var(--ink); background:#f8fafc; font:inherit; font-size:.83rem; transition:.15s; }.control:focus { outline:0; border-color:var(--blue); background:#fff; box-shadow:0 0 0 3px rgba(13,103,186,.11); }.control.is-invalid { border-color:var(--error); background:#fff6f6; }.field-error { min-height:16px; margin:4px 0 0; color:var(--error); font-size:.68rem; font-weight:500; }.profile-area { display:flex; align-items:center; gap:15px; }.preview { display:grid; width:66px; height:66px; flex:0 0 66px; place-items:center; overflow:hidden; border:1px solid #cbd9e5; border-radius:50%; background:#edf4fb; color:var(--blue); font-size:1.45rem; }.preview img { width:100%; height:100%; object-fit:cover; }.file-control { padding:9px; cursor:pointer; }.file-hint { margin:5px 0 0; color:var(--muted); font-size:.67rem; }.submit-button { width:100%; border:0; border-radius:8px; padding:12px; color:#fff; background:var(--blue); cursor:pointer; font:inherit; font-size:.9rem; font-weight:600; }.submit-button:hover { background:var(--blue-dark); }.login-link { margin:19px 0 0; color:var(--muted); text-align:center; font-size:.8rem; }.login-link a { color:var(--blue); font-weight:600; text-decoration:none; }.login-link a:hover { text-decoration:underline; }
        @media (max-width:560px) { .page { padding:18px 15px; }.page-actions { font-size:.78rem; }.content { padding-top:34px; }.form-card { padding:22px; }.form-grid { grid-template-columns:1fr; }.profile-area { align-items:flex-start; }.control { font-size:.8rem; } }
    </style>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/app-ui.css?v=thai-brand-15">
<script src="${pageContext.request.contextPath}/js/app-dialog.js?v=buddhist-picker-5"></script>
</head>
<body class="app-ui ui-registerMember">
    <main class="page">
        <header class="page-actions">
            <a class="back-link" href="${pageContext.request.contextPath}/loginMember">← กลับไปหน้าเข้าสู่ระบบ</a>
            <a class="admin-link" href="${pageContext.request.contextPath}/admin/loginAdmin">เข้าสู่ระบบผู้ดูแล</a>
        </header>

        <section class="content" aria-labelledby="register-title">
            
            <div class="intro"><h1 id="register-title">สมัครสมาชิก</h1><p>สร้างบัญชีเพื่อเริ่มแลกเปลี่ยนทักษะด้วยเครดิตเวลา</p></div>

            <form class="form-card" action="${pageContext.request.contextPath}/registerMember" method="post" enctype="multipart/form-data" novalidate data-register-form>
                <p class="field-error" <c:if test="${empty errors['form']}">hidden</c:if>><c:out value="${errors['form']}"/></p>
                <div class="form-grid">
                    <div class="form-group full">
                        <label for="imgProfile">รูปโปรไฟล์</label>
                        <div class="profile-area">
                            <div class="preview" id="image-preview" aria-hidden="true">♙</div>
                            <div>
                                <input class="control file-control" id="imgProfile" name="imgProfile" type="file" accept=".jpg,.jpeg,.png,.gif,image/jpeg,image/png,image/gif" aria-describedby="imgProfile-error" required>
                                <p class="file-hint">รองรับ JPG, JPEG, PNG, GIF และขนาดไม่เกิน 5MB</p>
                            </div>
                        </div>
                        <p id="imgProfile-error" class="field-error" data-error-for="imgProfile" <c:if test="${empty errors['imgProfile']}">hidden</c:if>><c:out value="${errors['imgProfile']}"/></p>
                    </div>

                    <div class="form-group full">
                        <label for="username">ชื่อผู้ใช้</label>
                        <input class="control" id="username" name="username" type="text" value="<c:out value='${username}'/>" maxlength="20" autocomplete="username" placeholder="กรอกชื่อผู้ใช้ " aria-describedby="username-error" required>
                        <p id="username-error" class="field-error" data-error-for="username" <c:if test="${empty errors['username']}">hidden</c:if>><c:out value="${errors['username']}"/></p>
                    </div>

                    <div class="form-group full">
                        <label for="password">รหัสผ่าน</label>
                        <input class="control" id="password" name="password" type="text" maxlength="16" autocomplete="new-password" placeholder="กรอกรหัสผ่าน" aria-describedby="password-error" required>
                        <p id="password-error" class="field-error" data-error-for="password" <c:if test="${empty errors['password']}">hidden</c:if>><c:out value="${errors['password']}"/></p>
                    </div>

                    <div class="form-group full">
                        <label for="confirmPassword">ยืนยันรหัสผ่าน</label>
                        <input class="control" id="confirmPassword" name="confirmPassword" type="text" maxlength="16" autocomplete="new-password" placeholder="กรอกรหัสผ่านยืนยัน" aria-describedby="confirmPassword-error" required>
                        <p id="confirmPassword-error" class="field-error" data-error-for="confirmPassword" <c:if test="${empty errors['confirmPassword']}">hidden</c:if>><c:out value="${errors['confirmPassword']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="email">อีเมล</label>
                        <input class="control" id="email" name="email" type="email" value="<c:out value='${email}'/>" maxlength="50" autocomplete="email" placeholder="กรอกอีเมล" aria-describedby="email-error" required>
                        <p id="email-error" class="field-error" data-error-for="email" <c:if test="${empty errors['email']}">hidden</c:if>><c:out value="${errors['email']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="phoneNumber">เบอร์โทรศัพท์</label>
                        <input class="control" id="phoneNumber" name="phoneNumber" type="tel" value="<c:out value='${phoneNumber}'/>" maxlength="10" autocomplete="tel" placeholder="กรอกเบอร์โทรศัพท์" aria-describedby="phoneNumber-error" required>
                        <p id="phoneNumber-error" class="field-error" data-error-for="phoneNumber" <c:if test="${empty errors['phoneNumber']}">hidden</c:if>><c:out value="${errors['phoneNumber']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="firstname">ชื่อ</label>
                        <input class="control" id="firstname" name="firstname" type="text" value="<c:out value='${firstname}'/>" maxlength="100" autocomplete="given-name" placeholder="กรอกชื่อ" aria-describedby="firstname-error" required>
                        <p id="firstname-error" class="field-error" data-error-for="firstname" <c:if test="${empty errors['firstname']}">hidden</c:if>><c:out value="${errors['firstname']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="surname">นามสกุล</label>
                        <input class="control" id="surname" name="surname" type="text" value="<c:out value='${surname}'/>" maxlength="100" autocomplete="family-name" placeholder="กรอกนามสกุล" aria-describedby="surname-error" required>
                        <p id="surname-error" class="field-error" data-error-for="surname" <c:if test="${empty errors['surname']}">hidden</c:if>><c:out value="${errors['surname']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="birthDay">วันเกิด</label>
                        <input class="control" id="birthDay" name="birthDay" type="date" value="<c:out value='${birthDay}'/>" autocomplete="bday" aria-describedby="birthDay-error" required>
                        <p id="birthDay-error" class="field-error" data-error-for="birthDay" <c:if test="${empty errors['birthDay']}">hidden</c:if>><c:out value="${errors['birthDay']}"/></p>
                    </div>

                    <div class="form-group">
                        <label for="gender">เพศ</label>
                        <select class="control" id="gender" name="gender" aria-describedby="gender-error" required>
                            <option value="">เลือกเพศ</option>
                            <option value="ชาย" <c:if test="${gender eq 'ชาย'}">selected</c:if>>ชาย</option>
                            <option value="หญิง" <c:if test="${gender eq 'หญิง'}">selected</c:if>>หญิง</option>
                            <option value="ไม่ระบุ" <c:if test="${gender eq 'ไม่ระบุ'}">selected</c:if>>ไม่ระบุ</option>
                        </select>
                        <p id="gender-error" class="field-error" data-error-for="gender" <c:if test="${empty errors['gender']}">hidden</c:if>><c:out value="${errors['gender']}"/></p>
                    </div>
                </div>
                <button class="submit-button" type="submit">สมัครสมาชิก</button>
                <p class="login-link">มีบัญชีอยู่แล้ว? <a href="${pageContext.request.contextPath}/loginMember">เข้าสู่ระบบ</a></p>
            </form>
        </section>
    </main>
    <script>
        const form = document.querySelector('[data-register-form]');
        const messages = {
            imgProfile: (input) => { const file = input.files[0]; if (!file) return 'กรุณาเลือกรูปโปรไฟล์'; const ext = file.name.split('.').pop().toLowerCase(); if (!['jpg','jpeg','png','gif'].includes(ext)) return 'รองรับเฉพาะไฟล์ .jpg, .jpeg, .png หรือ .gif'; if (file.size > 5 * 1024 * 1024) return 'ขนาดรูปโปรไฟล์ต้องไม่เกิน 5MB'; },
            username: (input) => { const value = input.value.trim(); if (!value) return 'กรุณากรอกชื่อผู้ใช้'; if (value.length < 4 || value.length > 20) return 'ชื่อผู้ใช้ต้องมีความยาว 4–20 ตัวอักษร'; if (!/^[A-Za-z][A-Za-z0-9]*$/.test(value)) return 'ชื่อผู้ใช้ต้องขึ้นต้นด้วยตัวอักษรภาษาอังกฤษ และใช้ได้เฉพาะตัวอักษรภาษาอังกฤษกับตัวเลข'; },
            password: (input) => { const value = input.value; if (!value) return 'กรุณากรอกรหัสผ่าน'; if (value.length < 8 || value.length > 16) return 'รหัสผ่านต้องมีความยาว 8–16 ตัวอักษร'; if (!/^[A-Za-z0-9!#_.]+$/.test(value)) return 'รหัสผ่านใช้ได้เฉพาะตัวอักษรภาษาอังกฤษ ตัวเลข และ ! # _ .'; if (!/[A-Z]/.test(value) || !/[a-z]/.test(value) || !/\d/.test(value)) return 'รหัสผ่านต้องมีตัวพิมพ์ใหญ่ ตัวพิมพ์เล็ก และตัวเลขอย่างน้อย 1 ตัว'; },
            confirmPassword: (input) => { if (!input.value) return 'กรุณายืนยันรหัสผ่าน'; if (input.value !== form.elements.password.value) return 'รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน'; },
            email: (input) => { const value = input.value.trim(); if (!value) return 'กรุณากรอกอีเมล'; if (value.length > 50) return 'อีเมลต้องมีความยาวไม่เกิน 50 ตัวอักษร'; if (!/^[a-z0-9]+([._-][a-z0-9]+)*@[a-z0-9]+([.-][a-z0-9]+)*\.[a-z]{2,}$/.test(value)) return 'รูปแบบอีเมลไม่ถูกต้อง'; },
            phoneNumber: (input) => { const value = input.value.trim(); if (!value) return 'กรุณากรอกเบอร์โทรศัพท์'; if (!/^(06|07|08|09)\d{8}$/.test(value)) return 'เบอร์โทรศัพท์ต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06, 07, 08 หรือ 09'; },
            firstname: (input) => validateName(input, 'ชื่อ'), surname: (input) => validateName(input, 'นามสกุล'),
            birthDay: (input) => { if (!input.value) return 'กรุณาเลือกวันเกิด'; const date = new Date(input.value + 'T00:00:00'); const today = new Date(); today.setHours(0,0,0,0); if (date >= today) return 'วันเกิดต้องอยู่ในอดีต'; let age = today.getFullYear() - date.getFullYear(); const month = today.getMonth() - date.getMonth(); if (month < 0 || (month === 0 && today.getDate() < date.getDate())) age--; if (age < 13) return 'ผู้สมัครต้องมีอายุอย่างน้อย 13 ปี'; },
            gender: (input) => !input.value ? 'กรุณาเลือกเพศ' : undefined
        };
        function validateName(input, label) { const value = input.value.trim(); if (!value) return 'กรุณากรอก' + label; if (value.length > 100) return label + 'ต้องมีความยาวไม่เกิน 100 ตัวอักษร'; if (!/^[A-Za-z\u0E00-\u0E7F]+$/.test(value)) return label + 'ต้องเป็นภาษาไทยหรือภาษาอังกฤษเท่านั้น และห้ามมีช่องว่าง'; }
        function setError(input, message) { const error = form.querySelector('[data-error-for="' + input.name + '"]'); input.classList.toggle('is-invalid', Boolean(message)); error.textContent = message || ''; error.hidden = !message; }
        Object.keys(messages).forEach((name) => { const input = form.elements[name]; const existingError = form.querySelector('[data-error-for="' + name + '"]'); if (!existingError.hidden) input.classList.add('is-invalid'); input.addEventListener(name === 'imgProfile' || name === 'gender' ? 'change' : 'input', () => { setError(input, messages[name](input)); if (name === 'password') setError(form.elements.confirmPassword, messages.confirmPassword(form.elements.confirmPassword)); }); });
        document.getElementById('imgProfile').addEventListener('change', (event) => { const file = event.target.files[0]; const preview = document.getElementById('image-preview'); if (file && file.type.startsWith('image/')) { const reader = new FileReader(); reader.onload = () => preview.innerHTML = '<img src="' + reader.result + '" alt="ตัวอย่างรูปโปรไฟล์">'; reader.readAsDataURL(file); } });
        form.addEventListener('submit', (event) => { let firstInvalid; Object.keys(messages).forEach((name) => { const input = form.elements[name]; const message = messages[name](input); setError(input, message); if (message && !firstInvalid) firstInvalid = input; }); if (firstInvalid) { event.preventDefault(); firstInvalid.focus(); } });
    </script>
</body>
</html>
