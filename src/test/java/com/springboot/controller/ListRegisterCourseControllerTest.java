package com.springboot.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import com.springboot.dto.RegisterCourseCardDto;
import com.springboot.model.*;
import com.springboot.repository.*;

class ListRegisterCourseControllerTest {
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    final CourseDateRepository dates = mock(CourseDateRepository.class);
    final ListRegisterCourseController controller = new ListRegisterCourseController(registrations, dates);

    RegisterCourse registration(String name, String status, String courseStatus) {
        var category = new Category(); category.setCategoryName("ภาษา");
        var course = new Course(); course.setCourseName(name); course.setCategory(category);
        course.setCourseStatus(courseStatus);
        var registration = new RegisterCourse(); registration.setCourse(course); registration.setRegisterStatus(status);
        return registration;
    }

    @Test void requiresLogin() {
        assertEquals("redirect:/loginMember", controller.getListRegisterCourse("registered", new MockHttpSession(), new ExtendedModelMap()));
        verifyNoInteractions(registrations, dates);
    }

    @Test void separatesLearnersRegistrationsByApprovalAndCourseLifecycle() {
        when(registrations.findByMember_MemberId(7)).thenReturn(List.of(
                registration("pending", "รออนุมัติ", "เปิดรับสมัคร"),
                registration("approved", "อนุมัติแล้ว", "เปิดรับสมัคร"),
                registration("learning", "อนุมัติแล้ว", "กำลังดำเนินการ"),
                registration("finished", "อนุมัติแล้ว", "สอนเสร็จสิ้น"),
                registration("legacy", "เรียนเสร็จสิ้น", "สอนเสร็จสิ้น"),
                registration("rejected", "ปฏิเสธคำขอ", "เปิดรับสมัคร"),
                registration("cancelled", "ยกเลิกแล้ว", "เปิดรับสมัคร")));
        assertEquals(List.of("pending"), names("registered"));
        assertEquals(List.of("approved"), names("approved"));
        assertEquals(List.of("learning"), names("ongoing"));
        assertEquals(List.of("finished", "legacy"), names("completed"));
        assertEquals(List.of("pending"), names("invalid"));
        assertEquals(List.of("rejected", "cancelled"), names("cancelled"));
        verify(registrations, times(6)).findByMember_MemberId(7);
    }

    @SuppressWarnings("unchecked")
    List<String> names(String tab) {
        var session = new MockHttpSession(); session.setAttribute("memberId", 7);
        var model = new ExtendedModelMap();
        assertEquals("listRegisterCourse", controller.getListRegisterCourse(tab, session, model));
        assertEquals("invalid".equals(tab) ? "registered" : tab, model.get("activeTab"));
        return ((List<RegisterCourseCardDto>)model.get("courses")).stream().map(RegisterCourseCardDto::getCourseName).toList();
    }
}
