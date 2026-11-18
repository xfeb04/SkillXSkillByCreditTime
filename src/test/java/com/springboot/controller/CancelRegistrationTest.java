package com.springboot.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import com.springboot.model.*;
import com.springboot.repository.*;

class CancelRegistrationTest {
    final RegisterCourseRepository registrations = mock(RegisterCourseRepository.class);
    final TeacherRepository members = mock(TeacherRepository.class);
    final CreditTransactionRepository transactions = mock(CreditTransactionRepository.class);
    final CancelRegistrationController controller = new CancelRegistrationController(registrations, members, transactions);
    MockHttpSession session() { var s = new MockHttpSession(); s.setAttribute("memberId", 7); return s; }

    @Test void refundsOnceAndPreservesHistory() {
        var registration = new RegisterCourse(); var course = new Course(); course.setCourseType("\u0e1b\u0e01\u0e15\u0e34"); registration.setCourse(course); registration.setRegisterStatus("รออนุมัติ");
        var member = new Teacher(); member.setCreditBalance(10.0); member.setCreditFrozen(5.0);
        var transaction = new CreditTransaction(); transaction.setAmount(2.0);
        when(registrations.findForCancellation(7, 12)).thenReturn(Optional.of(registration));
        when(members.findForUpdate(7)).thenReturn(Optional.of(member));
        when(transactions.findByRegisterCourse_RegisterCourseId(12)).thenReturn(Optional.of(transaction));
        assertEquals("redirect:/viewCourseDetail/null",
                controller.cancelRegistration(12, session(), new RedirectAttributesModelMap()));
        assertEquals(12.0, member.getCreditBalance()); assertEquals(3.0, member.getCreditFrozen());
        assertEquals("\u0e22\u0e01\u0e40\u0e25\u0e34\u0e01", registration.getRegisterStatus());
        assertEquals("ยกเลิกแล้ว", transaction.getCreditTranStatus());
        controller.cancelRegistration(12, session(), new RedirectAttributesModelMap());
        verify(members, times(1)).save(member);
        verify(registrations, never()).delete(any()); verify(transactions, never()).delete(any());
    }

    @Test void cannotCancelApprovedRegistration() {
        var registration = new RegisterCourse(); var course = new Course(); course.setCourseType("\u0e1b\u0e01\u0e15\u0e34"); registration.setCourse(course); registration.setRegisterStatus("อนุมัติแล้ว");
        when(registrations.findForCancellation(7, 12)).thenReturn(Optional.of(registration));
        controller.cancelRegistration(12, session(), new RedirectAttributesModelMap());
        verifyNoInteractions(members, transactions);
        assertEquals("อนุมัติแล้ว", registration.getRegisterStatus());
    }

    @Test void requiresLogin() {
        assertEquals("redirect:/loginMember", controller.cancelRegistration(12, new MockHttpSession(), new RedirectAttributesModelMap()));
        verifyNoInteractions(registrations, members, transactions);
    }
}
