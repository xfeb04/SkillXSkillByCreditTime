package com.springboot.controller;

import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.springboot.dto.ProfileCourseCardDto;
import com.springboot.model.Teacher;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.RegisterCourseRepository;
import com.springboot.repository.SkillRepository;
import com.springboot.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewProfileController {

    private static final String OPEN_FOR_REGISTRATION = "เปิดรับสมัคร";
    private static final String APPROVED_STATUS = "อนุมัติแล้ว";

    private final TeacherRepository teacherRepository;
    private final SkillRepository skillRepository;
    private final CourseRepository courseRepository;
    private final RegisterCourseRepository registerCourseRepository;

    public ViewProfileController(TeacherRepository teacherRepository, SkillRepository skillRepository,
            CourseRepository courseRepository, RegisterCourseRepository registerCourseRepository) {
        this.teacherRepository = teacherRepository;
        this.skillRepository = skillRepository;
        this.courseRepository = courseRepository;
        this.registerCourseRepository = registerCourseRepository;
    }

    @GetMapping("/viewProfile")
    public String viewProfile(HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";

        return viewProfile(memberId, true, model);
    }

    @GetMapping("/viewProfile/{teacherId}")
    public String viewProfile(@PathVariable Integer teacherId, HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        return viewProfile(teacherId, teacherId.equals(memberId), model);
    }

    private String viewProfile(Integer memberId, boolean isOwnProfile, Model model) {
        Teacher teacher = teacherRepository.findById(memberId).orElse(null);
        if (teacher == null) {
            model.addAttribute("errorMessage", "ไม่พบข้อมูลสมาชิก");
            model.addAttribute("errorCode", "404");
            model.addAttribute("errorTitle", "Not Found");
            return "err";
        }

        List<ProfileCourseCardDto> courses = courseRepository
                .findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(memberId, OPEN_FOR_REGISTRATION)
                .stream()
                .map(course -> ProfileCourseCardDto.from(course, registerCourseRepository
                        .countByCourse_CourseIdAndRegisterStatus(course.getCourseId(), APPROVED_STATUS)))
                .toList();

        model.addAttribute("reviews", registerCourseRepository
                .findByCourse_Teacher_MemberIdAndRatingScoreIsNotNullOrderByReviewDateDescRegisterCourseIdDesc(memberId)
                .stream()
                .filter(review -> review.getComment() != null && !review.getComment().isBlank())
                .map(com.springboot.dto.ReviewCardDto::new).toList());
        model.addAttribute("profile", teacher);
        Double average = registerCourseRepository.averageRatingForTeacher(memberId);
        model.addAttribute("rating", java.math.BigDecimal.valueOf(average == null ? 0 : average)
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        model.addAttribute("skills", skillRepository.findByTeacher_MemberIdOrderBySkillIdDesc(memberId));
        model.addAttribute("courses", courses);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("profileHasImage", teacher.getImgProfile() != null && teacher.getImgProfile().length > 0);
        model.addAttribute("profileImageUrl", "/member/" + memberId + "/profile-image");
        model.addAttribute("profileInitial", teacher.getFirstname() == null || teacher.getFirstname().isBlank()
                ? "-" : teacher.getFirstname().substring(0, 1));
        return "viewProfile";
    }
}
