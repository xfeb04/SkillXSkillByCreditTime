package com.springboot.controller;
import com.springboot.controller.admin.AdminReportController;
import com.springboot.model.*;
import com.springboot.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminReportActionsTest {
    final ReportRepository reports = mock(ReportRepository.class);
    final AdminReportController controller = new AdminReportController(reports);
    final Teacher member = new Teacher();
    final Report report = new Report();
    final MockHttpSession session = new MockHttpSession();
    final LocalDate today = LocalDate.now(ZoneId.of("Asia/Bangkok"));
    AdminReportActionsTest() {
        session.setAttribute("adminUsername", "admin");
        var registration = new RegisterCourse(); registration.setCourse(new Course());
        report.setRegisterCourse(registration); report.setStudent(member.getStudent()); report.setReportType("ผู้เรียน");
        when(reports.findById(5)).thenReturn(Optional.of(report));
    }
    @Test void bansUntilFutureDate() {
        controller.ban(5,today.plusDays(2).toString(),"ทดสอบเหตุผล",session,new ExtendedModelMap(),new RedirectAttributesModelMap());
        assertTrue(member.isCurrentlyBanned()); assertEquals(today.plusDays(2),member.getBanDate());
        verify(reports).flush();
        member.setBanDate(today); assertFalse(member.isCurrentlyBanned());
    }
    @Test void rejectsInvalidDateOrReason() {
        for(String date : new String[]{"", "2026-02-30", today.toString(), today.minusDays(1).toString()}) {
            var model = new ExtendedModelMap();
            assertEquals("admin/banMember",controller.ban(5,date,"เหตุผล",session,model,new RedirectAttributesModelMap()));
            assertTrue(model.containsAttribute("errors"));
        }
        for(String reason : new String[]{"", "a".repeat(256), "test!"}) {
            assertEquals("admin/banMember",controller.ban(5,today.plusDays(1).toString(),reason,session,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        }
        verify(reports,never()).flush(); assertNull(member.getBanStatus());
    }
    @Test void removedDeletePageReturnsNotFound() {
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.next(5,"delete",session,new ExtendedModelMap()));
        assertEquals(404,error.getStatusCode().value());
        verify(reports,never()).delete(any());
    }
}
