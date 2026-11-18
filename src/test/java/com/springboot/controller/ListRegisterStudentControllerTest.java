package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;
import com.springboot.model.*;
import com.springboot.repository.*;
class ListRegisterStudentControllerTest {
    final CourseRepository courses=mock(CourseRepository.class);
    final RegisterCourseRepository registrations=mock(RegisterCourseRepository.class);
    final ListRegisterStudentController controller=new ListRegisterStudentController(courses,registrations);
    @Test void requiresLogin() {
        assertEquals("redirect:/loginMember",controller.getListRegStudent(12,new MockHttpSession(),new ExtendedModelMap()));
        verifyNoInteractions(courses,registrations);
    }
    @Test void ownerSeesApprovedStudentsOnly() {
        var session=new MockHttpSession(); session.setAttribute("memberId",7);
        var teacher=new Teacher(); teacher.setMemberId(7);
        var course=new Course(); course.setTeacher(teacher);
        when(courses.findById(12)).thenReturn(Optional.of(course));
        var students=List.of(new RegisterCourse());
        when(registrations.findByCourse_CourseIdAndRegisterStatus(12,"อนุมัติแล้ว")).thenReturn(students);
        var model=new ExtendedModelMap();
        assertEquals("listRegisterStudent",controller.getListRegStudent(12,session,model));
        assertEquals(students,model.get("students")); assertSame(course,model.get("course"));
    }
    @Test void otherMemberCannotSeeStudents() {
        var session=new MockHttpSession(); session.setAttribute("memberId",8);
        var teacher=new Teacher(); teacher.setMemberId(7);
        var course=new Course(); course.setTeacher(teacher);
        when(courses.findById(12)).thenReturn(Optional.of(course));
        assertThrows(ResponseStatusException.class,()->controller.getListRegStudent(12,session,new ExtendedModelMap()));
        verifyNoInteractions(registrations);
    }
}