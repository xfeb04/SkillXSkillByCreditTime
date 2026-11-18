package com.springboot.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.springboot.model.*;
import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class ReportTeacherController {
    @ExceptionHandler({org.springframework.dao.DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public String failed(RedirectAttributes redirect) {
        redirect.addFlashAttribute("registrationError", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
        return "redirect:/listRegisterCourse";
    }
    private final RegisterCourseRepository registrations;
    private final ReportRepository reports;
    public ReportTeacherController(RegisterCourseRepository registrations, ReportRepository reports) {
        this.registrations=registrations; this.reports=reports;
    }
    private RegisterCourse getRegisterCourse(Integer id, Integer memberId) {
        return registrations.findById(id).filter(r -> r.getMember().getMemberId().equals(memberId) && !r.getCourse().getTeacher().getMemberId().equals(memberId))
                .filter(r -> Set.of("อนุมัติแล้ว","เรียนเสร็จสิ้น").contains(r.getRegisterStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"ไม่พบคอร์สที่คุณได้รับอนุมัติให้เรียน"));
    }
    private String form(Model model, RegisterCourse registration, String reason, Map<String,String> errors) {
        model.addAttribute("registration",registration); model.addAttribute("reason",reason); model.addAttribute("errors",errors);
        return "reportTeacher";
    }
    @GetMapping("/reportTeacher")
    @Transactional(readOnly=true)
    public String page(@RequestParam Integer registerCourseId, HttpSession session, Model model) {
        Integer memberId=(Integer)session.getAttribute("memberId");
        if(memberId==null) return "redirect:/loginMember";
        return form(model,getRegisterCourse(registerCourseId,memberId),"",Map.of());
    }
    @PostMapping("/reportTeacher")
    @Transactional
    public String submit(@RequestParam Integer registerCourseId, @RequestParam(defaultValue="") String reason,
            @RequestParam(required=false) MultipartFile imgReport, HttpSession session, Model model, RedirectAttributes redirect) {
        Integer memberId=(Integer)session.getAttribute("memberId");
        if(memberId==null) return "redirect:/loginMember";
        RegisterCourse registration=getRegisterCourse(registerCourseId,memberId);
        reason=reason.trim();
        Map<String,String> errors=new LinkedHashMap<>();
        if(reason.isBlank()) errors.put("reason","กรุณากรอกเหตุผลการรายงาน");
        else if(reason.length()>255) errors.put("reason","เหตุผลต้องมีความยาวไม่เกิน 255 ตัวอักษร");
        else if(!reason.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s]+"))
            errors.put("reason","เหตุผลใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข และช่องว่าง");
        byte[] image=null;
        if(imgReport!=null && !imgReport.isEmpty()) {
            String filename=Objects.toString(imgReport.getOriginalFilename(),"").toLowerCase(Locale.ROOT);
            if(!filename.matches(".*\\.(jpg|jpeg|png|gif)$")) errors.put("imgReport","รองรับเฉพาะไฟล์ JPG JPEG PNG และ GIF");
            else if(imgReport.getSize()>5*1024*1024) errors.put("imgReport","รูปภาพต้องมีขนาดไม่เกิน 5MB");
            else {
                try(ImageInputStream stream=ImageIO.createImageInputStream(imgReport.getInputStream())) {
                    var readers=ImageIO.getImageReaders(stream);
                    if(!readers.hasNext()) errors.put("imgReport","ไฟล์ที่เลือกไม่ใช่รูปภาพที่รองรับ");
                    else {
                        var reader=readers.next();
                        try {
                            reader.setInput(stream);
                            if(!Set.of("jpeg","png","gif").contains(reader.getFormatName().toLowerCase(Locale.ROOT))
                                    || reader.getWidth(0)<=0 || reader.getHeight(0)<=0) errors.put("imgReport","ไฟล์รูปภาพไม่ถูกต้อง");
                            else image=imgReport.getBytes();
                        } finally { reader.dispose(); }
                    }
                } catch(IOException | IllegalArgumentException ex) { errors.put("imgReport","ไม่สามารถอ่านรูปภาพได้ กรุณาเลือกไฟล์ใหม่"); }
            }
        }
        if(!errors.isEmpty()) return form(model,registration,reason,errors);
        doReportTeacher(registration, reason, image);
        redirect.addFlashAttribute("registrationSuccess","ส่งรายงานผู้สอนสำเร็จ");
        return "redirect:/viewCourseDetail/"+registration.getCourse().getCourseId();
    }

    private Report doReportTeacher(RegisterCourse registration, String reason, byte[] image) {
        return reports.saveAndFlush(new Report("ผู้สอน", reason, image, LocalDate.now(),
                registration.getCourse().getTeacher(), registration.getMember(), registration));
    }
}
