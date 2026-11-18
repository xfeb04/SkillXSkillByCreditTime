package com.springboot.controller;

import java.time.*;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.Locale;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import com.springboot.model.*;
import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class CreateCourseController {
    private final CourseRepository courses;
    private final CourseDateRepository dates;
    private final CategoryRepository categories;
    private final TeacherRepository teachers;
    private final CreditTransactionRepository transactions;

    public CreateCourseController(CourseRepository courses, CourseDateRepository dates,
            CategoryRepository categories, TeacherRepository teachers, CreditTransactionRepository transactions) {
        this.courses = courses;
        this.dates = dates;
        this.categories = categories;
        this.teachers = teachers;
        this.transactions = transactions;
    }

    @GetMapping("/createCourse")
    public String form(HttpSession session, Model model) {
        if (getMemberID(session) == null) return "redirect:/loginMember";
        Teacher teacher = teachers.findById(getMemberID(session)).orElse(null);
        if (teacher == null) return "redirect:/loginMember";
        addCreditBudget(model, teacher, BigDecimal.ZERO);
        return prepare(model, Map.of("delivery", "online", "courseType", "ปกติ", "maxMember", "1", "creditPerCourse", "1"),
                List.of(Map.of("date", "", "start", "", "end", "")), List.of());
    }

    @PostMapping("/createCourse")
    @Transactional
    public String doCreateCourse(@RequestParam MultiValueMap<String, String> params, HttpSession session, Model model, RedirectAttributes redirect) {
        return do_CalDiffCreditByCourseType(params, session, model, redirect, null);
    }
    @GetMapping("/editCourse/{courseId}")
    @Transactional(readOnly = true)
    public String edit(@PathVariable Integer courseId, HttpSession session, Model model) {
        Integer memberId = getMemberID(session);
        if (memberId == null) return "redirect:/loginMember";
        Course c = courses.findById(courseId).filter(item -> item.getTeacher().getMemberId().equals(memberId))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        requireEditable(c);
        addCreditBudget(model, c.getTeacher(), "ผู้สอนจ่ายให้ผู้เรียน".equals(c.getCourseType())
                ? BigDecimal.valueOf(c.getCreditPerCourse()).multiply(BigDecimal.valueOf(c.getMaxMember())) : BigDecimal.ZERO);
        model.addAttribute("editCourseId", courseId);
        Map<String,String> values = new HashMap<>();
        values.put("courseName", c.getCourseName()); values.put("courseDescription", c.getCourseDescription());
        values.put("categoryId", c.getCategory().getCategoryId().toString());
        values.put("delivery", Boolean.TRUE.equals(c.getIsOnline()) ? "online" : "offline");
        values.put("onlinePlatform", c.getOnlinePlatform()); values.put("location", c.getLocation());
        values.put("requiredEquip", c.getRequiredEquip()); values.put("maxMember", String.valueOf(c.getMaxMember()));
        values.put("courseType", c.getCourseType()); values.put("creditPerCourse", String.valueOf(c.getCreditPerCourse()));
        var rows = dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId).stream()
                .map(d -> Map.of("date", d.getTeachDate().toString(), "start", d.getStartTime().toString(), "end", d.getEndTime().toString())).toList();
        return prepare(model, values, rows.isEmpty() ? List.of(Map.of("date","","start","","end","")) : rows, List.of());
    }
    @PostMapping("/editCourse/{courseId}")
    @Transactional
    public String do_editCourse(@PathVariable Integer courseId, @RequestParam MultiValueMap<String,String> params,
            HttpSession session, Model model, RedirectAttributes redirect) {
        return do_CalDiffCreditByCourseType(params, session, model, redirect, courseId);
    }
    private void requireEditable(Course course) {
        if (!"สร้างสำเร็จ".equals(course.getCourseStatus())) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "แก้ไขได้เฉพาะคอร์สสถานะสร้างสำเร็จ");
    }
    private String do_CalDiffCreditByCourseType(MultiValueMap<String,String> params, HttpSession session, Model model,
            RedirectAttributes redirect, Integer courseId) {
        Integer memberId = getMemberID(session);
        if (memberId == null) return "redirect:/loginMember";
        Teacher teacher = teachers.findForUpdate(memberId).orElse(null);
        if (teacher == null) return "redirect:/loginMember";
        Course existing = null;
        if (courseId != null) {
            existing = courses.findOwnedForUpdate(courseId, memberId).orElseThrow(() ->
                    new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
            requireEditable(existing);
            model.addAttribute("editCourseId", courseId);
        }
        BigDecimal oldReserve = existing != null && "ผู้สอนจ่ายให้ผู้เรียน".equals(existing.getCourseType())
                ? BigDecimal.valueOf(existing.getCreditPerCourse()).multiply(BigDecimal.valueOf(existing.getMaxMember())) : BigDecimal.ZERO;
        addCreditBudget(model, teacher, oldReserve);
        Map<String, String> form = new HashMap<>();
        params.toSingleValueMap().forEach((key, value) -> form.put(key, value.trim()));
        List<String> errors = new ArrayList<>();
        String name = form.getOrDefault("courseName", "");
        String description = form.getOrDefault("courseDescription", "");
        String delivery = form.getOrDefault("delivery", "");
        boolean online = "online".equals(delivery);
        String platform = online ? form.getOrDefault("onlinePlatform", "") : null;
        String location = online ? null : form.getOrDefault("location", "");
        String equip = form.getOrDefault("requiredEquip", "");
        String type = form.getOrDefault("courseType", "");
        if (name.isBlank()) errors.add("กรุณากรอกชื่อคอร์ส");
        else if (name.length() < 4 || name.length() > 100) errors.add("ชื่อคอร์สต้องมีความยาว 4–100 ตัวอักษร");
        else if (!name.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E ]+")) errors.add("ชื่อคอร์สใช้ได้เฉพาะตัวอักษรภาษาไทย ภาษาอังกฤษ และช่องว่าง");
        if (description.isBlank()) errors.add("กรุณากรอกรายละเอียดคอร์ส");
        else if (description.length() < 20 || description.length() > 255) errors.add("รายละเอียดคอร์สต้องมีความยาว 20–255 ตัวอักษร");
        else if (!description.matches("[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s.,/#-]+")) errors.add("รายละเอียดคอร์สใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระ . , / # -");
        if (!Set.of("online", "offline").contains(delivery)) errors.add("กรุณาเลือกรูปแบบการเรียน");
        validateText(online ? platform : location, online ? "แพลตฟอร์มออนไลน์" : "สถานที่เรียน", 3, true, errors);
        validateText(equip, "อุปกรณ์ที่ต้องเตรียม", 4, false, errors);
        if (!Set.of("ปกติ", "เรียนฟรี", "ผู้สอนจ่ายให้ผู้เรียน").contains(type)) errors.add("กรุณาเลือกรูปแบบคอร์ส");
        Category category = null;
        int max = 0;
        double credit = 2;
        try { category = categories.findById(Integer.valueOf(form.getOrDefault("categoryId", ""))).orElse(null); }
        catch (NumberFormatException ignored) { }
        if (category == null) errors.add("กรุณาเลือกหมวดหมู่ที่มีอยู่ในระบบ");
        try { max = Integer.parseInt(form.getOrDefault("maxMember", "")); } catch (NumberFormatException ignored) { }
        if (max < 1) errors.add("จำนวนผู้เรียนสูงสุดต้องเป็นจำนวนเต็มอย่างน้อย 1 คน");
        if (!"เรียนฟรี".equals(type)) {
            try { credit = Double.parseDouble(form.getOrDefault("creditPerCourse", "")); } catch (NumberFormatException ignored) { credit = Double.NaN; }
            if (!Double.isFinite(credit) || credit < 0) errors.add("กรุณากรอกเครดิตเป็นตัวเลขตั้งแต่ 0 ขึ้นไป");
        }
        List<String> days = params.getOrDefault("teachDate", List.of());
        List<String> starts = params.getOrDefault("startTime", List.of());
        List<String> ends = params.getOrDefault("endTime", List.of());
        List<Map<String, String>> rows = new ArrayList<>();
        List<CourseDate> schedule = new ArrayList<>();
        if (days.isEmpty() || days.size() != starts.size() || days.size() != ends.size()) errors.add("กรุณากรอกวันและเวลาเรียนให้ครบอย่างน้อย 1 รายการ");
        for (int i = 0; i < days.size(); i++) {
            String start = i < starts.size() ? starts.get(i) : "";
            String end = i < ends.size() ? ends.get(i) : "";
            rows.add(Map.of("date", days.get(i), "start", start, "end", end));
            try {
                LocalDate day = LocalDate.parse(days.get(i));
                if (!start.matches("[0-2][0-9]:[0-5][0-9]") || !end.matches("[0-2][0-9]:[0-5][0-9]")) {
                    errors.add("กรุณาระบุเวลาในรูปแบบ HH:mm"); continue;
                }
                LocalTime from = LocalTime.parse(start), to = LocalTime.parse(end);
                if (!day.isAfter(LocalDate.now()) || !to.isAfter(from)) {
                    errors.add("วันเรียนที่ " + (i + 1) + ": วันที่ต้องหลังวันนี้ และเวลาสิ้นสุดต้องหลังเวลาเริ่ม");
                }
                for (CourseDate previous : schedule) {
                    if (day.equals(previous.getTeachDate()) && from.isBefore(previous.getEndTime()) && to.isAfter(previous.getStartTime())) {
                        errors.add("วันเรียนที่ " + (i + 1) + ": เวลาซ้อนกับรายการอื่น");
                        break;
                    }
                }
                schedule.add(new CourseDate(day, from, to, null));
            } catch (java.time.format.DateTimeParseException exception) {
                errors.add("วันเรียนที่ " + (i + 1) + ": กรุณากรอกวันที่และเวลาให้ถูกต้อง");
            }
        }
        BigDecimal reserve = checkCreditByCourseType(type, credit, max, teacher, oldReserve, errors);
        if (!errors.isEmpty()) {
            if (rows.isEmpty()) rows.add(Map.of("date", "", "start", "", "end", ""));
            return prepare(model, form, rows, errors);
        }
        doFrozenCredit(teacher, oldReserve, reserve);
        Course course = existing == null ? new Course() : existing;
        course.setCourseName(name); course.setCourseDescription(description); course.setIsOnline(online);
        course.setOnlinePlatform(platform); course.setLocation(location); course.setRequiredEquip(equip);
        course.setMaxMember(max); course.setCourseType(type); course.setCreditPerCourse(credit);
        course.setTeacher(teacher); course.setCategory(category);
        if (existing == null) { course.setCourseStatus("สร้างสำเร็จ"); course.setCreatedAt(LocalDate.now()); }
        if (existing == null) {
            do_createCourse(course, true, reserve, schedule);
        } else {
            editCourse(course, reserve, schedule);
        }
        redirect.addFlashAttribute("courseSuccess", existing == null ? "สร้างคอร์สสำเร็จ" : "บันทึกการแก้ไขคอร์สสำเร็จ");
        return "redirect:/viewCourseDetail/" + course.getCourseId();
    }

    private Integer getMemberID(HttpSession session) {
        return (Integer) session.getAttribute("memberId");
    }

    private List<Category> getListCategory() {
        List<Category> categoryList = categories.findAll();
        Collator thaiCollator = Collator.getInstance(Locale.forLanguageTag("th-TH"));
        categoryList.sort((left, right) -> thaiCollator.compare(left.getCategoryName(), right.getCategoryName()));
        return categoryList;
    }

    private BigDecimal checkCreditByCourseType(String type, double credit, int max, Teacher teacher,
            BigDecimal oldReserve, List<String> errors) {
        BigDecimal reserve = BigDecimal.ZERO;
        if (errors.isEmpty() && "ผู้สอนจ่ายให้ผู้เรียน".equals(type)) {
            reserve = BigDecimal.valueOf(credit).multiply(BigDecimal.valueOf(max));
            doCheckCredit(teacher, oldReserve, reserve, errors);
        }
        BigDecimal frozen = BigDecimal.valueOf(teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen());
        if (reserve.compareTo(oldReserve) != 0 && frozen.compareTo(oldReserve) < 0)
            errors.add("ยอดเครดิตที่พักไว้ไม่ตรงกับคอร์ส กรุณาตรวจสอบก่อนแก้ไข");
        return reserve;
    }

    private void doCheckCredit(Teacher teacher, BigDecimal oldReserve, BigDecimal reserve, List<String> errors) {
        BigDecimal balance = BigDecimal.valueOf(teacher.getCreditBalance() == null ? 0 : teacher.getCreditBalance());
        BigDecimal frozen = BigDecimal.valueOf(teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen());
        if (!Double.isFinite(reserve.doubleValue()) || balance.add(oldReserve).compareTo(reserve) < 0) {
            errors.add("เครดิตผู้สอนไม่เพียงพอ ต้องพักเครดิต " + reserve.toPlainString() + " เครดิต (เครดิตต่อคน × จำนวนผู้เรียนสูงสุด)");
        } else if (!Double.isFinite(frozen.subtract(oldReserve).add(reserve).doubleValue())) {
            errors.add("ยอดเครดิตที่พักไว้เกินขอบเขตที่ระบบรองรับ");
        }
    }

    private void doFrozenCredit(Teacher teacher, BigDecimal oldReserve, BigDecimal reserve) {
        if (reserve.compareTo(oldReserve) == 0) return;
        BigDecimal balance = BigDecimal.valueOf(teacher.getCreditBalance() == null ? 0 : teacher.getCreditBalance());
        BigDecimal frozen = BigDecimal.valueOf(teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen());
        teacher.setCreditBalance(balance.add(oldReserve).subtract(reserve).doubleValue());
        teacher.setCreditFrozen(frozen.subtract(oldReserve).add(reserve).doubleValue());
        teachers.save(teacher);
    }

    private void editCourse(Course course, BigDecimal reserve, List<CourseDate> schedule) {
        do_createCourse(course, false, reserve, schedule);
    }

    private void do_createCourse(Course course, boolean isNew, BigDecimal reserve, List<CourseDate> schedule) {
        courses.save(course);
        var hold = isNew ? null : transactions.findCourseReserveForUpdate(course.getCourseId()).orElse(null);
        if ("ผู้สอนจ่ายให้ผู้เรียน".equals(course.getCourseType()) || hold != null) {
            if (hold == null) hold = new CreditTransaction();
            hold.setReservedCourse(course);
            hold.setAmount(reserve.doubleValue());
            hold.setCreditTranDate(LocalDate.now(ZoneId.of("Asia/Bangkok")));
            hold.setCreditTranType("พักเครดิตผู้สอน");
            hold.setCreditTranStatus(reserve.signum() > 0 ? "พักเครดิตคอร์ส" : "สิ้นสุดการพักเครดิต");
            hold.setTranDescription("พักเครดิตสำหรับคอร์ส " + course.getCourseName() + " (เครดิตต่อคน × จำนวนผู้เรียนสูงสุด)");
            transactions.save(hold);
        }
        if (!isNew) {
            dates.deleteAll(dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(course.getCourseId()));
            dates.flush();
        }
        schedule.forEach(date -> date.setCourse(course));
        dates.saveAll(schedule);
    }

    private void validateText(String value, String label, int min, boolean allowUrl, List<String> errors) {
        String pattern = allowUrl
                ? "[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s.,/#:?=&%_~@!$'()*+;\\[\\]-]+"
                : "[A-Za-z\u0E01-\u0E3A\u0E40-\u0E4E\u0E50-\u0E590-9\\s.,/#-]+";
        if (value.isBlank()) errors.add("กรุณากรอก" + label);
        else if (value.length() < min || value.length() > 255) errors.add(label + "ต้องมีความยาว " + min + "–255 ตัวอักษร");
        else if (!value.matches(pattern)) errors.add(label + (allowUrl
                ? "ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระที่ใช้ในลิงก์"
                : "ใช้ได้เฉพาะภาษาไทย ภาษาอังกฤษ ตัวเลข ช่องว่าง และอักขระ . , / # -"));
    }
    private void addCreditBudget(Model model, Teacher teacher, BigDecimal oldReserve) {
        BigDecimal balance = BigDecimal.valueOf(teacher.getCreditBalance() == null ? 0 : teacher.getCreditBalance());
        model.addAttribute("creditBalance", balance.toPlainString());
        model.addAttribute("availableCourseCredit", balance.add(oldReserve).toPlainString());
        model.addAttribute("existingCourseReserve", oldReserve.toPlainString());
    }

    private String prepare(Model model, Map<String, String> form, List<Map<String, String>> rows, List<String> errors) {
        model.addAttribute("form", form);
        model.addAttribute("schedule", rows);
        model.addAttribute("errors", errors);
        model.addAttribute("categories", getListCategory());
        model.addAttribute("today", LocalDate.now().plusDays(1));
        return "createCourse";
    }
}
