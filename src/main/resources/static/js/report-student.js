const form=document.getElementById('report-form'), file=document.getElementById('imgReport'), reason=document.getElementById('reason');
const preview=document.getElementById('image-preview'), remove=document.getElementById('remove-image'), zone=document.getElementById('drop-zone');
const choose=document.getElementById('choose-image');
choose.hidden=false;
choose.setAttribute('aria-describedby','image-error');
file.hidden=true;
choose.addEventListener('click',()=>file.click());
let url=null, submitted=false;
form.noValidate=true;
function error(input,id,message){ document.getElementById(id).textContent=message; input.setAttribute('aria-invalid',message?'true':'false'); return !message; }
function validateImage(){const image=file.files[0];return error(file,'image-error',!image?'':! /\.(jpg|jpeg|png|gif)$/i.test(image.name)?'รองรับเฉพาะไฟล์ JPG JPEG PNG และ GIF':image.size>5*1024*1024?'รูปภาพต้องมีขนาดไม่เกิน 5MB':'');}
function validateReason(){const value=reason.value.trim();return error(reason,'reason-error',!value?'กรุณากรอกเหตุผลการรายงาน':value.length>255?'เหตุผลต้องมีความยาวไม่เกิน 255 ตัวอักษร':! /^[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\s]+$/.test(value)?'เหตุผลใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข และช่องว่าง':'');}
function updatePreview(){
    if(url) URL.revokeObjectURL(url);
    url=null;
    preview.hidden=true;
    preview.removeAttribute('src');
    remove.hidden=!file.files.length;
    const hasImage=validateImage() && !!file.files[0];
    zone.querySelectorAll('[data-upload-help]').forEach(note=>note.hidden=hasImage);
    choose.textContent=hasImage?'เปลี่ยนรูปภาพ':'เลือกรูปภาพ';
    if(hasImage){
        url=URL.createObjectURL(file.files[0]);
        preview.src=url;
        preview.hidden=false;
    }
}
file.addEventListener('change',updatePreview);
remove.addEventListener('click',()=>{file.value='';updatePreview();});
reason.addEventListener('input',()=>{if(submitted)validateReason();});
zone.addEventListener('dragover',event=>{event.preventDefault();zone.classList.add('dragging');});
zone.addEventListener('dragleave',()=>zone.classList.remove('dragging'));
zone.addEventListener('drop',event=>{event.preventDefault();zone.classList.remove('dragging');if(event.dataTransfer.files.length){const transfer=new DataTransfer();transfer.items.add(event.dataTransfer.files[0]);file.files=transfer.files;updatePreview();}});
form.addEventListener('submit',event=>{submitted=true;const imageOk=validateImage(),reasonOk=validateReason();if(!imageOk||!reasonOk){event.preventDefault();(!imageOk?choose:reason).focus();}});
window.addEventListener('pagehide',()=>{if(url)URL.revokeObjectURL(url);});