package com.springboot.controller;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.dto.SearchCourseCardDto;
import com.springboot.model.Course;
import com.springboot.repository.CategoryRepository;
import com.springboot.repository.CourseRepository;
import com.springboot.repository.RegisterCourseRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class SearchCourseController {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final RegisterCourseRepository registerCourseRepository;

    public SearchCourseController(CourseRepository courseRepository, CategoryRepository categoryRepository,
            RegisterCourseRepository registerCourseRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.registerCourseRepository = registerCourseRepository;
    }

    @GetMapping({ "/", "/searchCourse" })
    public String SearchCourseByKeyword(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "all") String delivery,
            @RequestParam(defaultValue = "all") String courseType,
            HttpSession session,
            Model model) {

        List<SearchCourseCardDto> courses = courseRepository.findAll().stream()
                .filter(course -> "เปิดรับสมัคร".equals(course.getCourseStatus()))
                .filter(course -> categoryId == null || course.getCategory().getCategoryId().equals(categoryId))
                .filter(course -> matchesDelivery(course, delivery))
                .filter(course -> matchesCourseType(course, courseType))
                .filter(course -> matchesKeyword(course, keyword))
                .map(SearchCourseCardDto::from)
                .toList();

        model.addAttribute("courses", courses);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("delivery", delivery);
        model.addAttribute("courseType", courseType);
        Integer memberId = (Integer) session.getAttribute("memberId");
        Set<Integer> registeredCourseIds = memberId == null ? Set.of()
                : registerCourseRepository.findByMember_MemberId(memberId).stream()
                        .filter(registration -> !Set.of("ยกเลิก", "ยกเลิกแล้ว", "ปฏิเสธคำขอ")
                                .contains(registration.getRegisterStatus()))
                        .map(registration -> registration.getCourse().getCourseId())
                        .collect(java.util.stream.Collectors.toSet());
        model.addAttribute("registeredCourseIds", registeredCourseIds);
        return "searchCourse";
    }

    private boolean matchesDelivery(Course course, String delivery) {
        return "all".equals(delivery)
                || ("online".equals(delivery) && Boolean.TRUE.equals(course.getIsOnline()))
                || ("offline".equals(delivery) && Boolean.FALSE.equals(course.getIsOnline()));
    }

    private boolean matchesCourseType(Course course, String courseType) {
        return "all".equals(courseType)
                || ("normal".equals(courseType) && "ปกติ".equals(course.getCourseType()))
                || ("free".equals(courseType) && "เรียนฟรี".equals(course.getCourseType()))
                || ("teacher-paid".equals(courseType) && "ผู้สอนจ่ายให้ผู้เรียน".equals(course.getCourseType()));
    }

    private boolean matchesKeyword(Course course, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String search = keyword.trim().toLowerCase(Locale.ROOT);
        return Stream.of(course.getCourseName(), course.getCourseDescription(), course.getCategory().getCategoryName(),
                course.getTeacher().getFirstname(),
                course.getTeacher().getSurname())
                .filter(value -> value != null)
                .anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(search));
    }
}
