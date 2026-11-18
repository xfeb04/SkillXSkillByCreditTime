package com.springboot.controller;

import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.model.Member;
import jakarta.servlet.http.HttpSession;

import com.springboot.repository.MemberRepository;

@Controller
public class LoginMemberController {

    private final MemberRepository memberRepository;

    public LoginMemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/loginMember")
    public String loginMember(@RequestParam(required = false) String registered, Model model) {
        if ("true".equals(registered)) {
            model.addAttribute("success", "ลงทะเบียนเสร็จแล้ว กรุณาเข้าสู่ระบบ");
        }
        return "loginMember";
    }

    @PostMapping("/loginMember")
    public String LoginMember(@RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password, Model model, HttpSession session) {
        username = username.trim();
        model.addAttribute("username", username);
        if (!StringUtils.hasText(username)) {
            model.addAttribute("usernameError", "กรุณากรอกชื่อผู้ใช้");
        }
        if (!StringUtils.hasText(password)) {
            model.addAttribute("passwordError", "กรุณากรอกรหัสผ่าน");
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return "loginMember";
        }
        Member member = memberRepository.findByUsername(username)
                .filter(foundMember -> password.equals(foundMember.getPassword()))
                .orElse(null);
        if (member == null) {
            model.addAttribute("authenticationError", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
            return "loginMember";
        }
        memberRepository.releaseExpiredBans(member.getMemberId(), java.time.LocalDate.now(java.time.ZoneId.of("Asia/Bangkok")));
        member = memberRepository.findById(member.getMemberId()).orElse(member);
        if (Integer.valueOf(1).equals(member.getBanStatus())) {
            session.removeAttribute("memberId"); session.removeAttribute("memberName");
            session.removeAttribute("memberInitial"); session.removeAttribute("memberHasProfileImage");
            session.setAttribute("bannedMemberId", member.getMemberId());
            return "redirect:/banned";
        }
        session.removeAttribute("bannedMemberId");
        session.setAttribute("memberId", member.getMemberId());
        session.setAttribute("memberName", displayName(member));
        session.setAttribute("memberInitial", memberInitial(member));
        session.setAttribute("memberHasProfileImage", member.getImgProfile() != null && member.getImgProfile().length > 0);
        return "redirect:/searchCourse";
    }

    @PostMapping("/logoutMember")
    public String logoutMember(HttpSession session) {
        session.invalidate();
        return "redirect:/searchCourse";
    }

    private String memberInitial(Member member) {
        String name = member.getFirstname();
        return StringUtils.hasText(name) ? name.substring(0, 1) : member.getUsername().substring(0, 1);
    }

    private String displayName(Member member) {
        return (StringUtils.hasText(member.getFirstname()) ? member.getFirstname() : "")
                + (StringUtils.hasText(member.getSurname()) ? " " + member.getSurname() : "");
    }
}
