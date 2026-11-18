package com.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.springboot.repository.SkillRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ListSkillController {

    private final SkillRepository skillRepository;

    public ListSkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @GetMapping("/listSkill")
    public String getListSkills(HttpSession session, Model model) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";

        model.addAttribute("skills", skillRepository.findByTeacher_MemberIdOrderBySkillIdDesc(memberId));
        return "listSkill";
    }
}
