const form = document.getElementById('course-form');
const schedule = document.getElementById('schedule');
const credit = document.getElementById('creditPerCourse');
let paidCredit = credit.value || '1';
const insufficientCreditMessage = 'เครดิตคงเหลือไม่เพียงพอ กรุณาลดเครดิตที่ใช้หรือลดจำนวนผู้เรียนสูงสุด';
function exceedsCreditBudget() {
    if (form.querySelector('[name=courseType]:checked')?.value !== 'ผู้สอนจ่ายให้ผู้เรียน') return false;
    const count = Number(document.getElementById('maxMember').value);
    const amount = Number(credit.value);
    const available = Number(document.getElementById('credit-balance-note').dataset.availableCredit);
    const total = count * amount;
    return count >= 1 && Number.isInteger(count) && Number.isFinite(amount) && amount >= 0
        && total > available + Number.EPSILON * Math.max(1, Math.abs(total), Math.abs(available)) * 4;
}
function syncCreditBudget() {
    document.getElementById('credit-balance-note').hidden = form.querySelector('[name=courseType]:checked')?.value !== 'ผู้สอนจ่ายให้ผู้เรียน';
    showError(credit, exceedsCreditBudget() ? insufficientCreditMessage : '');
}
function syncFields() {
    const online = form.querySelector('[name=delivery]:checked')?.value === 'online';
    [['platform-field', online], ['location-field', !online]].forEach(([id, visible]) => {
        const group = document.getElementById(id);
        group.hidden = !visible;
        if (!visible) group.querySelector('textarea').value = '';
        group.querySelector('textarea').disabled = !visible;
        group.querySelector('textarea').required = visible;
    });
    const type = form.querySelector('[name=courseType]:checked')?.value;
    const free = type === 'เรียนฟรี';
    if (free && !credit.readOnly) paidCredit = credit.value;
    if (!free && credit.readOnly) credit.value = paidCredit || '1';
    if (free) credit.value = '2';
    credit.readOnly = free;
    credit.required = !free;
    document.querySelectorAll('[data-course-type-help]').forEach(note => {
        note.hidden = note.dataset.courseTypeHelp !== type;
    });
    document.getElementById('credit-label').textContent = type === 'ผู้สอนจ่ายให้ผู้เรียน'
        ? 'เครดิตที่จ่ายให้ผู้เรียนต่อคน' : (free ? 'เครดิตที่ผู้สอนได้รับต่อผู้เรียน 1 คน' : 'ค่าเครดิตต่อคอร์ส');
}
function renumber() {
    const rows = schedule.querySelectorAll('.day-row');
    rows.forEach((row, i) => {
        row.querySelector('.day-number').textContent = i + 1;
        row.querySelector('.remove-day').disabled = rows.length === 1;
    });
}
form.addEventListener('change', syncFields);
document.getElementById('add-day').addEventListener('click', () => {
    const row = schedule.querySelector('.day-row').cloneNode(true);
    row.querySelectorAll('.field-error').forEach(error => error.remove());
    row.querySelectorAll('input').forEach(input => { input.value = ''; input.removeAttribute('aria-describedby'); input.removeAttribute('aria-invalid'); });
    schedule.appendChild(row);
    renumber();
    row.querySelector('input').focus();
});
schedule.addEventListener('click', event => {
    if (event.target.matches('.remove-day') && schedule.children.length > 1) {
        event.target.closest('.day-row').remove();
        renumber();
    }
});
syncFields();
renumber();

// Keep native validation as a fallback when JavaScript is unavailable.
form.noValidate = true;
let submitted = false;
let errorSequence = 0;
const labels = {categoryId:'หมวดหมู่', courseName:'ชื่อคอร์ส', courseDescription:'รายละเอียดคอร์ส',
    onlinePlatform:'แพลตฟอร์มออนไลน์', location:'สถานที่เรียน', requiredEquip:'อุปกรณ์ที่ต้องเตรียม',
    maxMember:'จำนวนผู้เรียนสูงสุด', creditPerCourse:'เครดิตต่อคอร์ส', teachDate:'วันที่สอน',
    startTime:'เวลาเริ่ม', endTime:'เวลาสิ้นสุด', delivery:'รูปแบบการเรียน', courseType:'รูปแบบคอร์ส'};
function showError(input, message) {
    let error = document.getElementById(input.getAttribute('aria-describedby'));
    if (!error) {
        error = document.createElement('p');
        error.id = `course-error-${++errorSequence}`;
        error.className = 'field-error';
        error.setAttribute('aria-live', 'polite');
        const anchor = input.type === 'radio' ? input.closest('fieldset').querySelector('.choices') : input;
        anchor.insertAdjacentElement('afterend', error);
        input.setAttribute('aria-describedby', error.id);
    }
    error.textContent = message;
    error.hidden = !message;
    input.setAttribute('aria-invalid', message ? 'true' : 'false');
}
function validateCourse(focusFirst = false) {
    let first = null;
    const seen = [];
    const radioGroups = new Set();
    form.querySelectorAll('input, textarea, select').forEach(input => {
        let message = '';
        const value = input.value.trim();
        const label = labels[input.name] || 'ข้อมูล';
        if (input.type === 'radio') {
            if (radioGroups.has(input.name)) return;
            radioGroups.add(input.name);
            if (!form.querySelector(`[name="${input.name}"]:checked`)) message = `กรุณาเลือก${label}`;
        } else if (!input.disabled) {
            if (input.validity.badInput) message = `กรุณากรอก${label}ให้ถูกต้อง`;
            else if (input.required && !value) message = `${input.tagName === 'SELECT' ? 'กรุณาเลือก' : 'กรุณากรอก'}${label}`;
            else if (value) {
                if (input.name === 'courseName') {
                    if (value.length < 4 || value.length > 100) message = 'ชื่อคอร์สต้องมีความยาว 4–100 ตัวอักษร';
                    else if (!/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E ]+$/.test(value)) message = 'ชื่อคอร์สใช้ได้เฉพาะตัวอักษรภาษาไทย ภาษาอังกฤษ และช่องว่าง';
                } else if (input.name === 'courseDescription') {
                    if (value.length < 20 || value.length > 255) message = 'รายละเอียดคอร์สต้องมีความยาว 20–255 ตัวอักษร';
                    else if (!/^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s.,/#-]+$/.test(value)) message = 'รายละเอียดคอร์สใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระ . , / # -';
                } else if (['onlinePlatform','location','requiredEquip'].includes(input.name)) {
                    const min = input.name === 'requiredEquip' ? 4 : 3;
                    if (value.length < min || value.length > 255) message = `${label}ต้องมีความยาว ${min}–255 ตัวอักษร`;
                    else {
                        const allowUrl = input.name !== 'requiredEquip';
                        const pattern = allowUrl
                            ? /^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s.,/#:?=&%_~@!$'()*+;\[\]-]+$/
                            : /^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s.,/#-]+$/;
                        if (!pattern.test(value)) message = allowUrl
                            ? `${label}ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระที่ใช้ในลิงก์`
                            : `${label}ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระ . , / # -`;
                    }
                } else if (input.name === 'maxMember') {
                    if (!Number.isInteger(Number(value)) || Number(value) < 1 || Number(value) > 2147483647) message = 'จำนวนผู้เรียนสูงสุดต้องเป็นจำนวนเต็มอย่างน้อย 1 คน';
                } else if (input.name === 'creditPerCourse') {
                    if (!Number.isFinite(Number(value)) || Number(value) < 0) message = 'กรุณากรอกเครดิตเป็นตัวเลขตั้งแต่ 0 ขึ้นไป';
                    else if (exceedsCreditBudget()) message = insufficientCreditMessage;
                } else if (input.name === 'teachDate') {
                    const today = new Date().toLocaleDateString('en-CA', {timeZone:'Asia/Bangkok'});
                    if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || value < input.min || value <= today) message = 'วันที่สอนต้องเป็นวันที่ถูกต้องและหลังวันนี้เท่านั้น';
                } else if (['startTime','endTime'].includes(input.name)) {
                    if (!/^([01]\d|2[0-3]):[0-5]\d$/.test(value)) message = `กรุณากรอก${label}ในรูปแบบ HH:mm`;
                    else if (input.name === 'endTime') {
                        const row = input.closest('.day-row');
                        const start = row.querySelector('[name=startTime]').value;
                        const date = row.querySelector('[name=teachDate]').value;
                        if (start && value <= start) message = 'เวลาสิ้นสุดต้องมากกว่าเวลาเริ่ม';
                        else if (seen.some(previous => previous.date === date && start < previous.end && value > previous.start)) message = 'เวลาเรียนซ้อนกับรายการอื่น กรุณาเลือกช่วงเวลาใหม่';
                        seen.push({date, start, end:value});
                    }
                }
            }
        }
        showError(input, message);
        if (message && !first) first = input;
    });
    if (first && focusFirst) { first.focus(); first.scrollIntoView({block:'center', behavior:'smooth'}); }
    return !first;
}
form.addEventListener('input', () => { syncCreditBudget(); if (submitted) validateCourse(); });
form.addEventListener('change', () => { syncCreditBudget(); if (submitted) validateCourse(); });
schedule.addEventListener('click', () => { if (submitted) validateCourse(); });
form.addEventListener('submit', event => {
    submitted = true;
    if (!validateCourse(true)) event.preventDefault();
});
syncCreditBudget();
