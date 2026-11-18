package com.springboot.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import com.springboot.model.*;
import com.springboot.repository.*;

class RegisterCourseControllerTest {
    final CourseRepository courses = mock(CourseRepository.class);
    final CourseDateRepository dates = mock(CourseDateRepository.class);
    final TeacherRepository members = mock(TeacherRepository.class);
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    final CreditTransactionRepository transactions = mock(CreditTransactionRepository.class);
    final RegisterCourseController controller = new RegisterCourseController(courses, dates, members, registrations, transactions);
    final Teacher learner = new Teacher();
    final MockHttpSession session = new MockHttpSession();

    void setup(String type, double balance) {
        session.setAttribute("memberId", 7);
        var teacher = new Teacher(); teacher.setMemberId(8);
        var course = new Course(); org.springframework.test.util.ReflectionTestUtils.setField(course, "courseId", 11); course.setTeacher(teacher);
        course.setCourseType(type); course.setCourseStatus("เปิดรับสมัคร"); course.setCreditPerCourse(4.0);
        learner.setMemberId(7); learner.setCreditBalance(balance); learner.setCreditFrozen(1.0);
        var date = new CourseDate(); date.setTeachDate(LocalDate.now().plusDays(10));
        date.setStartTime(LocalTime.of(10,0)); date.setEndTime(LocalTime.of(11,0));
        when(courses.findForUpdate(11)).thenReturn(Optional.of(course));
        when(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(11)).thenReturn(List.of(date));
        when(members.findForUpdate(7)).thenReturn(Optional.of(learner));
        when(registrations.save(any(RegisterCourse.class))).thenAnswer(i -> i.getArgument(0));
        when(transactions.save(any(CreditTransaction.class))).thenAnswer(i -> i.getArgument(0));
    }

    @ParameterizedTest
    @ValueSource(strings={"ปกติ", "เรียนฟรี", "ผู้สอนจ่ายให้ผู้เรียน"})
    void registersWithCreditRulesForEachType(String type) {
        setup(type,10);
        var redirect = new RedirectAttributesModelMap();
        assertEquals("redirect:/searchCourse",controller.registerCourse(11,session,redirect));
        assertTrue(redirect.getFlashAttributes().containsKey("registrationSuccess"));
        boolean normal = "ปกติ".equals(type);
        assertEquals(normal ? 6.0 : 10.0,learner.getCreditBalance());
        assertEquals(normal ? 5.0 : 1.0,learner.getCreditFrozen());
        verify(registrations,atLeastOnce()).save(argThat(r -> "รออนุมัติ".equals(r.getRegisterStatus())));
        verify(transactions,times(normal ? 1 : 0)).save(any(CreditTransaction.class));
    }

    @Test void insufficientCreditDoesNotWrite() {
        setup("ปกติ",2);
        var redirect = new RedirectAttributesModelMap();
        controller.registerCourse(11,session,redirect);
        assertTrue(redirect.getFlashAttributes().containsKey("registrationError"));
        assertEquals(2.0,learner.getCreditBalance());
        verify(members,never()).save(any());
        verify(registrations,never()).save(any());
        verifyNoInteractions(transactions);
    }
}

