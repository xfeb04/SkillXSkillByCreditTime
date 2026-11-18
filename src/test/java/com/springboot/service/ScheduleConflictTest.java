package com.springboot.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.springboot.model.*;
import com.springboot.repository.*;
class ScheduleConflictTest {
    final CourseRepository courses = mock(CourseRepository.class);
    final CourseDateRepository dates = mock(CourseDateRepository.class);
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    CourseDate lesson(int day, int start, int end) {
        return new CourseDate(LocalDate.of(2027,1,day),LocalTime.of(start,0),LocalTime.of(end,0),null);
    }
    @Test void checksAllLessonsAndAllowsAdjacentTimes() {
        var proposed=List.of(lesson(1,9,10),lesson(2,9,12));
        assertTrue(ScheduleConflict.overlaps(proposed,List.of(lesson(2,11,13))));
        assertFalse(ScheduleConflict.overlaps(proposed,List.of(lesson(2,12,14))));
        assertFalse(ScheduleConflict.overlaps(proposed,List.of(lesson(3,9,12))));
    }
    @Test void teachingChecksOpenAndOngoingAndExcludesCurrentCourse() {
        var course=mock(Course.class);when(course.getCourseId()).thenReturn(8);
        when(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(8)).thenReturn(List.of(lesson(2,9,12)));
        for(String status:List.of("เปิดรับสมัคร","กำลังดำเนินการ")) {
            reset(courses);
            when(courses.findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(7,status)).thenReturn(List.of(course));
            assertTrue(ScheduleConflict.teaching(7,9,List.of(lesson(2,10,11)),courses,dates));
            assertFalse(ScheduleConflict.teaching(7,8,List.of(lesson(2,10,11)),courses,dates));
        }
    }
    @Test void learningCountsPendingAndApprovedButNotCancelledOrCompleted() {
        var course=mock(Course.class);when(course.getCourseId()).thenReturn(8);
        var r=new RegisterCourse();r.setCourse(course);
        when(registrations.findByMember_MemberId(7)).thenReturn(List.of(r));
        when(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(8)).thenReturn(List.of(lesson(2,9,12)));
        for(String status:List.of("รออนุมัติ","อนุมัติแล้ว","ยกเลิก","ปฏิเสธคำขอ","เรียนเสร็จสิ้น")) {
            r.setRegisterStatus(status);
            assertEquals(Set.of("รออนุมัติ","อนุมัติแล้ว").contains(status),
                ScheduleConflict.learning(7,List.of(lesson(2,10,11)),registrations,dates));
        }
    }
}
