(() => {
    let queue = Promise.resolve();
    const ready = document.readyState === 'loading'
        ? new Promise(resolve => document.addEventListener('DOMContentLoaded', resolve, {once:true}))
        : Promise.resolve();
    function show(message, confirmation) {
        const result = queue.then(() => ready).then(() => new Promise(resolve => {
            const previous = document.activeElement;
            const dialog = document.createElement('dialog');
            dialog.className = 'result-dialog';
            dialog.setAttribute('aria-labelledby', 'app-dialog-message');
            const text = document.createElement('p');
            text.id = 'app-dialog-message'; text.textContent = message;
            const actions = document.createElement('form');
            actions.method = 'dialog'; actions.style.gap = '12px';
            if (confirmation) {
                const cancel = document.createElement('button');
                cancel.textContent = 'ยกเลิก'; cancel.value = 'cancel';
                cancel.className = 'detail-button'; cancel.autofocus = true;
                actions.append(cancel);
            }
            const ok = document.createElement('button');
            ok.textContent = confirmation ? 'ยืนยัน' : 'ตกลง';
            ok.value = 'ok'; ok.className = 'primary-button'; ok.autofocus = !confirmation;
            actions.append(ok); dialog.append(text, actions);
            dialog.addEventListener('close', () => {
                const accepted = dialog.returnValue === 'ok';
                dialog.remove();
                if (previous && previous.isConnected) previous.focus();
                resolve(accepted);
            }, {once:true});
            document.body.append(dialog); dialog.showModal();
        }));
        queue = result.catch(() => {});
        return result;
    }
    window.appAlert = message => show(message, false);
    const approved = new WeakSet();
    const waiting = new WeakSet();
    document.addEventListener('submit', async event => {
        const form = event.target;
        if (!form.matches('form[data-confirm]') || event.defaultPrevented) return;
        if (approved.delete(form)) return;
        event.preventDefault();
        if (waiting.has(form)) return;
        waiting.add(form);
        const submitter = event.submitter;
        try {
            if (await show(form.dataset.confirm, true)) {
                approved.add(form);
                form.requestSubmit(submitter || undefined);
                approved.delete(form);
            }
        } finally { waiting.delete(form); }
    });
    ready.then(() => {
        document.querySelectorAll('[data-app-alert]').forEach(element => {
            window.appAlert(element.textContent.trim());
            element.remove();
        });
    });
})();

// Native date controls keep ISO values for validation and submission.
window.formatBuddhistDate = value => {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value || '')) return '';
    const [year, month, day] = value.split('-');
    return day + '/' + month + '/' + (Number(year) + 543);
};
document.addEventListener('DOMContentLoaded', () => {
    const initialized = new WeakSet();
    const months = ['มกราคม','กุมภาพันธ์','มีนาคม','เมษายน','พฤษภาคม','มิถุนายน','กรกฎาคม','สิงหาคม','กันยายน','ตุลาคม','พฤศจิกายน','ธันวาคม'];
    const pad = n => String(n).padStart(2, '0');
    function setup(input) {
        if (initialized.has(input)) return;
        initialized.add(input);
        // Cloned course rows need fresh controls and listeners.
        if (input.nextElementSibling?.classList.contains('buddhist-picker')) input.nextElementSibling.remove();
        const box = document.createElement('span');
        box.className = 'buddhist-picker';
        box.style.cssText = 'display:flex;gap:8px;align-items:center;width:100%';
        const fields = ['วัน','เดือน','ปี พ.ศ.'].map(label => {
            const select = document.createElement('select');
            select.setAttribute('aria-label', label);
            select.required = input.required;
            select.disabled = input.disabled;
            select.style.cssText = 'flex:1;min-width:0;width:auto';
            select.add(new Option(label, ''));
            box.append(select);
            return select;
        });
        const [day, month, year] = fields;
        months.forEach((label, i) => month.add(new Option(label, i + 1)));
        const current = new Date().getFullYear();
        const selectedYear = Number(input.value.slice(0,4));
        const minYear = input.min ? Number(input.min.slice(0,4)) : Math.min(current - 120, selectedYear || current);
        const maxYear = input.max ? Number(input.max.slice(0,4)) : Math.max(current + 20, selectedYear);
        for (let y = maxYear; y >= minYear; y--) year.add(new Option(String(y + 543), y));
        function days() {
            const selected = day.value;
            const count = month.value ? new Date(Number(year.value) || 2000, Number(month.value), 0).getDate() : 31;
            day.length = 1;
            for (let d = 1; d <= count; d++) day.add(new Option(String(d), d));
            day.value = selected;
        }
        function sync() {
            const parts = input.value.split('-').map(Number);
            year.value = parts[0] || '';
            month.value = parts[1] || '';
            days(); day.value = parts[2] || '';
        }
        fields.forEach(select => select.addEventListener('change', () => {
            days();
            input.value = day.value && month.value && year.value
                ? `${year.value}-${pad(month.value)}-${pad(day.value)}` : '';
            input.dispatchEvent(new Event('input', {bubbles:true}));
            input.dispatchEvent(new Event('change', {bubbles:true}));
            const message = input.validity.rangeUnderflow ? 'วันที่ต้องไม่ก่อน ' + window.formatBuddhistDate(input.min)
                : input.validity.rangeOverflow ? 'วันที่ต้องไม่หลัง ' + window.formatBuddhistDate(input.max) : '';
            year.setCustomValidity(message);
            if (message) year.reportValidity();
        }));
        input.style.cssText = 'position:absolute;width:1px;height:1px;padding:0;border:0;opacity:0;pointer-events:none';
        input.tabIndex = -1;
        input.after(box);
        input.addEventListener('focus', () => day.focus());
        input.addEventListener('invalid', event => { event.preventDefault(); day.focus(); });
        input.form?.addEventListener('reset', () => setTimeout(() => { sync(); year.setCustomValidity(''); }, 0));
        sync();
    }
    function scan() { document.querySelectorAll('input[type=date]').forEach(setup); }
    scan();
    new MutationObserver(scan).observe(document.body, {childList:true, subtree:true});
});
