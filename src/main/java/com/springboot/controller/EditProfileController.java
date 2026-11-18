package com.springboot.controller;

import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class EditProfileController {
    private final TeacherRepository teachers;
    private final MemberRepository members;
    public EditProfileController(TeacherRepository teachers, MemberRepository members) { this.teachers=teachers; this.members=members; }
    @GetMapping("/editProfile")
    public String viewProfileData(HttpSession session, Model model) {
        Integer id=(Integer)session.getAttribute("memberId");
        if(id==null) return "redirect:/loginMember";
        var p=teachers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        var values=new HashMap<String,String>();
        values.put("firstname",p.getFirstname()); values.put("surname",p.getSurname()); values.put("email",p.getEmail());
        values.put("phoneNumber",p.getPhoneNumber()); values.put("birthDay",p.getBirthDay()==null?"":p.getBirthDay().toString());
        values.put("gender",p.getGender()); values.put("bio",p.getBio());
        model.addAttribute("values",values); return form(model);
    }
    private String form(Model model) {
        model.addAttribute("maxBirthDay",LocalDate.now(ZoneId.of("Asia/Bangkok")).minusYears(13));
        return "editProfile";
    }
    @PostMapping("/editProfile")
    @Transactional
    public String editProfile(@RequestParam Map<String,String> input, @RequestParam(required=false) MultipartFile imgProfile,
            HttpSession session, Model model, RedirectAttributes redirect) {
        Integer id=(Integer)session.getAttribute("memberId");
        if(id==null) return "redirect:/loginMember";
        var p=teachers.findForUpdate(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        var v=new HashMap<String,String>();
        for(String field:List.of("firstname","surname","email","phoneNumber","birthDay","gender","bio")) v.put(field,input.getOrDefault(field,"").trim());
        var errors=new LinkedHashMap<String,String>();
        for(String field:List.of("firstname","surname")) {
            if(v.get(field).isEmpty()) errors.put(field,"กรุณากรอก"+(field.equals("firstname")?"ชื่อ":"นามสกุล"));
            else if(v.get(field).length()>100 || !v.get(field).matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E]+")) errors.put(field,"ใช้ภาษาไทยหรืออังกฤษ ไม่เกิน 100 ตัวอักษร และไม่มีช่องว่าง");
        }
        String email=v.get("email");
        if(email.length()>50 || !email.matches("[a-z0-9]+([._-][a-z0-9]+)*@[a-z0-9]+([.-][a-z0-9]+)*\\.[a-z]{2,}")) errors.put("email","กรุณากรอกอีเมลให้ถูกต้อง ไม่เกิน 50 ตัวอักษร");
        else if(members.existsByEmailIgnoreCaseAndMemberIdNot(email,id)) errors.put("email","อีเมลนี้ถูกใช้งานแล้ว");
        if(!v.get("phoneNumber").matches("(06|07|08|09)\\d{8}")) errors.put("phoneNumber","กรุณากรอกเบอร์โทร 10 หลัก ขึ้นต้นด้วย 06 07 08 หรือ 09");
        LocalDate birth=null;
        try { birth=LocalDate.parse(v.get("birthDay")); if(birth.isAfter(LocalDate.now(ZoneId.of("Asia/Bangkok")).minusYears(13))) errors.put("birthDay","ต้องมีอายุอย่างน้อย 13 ปี"); }
        catch(java.time.format.DateTimeParseException ex) { errors.put("birthDay","กรุณากรอกวันเกิดให้ถูกต้อง"); }
        if(!Set.of("ชาย","หญิง","ไม่ระบุ").contains(v.get("gender"))) errors.put("gender","กรุณาเลือกเพศ");
        if(v.get("bio").length()>255) errors.put("bio","ประวัติย่อต้องไม่เกิน 255 ตัวอักษร");
        byte[] image=null;
        if(imgProfile!=null && !imgProfile.isEmpty()) {
            if(imgProfile.getSize()>5*1024*1024 || !Objects.toString(imgProfile.getOriginalFilename(),"").toLowerCase(Locale.ROOT).matches(".*\\.(jpg|jpeg|png|gif)$")) errors.put("imgProfile","รองรับ JPG JPEG PNG GIF ขนาดไม่เกิน 5MB");
            else try(var stream=javax.imageio.ImageIO.createImageInputStream(imgProfile.getInputStream())) {
                var readers=javax.imageio.ImageIO.getImageReaders(stream);
                if(!readers.hasNext()) errors.put("imgProfile","ไฟล์ไม่ใช่รูปภาพที่รองรับ");
                else { var reader=readers.next(); try { reader.setInput(stream);
                    if(!Set.of("jpeg","png","gif").contains(reader.getFormatName().toLowerCase(Locale.ROOT)) || reader.getWidth(0)<=0 || reader.getHeight(0)<=0) errors.put("imgProfile","รูปภาพไม่ถูกต้อง");
                    else image=imgProfile.getBytes();
                } finally {reader.dispose();} }
            } catch(java.io.IOException | IllegalArgumentException ex) { errors.put("imgProfile","ไม่สามารถอ่านรูปภาพได้"); }
        }
        if(!errors.isEmpty()) {model.addAttribute("values",v);model.addAttribute("errors",errors);return form(model);}
        p.setFirstname(v.get("firstname"));p.setSurname(v.get("surname"));p.setEmail(email);p.setPhoneNumber(v.get("phoneNumber"));
        p.setBirthDay(birth);p.setGender(v.get("gender"));p.setBio(v.get("bio"));if(image!=null)p.setImgProfile(image);
        teachers.saveAndFlush(p);
        session.setAttribute("memberName",p.getFirstname()+" "+p.getSurname());session.setAttribute("memberInitial",p.getFirstname().substring(0,1));
        session.setAttribute("memberHasProfileImage",p.getImgProfile()!=null && p.getImgProfile().length>0);
        redirect.addFlashAttribute("profileSuccess","บันทึกข้อมูลโปรไฟล์สำเร็จ");return "redirect:/viewProfile";
    }
}
