package com.springboot.controller;

import java.io.IOException;
import java.text.Collator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.model.Category;
import com.springboot.model.Skill;
import com.springboot.model.Teacher;
import com.springboot.repository.CategoryRepository;
import com.springboot.repository.SkillRepository;
import com.springboot.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class SkillFormController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final SkillRepository skillRepository;
    private final CategoryRepository categoryRepository;
    private final TeacherRepository teacherRepository;

    public SkillFormController(SkillRepository skillRepository, CategoryRepository categoryRepository,
            TeacherRepository teacherRepository) {
        this.skillRepository = skillRepository;
        this.categoryRepository = categoryRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/addSkill")
    public String addSkill(HttpSession session, Model model) {
        Integer memberId = loggedInMemberId(session);
        if (memberId == null) return "redirect:/loginMember";
        prepareForm(model, "เพิ่มทักษะ", "/addSkill", false, null, null, false, new LinkedHashMap<>());
        return "skillForm";
    }

    @PostMapping("/addSkill")
    public String do_addskill(@RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "") String skillDescription,
            @RequestParam(required = false) MultipartFile skillImg,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer memberId = loggedInMemberId(session);
        if (memberId == null) return "redirect:/loginMember";

        skillDescription = skillDescription.trim();
        Map<String, String> errors = validate(categoryId, skillDescription, skillImg);
        if (!errors.isEmpty()) {
            prepareForm(model, "เพิ่มทักษะ", "/addSkill", false, categoryId, skillDescription, false, errors);
            return "skillForm";
        }

        try {
            Skill skill = new Skill(skillDescription, imageBytes(skillImg), findTeacher(memberId),
                    categoryRepository.findById(categoryId).orElseThrow());
            skillRepository.save(skill);
            redirectAttributes.addFlashAttribute("skillSuccess", "เพิ่มทักษะเรียบร้อยแล้ว");
            return "redirect:/listSkill";
        } catch (IOException exception) {
            errors.put("skillImg", "ไม่สามารถอ่านไฟล์รูปภาพได้");
            prepareForm(model, "เพิ่มทักษะ", "/addSkill", false, categoryId, skillDescription, false, errors);
            return "skillForm";
        }
    }

    @GetMapping("/editSkill/{skillId}")
    public String getSkill(@PathVariable Integer skillId, HttpSession session, Model model) {
        Integer memberId = loggedInMemberId(session);
        if (memberId == null) return "redirect:/loginMember";
        Skill skill = findOwnedSkill(skillId, memberId);
        model.addAttribute("skillId", skillId);
        prepareForm(model, "แก้ไขทักษะ", "/editSkill/" + skillId, true,
                skill.getCategory().getCategoryId(), skill.getSkillDescription(), hasImage(skill), new LinkedHashMap<>());
        return "skillForm";
    }

    @PostMapping("/editSkill/{skillId}")
    public String do_editSkill(@PathVariable Integer skillId, @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "") String skillDescription,
            @RequestParam(required = false) MultipartFile skillImg,
            @RequestParam(defaultValue = "false") boolean removeSkillImg,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer memberId = loggedInMemberId(session);
        if (memberId == null) return "redirect:/loginMember";
        Skill skill = findOwnedSkill(skillId, memberId);
        model.addAttribute("skillId", skillId);
        skillDescription = skillDescription.trim();
        model.addAttribute("removeSkillImg", removeSkillImg);
        Map<String, String> errors = validate(categoryId, skillDescription, skillImg);
        if (!errors.isEmpty()) {
            prepareForm(model, "แก้ไขทักษะ", "/editSkill/" + skillId, true, categoryId, skillDescription,
                    hasImage(skill), errors);
            return "skillForm";
        }

        try {
            skill.setSkillDescription(skillDescription);
            skill.setCategory(categoryRepository.findById(categoryId).orElseThrow());
            if (skillImg != null && !skillImg.isEmpty()) {
                skill.setSkillImg(imageBytes(skillImg));
            } else if (removeSkillImg) {
                skill.setSkillImg(null);
            }
            skillRepository.save(skill);
            redirectAttributes.addFlashAttribute("skillSuccess", "แก้ไขทักษะเรียบร้อยแล้ว");
            return "redirect:/listSkill";
        } catch (IOException exception) {
            errors.put("skillImg", "ไม่สามารถอ่านไฟล์รูปภาพได้");
            prepareForm(model, "แก้ไขทักษะ", "/editSkill/" + skillId, true, categoryId, skillDescription,
                    hasImage(skill), errors);
            return "skillForm";
        }
    }

    private Map<String, String> validate(Integer categoryId, String description, MultipartFile image) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (categoryId == null || categoryRepository.findById(categoryId).isEmpty()) errors.put("categoryId", "กรุณาเลือกหมวดหมู่ทักษะ");
        if (!StringUtils.hasText(description)) errors.put("skillDescription", "กรุณากรอกรายละเอียดหรือประสบการณ์เกี่ยวกับทักษะนี้");
        else if (description.length() > 255) errors.put("skillDescription", "รายละเอียดต้องมีความยาวไม่เกิน 255 ตัวอักษร");
        validateImage(image, errors);
        return errors;
    }

    private void validateImage(MultipartFile image, Map<String, String> errors) {
        if (image == null || image.isEmpty()) return;
        String filename = image.getOriginalFilename();
        String extension = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!IMAGE_EXTENSIONS.contains(extension)) errors.put("skillImg", "รองรับเฉพาะไฟล์ .jpg, .jpeg และ .png");
        else if (image.getSize() > MAX_IMAGE_SIZE) errors.put("skillImg", "ขนาดรูปภาพต้องไม่เกิน 5MB");
    }

    private void prepareForm(Model model, String title, String formAction, boolean isEdit,
            Integer categoryId, String description, boolean hasExistingImage, Map<String, String> errors) {
        model.addAttribute("title", title);
        model.addAttribute("formAction", formAction);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("skillDescription", description);
        model.addAttribute("hasExistingImage", hasExistingImage);
        model.addAttribute("categories", getListCategory());
        model.addAttribute("errors", errors);
    }

    private List<Category> getListCategory() {
        List<Category> categories = categoryRepository.findAll();
        Collator thaiCollator = Collator.getInstance(Locale.forLanguageTag("th-TH"));
        categories.sort((left, right) -> thaiCollator.compare(left.getCategoryName(), right.getCategoryName()));
        return categories;
    }

    private Integer loggedInMemberId(HttpSession session) {
        return (Integer) session.getAttribute("memberId");
    }

    private Teacher findTeacher(Integer memberId) {
        return teacherRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("ไม่พบข้อมูลสมาชิก"));
    }

    private Skill findOwnedSkill(Integer skillId, Integer memberId) {
        return skillRepository.findById(skillId)
                .filter(skill -> skill.getTeacher().getMemberId().equals(memberId))
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบทักษะที่เลือก"));
    }

    private byte[] imageBytes(MultipartFile image) throws IOException {
        return image == null || image.isEmpty() ? null : image.getBytes();
    }

    private boolean hasImage(Skill skill) {
        return skill.getSkillImg() != null && skill.getSkillImg().length > 0;
    }
}
