const form = document.getElementById('profile-form');
const file = document.getElementById('imgProfile'), preview = document.getElementById('preview');
let previewUrl;
function imageError() {
    const f = file.files[0];
    return f && (!/\.(jpe?g|png|gif)$/i.test(f.name) || f.size > 5 * 1024 * 1024) ? 'รองรับ JPG JPEG PNG GIF ขนาดไม่เกิน 5MB' : '';
}
file.addEventListener('change', () => {
    const error = imageError(); document.getElementById('imgProfile-error').textContent = error;
    if (file.files[0] && !error) {
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        previewUrl = URL.createObjectURL(file.files[0]); preview.src = previewUrl; preview.hidden = false;
    }
});
form.addEventListener('submit', event => {
    const errors = {};
    for (const field of ['firstname','surname']) {
        const v = form.elements[field].value.trim();
        if (!v) errors[field] = 'กรุณากรอก' + (field === 'firstname' ? 'ชื่อ' : 'นามสกุล');
        else if (v.length > 100 || !/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E]+$/.test(v)) errors[field] = 'ใช้ภาษาไทยหรืออังกฤษ ไม่เกิน 100 ตัวอักษร และไม่มีช่องว่าง';
    }
    const email = form.elements.email.value.trim();
    if (email.length > 50 || !/^[a-z0-9]+([._-][a-z0-9]+)*@[a-z0-9]+([.-][a-z0-9]+)*\.[a-z]{2,}$/.test(email)) errors.email = 'กรุณากรอกอีเมลให้ถูกต้อง ไม่เกิน 50 ตัวอักษร';
    if (!/^(06|07|08|09)\d{8}$/.test(form.elements.phoneNumber.value.trim())) errors.phoneNumber = 'กรุณากรอกเบอร์โทร 10 หลัก ขึ้นต้นด้วย 06 07 08 หรือ 09';
    if (!form.elements.birthDay.value || !form.elements.birthDay.validity.valid) errors.birthDay = 'กรุณาเลือกวันเกิดให้ถูกต้อง ต้องมีอายุอย่างน้อย 13 ปี';
    if (!form.querySelector('[name=gender]:checked')) errors.gender = 'กรุณาเลือกเพศ';
    if (form.elements.bio.value.trim().length > 255) errors.bio = 'ประวัติย่อต้องไม่เกิน 255 ตัวอักษร';
    const image = imageError(); if (image) errors.imgProfile = image;
    form.querySelectorAll('.field-error').forEach(el => el.textContent = '');
    for (const [field,message] of Object.entries(errors)) document.getElementById(field + '-error').textContent = message;
    const first = Object.keys(errors)[0];
    if (first) { event.preventDefault(); form.querySelector('[name="' + first + '"]').focus(); }
});
const serverError = [...form.querySelectorAll('.field-error')].find(el => el.textContent.trim());
if (serverError) form.querySelector('[name="' + serverError.id.replace('-error','') + '"]').focus();
