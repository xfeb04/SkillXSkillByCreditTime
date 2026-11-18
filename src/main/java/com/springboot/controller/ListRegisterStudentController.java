package com.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.RegisterCourseRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class ListRegisterStudentController {
    private final CourseRepository courses;
    private final RegisterCourseRepository registrations;
    public ListRegisterStudentController(CourseRepository courses, RegisterCourseRepository registrations) {
        this.courses = courses; this.registrations = registrations;
    }
    @GetMapping("/listRegisterStudent")
    @Transactional(readOnly = true)
    public String getListRegStudent(@RequestParam Integer courseId, HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var course = courses.findById(courseId).filter(c -> c.getTeacher().getMemberId().equals(memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สของคุณ"));
        model.addAttribute("course", course);
        var students = new java.util.ArrayList<>(registrations.findByCourse_CourseIdAndRegisterStatus(courseId, "อนุมัติแล้ว"));
        students.addAll(registrations.findByCourse_CourseIdAndRegisterStatus(courseId, "เรียนเสร็จสิ้น"));
        model.addAttribute("students", students);
        return "listRegisterStudent";
    }
}
