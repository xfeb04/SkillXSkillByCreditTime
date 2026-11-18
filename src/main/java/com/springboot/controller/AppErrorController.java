package com.springboot.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {
    @RequestMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = value instanceof Integer ? (Integer) value : 500;
        if (code < 400 || code > 599) code = 500;
        HttpStatus status = HttpStatus.resolve(code);
        response.setStatus(code);
        model.addAttribute("errorCode", code);
        model.addAttribute("errorTitle", status == null ? "Request Failed" : status.getReasonPhrase());
        model.addAttribute("errorMessage", switch (code) {
            case 404 -> "ขออภัย ไม่พบหน้าที่คุณต้องการครับ";
            case 403 -> "ขออภัย คุณไม่มีสิทธิ์เข้าถึงหน้านี้ครับ";
            case 401 -> "กรุณาเข้าสู่ระบบก่อนใช้งานครับ";
            default -> code >= 500 ? "ขออภัย ตอนนี้เซิร์ฟเวอร์ขัดข้อง กรุณาลองใหม่อีกครั้งครับ"
                    : "ขออภัย ไม่สามารถดำเนินการได้ กรุณาลองใหม่อีกครั้งครับ";
        });
        return "err";
    }
}
