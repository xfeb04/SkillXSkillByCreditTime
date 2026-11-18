package com.springboot.controller;

import java.util.Comparator;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.springboot.dto.RegisterCourseCardDto;
import com.springboot.model.RegisterCourse;
import com.springboot.repository.CourseDateRepository;
import com.springboot.repository.RegisterCourseRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class ListRegisterCourseController {
    private final RegisterCourseRepository registrations;
    private final CourseDateRepository dates;

    public ListRegisterCourseController(RegisterCourseRepository registrations, CourseDateRepository dates) {
        this.registrations = registrations;
        this.dates = dates;
    }

    @GetMapping("/listRegisterCourse")
    @Transactional(readOnly = true)
    public String getListRegisterCourse(@RequestParam(defaultValue = "ongoing") String tab, HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        String activeTab = Set.of("registered", "approved", "ongoing", "completed", "cancelled").contains(tab) ? tab : "ongoing";
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("courses", registrations.findByMember_MemberId(memberId).stream()
                .filter(registration -> activeTab.equals(tabFor(registration)))
                .sorted(Comparator.comparing(RegisterCourse::getRegisterCourseId,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(registration -> {
                    Integer courseId = registration.getCourse().getCourseId();
                    return new RegisterCourseCardDto(registration,
                            dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId).stream()
                                    .map(date -> date.getTeachDate()).findFirst().orElse(null),
                            registrations.countByCourse_CourseIdAndRegisterStatus(courseId, "อนุมัติแล้ว")
                                    + registrations.countByCourse_CourseIdAndRegisterStatus(courseId, "เรียนเสร็จสิ้น"));
                }).toList());
        return "listRegisterCourse";
    }

    private String tabFor(RegisterCourse registration) {
        String status = registration.getRegisterStatus();
        if ("ยกเลิก".equals(status) || "ยกเลิกแล้ว".equals(status) || "ปฏิเสธคำขอ".equals(status)) return "cancelled";
        if ("รออนุมัติ".equals(status)) return "registered";
        if ("เรียนเสร็จสิ้น".equals(status)) return "completed";
        if (!"อนุมัติแล้ว".equals(status)) return null;
        return switch (registration.getCourse().getCourseStatus()) {
            case "กำลังดำเนินการ" -> "ongoing";
            case "สอนเสร็จสิ้น" -> "completed";
            default -> "approved";
        };
    }
}
