package com.springboot.controller;
import com.springboot.model.Teacher;
import com.springboot.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EditProfileControllerTest {
    final TeacherRepository teachers=mock(TeacherRepository.class);
    final MemberRepository members=mock(MemberRepository.class);
    final EditProfileController controller=new EditProfileController(teachers,members);
    Map<String,String> values(){return new HashMap<>(Map.of("firstname","สมชาย","surname","ใจดี","email","test@example.com","phoneNumber","0812345678","birthDay","2000-01-01","gender","ชาย","bio","ประวัติ"));}
    MockHttpSession session(){var s=new MockHttpSession();s.setAttribute("memberId",7);return s;}
    @Test void updatesOwnProfileAndPreservesUnrelatedDataAndImage(){
        var p=new Teacher();p.setImgProfile(new byte[]{1});p.setCreditBalance(20.0);p.setUsername("original");
        when(teachers.findForUpdate(7)).thenReturn(Optional.of(p));var s=session();var input=values();input.put("memberId","99");input.put("creditBalance","999");
        assertEquals("redirect:/viewProfile",controller.editProfile(input,null,s,new ExtendedModelMap(),new RedirectAttributesModelMap()));
        assertEquals("สมชาย",p.getFirstname());assertEquals(20.0,p.getCreditBalance());assertEquals("original",p.getUsername());assertArrayEquals(new byte[]{1},p.getImgProfile());
        assertEquals("สมชาย ใจดี",s.getAttribute("memberName"));verify(teachers).saveAndFlush(p);
    }
    @Test void rejectsDuplicateEmailAndFakeImageWithoutSaving(){
        var p=new Teacher();when(teachers.findForUpdate(7)).thenReturn(Optional.of(p));when(members.existsByEmailIgnoreCaseAndMemberIdNot("test@example.com",7)).thenReturn(true);
        var model=new ExtendedModelMap();
        assertEquals("editProfile",controller.editProfile(values(),new MockMultipartFile("imgProfile","fake.png","image/png",new byte[]{1,2}),session(),model,new RedirectAttributesModelMap()));
        var errors=(Map<?,?>)model.get("errors");assertTrue(errors.containsKey("email"));assertTrue(errors.containsKey("imgProfile"));verify(teachers,never()).saveAndFlush(any());assertNull(p.getFirstname());
    }
    @Test void requiresLogin(){assertEquals("redirect:/loginMember",controller.viewProfileData(new MockHttpSession(),new ExtendedModelMap()));assertEquals("redirect:/loginMember",controller.editProfile(values(),null,new MockHttpSession(),new ExtendedModelMap(),new RedirectAttributesModelMap()));verifyNoInteractions(teachers,members);}
}
