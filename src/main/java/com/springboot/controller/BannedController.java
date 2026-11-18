package com.springboot.controller;

import com.springboot.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
public class BannedController {
    private final MemberRepository members;
    public BannedController(MemberRepository members) { this.members = members; }
    @GetMapping("/banned")
    public String page(HttpSession session, Model model) {
        if (!(session.getAttribute("bannedMemberId") instanceof Integer id)) return "redirect:/loginMember";
        members.releaseExpiredBans(id, LocalDate.now(ZoneId.of("Asia/Bangkok")));
        var member = members.findById(id).orElse(null);
        if (member == null || !Integer.valueOf(1).equals(member.getBanStatus())) {
            session.removeAttribute("bannedMemberId");
            return "redirect:/loginMember";
        }
        model.addAttribute("banUntil", member.getBanDate() == null ? "ไม่มีกำหนด" : member.getBanDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE)));
        model.addAttribute("banReason", member.getBanReason());
        return "banned";
    }
}
