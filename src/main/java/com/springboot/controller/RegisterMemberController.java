package com.springboot.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.model.Teacher;
import com.springboot.repository.MemberRepository;
import com.springboot.repository.TeacherRepository;

@Controller
public class RegisterMemberController {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> PROFILE_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> GENDERS = Set.of("ชาย", "หญิง", "ไม่ระบุ");

    private final MemberRepository memberRepository;
    private final TeacherRepository teacherRepository;

    public RegisterMemberController(MemberRepository memberRepository, TeacherRepository teacherRepository) {
        this.memberRepository = memberRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/registerMember")
    public String registerMember() {
        return "registerMember";
    }

    @PostMapping("/registerMember")
    public String registerMember(
            @RequestParam(required = false) MultipartFile imgProfile,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            @RequestParam(defaultValue = "") String confirmPassword,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String phoneNumber,
            @RequestParam(defaultValue = "") String firstname,
            @RequestParam(defaultValue = "") String surname,
            @RequestParam(defaultValue = "") String birthDay,
            @RequestParam(defaultValue = "") String gender,
            Model model) {

        username = username.trim();
        email = email.trim();
        phoneNumber = phoneNumber.trim();
        firstname = firstname.trim();
        surname = surname.trim();
        gender = gender.trim();
        Map<String, String> errors = validate(imgProfile, username, password, confirmPassword, email, phoneNumber, firstname, surname,
                birthDay, gender);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            addFormValues(model, username, email, phoneNumber, firstname, surname, birthDay, gender);
            return "registerMember";
        }

        try {
            Teacher member = new Teacher();
            member.setUsername(username);
            member.setPassword(password);
            member.setEmail(email);
            member.setPhoneNumber(phoneNumber);
            member.setFirstname(firstname);
            member.setSurname(surname);
            member.setBirthDay(LocalDate.parse(birthDay));
            member.setGender(gender);
            member.setImgProfile(imgProfile.getBytes());
            member.setCreatedAt(LocalDate.now());
            member.setCreditBalance(10.0);
            member.setCreditFrozen(0.0);
            member.setBanStatus(0);
            member.getStudent();
            teacherRepository.save(member);
            return "redirect:/loginMember?registered=true";
        } catch (IOException exception) {
            errors.put("imgProfile", "ไม่สามารถอ่านไฟล์รูปโปรไฟล์ได้");
            model.addAttribute("errors", errors);
            addFormValues(model, username, email, phoneNumber, firstname, surname, birthDay, gender);
            return "registerMember";
        } catch (DataIntegrityViolationException exception) {
            errors.put("form", "ไม่สามารถบันทึกข้อมูลได้ กรุณาตรวจสอบชื่อผู้ใช้และอีเมลอีกครั้ง");
            model.addAttribute("errors", errors);
            addFormValues(model, username, email, phoneNumber, firstname, surname, birthDay, gender);
            return "registerMember";
        }
    }

    private Map<String, String> validate(MultipartFile imgProfile, String username, String password, String confirmPassword, String email,
            String phoneNumber, String firstname, String surname, String birthDay, String gender) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateProfileImage(imgProfile, errors);
        validateUsername(username, errors);
        validatePassword(password, errors);
        validateConfirmPassword(password, confirmPassword, errors);
        validateEmail(email, errors);
        validatePhoneNumber(phoneNumber, errors);
        validateName("firstname", "ชื่อ", firstname, errors);
        validateName("surname", "นามสกุล", surname, errors);
        validateBirthDay(birthDay, errors);
        if (!StringUtils.hasText(gender)) errors.put("gender", "กรุณาเลือกเพศ");
        else if (!GENDERS.contains(gender)) errors.put("gender", "กรุณาเลือกเพศจากรายการที่กำหนด");
        return errors;
    }

    private void validateProfileImage(MultipartFile image, Map<String, String> errors) {
        if (image == null || image.isEmpty()) {
            errors.put("imgProfile", "กรุณาเลือกรูปโปรไฟล์");
            return;
        }
        String filename = image.getOriginalFilename();
        String extension = filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!PROFILE_IMAGE_EXTENSIONS.contains(extension)) errors.put("imgProfile", "รองรับเฉพาะไฟล์ .jpg, .jpeg, .png หรือ .gif");
        else if (image.getSize() > MAX_PROFILE_IMAGE_SIZE) errors.put("imgProfile", "ขนาดรูปโปรไฟล์ต้องไม่เกิน 5MB");
    }

    private void validateUsername(String username, Map<String, String> errors) {
        if (!StringUtils.hasText(username)) errors.put("username", "กรุณากรอกชื่อผู้ใช้");
        else if (username.length() < 4 || username.length() > 20) errors.put("username", "ชื่อผู้ใช้ต้องมีความยาว 4–20 ตัวอักษร");
        else if (!username.matches("[A-Za-z][A-Za-z0-9]*")) errors.put("username", "ชื่อผู้ใช้ต้องขึ้นต้นด้วยตัวอักษรภาษาอังกฤษ และใช้ได้เฉพาะตัวอักษรภาษาอังกฤษกับตัวเลข");
        else if (memberRepository.existsByUsername(username)) errors.put("username", "ชื่อผู้ใช้นี้ถูกใช้งานแล้ว");
    }

    private void validatePassword(String password, Map<String, String> errors) {
        if (!StringUtils.hasText(password)) errors.put("password", "กรุณากรอกรหัสผ่าน");
        else if (password.length() < 8 || password.length() > 16) errors.put("password", "รหัสผ่านต้องมีความยาว 8–16 ตัวอักษร");
        else if (!password.matches("[A-Za-z0-9!#_.]+")) errors.put("password", "รหัสผ่านใช้ได้เฉพาะตัวอักษรภาษาอังกฤษ ตัวเลข และ ! # _ .");
        else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) errors.put("password", "รหัสผ่านต้องมีตัวพิมพ์ใหญ่ ตัวพิมพ์เล็ก และตัวเลขอย่างน้อย 1 ตัว");
    }

    private void validateConfirmPassword(String password, String confirmPassword, Map<String, String> errors) {
        if (!StringUtils.hasText(confirmPassword)) errors.put("confirmPassword", "กรุณายืนยันรหัสผ่าน");
        else if (!password.equals(confirmPassword)) errors.put("confirmPassword", "รหัสผ่านและยืนยันรหัสผ่านไม่ตรงกัน");
    }

    private void validateEmail(String email, Map<String, String> errors) {
        if (!StringUtils.hasText(email)) errors.put("email", "กรุณากรอกอีเมล");
        else if (email.length() > 50) errors.put("email", "อีเมลต้องมีความยาวไม่เกิน 50 ตัวอักษร");
        else if (!email.matches("[a-z0-9]+([._-][a-z0-9]+)*@[a-z0-9]+([.-][a-z0-9]+)*\\.[a-z]{2,}")) errors.put("email", "รูปแบบอีเมลไม่ถูกต้อง");
        else if (memberRepository.existsByEmail(email)) errors.put("email", "อีเมลนี้ถูกใช้งานแล้ว");
    }

    private void validatePhoneNumber(String phoneNumber, Map<String, String> errors) {
        if (!StringUtils.hasText(phoneNumber)) errors.put("phoneNumber", "กรุณากรอกเบอร์โทรศัพท์");
        else if (!phoneNumber.matches("(06|07|08|09)\\d{8}")) errors.put("phoneNumber", "เบอร์โทรศัพท์ต้องเป็นตัวเลข 10 หลัก และขึ้นต้นด้วย 06, 07, 08 หรือ 09");
    }

    private void validateName(String field, String label, String value, Map<String, String> errors) {
        if (!StringUtils.hasText(value)) errors.put(field, "กรุณากรอก" + label);
        else if (value.length() > 100) errors.put(field, label + "ต้องมีความยาวไม่เกิน 100 ตัวอักษร");
        else if (!value.matches("[A-Za-z\u0E00-\u0E7F]+")) errors.put(field, label + "ต้องเป็นภาษาไทยหรือภาษาอังกฤษเท่านั้น และห้ามมีช่องว่าง");
    }

    private void validateBirthDay(String birthDay, Map<String, String> errors) {
        if (!StringUtils.hasText(birthDay)) {
            errors.put("birthDay", "กรุณาเลือกวันเกิด");
            return;
        }
        try {
            LocalDate date = LocalDate.parse(birthDay);
            if (!date.isBefore(LocalDate.now())) errors.put("birthDay", "วันเกิดต้องอยู่ในอดีต");
            else if (Period.between(date, LocalDate.now()).getYears() < 13) errors.put("birthDay", "ผู้สมัครต้องมีอายุอย่างน้อย 13 ปี");
        } catch (RuntimeException exception) {
            errors.put("birthDay", "วันเกิดต้องเป็นวันที่ถูกต้อง");
        }
    }

    private void addFormValues(Model model, String username, String email, String phoneNumber, String firstname,
            String surname, String birthDay, String gender) {
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("firstname", firstname);
        model.addAttribute("surname", surname);
        model.addAttribute("birthDay", birthDay);
        model.addAttribute("gender", gender);
    }
}
