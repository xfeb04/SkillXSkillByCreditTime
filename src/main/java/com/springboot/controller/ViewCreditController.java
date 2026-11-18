package com.springboot.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.model.CreditTransaction;
import com.springboot.model.Teacher;
import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.TeacherRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewCreditController {

    private final TeacherRepository teacherRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public ViewCreditController(TeacherRepository teacherRepository,
            CreditTransactionRepository creditTransactionRepository) {
        this.teacherRepository = teacherRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    @GetMapping("/viewCreditTime")
    public String viewCreditTime(HttpSession session, Model model) {
        Teacher member = findLoggedInMember(session);
        if (member == null) return "redirect:/loginMember";

        model.addAttribute("creditBalance", getCredits(member.getCreditBalance()));
        model.addAttribute("creditFrozen", getCredits(member.getCreditFrozen()));
        return "viewCreditTime";
    }

    @GetMapping("/viewCreditTransaction")
    public String getCreditTrans(HttpSession session, Model model,
            @RequestParam(defaultValue = "spent") String tab,
            @RequestParam(defaultValue = "completed") String status) {
        Teacher member = findLoggedInMember(session);
        if (member == null) return "redirect:/loginMember";

        String activeTab = "received".equals(tab) ? "received" : "spent";
        String activeStatus = "pending".equals(status) || "frozen".equals(tab) ? "pending" : "completed";
        boolean pending = "pending".equals(activeStatus);
        String label = "received".equals(activeTab)
                ? (pending ? "รอรับเครดิต" : "ได้รับแล้ว")
                : (pending ? "พักเครดิตไว้" : "จ่ายแล้ว");
        model.addAttribute("creditBalance", getCredits(member.getCreditBalance()));
        model.addAttribute("creditFrozen", getCredits(member.getCreditFrozen()));
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("activeStatus", activeStatus);
        model.addAttribute("transactionLabel", label);
        model.addAttribute("emptyMessage", "ยังไม่มีรายการ" + label);
        model.addAttribute("transactions", creditTransactionRepository.findForMember(member.getMemberId()).stream()
                .filter(transaction -> transaction.getAmount() != null && transaction.getAmount() > 0)
                .filter(transaction -> matchesDirection(transaction, member.getMemberId(), activeTab, pending))
                .toList());
        return "viewCreditTransaction";
    }

    private boolean matchesDirection(CreditTransaction transaction, Integer memberId, String tab, boolean pending) {
        if (transaction.getReservedCourse() != null) {
            return "spent".equals(tab) && pending && "พักเครดิตคอร์ส".equals(transaction.getCreditTranStatus())
                    && memberId.equals(transaction.getReservedCourse().getTeacher().getMemberId());
        }
        var registration = transaction.getRegisterCourse();
        String courseType = registration.getCourse().getCourseType();
        boolean teacherPays = "ผู้สอนจ่ายให้ผู้เรียน".equals(courseType);
        if ("เรียนฟรี".equals(courseType)) {
            return "received".equals(tab) && memberId.equals(registration.getCourse().getTeacher().getMemberId())
                    && (pending ? "รอสอนเสร็จ".equals(transaction.getCreditTranStatus())
                    : "สำเร็จ".equals(transaction.getCreditTranStatus()) && "เรียนเสร็จสิ้น".equals(registration.getRegisterStatus()));
        }
        if (!teacherPays && !"ปกติ".equals(courseType)) return false;

        Integer learnerId = registration.getMember().getMemberId();
        Integer teacherId = registration.getCourse().getTeacher().getMemberId();
        Integer payerId = teacherPays ? teacherId : learnerId;
        Integer recipientId = teacherPays ? learnerId : teacherId;
        String status = transaction.getCreditTranStatus();
        boolean received = "received".equals(tab);
        boolean paid = "สำเร็จ".equals(status);
        boolean delivered = paid && "เรียนเสร็จสิ้น".equals(registration.getRegisterStatus());
        boolean awaitingDelivery = "รอสอนเสร็จ".equals(status)
                || (paid && "อนุมัติแล้ว".equals(registration.getRegisterStatus()));
        boolean held = "รอผู้สอนอนุมัติ".equals(status) || "รอการสอนเสร็จ".equals(status);
        if (received) {
            return memberId.equals(recipientId) && (pending ? awaitingDelivery || held : delivered);
        }
        return memberId.equals(payerId) && (pending ? held : paid || awaitingDelivery);
    }
    private Teacher findLoggedInMember(HttpSession session) {
        Integer memberId = (Integer) session.getAttribute("memberId");
        return memberId == null ? null : teacherRepository.findById(memberId).orElse(null);
    }

    private String getCredits(Double value) {
        return String.format(Locale.ROOT, "%.2f", value == null ? 0 : value);
    }
}
