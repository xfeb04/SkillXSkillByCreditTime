package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.server.ResponseStatusException;
import com.springboot.model.Course;
import com.springboot.repository.*;

class ListCourseControllerTest {
    @Test void startsOnlyWithApprovedLearnerAndDoesNotRepeat() {
        var course = new Course(); course.setCourseStatus("เปิดรับสมัคร");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        assertEquals("redirect:/listCourse?tab=open", controller.start(11,session(),new RedirectAttributesModelMap()));
        verifyNoInteractions(lifecycle);
        when(registrations.countByCourse_CourseIdAndRegisterStatus(11,"อนุมัติแล้ว")).thenReturn(1L);
        assertEquals("redirect:/listCourse?tab=ongoing", controller.start(11,session(),new RedirectAttributesModelMap()));
        course.setCourseStatus("กำลังดำเนินการ");
        controller.start(11,session(),new RedirectAttributesModelMap());
        verify(lifecycle,times(1)).transitionToTeaching(course);
    }
    @Test void startRequiresLoginAndOwnership() {
        assertEquals("redirect:/loginMember", controller.start(11,new MockHttpSession(),new RedirectAttributesModelMap()));
        assertThrows(ResponseStatusException.class, () -> controller.start(11,session(),new RedirectAttributesModelMap()));
        verifyNoInteractions(lifecycle);
    }
    @Test void completesOnlyOwnedOngoingCourse() {
        var course = new Course(); course.setCourseStatus("กำลังดำเนินการ");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        assertEquals("redirect:/listCourse?tab=completed",controller.complete(11,session(),new RedirectAttributesModelMap()));
        assertEquals("สอนเสร็จสิ้น",course.getCourseStatus()); verify(courses).save(course);
        controller.complete(11,session(),new RedirectAttributesModelMap());
        verify(courses,times(1)).save(course);
    }
    @Test void completionRequiresOwnershipAndLogin() {
        assertThrows(ResponseStatusException.class,()->controller.complete(11,session(),new RedirectAttributesModelMap()));
        assertEquals("redirect:/loginMember",controller.complete(11,new MockHttpSession(),new RedirectAttributesModelMap()));
        verify(courses,never()).save(any());
    }
    @Test void cannotCompleteOpenCourse() {
        var course = new Course(); course.setCourseStatus("เปิดรับสมัคร");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        controller.complete(11,session(),new RedirectAttributesModelMap());
        assertEquals("เปิดรับสมัคร",course.getCourseStatus()); verify(courses,never()).save(any());
    }
    final CourseRepository courses = mock(CourseRepository.class);
    final CourseDateRepository dates = mock(CourseDateRepository.class);
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    final com.springboot.service.CourseCreditSettlementService settlement = mock(com.springboot.service.CourseCreditSettlementService.class);
    final com.springboot.service.CourseStatusScheduler lifecycle = mock(com.springboot.service.CourseStatusScheduler.class);
    final ListCourseController controller = new ListCourseController(courses, dates, registrations, settlement, lifecycle);
    MockHttpSession session() { var session = new MockHttpSession(); session.setAttribute("memberId", 7); return session; }
    @Test void opensOwnedCreatedCourse() {
        var date = new com.springboot.model.CourseDate();
        date.setTeachDate(java.time.LocalDate.now().plusDays(2));
        date.setStartTime(java.time.LocalTime.NOON);
        when(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(11)).thenReturn(List.of(date));
        var course = new Course(); course.setCourseStatus("สร้างสำเร็จ");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        assertEquals("redirect:/listCourse?tab=open", controller.do_OpenRegister(11, session(), new RedirectAttributesModelMap()));
        assertEquals("เปิดรับสมัคร", course.getCourseStatus()); verify(courses).save(course);
    }
    @Test void cannotOpenPastOrMissingSchedule() {
        var course = new Course(); course.setCourseStatus("สร้างสำเร็จ");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        assertEquals("redirect:/listCourse", controller.do_OpenRegister(11,session(),new RedirectAttributesModelMap()));
        var date = new com.springboot.model.CourseDate();
        date.setTeachDate(java.time.LocalDate.now().minusDays(1)); date.setStartTime(java.time.LocalTime.NOON);
        when(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(11)).thenReturn(List.of(date));
        assertEquals("redirect:/listCourse", controller.do_OpenRegister(11,session(),new RedirectAttributesModelMap()));
        verify(courses,never()).save(any());
    }
    @Test void cannotOpenSomeoneElsesCourse() {
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> controller.do_OpenRegister(11, session(), new RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
    }
    @Test void doesNotReopenCompletedCourse() {
        var course = new Course(); course.setCourseStatus("สอนเสร็จสิ้น");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        controller.do_OpenRegister(11, session(), new RedirectAttributesModelMap());
        assertEquals("สอนเสร็จสิ้น", course.getCourseStatus()); verify(courses, never()).save(any());
    }
    @Test void tabsQueryOnlyLoggedInTeachersCourses() {
        var tabs = Map.of("created","สร้างสำเร็จ","open","เปิดรับสมัคร","ongoing","กำลังดำเนินการ","completed","สอนเสร็จสิ้น");
        tabs.forEach((tab,status) -> {
            controller.getListCourseByOwner(tab,session(),new ExtendedModelMap());
            verify(courses).findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(7,status);
        });
    }
    @Test void requiresLogin() {
        assertEquals("redirect:/loginMember",controller.getListCourseByOwner("created",new MockHttpSession(),new ExtendedModelMap()));
        assertEquals("redirect:/loginMember",controller.do_OpenRegister(11,new MockHttpSession(),new RedirectAttributesModelMap()));
        verifyNoInteractions(courses);
    }
}
