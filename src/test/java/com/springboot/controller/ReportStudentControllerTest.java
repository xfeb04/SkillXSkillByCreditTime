package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.server.ResponseStatusException;
import com.springboot.model.*;
import com.springboot.repository.*;
class ReportStudentControllerTest {
    final RegisterCourseRepository registrations=mock(RegisterCourseRepository.class);
    final ReportRepository reports=mock(ReportRepository.class);
    final ReportStudentController controller=new ReportStudentController(registrations,reports);
    final MockHttpSession session=new MockHttpSession();
    final Teacher teacher=new Teacher(), learner=new Teacher();
    void setup(){session.setAttribute("memberId",7);teacher.setMemberId(7);learner.setMemberId(8);
        var course=new Course();course.setTeacher(teacher);
        var registration=new RegisterCourse();registration.setCourse(course);registration.setMember(learner);registration.setRegisterStatus("อนุมัติแล้ว");
        when(registrations.findById(9)).thenReturn(Optional.of(registration));}
    @Test void savesReasonWithoutImageAndUsesDatabaseIdentities(){setup();
        controller.submit(9,"ผู้เรียนรบกวนชั้นเรียน",null,session,new ExtendedModelMap(),new RedirectAttributesModelMap());
        verify(reports).save(argThat(r -> r.getImgReport()==null && r.getReporter()==teacher.getMember() && r.getReportedMember()==learner.getMember() && "ผู้เรียน".equals(r.getReportType())));}
    @Test void rejectsBlankOrInvalidReason(){setup();
        for(String reason:List.of("", "test!", "a".repeat(256))) assertEquals("reportStudent",controller.submit(9,reason,null,session,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        verifyNoInteractions(reports);}
    @Test void rejectsFakeImage(){setup();
        var file=new MockMultipartFile("imgReport","fake.png","image/png",new byte[]{1,2,3});
        assertEquals("reportStudent",controller.submit(9,"เหตุผล",file,session,new ExtendedModelMap(),new RedirectAttributesModelMap()));verifyNoInteractions(reports);}
    @Test void rejectsOversizedImage(){setup();
        var file=new MockMultipartFile("imgReport","big.jpg","image/jpeg",new byte[5*1024*1024+1]);
        assertEquals("reportStudent",controller.submit(9,"เหตุผล",file,session,new ExtendedModelMap(),new RedirectAttributesModelMap()));verifyNoInteractions(reports);}
    @Test void preventsOtherTeacher(){setup();session.setAttribute("memberId",99);
        assertThrows(ResponseStatusException.class,()->controller.page(9,session,new ExtendedModelMap()));verifyNoInteractions(reports);}
    @Test void savesValidPng() throws Exception {setup();
        var output=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(1,1,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",output);
        var file=new MockMultipartFile("imgReport","proof.png","image/png",output.toByteArray());
        controller.submit(9,"เหตุผล",file,session,new ExtendedModelMap(),new RedirectAttributesModelMap());
        verify(reports).save(argThat(r -> Arrays.equals(output.toByteArray(),r.getImgReport())));}
}