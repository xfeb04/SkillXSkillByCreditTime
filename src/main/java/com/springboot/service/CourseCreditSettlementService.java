package com.springboot.service;

import com.springboot.repository.CreditTransactionRepository;
import com.springboot.repository.TeacherRepository;
import java.math.BigDecimal;
import com.springboot.model.CreditTransaction;
import com.springboot.model.RegisterCourse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseCreditSettlementService {
    private final CreditTransactionRepository transactions;
    private final TeacherRepository teachers;
    public CourseCreditSettlementService(CreditTransactionRepository transactions, TeacherRepository teachers) {
        this.transactions = transactions; this.teachers = teachers;
    }
    @Transactional
    public void settle(Integer courseId) {
        for (var transaction : transactions.findAwaitingCompletionForUpdate(courseId)) {
            // Recheck after acquiring the lock, including repeated submissions.
            if (!"รอสอนเสร็จ".equals(transaction.getCreditTranStatus())) continue;
            var registration = transaction.getRegisterCourse();
            var course = registration.getCourse();
            if (!"สอนเสร็จสิ้น".equals(course.getCourseStatus())) throw new IllegalStateException("คอร์สยังสอนไม่เสร็จ");
            if (!java.util.Set.of("อนุมัติแล้ว", "เรียนเสร็จสิ้น").contains(registration.getRegisterStatus()))
                throw new IllegalStateException("สถานะลงทะเบียนไม่ถูกต้องสำหรับโอนเครดิต");
            Double amount = transaction.getAmount();
            if (amount == null || !Double.isFinite(amount) || amount < 0) throw new IllegalStateException("ยอดเครดิตไม่ถูกต้อง");
            boolean free = "เรียนฟรี".equals(course.getCourseType());
            boolean incentive = "ผู้สอนจ่ายให้ผู้เรียน".equals(course.getCourseType());
            if (!free && !incentive && !"ปกติ".equals(course.getCourseType())) throw new IllegalStateException("ประเภทคอร์สไม่ถูกต้อง");
            if (free && amount != 0 && amount != 2) throw new IllegalStateException("เครดิตสนับสนุนคอร์สฟรีต้องเป็น 2 เครดิตต่อคน");
            manageCPCreditByCourseType(transaction);
            // The payer's frozen credits were deducted when the request was approved.
            doCompleteStudy(registration);
            doCompleteTransSs(transaction);
        }
        // Compatibility for courses that completed before the start-time refund was introduced.
        refundUnusedReserve(courseId);
    }

    private void manageCPCreditByCourseType(CreditTransaction transaction) {
        switch (transaction.getRegisterCourse().getCourse().getCourseType()) {
            case "ปกติ" -> addTeacherCreditByNm(transaction);
            case "เรียนฟรี" -> addTeacherCreditByFr(transaction);
            case "ผู้สอนจ่ายให้ผู้เรียน" -> addStuCreditBySp(transaction);
            default -> throw new IllegalStateException("ประเภทคอร์สไม่ถูกต้อง");
        }
    }

    private void addTeacherCreditByNm(CreditTransaction transaction) {
        addRecipientCredit(transaction.getRegisterCourse().getCourse().getTeacher().getMemberId(), transaction.getAmount());
    }

    private void addTeacherCreditByFr(CreditTransaction transaction) {
        addRecipientCredit(transaction.getRegisterCourse().getCourse().getTeacher().getMemberId(), transaction.getAmount());
    }

    private void addStuCreditBySp(CreditTransaction transaction) {
        addRecipientCredit(transaction.getRegisterCourse().getMember().getMemberId(), transaction.getAmount());
    }

    private void addRecipientCredit(Integer recipientId, double amount) {
        if (amount <= 0) return;
        var recipient = teachers.findForUpdate(recipientId).orElseThrow();
        double balance = recipient.getCreditBalance() == null ? 0 : recipient.getCreditBalance();
        if (!Double.isFinite(balance)) throw new IllegalStateException("ยอดคงเหลือไม่ถูกต้อง");
        recipient.setCreditBalance(BigDecimal.valueOf(balance).add(BigDecimal.valueOf(amount)).doubleValue());
        teachers.save(recipient);
    }

    private void doCompleteStudy(RegisterCourse registration) {
        registration.setRegisterStatus("เรียนเสร็จสิ้น");
    }

    private void doCompleteTransSs(CreditTransaction transaction) {
        transaction.setCreditTranStatus("สำเร็จ");
        transaction.setTranDescription("คอร์ส " + transaction.getRegisterCourse().getCourse().getCourseName()
                + " สอนเสร็จสิ้น ส่งมอบเครดิตสำเร็จ");
        transactions.save(transaction);
    }

    /** Caller holds the course lock; allocated learner credits remain awaiting completion. */
    @Transactional
    public void refundUnusedReserve(Integer courseId) {
        var hold = transactions.findCourseReserveForUpdate(courseId).orElse(null);
        if (hold != null && "พักเครดิตคอร์ส".equals(hold.getCreditTranStatus())) {
            if (!java.util.Set.of("กำลังดำเนินการ", "สอนเสร็จสิ้น").contains(hold.getReservedCourse().getCourseStatus()))
                throw new IllegalStateException("คอร์สยังไม่เริ่มสอน");
            double remaining = hold.getAmount();
            if (!Double.isFinite(remaining) || remaining < 0) throw new IllegalStateException("ยอดพักเครดิตไม่ถูกต้อง");
            var teacher = teachers.findForUpdate(hold.getReservedCourse().getTeacher().getMemberId()).orElseThrow();
            double frozen = teacher.getCreditFrozen() == null ? 0 : teacher.getCreditFrozen();
            if (frozen < remaining) throw new IllegalStateException("ยอดเครดิตพักไว้ไม่เพียงพอสำหรับคืน");
            teacher.setCreditFrozen(BigDecimal.valueOf(frozen).subtract(BigDecimal.valueOf(remaining)).doubleValue());
            teacher.setCreditBalance(BigDecimal.valueOf(teacher.getCreditBalance() == null ? 0 : teacher.getCreditBalance()).add(BigDecimal.valueOf(remaining)).doubleValue());
            teachers.save(teacher);
            hold.setAmount(0.0);
            hold.setCreditTranStatus("สิ้นสุดการพักเครดิต");
            hold.setTranDescription("คืนเครดิตส่วนที่ไม่ได้ใช้หลังปิดรับสมัคร " + remaining + " เครดิต");
            transactions.save(hold);
        }
    }

    @Transactional
    public void settleCompletedCourses() {
        for (Integer courseId : transactions.findCompletedCourseIdsAwaitingPayment()) settle(courseId);
    }
}
