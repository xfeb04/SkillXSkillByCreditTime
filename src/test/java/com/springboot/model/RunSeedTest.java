package com.springboot.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.springboot.repository.*;
import java.util.Optional;
class RunSeedTest {
    @Test void repairsMissingPendingTransactionAndDoesNotDebitTwice() throws Exception {
        var registrations=mock(RegisterCourseRepository.class);
        var transactions=mock(CreditTransactionRepository.class);
        var teachers=mock(TeacherRepository.class);
        var course=mock(Course.class);
        when(course.getCourseId()).thenReturn(1);
        when(course.getCourseType()).thenReturn("ปกติ");
        when(course.getCreditPerCourse()).thenReturn(6.0);
        var learner=new Teacher();learner.setMemberId(2);learner.setCreditBalance(4.0);learner.setCreditFrozen(0.0);
        var registration=new RegisterCourse("รออนุมัติ",java.time.LocalDate.now(),null,null,null,learner,course,null);
        when(registrations.findByMember_MemberIdAndCourse_CourseId(2,1)).thenReturn(Optional.of(registration));
        when(transactions.save(any())).thenAnswer(i->i.getArgument(0));
        var method=Run.class.getDeclaredMethod("createPendingRegistrations",RegisterCourseRepository.class,
            CreditTransactionRepository.class,TeacherRepository.class,Course.class,Teacher[].class);
        method.setAccessible(true);
        method.invoke(null,registrations,transactions,teachers,course,new Teacher[]{learner});
        assertEquals(0.0,learner.getCreditBalance());assertEquals(6.0,learner.getCreditFrozen());
        assertEquals(6.0,registration.getCreditTransaction().getAmount());
        assertEquals("รอผู้สอนอนุมัติ",registration.getCreditTransaction().getCreditTranStatus());
        when(transactions.findByRegisterCourse_RegisterCourseId(null)).thenReturn(Optional.of(registration.getCreditTransaction()));
        method.invoke(null,registrations,transactions,teachers,course,new Teacher[]{learner});
        verify(transactions,times(1)).save(any());
        assertEquals(6.0,learner.getCreditFrozen());
    }
}
