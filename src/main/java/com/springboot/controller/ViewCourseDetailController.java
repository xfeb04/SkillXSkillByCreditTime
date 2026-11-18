package com.springboot.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.springboot.dto.CourseDetailDto;
import com.springboot.model.Course;
import com.springboot.model.CourseDate;
import com.springboot.model.RegisterCourse;
import com.springboot.model.Teacher;
import com.springboot.repository.CourseDateRepository;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.RegisterCourseRepository;
import com.springboot.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewCourseDetailController {

    private static final String APPROVED_STATUS = "อนุมัติแล้ว";
    private static final String PENDING_STATUS = "รออนุมัติ";
    private static final String REJECTED_STATUS = "ปฏิเสธคำขอ";

    private final CourseRepository courseRepository;
    private final CourseDateRepository courseDateRepository;
    private final RegisterCourseRepository registerCourseRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final TeacherRepository teacherRepository;

    public ViewCourseDetailController(CourseRepository courseRepository, CourseDateRepository courseDateRepository,
            RegisterCourseRepository registerCourseRepository, CreditTransactionRepository creditTransactionRepository,
            TeacherRepository teacherRepository) {
        this.courseRepository = courseRepository;
        this.courseDateRepository = courseDateRepository;
        this.registerCourseRepository = registerCourseRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/viewCourseDetail/{courseId}")
    @Transactional
    public String viewCourseDetail(@PathVariable int courseId, HttpSession session, Model model) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สที่เลือก"));
        List<CourseDate> courseDates = courseDateRepository
                .findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId);
        boolean courseStarted = courseDates.stream()
                .map(date -> LocalDateTime.of(date.getTeachDate(), date.getStartTime()))
                .min(LocalDateTime::compareTo)
                .map(startDateTime -> !LocalDateTime.now(java.time.ZoneId.of("Asia/Bangkok")).isBefore(startDateTime))
                .orElse(false);

        long approvedMemberCount = registerCourseRepository
                .countByCourse_CourseIdAndRegisterStatus(courseId, APPROVED_STATUS)
                + registerCourseRepository.countByCourse_CourseIdAndRegisterStatus(courseId, "เรียนเสร็จสิ้น");
        Integer memberId = (Integer) session.getAttribute("memberId");
        RegisterCourse registration = memberId == null ? null
                : registerCourseRepository.findByMember_MemberIdAndCourse_CourseId(memberId, courseId).orElse(null);
        boolean isCourseOwner = memberId != null && course.getTeacher().getMemberId().equals(memberId);
        model.addAttribute("canEditCourse", isCourseOwner && "สร้างสำเร็จ".equals(course.getCourseStatus()));
        model.addAttribute("canCompleteCourse", isCourseOwner && "กำลังดำเนินการ".equals(course.getCourseStatus()));
        model.addAttribute("canManageRequests", isCourseOwner && "เปิดรับสมัคร".equals(course.getCourseStatus()));
        model.addAttribute("canViewStudents", isCourseOwner
                && java.util.List.of("เปิดรับสมัคร", "กำลังดำเนินการ", "สอนเสร็จสิ้น").contains(course.getCourseStatus()));

        model.addAttribute("course", CourseDetailDto.from(course, approvedMemberCount, courseDates));
        model.addAttribute("canRegister", "เปิดรับสมัคร".equals(course.getCourseStatus())
                && memberId != null && !isCourseOwner && !courseStarted && (registration == null || java.util.Set.of("ยกเลิก", "ยกเลิกแล้ว", "ปฏิเสธคำขอ").contains(registration.getRegisterStatus())));
        model.addAttribute("canCancelRegistration", registration != null && PENDING_STATUS.equals(registration.getRegisterStatus()));
        model.addAttribute("registrationStatus", registration == null ? null : registration.getRegisterStatus());
        model.addAttribute("registrationId", registration == null ? null : registration.getRegisterCourseId());
        model.addAttribute("canReviewCourse", ReviewCourseController.canReview(registration));
        model.addAttribute("canReportTeacher", registration != null && !isCourseOwner
                && java.util.Set.of("อนุมัติแล้ว", "เรียนเสร็จสิ้น").contains(registration.getRegisterStatus()));
        model.addAttribute("alreadyReviewed", registration != null && registration.getRatingScore() != null);
        model.addAttribute("reviewScore", registration == null ? null : registration.getRatingScore());
        return "viewCourseDetail";
    }

}
