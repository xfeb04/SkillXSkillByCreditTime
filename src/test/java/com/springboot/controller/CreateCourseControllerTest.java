package com.springboot.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.util.LinkedMultiValueMap;
import com.springboot.model.*;
import com.springboot.repository.*;

class CreateCourseControllerTest {
    private final CourseRepository courses = mock(CourseRepository.class);
    private final CourseDateRepository dates = mock(CourseDateRepository.class);
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final TeacherRepository teachers = mock(TeacherRepository.class);
    private final CreditTransactionRepository transactions = mock(CreditTransactionRepository.class);
    private final CreateCourseController controller = new CreateCourseController(courses, dates, categories, teachers, transactions);
    private final MockHttpSession session = new MockHttpSession();

    private LinkedMultiValueMap<String, String> validForm() {
        session.setAttribute("memberId", 7);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(new Teacher()));
        when(categories.findById(1)).thenReturn(Optional.of(new Category()));
        when(courses.save(any(Course.class))).thenAnswer(call -> call.getArgument(0));
        var form = new LinkedMultiValueMap<String, String>();
        form.add("courseName", "Test course");
        form.add("courseDescription", "Course description for learners");
        form.add("delivery", "online");
        form.add("onlinePlatform", "Zoom");
        form.add("location", "Old location");
        form.add("categoryId", "1");
        form.add("requiredEquip", "Notebook");
        form.add("maxMember", "10");
        form.add("courseType", "ปกติ");
        form.add("creditPerCourse", "3");
        form.add("teachDate", LocalDate.now().plusDays(2).toString());
        form.add("startTime", "09:00");
        form.add("endTime", "12:00");
        return form;
    }

    @Test void onlineDiscardsHiddenLocationAndSavesSchedule() {
        controller.doCreateCourse(validForm(), session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        verify(courses).save(argThat(c -> c.getLocation() == null && "Zoom".equals(c.getOnlinePlatform())
                && "สร้างสำเร็จ".equals(c.getCourseStatus())));
        verify(dates).saveAll(anyList());
    }

    @Test void offlineDiscardsPlatformAndFreeCourseIgnoresSubmittedCredit() {
        var form = validForm();
        form.set("delivery", "offline");
        form.set("courseType", "เรียนฟรี");
        controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        verify(courses).save(argThat(c -> c.getOnlinePlatform() == null && "Old location".equals(c.getLocation())
                && c.getCreditPerCourse() == 2.0));
    }

    @Test void overlappingDatesRedisplayWithoutSaving() {
        var form = validForm();
        form.add("teachDate", form.getFirst("teachDate"));
        form.add("startTime", "11:00");
        form.add("endTime", "13:00");
        var model = new ExtendedModelMap();
        assertEquals("createCourse", controller.doCreateCourse(form, session, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
        verify(dates, never()).saveAll(any());
        assertNotNull(model.get("errors"));
    }
    @Test void teacherPaidReservesAllSeats() {
        var form = validForm();
        var teacher = new Teacher();
        teacher.setCreditBalance(40.0); teacher.setCreditFrozen(5.0);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(teacher));
        form.set("courseType", "ผู้สอนจ่ายให้ผู้เรียน");
        var redirect = new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap();
        controller.doCreateCourse(form, session, new ExtendedModelMap(), redirect);
        assertEquals(10.0, teacher.getCreditBalance());
        assertEquals(35.0, teacher.getCreditFrozen());
        verify(transactions).save(argThat(t -> t.getAmount() == 30.0 && t.getRegisterCourse() == null
                && t.getReservedCourse() != null && "พักเครดิตคอร์ส".equals(t.getCreditTranStatus())));
        assertEquals("สร้างคอร์สสำเร็จ", redirect.getFlashAttributes().get("courseSuccess"));
    }
    @Test void insufficientCreditDoesNotSaveOrFreeze() {
        var form = validForm();
        form.set("courseType", "ผู้สอนจ่ายให้ผู้เรียน");
        assertEquals("createCourse", controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
        verify(teachers, never()).save(any());
    }
    @Test void todayIsRejectedEvenWithLateStart() {
        var form = validForm();
        form.set("teachDate", LocalDate.now().toString());
        form.set("startTime", "23:58"); form.set("endTime", "23:59");
        assertEquals("createCourse", controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
    }    @Test void rejectsInvalidNameAndShortDescription() {
        var form = validForm();
        form.set("courseName", "Course 123");
        form.set("courseDescription", "Short");
        var model = new ExtendedModelMap();
        assertEquals("createCourse", controller.doCreateCourse(form, session, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
        assertTrue(model.get("errors").toString().contains("20–255"));
        assertTrue(model.get("errors").toString().contains("เฉพาะตัวอักษร"));
    }    @Test void zeroCreditIsAllowed() {
        var form = validForm(); form.set("creditPerCourse", "0");
        controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        verify(courses).save(argThat(c -> c.getCreditPerCourse() == 0.0));
    }
    @Test void missingCreditAndEquipmentAreRejected() {
        var form = validForm(); form.set("creditPerCourse", ""); form.set("requiredEquip", "");
        assertEquals("createCourse", controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
    }
    @Test void platformRejectsUnsupportedCharactersButHiddenLocationIsIgnored() {
        var form = validForm(); form.set("onlinePlatform", "Zoom<example>");
        assertEquals("createCourse", controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses, never()).save(any());
        form.set("onlinePlatform", "https://meet.google.com/abc-def?pwd=A%2Bb&user=test_1#room"); form.set("location", "@!");
        controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        verify(courses).save(argThat(c -> c.getLocation() == null));
    }    @Test void offlineLocationAcceptsMapLink() {
        var form = validForm();
        form.set("delivery", "offline");
        form.set("location", "https://maps.google.com/?q=Bangkok&zoom=15");
        controller.doCreateCourse(form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        verify(courses).save(argThat(c -> c.getLocation().contains("?q=") && c.getOnlinePlatform() == null));
    }    @Test void editingRefundsOnlyReserveDifference() {
        var form = validForm();
        var teacher = new Teacher(); teacher.setCreditBalance(10.0); teacher.setCreditFrozen(35.0);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(teacher));
        var existing = new Course(); existing.setCourseStatus("สร้างสำเร็จ");
        existing.setCourseType("ผู้สอนจ่ายให้ผู้เรียน"); existing.setCreditPerCourse(3.0); existing.setMaxMember(10);
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(existing));
        form.set("courseType", "ผู้สอนจ่ายให้ผู้เรียน"); form.set("maxMember","5");
        controller.do_editCourse(11, form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        assertEquals(25.0,teacher.getCreditBalance()); assertEquals(20.0,teacher.getCreditFrozen());
        verify(courses).save(existing); verify(dates).deleteAll(anyList());
    }
    @Test void cannotEditOtherOwnersCourse() {
        var form = validForm();
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.do_editCourse(11,form,session,new ExtendedModelMap(),new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses,never()).save(any());
    }    @Test void unchangedReservationAllowsDetailsEditWithoutTouchingCredits() {
        var form = validForm();
        var teacher = new Teacher(); teacher.setCreditBalance(10.0); teacher.setCreditFrozen(0.0);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(teacher));
        var existing = new Course(); existing.setCourseStatus("สร้างสำเร็จ");
        existing.setCourseType("ผู้สอนจ่ายให้ผู้เรียน"); existing.setCreditPerCourse(3.0); existing.setMaxMember(10);
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(existing));
        form.set("courseType", "ผู้สอนจ่ายให้ผู้เรียน");
        var result = controller.do_editCourse(11, form, session, new ExtendedModelMap(), new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
        assertTrue(result.startsWith("redirect:"));
        verify(courses).save(existing); verify(teachers,never()).save(any());
        assertEquals(10.0,teacher.getCreditBalance()); assertEquals(0.0,teacher.getCreditFrozen());
    }
    @Test void missingReservationCannotBeRefundedWhenReducingSeats() {
        var form = validForm();
        var teacher = new Teacher(); teacher.setCreditBalance(10.0); teacher.setCreditFrozen(0.0);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(teacher));
        var existing = new Course(); existing.setCourseStatus("สร้างสำเร็จ");
        existing.setCourseType("ผู้สอนจ่ายให้ผู้เรียน"); existing.setCreditPerCourse(3.0); existing.setMaxMember(10);
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(existing));
        form.set("courseType", "ผู้สอนจ่ายให้ผู้เรียน"); form.set("maxMember","5");
        assertEquals("createCourse", controller.do_editCourse(11,form,session,new ExtendedModelMap(),new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(courses,never()).save(any()); verify(teachers,never()).save(any());
    }}
