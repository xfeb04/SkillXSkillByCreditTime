package com.springboot.controller;
import com.springboot.service.CourseCreditSettlementService;
import com.springboot.repository.*;
import com.springboot.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CourseCreditSettlementTest {
    @Test void returnsUnusedCourseReserveOnlyOnce() {
        var tx=setup("ผู้สอนจ่ายให้ผู้เรียน",4);
        var hold=new CreditTransaction();hold.setReservedCourse(tx.getRegisterCourse().getCourse());
        hold.setAmount(3.0);hold.setCreditTranStatus("พักเครดิตคอร์ส");
        when(transactions.findCourseReserveForUpdate(5)).thenReturn(Optional.of(hold));
        service.settle(5);service.settle(5);
        assertEquals(13.0,teacher.getCreditBalance());assertEquals(4.0,teacher.getCreditFrozen());
        assertEquals(24.0,learner.getCreditBalance());assertEquals(0.0,hold.getAmount());
        assertEquals("สิ้นสุดการพักเครดิต",hold.getCreditTranStatus());
    }
    @Test void refundsAtStartAndPaysLearnerOnlyOnCompletion() {
        var tx = setup("ผู้สอนจ่ายให้ผู้เรียน",4);
        var course = tx.getRegisterCourse().getCourse();
        course.setCourseStatus("กำลังดำเนินการ");
        var hold = new CreditTransaction(); hold.setReservedCourse(course);
        hold.setAmount(3.0); hold.setCreditTranStatus("พักเครดิตคอร์ส");
        when(transactions.findCourseReserveForUpdate(5)).thenReturn(Optional.of(hold));
        service.refundUnusedReserve(5); service.refundUnusedReserve(5);
        assertEquals(13.0,teacher.getCreditBalance());
        assertEquals(4.0,teacher.getCreditFrozen());
        assertEquals(20.0,learner.getCreditBalance());
        assertEquals("รอสอนเสร็จ",tx.getCreditTranStatus());
        course.setCourseStatus("สอนเสร็จสิ้น");
        service.settle(5);
        assertEquals(13.0,teacher.getCreditBalance());
        assertEquals(24.0,learner.getCreditBalance());
        verify(teachers,times(1)).save(teacher);
    }
    final CreditTransactionRepository transactions = mock(CreditTransactionRepository.class);
    final TeacherRepository teachers = mock(TeacherRepository.class);
    final CourseCreditSettlementService service = new CourseCreditSettlementService(transactions, teachers);
    final Teacher teacher = new Teacher(), learner = new Teacher();
    CreditTransaction setup(String type, double amount) {
        teacher.setMemberId(1); learner.setMemberId(2);
        teacher.setCreditBalance(10.0); learner.setCreditBalance(20.0);
        teacher.setCreditFrozen(7.0); learner.setCreditFrozen(8.0);
        var course = new Course(); course.setTeacher(teacher); course.setCourseType(type); course.setCourseStatus("สอนเสร็จสิ้น");
        var registration = new RegisterCourse(); registration.setMember(learner); registration.setCourse(course); registration.setRegisterStatus("อนุมัติแล้ว");
        var tx = new CreditTransaction(); tx.setRegisterCourse(registration); tx.setAmount(amount); tx.setCreditTranStatus("รอสอนเสร็จ");
        when(transactions.findAwaitingCompletionForUpdate(5)).thenReturn(List.of(tx));
        when(teachers.findForUpdate(1)).thenReturn(Optional.of(teacher)); when(teachers.findForUpdate(2)).thenReturn(Optional.of(learner));
        return tx;
    }
    @Test void normalPaysTeacherOnceWithoutDebitingFrozenAgain() {
        var tx = setup("ปกติ",3); service.settle(5); service.settle(5);
        assertEquals(13.0,teacher.getCreditBalance()); assertEquals(20.0,learner.getCreditBalance());
        assertEquals(8.0,learner.getCreditFrozen()); assertEquals("สำเร็จ",tx.getCreditTranStatus());
        assertEquals("เรียนเสร็จสิ้น",tx.getRegisterCourse().getRegisterStatus()); verify(teachers,times(1)).save(teacher);
    }
    @Test void incentivePaysLearner() {
        setup("ผู้สอนจ่ายให้ผู้เรียน",4); service.settle(5);
        assertEquals(24.0,learner.getCreditBalance()); assertEquals(10.0,teacher.getCreditBalance()); assertEquals(7.0,teacher.getCreditFrozen());
    }
    @Test void freeAwardsTeacherOnceWithoutChargingLearner() {
        var tx = setup("เรียนฟรี",2); service.settle(5); service.settle(5);
        assertEquals(12.0,teacher.getCreditBalance()); assertEquals(20.0,learner.getCreditBalance());
        assertEquals(8.0,learner.getCreditFrozen()); verify(teachers,times(1)).save(teacher);
        assertEquals("สำเร็จ",tx.getCreditTranStatus());
    }
    @Test void freeCompletesWithoutTransferring() {
        var tx = setup("เรียนฟรี",0); service.settle(5);
        verify(teachers,never()).save(any()); assertEquals("สำเร็จ",tx.getCreditTranStatus());
    }
    @Test void invalidAmountCannotComplete() {
        var tx = setup("ปกติ",-1); assertThrows(IllegalStateException.class,()->service.settle(5));
        assertEquals("รอสอนเสร็จ",tx.getCreditTranStatus()); verify(teachers,never()).save(any());
    }
    @Test void schedulerSettlesFinishedCourses() {
        setup("ปกติ",3); when(transactions.findCompletedCourseIdsAwaitingPayment()).thenReturn(List.of(5));
        service.settleCompletedCourses(); assertEquals(13.0,teacher.getCreditBalance());
    }
}
