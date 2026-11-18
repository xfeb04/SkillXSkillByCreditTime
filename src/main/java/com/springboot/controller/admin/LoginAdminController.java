package com.springboot.controller.admin;

import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.repository.AdminRepository;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginAdminController {

    private final AdminRepository adminRepository;

    public LoginAdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @GetMapping("/admin/loginAdmin")
    public String loginAdmin() {
        return "admin/loginAdmin";
    }

    @PostMapping("/admin/loginAdmin")
    public String submitLoginAdmin(@RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password, Model model, HttpServletRequest request) {
        username = username.trim();
        model.addAttribute("username", username);
        if (!StringUtils.hasText(username)) {
            model.addAttribute("usernameError", "กรุณากรอกชื่อผู้ใช้");
        }
        if (!StringUtils.hasText(password)) {
            model.addAttribute("passwordError", "กรุณากรอกรหัสผ่าน");
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return "admin/loginAdmin";
        }
        boolean validCredentials = adminRepository.findByUsername(username)
                .map(admin -> password.equals(admin.getPassword()))
                .orElse(false);
        if (!validCredentials) {
            model.addAttribute("authenticationError", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
            return "admin/loginAdmin";
        }
        request.getSession();
        request.changeSessionId();
        request.getSession().setAttribute("adminUsername", username);
        return "redirect:/admin/adminHome";
    }

    @GetMapping({"/admin/adminHome", "/adminHome"})
    public String adminHome(jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        return "admin/adminHome";
    }

    @PostMapping("/admin/logoutAdmin")
    public String logoutAdmin(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/admin/loginAdmin";
    }
}
