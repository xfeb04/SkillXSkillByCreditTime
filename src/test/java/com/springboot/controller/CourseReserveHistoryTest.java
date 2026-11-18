package com.springboot.controller;
import com.springboot.model.*;
import com.springboot.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CourseReserveHistoryTest {
    @Test void courseHoldAppearsOnlyInPayersFrozenHistory() {
        var teachers=mock(TeacherRepository.class); var transactions=mock(CreditTransactionRepository.class);
        var controller=new ViewCreditController(teachers,transactions);
        var teacher=new Teacher();teacher.setMemberId(7);
        var course=new Course();course.setTeacher(teacher);course.setCourseName("คอร์สใหม่");
        var hold=new CreditTransaction();hold.setReservedCourse(course);hold.setAmount(1.0);hold.setCreditTranStatus("พักเครดิตคอร์ส");
        when(teachers.findById(7)).thenReturn(Optional.of(teacher));when(transactions.findForMember(7)).thenReturn(List.of(hold));
        var session=new MockHttpSession();session.setAttribute("memberId",7);
        var model=new ExtendedModelMap();controller.getCreditTrans(session,model,"spent","pending");
        assertEquals(List.of(hold),model.get("transactions"));assertEquals(course,hold.getCourse());
        controller.getCreditTrans(session,model,"received","pending");assertEquals(List.of(),model.get("transactions"));
        controller.getCreditTrans(session,model,"spent","completed");assertEquals(List.of(),model.get("transactions"));
    }
}
