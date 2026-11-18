package com.springboot.controller;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.springboot.dto.ListCourseCardDto;
import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class ListCourseController {
    private static final Map<String, String> STATUSES = Map.of("created", "สร้างสำเร็จ", "open", "เปิดรับสมัคร",
            "ongoing", "กำลังดำเนินการ", "completed", "สอนเสร็จสิ้น");
    private final CourseRepository courses;
    private final CourseDateRepository dates;
    private final RegisterCourseRepository registrations;
    private final com.springboot.service.CourseCreditSettlementService settlement;
    private final com.springboot.service.CourseStatusScheduler lifecycle;
    public ListCourseController(CourseRepository courses, CourseDateRepository dates,
            RegisterCourseRepository registrations, com.springboot.service.CourseCreditSettlementService settlement, com.springboot.service.CourseStatusScheduler lifecycle) {
        this.courses = courses; this.dates = dates; this.registrations = registrations;
        this.settlement = settlement; this.lifecycle = lifecycle;
    }
    @GetMapping("/listCourse")
    @Transactional(readOnly = true)
    public String getListCourseByOwner(@RequestParam(defaultValue = "ongoing") String tab,
            HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        String activeTab = STATUSES.containsKey(tab) ? tab : "ongoing";
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("courses", courses.findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(memberId, STATUSES.get(activeTab))
                .stream()
                .map(course -> new ListCourseCardDto(course,
                        dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(course.getCourseId()).stream()
                                .map(date -> date.getTeachDate()).findFirst().orElse(null),
                        registrations.countByCourse_CourseIdAndRegisterStatus(course.getCourseId(), "อนุมัติแล้ว")
                                + registrations.countByCourse_CourseIdAndRegisterStatus(course.getCourseId(), "เรียนเสร็จสิ้น")))
                .toList());
        return "listCourse";
    }
    @PostMapping("/startCourse/{courseId}")
    @Transactional
    public String start(@PathVariable Integer courseId, HttpSession session, RedirectAttributes redirect) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var course = courses.findOwnedForUpdate(courseId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สของคุณ"));
        if (!"เปิดรับสมัคร".equals(course.getCourseStatus())) {
            redirect.addFlashAttribute("courseError", "เริ่มสอนได้เฉพาะคอร์สที่เปิดรับสมัคร");
            return "redirect:/listCourse?tab=open";
        }
        if (registrations.countByCourse_CourseIdAndRegisterStatus(courseId, "อนุมัติแล้ว") < 1) {
            redirect.addFlashAttribute("courseError", "ต้องมีผู้เรียนที่ได้รับอนุมัติอย่างน้อย 1 คนก่อนเริ่มสอน");
            return "redirect:/listCourse?tab=open";
        }
        lifecycle.transitionToTeaching(course);
        redirect.addFlashAttribute("courseSuccess", "เริ่มดำเนินการสอนแล้ว ปิดคำขอที่ยังไม่ได้อนุมัติและคืนเครดิตตามเงื่อนไขเรียบร้อยแล้ว");
        return "redirect:/listCourse?tab=ongoing";
    }

    @PostMapping("/completeCourse/{courseId}")
    @Transactional
    public String complete(@PathVariable Integer courseId, HttpSession session, RedirectAttributes redirect) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var course = courses.findOwnedForUpdate(courseId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สของคุณ"));
        if (!"กำลังดำเนินการ".equals(course.getCourseStatus())) {
            redirect.addFlashAttribute("courseError", "ยืนยันสอนเสร็จได้เฉพาะคอร์สที่กำลังดำเนินการ");
            return "redirect:/listCourse?tab=ongoing";
        }
        do_ConfirmCompleteCourse(course);
        settlement.settle(courseId);
        redirect.addFlashAttribute("courseSuccess", "ยืนยันสอนเสร็จและส่งมอบเครดิตสำเร็จ");
        return "redirect:/listCourse?tab=completed";
    }

    private void do_ConfirmCompleteCourse(com.springboot.model.Course course) {
        course.setCourseStatus("สอนเสร็จสิ้น");
        courses.save(course);
    }

    @PostMapping("/openCourse/{courseId}")
    @Transactional
    public String do_OpenRegister(@PathVariable Integer courseId, HttpSession session, RedirectAttributes redirect) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var course = courses.findOwnedForUpdate(courseId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สของคุณ"));
        if (!"สร้างสำเร็จ".equals(course.getCourseStatus())) {
            redirect.addFlashAttribute("courseError", "เปิดรับสมัครได้เฉพาะคอร์สที่มีสถานะสร้างสำเร็จ");
            return "redirect:/listCourse";
        }
        var schedule = dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId);
        if (!checkDateTimeFuture(schedule)) {
            redirect.addFlashAttribute("courseError", "กรุณาแก้ไขวันและเวลาเริ่มเรียนให้เป็นเวลาในอนาคตก่อนเปิดรับสมัคร");
            return "redirect:/listCourse";
        }
        if (checkTeachingOverlapReg(memberId, schedule)) {
            redirect.addFlashAttribute("courseError", "เปิดรับสมัครไม่ได้ เนื่องจากเวลาสอนทับกับคอร์สที่คุณลงทะเบียนเรียนไว้");
            return "redirect:/listCourse";
        }
        if (checkTeachingOverlapTeach(memberId, courseId, schedule)) {
            redirect.addFlashAttribute("courseError", "เปิดรับสมัครไม่ได้ เนื่องจากเวลาสอนทับกับคอร์สอื่นที่คุณเปิดสอน");
            return "redirect:/listCourse";
        }
        course.setCourseStatus("เปิดรับสมัคร");
        courses.save(course);
        redirect.addFlashAttribute("courseSuccess", "เปิดรับสมัครคอร์สสำเร็จ");
        return "redirect:/listCourse?tab=open";
    }

    private boolean checkDateTimeFuture(java.util.List<com.springboot.model.CourseDate> schedule) {
        var now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Bangkok"));
        return !schedule.isEmpty() && schedule.stream().allMatch(d -> d.getTeachDate() != null
                && d.getStartTime() != null
                && java.time.LocalDateTime.of(d.getTeachDate(), d.getStartTime()).isAfter(now));
    }

    private boolean checkTeachingOverlapReg(Integer memberId, java.util.List<com.springboot.model.CourseDate> schedule) {
        return com.springboot.service.ScheduleConflict.learning(memberId, schedule, registrations, dates);
    }

    private boolean checkTeachingOverlapTeach(Integer memberId, Integer courseId,
            java.util.List<com.springboot.model.CourseDate> schedule) {
        return com.springboot.service.ScheduleConflict.teaching(memberId, courseId, schedule, courses, dates);
    }
}
