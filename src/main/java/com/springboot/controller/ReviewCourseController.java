package com.springboot.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import com.springboot.dto.CourseDetailDto;
import com.springboot.model.RegisterCourse;
import com.springboot.repository.RegisterCourseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewCourseController {
    private final RegisterCourseRepository registrations;
    private final com.springboot.repository.CourseDateRepository dates;
    private final com.springboot.repository.TeacherRepository teachers;
    public ReviewCourseController(RegisterCourseRepository registrations, com.springboot.repository.CourseDateRepository dates,
            com.springboot.repository.TeacherRepository teachers) {
        this.teachers = teachers;
        this.registrations = registrations;
        this.dates = dates;
    }

    public static boolean canReview(RegisterCourse r) {
        return r != null && r.getRatingScore() == null
                && ("เรียนเสร็จสิ้น".equals(r.getRegisterStatus())
                || ("อนุมัติแล้ว".equals(r.getRegisterStatus()) && "สอนเสร็จสิ้น".equals(r.getCourse().getCourseStatus())))
                && !r.getMember().getMemberId().equals(r.getCourse().getTeacher().getMemberId());
    }

    @GetMapping("/reviewCourse")
    @Transactional(readOnly = true)
    public String page(@RequestParam Integer courseId, HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var r = registrations.findByMember_MemberIdAndCourse_CourseId(memberId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canReview(r)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return form(r, model);
    }

    private String form(RegisterCourse r, Model model) {
        model.addAttribute("course", CourseDetailDto.from(r.getCourse(), 0, dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(r.getCourse().getCourseId())));
        model.addAttribute("teacherId", r.getCourse().getTeacher().getMemberId());
        model.addAttribute("registrationId", r.getRegisterCourseId());
        return "reviewCourse";
    }

    @PostMapping("/reviewCourse")
    @Transactional
    public String submit(@RequestParam Integer registrationId,
            @RequestParam(defaultValue = "") String rating, @RequestParam(defaultValue = "") String comment,
            HttpSession session, Model model, RedirectAttributes redirect) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var r = registrations.findForCancellation(memberId, registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canReview(r)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        model.addAttribute("rating", rating); model.addAttribute("comment", comment);
        if (!rating.matches("[1-5]")) model.addAttribute("ratingError", "กรุณากรอกคะแนนให้ถูกต้อง");
        if (comment.length() > 255) model.addAttribute("commentError", "ความคิดเห็นต้องไม่เกิน 255 ตัวอักษร");
        if (model.containsAttribute("ratingError") || model.containsAttribute("commentError")) return form(r, model);
        doReviewCourse(r, rating, comment);
        redirect.addFlashAttribute("registrationSuccess", "รีวิวคอร์สสำเร็จ");
        return "redirect:/viewCourseDetail/" + r.getCourse().getCourseId();
    }

    private void doReviewCourse(RegisterCourse r, String rating, String comment) {
        // Serialize updates for reviews belonging to the same teacher.
        var teacher = teachers.findForUpdate(r.getCourse().getTeacher().getMemberId()).orElseThrow();
        r.setRatingScore(Double.valueOf(rating));
        r.setComment(comment.isBlank() ? null : comment.trim());
        r.setReviewDate(LocalDate.now(ZoneId.of("Asia/Bangkok")));
        registrations.saveAndFlush(r);
        Double average = registrations.averageRatingForTeacher(teacher.getMemberId());
        teacher.setAvgRatingScore(java.math.BigDecimal.valueOf(average == null ? 0 : average)
                .setScale(2, java.math.RoundingMode.HALF_UP));
        teachers.save(teacher);
    }

    @ExceptionHandler({org.springframework.dao.DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public String failed(RedirectAttributes redirect) {
        redirect.addFlashAttribute("registrationError", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
        return "redirect:/listRegisterCourse?tab=completed";
    }
}
