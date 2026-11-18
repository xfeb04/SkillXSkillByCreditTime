package com.springboot.controller;
import com.springboot.model.Member;
import com.springboot.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BannedControllerTest {
    final MemberRepository members = mock(MemberRepository.class);
    final BannedController controller = new BannedController(members);
    @Test void displaysOnlyAuthenticatedBannedMembersReasonAndDate() {
        var session = new MockHttpSession(); session.setAttribute("bannedMemberId", 7);
        var member = new Member(); member.setBanStatus(1); member.setBanDate(LocalDate.of(2099,1,2)); member.setBanReason("เหตุผล");
        when(members.findById(7)).thenReturn(Optional.of(member));
        var model = new ExtendedModelMap();
        assertEquals("banned",controller.page(session,model));
        assertEquals("02/01/2642",model.get("banUntil")); assertEquals("เหตุผล",model.get("banReason"));
    }
    @Test void releasesExpiredBanBeforeLoadingMember() {
        var session = new MockHttpSession(); session.setAttribute("bannedMemberId",7);
        var member = new Member(); member.setBanStatus(0);
        when(members.findById(7)).thenReturn(Optional.of(member));
        assertEquals("redirect:/loginMember",controller.page(session,new ExtendedModelMap()));
        var order = inOrder(members); order.verify(members).releaseExpiredBans(eq(7),any()); order.verify(members).findById(7);
        assertNull(session.getAttribute("bannedMemberId"));
    }
    @Test void doesNotExposeOtherMembersBanInformation() {
        assertEquals("redirect:/loginMember",controller.page(new MockHttpSession(),new ExtendedModelMap()));
        verifyNoInteractions(members);
    }
}
