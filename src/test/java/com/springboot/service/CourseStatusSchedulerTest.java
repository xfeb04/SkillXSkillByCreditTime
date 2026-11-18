package com.springboot.service;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.springboot.model.*;
import com.springboot.repository.*;
class CourseStatusSchedulerTest {
    final CourseRepository courses = mock(CourseRepository.class);
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    final TeacherRepository members = mock(TeacherRepository.class);
    final CreditTransactionRepository transactions = mock(CreditTransactionRepository.class);
    final CourseCreditSettlementService settlement = mock(CourseCreditSettlementService.class);
    final Clock clock = Clock.fixed(Instant.parse("2026-10-16T05:00:00Z"), ZoneId.of("Asia/Bangkok"));
    final CourseStatusScheduler scheduler = new CourseStatusScheduler(courses, clock, settlement, registrations, members, transactions);
    Course course() {
        var c = new Course(); c.setCourseStatus("เปิดรับสมัคร"); c.setCourseType("ปกติ");
        when(courses.findDueForUpdate(LocalDate.of(2026,10,16),LocalTime.NOON)).thenReturn(List.of(c));
        return c;
    }
    @Test void emptyCourseReturnsToDraft() {
        var c = course(); scheduler.updateStatuses(); assertEquals("สร้างสำเร็จ",c.getCourseStatus());
    }
    @Test void approvedCourseStarts() {
        var c = course();
        when(registrations.countByCourse_CourseIdAndRegisterStatus(null,"อนุมัติแล้ว")).thenReturn(1L);
        scheduler.updateStatuses(); assertEquals("กำลังดำเนินการ",c.getCourseStatus());
        verify(settlement).refundUnusedReserve(c.getCourseId());
    }
    @Test void cancelsPendingAndRefundsOnlyOnce() {
        var c = course();
        var m = new Teacher(); m.setCreditBalance(7.0); m.setCreditFrozen(3.0);
        var r = new RegisterCourse(); r.setMember(m); r.setCourse(c); r.setRegisterStatus("รออนุมัติ");
        var t = new CreditTransaction(); t.setAmount(3.0); t.setCreditTranStatus("รอผู้สอนอนุมัติ");
        when(registrations.findByCourse_CourseIdAndRegisterStatus(null,"รออนุมัติ")).thenReturn(List.of(r));
        when(registrations.findForCancellation(null,null)).thenReturn(Optional.of(r));
        when(transactions.findByRegisterCourse_RegisterCourseId(null)).thenReturn(Optional.of(t));
        when(members.findForUpdate(null)).thenReturn(Optional.of(m));
        scheduler.updateStatuses(); scheduler.updateStatuses();
        assertEquals("ยกเลิก",r.getRegisterStatus()); assertEquals("ยกเลิกแล้ว",t.getCreditTranStatus());
        assertEquals(10.0,m.getCreditBalance()); assertEquals(0.0,m.getCreditFrozen());
        verify(members,times(1)).save(m);
    }
}
