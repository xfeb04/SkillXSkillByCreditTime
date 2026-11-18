package com.springboot.controller.admin;

import com.springboot.model.Category;
import com.springboot.repository.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class CategoryAdminController {
    private final CategoryRepository categories;
    private final CourseRepository courses;
    private final SkillRepository skills;
    public CategoryAdminController(CategoryRepository categories, CourseRepository courses, SkillRepository skills) {
        this.categories = categories; this.courses = courses; this.skills = skills;
    }
    private Category find(Integer id) { return categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    private String form(Category category, String mode, Model model) {
        model.addAttribute("category", category); model.addAttribute("mode", mode);
        if ("delete".equals(mode)) model.addAttribute("inUse", courses.existsByCategory_CategoryId(category.getCategoryId()) || skills.existsByCategory_CategoryId(category.getCategoryId()));
        return "admin/categoryForm";
    }
    @GetMapping("/admin/addCategory")
    public String add(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        return form(new Category(), "add", model);
    }
    @GetMapping("/admin/editCategory/{id}")
    public String edit(@PathVariable Integer id, HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        return form(find(id), "edit", model);
    }
    @GetMapping("/admin/deleteCategory/{id}")
    public String deletePage(@PathVariable Integer id, HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        return form(find(id), "delete", model);
    }
    private boolean loggedIn(HttpSession session) { return session.getAttribute("adminUsername") != null; }

    @GetMapping({"/admin/listCategory", "/listCategory"})
    public String list(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        List<Category> categoryList = getListCategory();
        Map<Integer, Boolean> categoryInUse = new LinkedHashMap<>();
        for (Category category : categoryList) {
            Integer id = category.getCategoryId();
            categoryInUse.put(id, courses.existsByCategory_CategoryId(id) || skills.existsByCategory_CategoryId(id));
        }
        model.addAttribute("categories", categoryList);
        model.addAttribute("categoryInUse", categoryInUse);
        return "admin/listCategory";
    }

    private List<Category> getListCategory() {
        return categories.findAll(Sort.by("categoryId"));
    }

    @PostMapping("/admin/addCategory")
    public String doAddCategory(@RequestParam(defaultValue="") String categoryName,
            @RequestParam(defaultValue="") String cateDescription,
            HttpSession session, Model model, RedirectAttributes redirect) {
        return saveCategory(null, categoryName, cateDescription, session, model, redirect);
    }

    @PostMapping("/admin/saveCategory")
    public String doUpdateCategory(@RequestParam Integer categoryId,
            @RequestParam(defaultValue="") String categoryName, @RequestParam(defaultValue="") String cateDescription,
            HttpSession session, Model model, RedirectAttributes redirect) {
        return saveCategory(categoryId, categoryName, cateDescription, session, model, redirect);
    }

    private String saveCategory(Integer categoryId, String categoryName, String cateDescription,
            HttpSession session, Model model, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        if (categoryId != null) find(categoryId);
        categoryName = categoryName.trim(); cateDescription = cateDescription.trim();
        Map<String,String> errors = new LinkedHashMap<>();
        if (categoryName.isBlank()) errors.put("categoryName", "กรุณากรอกชื่อหมวดหมู่");
        else if (categoryName.length()<2 || categoryName.length()>50) errors.put("categoryName", "ชื่อหมวดหมู่ต้องมีความยาว 2–50 ตัวอักษร");
        else if (!categoryName.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E ]+")) errors.put("categoryName", "ชื่อหมวดหมู่ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ และช่องว่าง");
        else if (categories.findByCategoryNameIgnoreCase(categoryName).stream().anyMatch(c -> !java.util.Objects.equals(c.getCategoryId(), categoryId))) errors.put("categoryName", "ชื่อหมวดหมู่นี้มีอยู่ในระบบแล้ว");
        if (cateDescription.isBlank()) errors.put("cateDescription", "กรุณากรอกรายละเอียด");
        else if (cateDescription.length()>255) errors.put("cateDescription", "รายละเอียดต้องไม่เกิน 255 ตัวอักษร");
        else if (!cateDescription.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s.,/#-]+")) errors.put("cateDescription", "รายละเอียดใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และ . , / # -");
        Category category = new Category(categoryId, categoryName, cateDescription);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return form(category, categoryId == null ? "add" : "edit", model);
        }
        categories.saveAndFlush(category);
        redirect.addFlashAttribute("categorySuccess", "บันทึกหมวดหมู่สำเร็จ");
        return "redirect:/admin/listCategory";
    }

    @PostMapping("/admin/deleteCategory/{id}")
    public String do_delCategory(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/admin/loginAdmin";
        var category = categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (courses.existsByCategory_CategoryId(id) || skills.existsByCategory_CategoryId(id)) {
            redirect.addFlashAttribute("categoryError", "ไม่สามารถลบหมวดหมู่ที่มีคอร์สหรือทักษะใช้งานอยู่ได้");
            return "redirect:/admin/deleteCategory/" + id;
        }
        categories.delete(category);
        redirect.addFlashAttribute("categorySuccess", "ลบหมวดหมู่สำเร็จ");
        return "redirect:/admin/listCategory";
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public String inUse(RedirectAttributes redirect) {
        redirect.addFlashAttribute("categoryError", "ไม่สามารถดำเนินการได้ หมวดหมู่อาจถูกใช้งานโดยคอร์สหรือทักษะอยู่");
        return "redirect:/admin/listCategory";
    }
}
