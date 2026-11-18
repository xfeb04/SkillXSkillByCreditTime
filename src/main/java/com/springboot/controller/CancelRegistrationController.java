package com.springboot.controller;

import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CancelRegistrationController {
    private final RegisterCourseRepository registrations;
    private final TeacherRepository members;
    private final CreditTransactionRepository transactions;
    public CancelRegistrationController(RegisterCourseRepository registrations, TeacherRepository members,
            CreditTransactionRepository transactions) {
        this.registrations = registrations; this.members = members; this.transactions = transactions;
    }

    @PostMapping("/cancelRegisterCourse/{registrationId}")
    @Transactional
    public String cancelRegistration(@PathVariable Integer registrationId, HttpSession session, RedirectAttributes redirect) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        if (memberId == null) return "redirect:/loginMember";
        var registration = registrations.findForCancellation(memberId, registrationId).orElseThrow(IllegalStateException::new);
        if (!"รออนุมัติ".equals(registration.getRegisterStatus())) {
            redirect.addFlashAttribute("registrationError", "ไม่สามารถยกเลิกคำขอลงทะเบียนนี้ได้");
            return "redirect:/viewCourseDetail/" + registration.getCourse().getCourseId();
        }
        registration.setRegisterStatus("ยกเลิก");
        registrations.save(registration);
        var transaction = transactions.findByRegisterCourse_RegisterCourseId(registrationId).orElse(null);
        if ("ปกติ".equals(registration.getCourse().getCourseType())) {
            if (transaction == null || transaction.getAmount() == null) throw new IllegalStateException();
            double amount = transaction.getAmount();
            var member = members.findForUpdate(memberId).orElseThrow(IllegalStateException::new);
            double frozen = member.getCreditFrozen() == null ? 0 : member.getCreditFrozen();
            if (!Double.isFinite(amount) || amount < 0 || frozen < amount) throw new IllegalStateException();
            member.setCreditFrozen(frozen - amount);
            member.setCreditBalance((member.getCreditBalance() == null ? 0 : member.getCreditBalance()) + amount);
            members.save(member);
        }
        if (transaction != null) {
            transaction.setCreditTranStatus("ยกเลิกแล้ว");
            transactions.save(transaction);
        }
        redirect.addFlashAttribute("registrationSuccess", "ยกเลิกคำขอลงทะเบียนสำเร็จ");
        return "redirect:/viewCourseDetail/" + registration.getCourse().getCourseId();
    }

    @ExceptionHandler({IllegalStateException.class, org.springframework.dao.DataAccessException.class,
            org.springframework.transaction.TransactionException.class})
    public String cancellationFailed(RedirectAttributes redirect) {
        redirect.addFlashAttribute("registrationError", "เกิดข้อผิดพลาด กรุณาลองใหม่อีกครั้ง");
        return "redirect:/listRegisterCourse";
    }
}
