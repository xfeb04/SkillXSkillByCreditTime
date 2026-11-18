const categoryForm = document.getElementById('category-form');
if (categoryForm.dataset.mode !== 'delete') {
    const name = document.getElementById('categoryName');
    const description = document.getElementById('cateDescription');
    const fields = [[name, document.getElementById('name-error')], [description, document.getElementById('description-error')]];
    fields.forEach(([input, error]) => input.addEventListener('input', () => { error.hidden = true; input.removeAttribute('aria-invalid'); }));
    categoryForm.addEventListener('submit', event => {
        const n = name.value.trim(), d = description.value.trim();
        const messages = [
            !n ? 'กรุณากรอกชื่อหมวดหมู่' : n.length < 2 || n.length > 50 ? 'ชื่อหมวดหมู่ต้องมีความยาว 2–50 ตัวอักษร' : !/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E ]+$/.test(n) ? 'ชื่อหมวดหมู่ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ และช่องว่าง' : '',
            !d ? 'กรุณากรอกรายละเอียด' : d.length > 255 ? 'รายละเอียดต้องไม่เกิน 255 ตัวอักษร' : !/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s.,/#-]+$/.test(d) ? 'รายละเอียดใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และ . , / # -' : ''
        ];
        let first;
        fields.forEach(([input, error], index) => {
            error.textContent = messages[index]; error.hidden = !messages[index];
            input.setAttribute('aria-invalid', String(!!messages[index]));
            if (messages[index] && !first) first = input;
        });
        if (first) { event.preventDefault(); first.focus(); }
    });
    const invalid = fields.find(([, error]) => !error.hidden);
    if (invalid) invalid[0].focus();
}
