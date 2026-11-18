package com.springboot.controller;

import java.time.LocalDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.springboot.model.*;
import com.springboot.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class ApproveRequestController {
    private final CourseRepository courses;
    private final RegisterCourseRepository requests;
    private final CourseDateRepository dates;
    private final CreditTransactionRepository transactions;
    private final TeacherRepository teachers;
    public ApproveRequestController(CourseRepository courses, RegisterCourseRepository requests,
            CourseDateRepository dates, CreditTransactionRepository transactions, TeacherRepository teachers) {
        this.courses=courses; this.requests=requests; this.dates=dates; this.transactions=transactions; this.teachers=teachers;
    }
    @GetMapping("/approveRequest")
    @Transactional(readOnly=true)
    public String page(@RequestParam Integer courseId, HttpSession session, Model model) {
        Integer memberId=(Integer)session.getAttribute("memberId");
        if(memberId==null) return "redirect:/loginMember";
        Course course=courses.findById(courseId).filter(c -> c.getTeacher().getMemberId().equals(memberId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireOpen(course);
        long count=requests.countByCourse_CourseIdAndRegisterStatus(courseId,"อนุมัติแล้ว");
        model.addAttribute("course",course);
        model.addAttribute("approvedCount",count);
        var pendingRequests = getRegisterRequests(courseId);
        var learnerRatings = new java.util.HashMap<Integer, String>();
        var completedCourseCounts = new java.util.HashMap<Integer, Long>();
        for (RegisterCourse request : pendingRequests) {
            Integer learnerId = request.getMember().getMemberId();
            if (completedCourseCounts.containsKey(learnerId)) continue;
            Double average = requests.averageRatingForTeacher(learnerId);
            learnerRatings.put(learnerId, average == null ? "0.00"
                    : String.format(java.util.Locale.ROOT, "%.2f / 5", average));
            completedCourseCounts.put(learnerId, requests.countCompletedCoursesForMember(learnerId));
        }
        model.addAttribute("requests",pendingRequests);
        model.addAttribute("learnerRatings",learnerRatings);
        model.addAttribute("completedCourseCounts",completedCourseCounts);
        model.addAttribute("canAccept",course.getMaxMember()!=null && count<course.getMaxMember() && !started(courseId));
        return "approveRequest";
    }
    @PostMapping("/approveRequest")
    @Transactional
    public String do_ApproeRequest(@RequestParam Integer courseId, @RequestParam Integer requestId,
            @RequestParam String action, HttpSession session, RedirectAttributes redirect) {
        Integer memberId=(Integer)session.getAttribute("memberId");
        if(memberId==null) return "redirect:/loginMember";
        Course course=courses.findOwnedForUpdate(courseId,memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requireOpen(course);
        if(!"accept".equals(action) && !"reject".equals(action)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        RegisterCourse request=requests.findById(requestId).filter(r -> r.getCourse().getCourseId().equals(courseId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String target="redirect:/approveRequest?courseId="+courseId;
        if(!"รออนุมัติ".equals(request.getRegisterStatus())) {
            redirect.addFlashAttribute("requestError","คำขอนี้ได้รับการดำเนินการแล้ว"); return target;
        }
        boolean accept="accept".equals(action);
        if(accept && (started(courseId) || course.getMaxMember()==null || requests.countByCourse_CourseIdAndRegisterStatus(courseId,"อนุมัติแล้ว")>=course.getMaxMember())) {
            redirect.addFlashAttribute("requestError","ไม่สามารถอนุมัติได้ เนื่องจากคอร์สเต็มหรือเริ่มเรียนแล้ว"); return target;
        }
        var transaction=transactions.findByRegisterCourse_RegisterCourseId(requestId).orElse(null);
        if (accept) {
            transaction = manageCreditByCourseType(course, request, memberId, transaction, redirect);
            if (transaction == null) return target;
        }
        if(!accept && transaction!=null && "ปกติ".equals(course.getCourseType())) {
            double amount=transaction.getAmount()==null?0:transaction.getAmount();
            Teacher learner=teachers.findForUpdate(request.getMember().getMemberId()).orElseThrow();
            double frozen=learner.getCreditFrozen()==null?0:learner.getCreditFrozen();
            if(!Double.isFinite(amount) || amount<0 || frozen<amount) {
                redirect.addFlashAttribute("requestError","ยอดเครดิตที่พักไว้ไม่เพียงพอสำหรับคืน กรุณาตรวจสอบข้อมูลเครดิต"); return target;
            }
            learner.setCreditFrozen(frozen-amount);
            learner.setCreditBalance((learner.getCreditBalance()==null?0:learner.getCreditBalance())+amount);
            teachers.save(learner);
        }
        doApproeRequest(request, transaction, accept);
        redirect.addFlashAttribute("requestSuccess",accept?"อนุมัติคำขอลงทะเบียนสำเร็จ":"ปฏิเสธคำขอลงทะเบียนสำเร็จ");
        return target;
    }

    private void doApproeRequest(RegisterCourse request, CreditTransaction transaction, boolean accept) {
        request.setRegisterStatus(accept?"อนุมัติแล้ว":"ปฏิเสธคำขอ");

        if(transaction!=null) {
            transaction.setCreditTranStatus(accept?"รอสอนเสร็จ":"ปฏิเสธคำขอ");
            transactions.save(transaction);
        }
        requests.save(request);
    }
    // Returns null when credit validation fails; the caller displays the supplied error.
    private CreditTransaction manageCreditByCourseType(Course course, RegisterCourse request, Integer memberId,
            CreditTransaction transaction, RedirectAttributes redirect) {
            boolean normal="ปกติ".equals(course.getCourseType());
            boolean incentive="ผู้สอนจ่ายให้ผู้เรียน".equals(course.getCourseType());
            boolean free="เรียนฟรี".equals(course.getCourseType());
            if(!normal && !incentive && !free) throw new ResponseStatusException(HttpStatus.CONFLICT,"ประเภทคอร์สไม่ถูกต้อง");
            Double amount = free ? 2.0 : (incentive ? course.getCreditPerCourse() : transaction == null ? null : transaction.getAmount());
            if(amount==null || !Double.isFinite(amount) || amount<0) {
                redirect.addFlashAttribute("requestError","ไม่พบยอดเครดิตที่พักไว้สำหรับคำขอนี้ กรุณาตรวจสอบข้อมูล"); return null;
            }
            if (normal && !deductStuFrozenCredit(request, amount, redirect)) return null;
            if (incentive && !deductTeacherFrozenCredit(course, memberId, amount, redirect)) return null;
            if (free) return perpareFreeCourseCredit(course, request, transaction);
            return prepareApprovalTransaction(course, request, transaction, amount,
                    incentive ? "ผู้สอนจ่ายให้ผู้เรียน" : "ชำระค่าเรียน");
    }

    private boolean deductStuFrozenCredit(RegisterCourse request, double amount, RedirectAttributes redirect) {
        return deductFrozenCredit(request.getMember().getMemberId(), amount, redirect);
    }

    private CreditTransaction perpareFreeCourseCredit(Course course, RegisterCourse request, CreditTransaction transaction) {
        return prepareApprovalTransaction(course, request, transaction, 2.0, "เรียนฟรี");
    }

    private boolean deductTeacherFrozenCredit(Course course, Integer memberId, double amount, RedirectAttributes redirect) {
        if (!deductFrozenCredit(memberId, amount, redirect)) return false;
        if (amount > 0) {
            var hold = transactions.findCourseReserveForUpdate(course.getCourseId()).orElse(null);
            if (hold != null) {
                if (!"พักเครดิตคอร์ส".equals(hold.getCreditTranStatus()) || hold.getAmount() == null || hold.getAmount() < amount)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "ยอดพักเครดิตของคอร์สไม่เพียงพอ");
                hold.setAmount(java.math.BigDecimal.valueOf(hold.getAmount()).subtract(java.math.BigDecimal.valueOf(amount)).doubleValue());
                transactions.save(hold);
            }
        }
        return true;
    }

    private boolean deductFrozenCredit(Integer payerId, double amount, RedirectAttributes redirect) {
            if(amount>0) {
                Teacher payer=teachers.findForUpdate(payerId).orElseThrow();
                java.math.BigDecimal frozen=java.math.BigDecimal.valueOf(payer.getCreditFrozen()==null?0:payer.getCreditFrozen());
                java.math.BigDecimal debit=java.math.BigDecimal.valueOf(amount);
                if(frozen.compareTo(debit)<0) {
                    redirect.addFlashAttribute("requestError","เครดิตที่พักไว้ของผู้จ่ายไม่เพียงพอ ไม่สามารถอนุมัติได้"); return false;
                }
                payer.setCreditFrozen(frozen.subtract(debit).doubleValue());
                teachers.save(payer);
            }
        return true;
    }

    private CreditTransaction prepareApprovalTransaction(Course course, RegisterCourse request,
            CreditTransaction transaction, double amount, String type) {
            boolean free = "เรียนฟรี".equals(type);
            if(transaction==null) {
                transaction=new CreditTransaction(amount,java.time.LocalDate.now(),free?"เรียนฟรี":"ผู้สอนจ่ายให้ผู้เรียน",
                        "คอร์ส " + course.getCourseName(),"รอสอนเสร็จ",request);
                request.setCreditTransaction(transaction);
            }
            transaction.setAmount(amount);
            transaction.setCreditTranType(type);
            transaction.setTranDescription("คอร์ส " + course.getCourseName() + (free?" ลงทะเบียนฟรี":" ชำระแล้ว รอส่งมอบเครดิตเมื่อสอนเสร็จ"));
        return transaction;
    }

    private java.util.List<RegisterCourse> getRegisterRequests(Integer courseId) {
        return requests.findByCourse_CourseIdAndRegisterStatus(courseId, "รออนุมัติ");
    }

    private void requireOpen(Course course) {
        if(!"เปิดรับสมัคร".equals(course.getCourseStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,"คอร์สไม่ได้อยู่ในสถานะเปิดรับสมัคร");
    }
    private boolean started(Integer courseId) {
        return dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(courseId).stream()
                .anyMatch(d -> !LocalDateTime.of(d.getTeachDate(),d.getStartTime()).isAfter(LocalDateTime.now()));
    }
}
