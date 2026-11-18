package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.server.ResponseStatusException;
import com.springboot.model.*;
import com.springboot.repository.*;
class ApproveRequestControllerTest {
    final CourseRepository courses=mock(CourseRepository.class);
    final RegisterCourseRepository requests=mock(RegisterCourseRepository.class);
    final CourseDateRepository dates=mock(CourseDateRepository.class);
    final CreditTransactionRepository transactions=mock(CreditTransactionRepository.class);
    final TeacherRepository teachers=mock(TeacherRepository.class);
    final ApproveRequestController controller=new ApproveRequestController(courses,requests,dates,transactions,teachers);
    final MockHttpSession session=new MockHttpSession();
    final Course course=mock(Course.class);
    final RegisterCourse request=new RegisterCourse();
    void setup() {
        session.setAttribute("memberId",7);
        when(course.getCourseId()).thenReturn(11); when(course.getCourseStatus()).thenReturn("เปิดรับสมัคร");
        when(course.getMaxMember()).thenReturn(1); when(course.getCourseType()).thenReturn("ปกติ");
        when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.of(course));
        request.setCourse(course); request.setRegisterStatus("รออนุมัติ");
        when(requests.findById(9)).thenReturn(Optional.of(request));
    }
    @Test void acceptsPendingAndDoesNotRepeat() {
        setup(); when(course.getCourseType()).thenReturn("เรียนฟรี"); controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals("อนุมัติแล้ว",request.getRegisterStatus());
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        verify(requests,times(1)).save(request); verifyNoInteractions(teachers);
    }
    @Test void fullCourseCannotAccept() {
        setup(); when(requests.countByCourse_CourseIdAndRegisterStatus(11,"อนุมัติแล้ว")).thenReturn(1L);
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals("รออนุมัติ",request.getRegisterStatus()); verify(requests,never()).save(any());
    }
    @Test void ownerRequired() {
        setup(); when(courses.findOwnedForUpdate(11,7)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,()->controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap()));
        verify(requests,never()).save(any());
    }
    @Test void rejectsAndRefundsOnce() {
        setup(); var learner=new Teacher(); learner.setMemberId(8); learner.setCreditBalance(4.0); learner.setCreditFrozen(2.0);
        request.setMember(learner); when(teachers.findForUpdate(8)).thenReturn(Optional.of(learner));
        var transaction=new CreditTransaction(); transaction.setAmount(2.0);
        when(transactions.findByRegisterCourse_RegisterCourseId(9)).thenReturn(Optional.of(transaction));
        controller.do_ApproeRequest(11,9,"reject",session,new RedirectAttributesModelMap());
        controller.do_ApproeRequest(11,9,"reject",session,new RedirectAttributesModelMap());
        assertEquals(6.0,learner.getCreditBalance()); assertEquals(0.0,learner.getCreditFrozen());
        assertEquals("ปฏิเสธคำขอ",request.getRegisterStatus()); verify(teachers,times(1)).save(learner);
    }
    @Test void normalApprovalMovesFrozenToPendingWithoutCreditingBalance() {
        setup(); var payer=new Teacher(); payer.setCreditFrozen(2.0); payer.setCreditBalance(5.0); payer.setMemberId(8);
        request.setMember(payer); when(teachers.findForUpdate(8)).thenReturn(Optional.of(payer));
        var transaction=new CreditTransaction(); transaction.setAmount(2.0);
        when(transactions.findByRegisterCourse_RegisterCourseId(9)).thenReturn(Optional.of(transaction));
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals(0.0,payer.getCreditFrozen()); assertEquals(5.0,payer.getCreditBalance());
        assertEquals("รอสอนเสร็จ",transaction.getCreditTranStatus());
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        verify(teachers,times(1)).save(payer);
    }
    @Test void incentiveUsesTeachersFrozenAndUpdatesZeroTransaction() {
        setup(); when(course.getCourseType()).thenReturn("ผู้สอนจ่ายให้ผู้เรียน"); when(course.getCreditPerCourse()).thenReturn(3.0);
        var payer=new Teacher(); payer.setCreditFrozen(9.0); payer.setCreditBalance(5.0);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(payer));
        var transaction=new CreditTransaction(); transaction.setAmount(0.0);
        when(transactions.findByRegisterCourse_RegisterCourseId(9)).thenReturn(Optional.of(transaction));
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals(6.0,payer.getCreditFrozen()); assertEquals(5.0,payer.getCreditBalance());
        assertEquals(3.0,transaction.getAmount()); assertEquals("รอสอนเสร็จ",transaction.getCreditTranStatus());
    }
    @Test void incentiveAllocatesTwoFromTwentyOnlyOnApproval() {
        setup();
        when(course.getCourseType()).thenReturn("ผู้สอนจ่ายให้ผู้เรียน");
        when(course.getCreditPerCourse()).thenReturn(2.0);
        when(course.getMaxMember()).thenReturn(10);
        var payer = new Teacher(); payer.setCreditFrozen(20.0); payer.setCreditBalance(30.0);
        var learner = new Teacher(); learner.setCreditBalance(10.0);
        request.setMember(learner);
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(payer));
        var hold = new CreditTransaction(); hold.setReservedCourse(course);
        hold.setAmount(20.0); hold.setCreditTranStatus("พักเครดิตคอร์ส");
        when(transactions.findCourseReserveForUpdate(11)).thenReturn(Optional.of(hold));
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals(18.0,payer.getCreditFrozen()); assertEquals(30.0,payer.getCreditBalance());
        assertEquals(18.0,hold.getAmount()); assertEquals(10.0,learner.getCreditBalance());
        var payment = request.getCreditTransaction();
        assertNotNull(payment); assertEquals(2.0,payment.getAmount());
        assertEquals("รอสอนเสร็จ",payment.getCreditTranStatus());
        assertSame(request,payment.getRegisterCourse());
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        verify(transactions,times(1)).save(payment);
        verify(transactions,times(1)).save(hold);
        assertEquals(18.0,payer.getCreditFrozen());
    }
    @Test void insufficientFrozenDoesNotApprove() {
        setup(); when(course.getCourseType()).thenReturn("ผู้สอนจ่ายให้ผู้เรียน"); when(course.getCreditPerCourse()).thenReturn(3.0);
        var payer=new Teacher(); payer.setCreditFrozen(2.0); when(teachers.findForUpdate(7)).thenReturn(Optional.of(payer));
        controller.do_ApproeRequest(11,9,"accept",session,new RedirectAttributesModelMap());
        assertEquals("รออนุมัติ",request.getRegisterStatus()); verify(requests,never()).save(any());
    }}