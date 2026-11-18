(() => {
    const input = document.getElementById('skillImg');
    if (!input) return;
    const preview = document.getElementById('skill-image-preview');
    const choose = document.getElementById('choose-skill-image');
    const remove = document.getElementById('remove-skill-image');
    const deleted = document.getElementById('removeSkillImg');
    const error = document.getElementById('skill-image-error');
    const removalHint = document.getElementById('skill-image-removal');
    const helpers = document.querySelectorAll('[data-image-help]');
    let objectUrl;
    let savedSource = preview.getAttribute('src');
    input.hidden = true;
    choose.hidden = false;
    function update() {
        const hasImage = preview.hasAttribute('src');
        preview.hidden = !hasImage;
        remove.hidden = !hasImage;
        choose.textContent = hasImage ? 'เปลี่ยนรูปภาพ' : 'เลือกรูปภาพ';
        helpers.forEach(element => element.hidden = hasImage);
        removalHint.hidden = deleted.value !== 'true';
    }
    choose.addEventListener('click', () => input.click());
    input.addEventListener('change', () => {
        const file = input.files[0];
        if (!file) return;
        if (!/\.(jpe?g|png)$/i.test(file.name) || file.size > 5 * 1024 * 1024) {
            input.value = '';
            error.textContent = 'กรุณาเลือกไฟล์ JPG JPEG PNG ขนาดไม่เกิน 5MB';
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
                objectUrl = undefined;
                if (savedSource) preview.src = savedSource;
                else preview.removeAttribute('src');
                deleted.value = savedSource ? 'false' : deleted.value;
            }
            update();
            choose.focus();
            return;
        }
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = URL.createObjectURL(file);
        preview.src = objectUrl;
        deleted.value = 'false';
        error.textContent = '';
        update();
    });
    remove.addEventListener('click', () => {
        if (objectUrl) URL.revokeObjectURL(objectUrl);
        objectUrl = undefined;
        input.value = '';
        preview.removeAttribute('src');
        savedSource = null;
        deleted.value = 'true';
        error.textContent = '';
        update();
        choose.focus();
    });
    update();
})();
