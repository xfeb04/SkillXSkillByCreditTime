package com.springboot.controller.admin;

import com.springboot.dto.AdminReportDto;
import com.springboot.repository.ReportRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Locale;

@Controller
@Transactional(readOnly=true)
public class AdminReportController {
    private final ReportRepository reports;
    public AdminReportController(ReportRepository reports) { this.reports = reports; }
    @GetMapping("/admin/listReportStudent")
    public String getListReportStudent(@RequestParam(defaultValue="") String q, HttpSession session, Model model) {
        return list("Student", "ผู้เรียน", q, session, model);
    }
    @GetMapping("/admin/listReportTeacher")
    public String getListReportTeacher(@RequestParam(defaultValue="") String q, HttpSession session, Model model) {
        return list("Teacher", "ผู้สอน", q, session, model);
    }
    private String list(String type, String party, String q, HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        String query = q.trim().toLowerCase(Locale.ROOT);
        model.addAttribute("type", type); model.addAttribute("party", party); model.addAttribute("q", q);
        model.addAttribute("reports", reports.findByReportTypeOrderByReportDateDescReportIdDesc(party).stream()
                .filter(r -> (r.getReportedMember().getFirstname() + " " + r.getReportedMember().getSurname()
                        + " " + r.getReportedMember().getEmail()).toLowerCase(Locale.ROOT).contains(query))
                .map(AdminReportDto::new).toList());
        return "admin/listReport";
    }
    @GetMapping("/admin/report/{id}/{action}")
    public String next(@PathVariable Integer id, @PathVariable String action, HttpSession session, Model model) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        if (!java.util.Set.of("detail", "ban").contains(action)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var report = getMemberData(id);
        model.addAttribute("report", new AdminReportDto(report));
        model.addAttribute("type", "ผู้เรียน".equals(report.getReportType()) ? "Student" : "Teacher");
        model.addAttribute("action", action);
        if ("ban".equals(action)) return banForm(report, model);
        model.addAttribute("course", com.springboot.dto.CourseDetailDto.from(report.getRegisterCourse().getCourse(), 0, java.util.List.of()));
        model.addAttribute("courseStatus", report.getRegisterCourse().getCourse().getCourseStatus());
        model.addAttribute("evidence", report.getImgReport() == null ? null : java.util.Base64.getEncoder().encodeToString(report.getImgReport()));
        byte[] image = report.getImgReport();
        model.addAttribute("evidenceMime", image != null && image.length > 2 && (image[0] & 255) == 255
                && (image[1] & 255) == 216 ? "image/jpeg"
                : image != null && image.length > 3 && image[0] == 'G' && image[1] == 'I' && image[2] == 'F' ? "image/gif" : "image/png");
        return "admin/reportDetail";
    }

    private com.springboot.model.Report getMemberData(Integer reportId) {
        return reports.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String banForm(com.springboot.model.Report report, Model model) {
        model.addAttribute("report", new AdminReportDto(report));
        model.addAttribute("party", report.getReportType());
        model.addAttribute("joined", report.getReportedMember().getCreatedAt() == null ? "ไม่ระบุ"
                : report.getReportedMember().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE)));
        model.addAttribute("minDate", java.time.LocalDate.now(java.time.ZoneId.of("Asia/Bangkok")).plusDays(1));
        return "admin/banMember";
    }

    @PostMapping("/admin/report/{id}/banStudent")
    @Transactional
    public String doBanStudent(@PathVariable Integer id, @RequestParam(defaultValue="") String unbanDate,
            @RequestParam(defaultValue="") String reason, HttpSession session, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        var report = getMemberData(id);
        if (!"ผู้เรียน".equals(report.getReportType())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ban(id, unbanDate, reason, session, model, redirect);
    }

    @PostMapping("/admin/report/{id}/banTeacher")
    @Transactional
    public String doBanTeacher(@PathVariable Integer id, @RequestParam(defaultValue="") String unbanDate,
            @RequestParam(defaultValue="") String reason, HttpSession session, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        var report = getMemberData(id);
        if (!"ผู้สอน".equals(report.getReportType())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ban(id, unbanDate, reason, session, model, redirect);
    }

    @PostMapping("/admin/report/{id}/ban")
    @Transactional
    public String ban(@PathVariable Integer id, @RequestParam(defaultValue="") String unbanDate,
            @RequestParam(defaultValue="") String reason, HttpSession session, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (session.getAttribute("adminUsername") == null) return "redirect:/admin/loginAdmin";
        var report = getMemberData(id);
        var errors = new java.util.LinkedHashMap<String,String>();
        java.time.LocalDate date = null;
        try {
            date = java.time.LocalDate.parse(unbanDate);
            if (!date.isAfter(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Bangkok")))) errors.put("date", "วันปลดแบนต้องเป็นวันในอนาคตเท่านั้น");
        } catch (java.time.format.DateTimeParseException ex) { errors.put("date", "กรุณากรอกวันปลดแบนให้ถูกต้อง"); }
        reason = reason.trim();
        if (reason.isBlank()) errors.put("reason", "กรุณากรอกสาเหตุการแบน");
        else if (reason.length() > 255) errors.put("reason", "สาเหตุการแบนต้องไม่เกิน 255 ตัวอักษร");
        else if (!reason.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s.,/#-]+")) errors.put("reason", "ใช้ได้เฉพาะภาษาไทย อังกฤษ ตัวเลข ช่องว่าง และ . , / # -");
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors); model.addAttribute("reason", reason); model.addAttribute("unbanDate", unbanDate);
            return banForm(report, model);
        }
        var member = report.getReportedMember();
        member.setBanStatus(1); member.setBanDate(date); member.setBanReason(reason);
        reports.flush();
        redirect.addFlashAttribute("reportMessage", "แบนสมาชิกสำเร็จ กำหนดปลดแบนวันที่ " + date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE)));
        return "redirect:/admin/listReport" + ("ผู้เรียน".equals(report.getReportType()) ? "Student" : "Teacher");
    }

}
