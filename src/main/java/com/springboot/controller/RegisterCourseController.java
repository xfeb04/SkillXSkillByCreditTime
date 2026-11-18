package com.springboot.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.model.CreditTransaction;
import com.springboot.model.Course;
import com.springboot.model.CourseDate;
import com.springboot.model.RegisterCourse;
import com.springboot.model.Teacher;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.CourseDateRepository;
import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.RegisterCourseRepository;
import com.springboot.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class RegisterCourseController {

    private static final String OPEN_FOR_REGISTRATION = "เปิดรับสมัคร";
    private static final String PENDING_STATUS = "รออนุมัติ";
    private static final Set<String> CONFLICTING_REGISTRATION_STATUSES = Set.of("รออนุมัติ", "อนุมัติแล้ว");

    private final CourseRepository courseRepository;
    private final CourseDateRepository courseDateRepository;
    private final TeacherRepository teacherRepository;
    private final RegisterCourseRepository registerCourseRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public RegisterCourseController(CourseRepository courseRepository, CourseDateRepository courseDateRepository,
            TeacherRepository teacherRepository,
            RegisterCourseRepository registerCourseRepository, CreditTransactionRepository creditTransactionRepository) {
        this.courseRepository = courseRepository;
        this.courseDateRepository = courseDateRepository;
        this.teacherRepository = teacherRepository;
        this.registerCourseRepository = registerCourseRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    @PostMapping("/registerCourse/{courseId}")
    @Transactional
    public String registerCourse(@PathVariable Integer courseId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Basic Flow 2: courseId comes from the URL; memberId comes from the session.
        Integer memberId = getMemberId(session);
        if (memberId == null) return "redirect:/loginMember";

        Course course = courseRepository.findForUpdate(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบคอร์สที่เลือก"));
        if (!OPEN_FOR_REGISTRATION.equals(course.getCourseStatus())) {
            redirectAttributes.addFlashAttribute("registrationError", "คอร์สนี้ปิดรับสมัครแล้ว");
            return "redirect:/searchCourse";
        }
        if (course.getTeacher().getMemberId().equals(memberId)) {
            redirectAttributes.addFlashAttribute("registrationError", "ไม่สามารถลงทะเบียนคอร์สของตนเองได้");
            return "redirect:/searchCourse";
        }
        if (registerCourseRepository.findByMember_MemberIdAndCourse_CourseId(memberId, courseId)
                .filter(r -> !Set.of("ยกเลิก", "ยกเลิกแล้ว", "ปฏิเสธคำขอ").contains(r.getRegisterStatus())).isPresent()) {
            redirectAttributes.addFlashAttribute("registrationError", "คุณส่งคำขอลงทะเบียนคอร์สนี้แล้ว");
            return "redirect:/searchCourse";
        }
        var schedule = courseDateRepository.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId);
        var now = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Bangkok"));
        if (schedule.isEmpty() || schedule.stream().anyMatch(d ->
                !java.time.LocalDateTime.of(d.getTeachDate(), d.getStartTime()).isAfter(now))) {
            redirectAttributes.addFlashAttribute("registrationError", "คอร์สนี้ถึงเวลาเริ่มเรียนแล้ว");
            return "redirect:/searchCourse";
        }
        if (checkTeachingTime(memberId, courseId, schedule)) {
            redirectAttributes.addFlashAttribute("registrationError", "ลงทะเบียนไม่ได้ เนื่องจากเวลาเรียนทับกับเวลาที่คุณต้องสอน");
            return "redirect:/searchCourse";
        }
        if (checkRegisterTime(memberId, course)) {
            redirectAttributes.addFlashAttribute("registrationError",
                    "ไม่สามารถลงทะเบียนได้ เนื่องจากเวลาเรียนชนกับคอร์สที่ส่งคำขอไว้แล้ว");
            return "redirect:/searchCourse";
        }

        Teacher member = teacherRepository.findForUpdate(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ไม่พบข้อมูลสมาชิก"));
        double amount = checkTypeCourse(course);
        if (!checkEnoughCredit(member, amount)) {
            redirectAttributes.addFlashAttribute("registrationError", "เครดิตคงเหลือไม่เพียงพอสำหรับชำระค่าเรียน");
            return "redirect:/searchCourse";
        }

        doFreezeCredit(member, amount);
        RegisterCourse registration = doRegisterCourse(member, course);
        doInsertCreditTrans(registration, amount);
        return showRegistrationSuccess(redirectAttributes);
    }

    // Basic Flow 2: identify the logged-in learner.
    private Integer getMemberId(HttpSession session) {
        return (Integer) session.getAttribute("memberId");
    }

    // Basic Flow 3: check overlap with courses the learner teaches.
    private boolean checkTeachingTime(Integer memberId, Integer courseId, List<CourseDate> schedule) {
        return com.springboot.service.ScheduleConflict.teaching(
                memberId, courseId, schedule, courseRepository, courseDateRepository);
    }

    // Basic Flow 6: check available credit before making any changes.
    private boolean checkEnoughCredit(Teacher member, double amount) {
        return valueOrZero(member.getCreditBalance()) >= amount;
    }

    // Basic Flow 7: move the calculated amount from balance to frozen credit.
    private void doFreezeCredit(Teacher member, double amount) {
        member.setCreditBalance(valueOrZero(member.getCreditBalance()) - amount);
        member.setCreditFrozen(valueOrZero(member.getCreditFrozen()) + amount);
        teacherRepository.save(member);
    }

    // Basic Flow 8 (8.1–8.2): save and return the pending registration.
    // Return the saved entity so step 9 can link its transaction; failures propagate for rollback.
    private RegisterCourse doRegisterCourse(Teacher member, Course course) {
        return registerCourseRepository.save(new RegisterCourse(PENDING_STATUS, LocalDate.now(),
                null, null, null, member, course, null));
    }

    // Basic Flow 9: only normal courses create a credit transaction at registration time.
    private void doInsertCreditTrans(RegisterCourse registration, double amount) {
        Course course = registration.getCourse();
        if (!"ปกติ".equals(course.getCourseType())) return;
        String description = "ชำระค่าเรียนคอร์ส " + course.getCourseName() + " ของผู้เรียน "
                + registration.getMember().getUsername() + " (คำขอรออนุมัติ)";
        CreditTransaction transaction = creditTransactionRepository.save(new CreditTransaction(amount, LocalDate.now(),
                "ชำระค่าเรียน", description, "รอผู้สอนอนุมัติ", registration));
        registration.setCreditTransaction(transaction);
        registerCourseRepository.save(registration);
    }

    // Basic Flow 10–11: display the success result and end the request.
    private String showRegistrationSuccess(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("registrationSuccess", "ส่งคำขอลงทะเบียนแล้ว");
        return "redirect:/searchCourse";
    }

    // Basic Flow 5: calculate the learner's cost for the selected course type.

    private double checkTypeCourse(Course course) {
        return "ปกติ".equals(course.getCourseType()) ? valueOrZero(course.getCreditPerCourse()) : 0;
    }

    // Basic Flow 4: check overlap with pending or approved learning registrations.
    private boolean checkRegisterTime(Integer memberId, Course newCourse) {
        List<CourseDate> newCourseDates = courseDateRepository
                .findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(newCourse.getCourseId());
        if (newCourseDates.isEmpty()) return false;

        return registerCourseRepository.findByMember_MemberId(memberId).stream()
                .filter(registration -> CONFLICTING_REGISTRATION_STATUSES.contains(registration.getRegisterStatus()))
                .anyMatch(registration -> datesOverlap(newCourseDates, courseDateRepository
                        .findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(registration.getCourse().getCourseId())));
    }

    private boolean datesOverlap(List<CourseDate> newCourseDates, List<CourseDate> existingCourseDates) {
        return newCourseDates.stream().anyMatch(newDate -> existingCourseDates.stream()
                .filter(existingDate -> newDate.getTeachDate().equals(existingDate.getTeachDate()))
                .anyMatch(existingDate -> newDate.getStartTime().isBefore(existingDate.getEndTime())
                        && newDate.getEndTime().isAfter(existingDate.getStartTime())));
    }

    private double valueOrZero(Double value) {
        return value == null ? 0 : value;
    }
}
