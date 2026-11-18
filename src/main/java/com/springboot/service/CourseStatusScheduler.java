package com.springboot.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import com.springboot.repository.CourseRepository;

@Service
public class CourseStatusScheduler {
    private final CourseRepository courses;
    private final Clock clock;
    private final com.springboot.repository.RegisterCourseRepository registrations;
    private final com.springboot.repository.TeacherRepository members;
    private final com.springboot.repository.CreditTransactionRepository transactions;
    private final CourseCreditSettlementService settlement;

    @org.springframework.beans.factory.annotation.Autowired
    public CourseStatusScheduler(CourseRepository courses, CourseCreditSettlementService settlement, com.springboot.repository.RegisterCourseRepository registrations, com.springboot.repository.TeacherRepository members, com.springboot.repository.CreditTransactionRepository transactions) {
        this(courses, Clock.system(ZoneId.of("Asia/Bangkok")), settlement, registrations, members, transactions);
    }
    CourseStatusScheduler(CourseRepository courses, Clock clock, CourseCreditSettlementService settlement, com.springboot.repository.RegisterCourseRepository registrations, com.springboot.repository.TeacherRepository members, com.springboot.repository.CreditTransactionRepository transactions) {
        this.courses = courses;
        this.registrations = registrations; this.members = members; this.transactions = transactions;
        this.clock = clock;
        this.settlement = settlement;
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    @Transactional
    public void updateStatuses() {
        LocalDateTime current = LocalDateTime.now(clock);
        // Running both updates also catches courses whose entire schedule elapsed while the app was stopped.
        for (var course : courses.findDueForUpdate(current.toLocalDate(), current.toLocalTime())) {
            transitionToTeaching(course);
        }
        courses.flush();
        courses.finishDueCourses(current.toLocalDate(), current.toLocalTime());
        settlement.settleCompletedCourses();
    }
    /** Caller must hold the course lock before closing pending registrations. */
    @Transactional
    public void transitionToTeaching(com.springboot.model.Course course) {
        rejectReq(course);
        boolean hasLearners = doActiveCourse(course);
        if (hasLearners) refundUnuseTeachFrozCredit(course.getCourseId());
    }

    private void rejectReq(com.springboot.model.Course course) {
            // Lock the course first, matching approval/registration, then each pending request.
            for (var candidate : registrations.findByCourse_CourseIdAndRegisterStatus(course.getCourseId(), "รออนุมัติ")) {
                var request = registrations.findForCancellation(candidate.getMember().getMemberId(),
                        candidate.getRegisterCourseId()).orElseThrow();
                if (!"รออนุมัติ".equals(request.getRegisterStatus())) continue;
                var transaction = transactions.findByRegisterCourse_RegisterCourseId(request.getRegisterCourseId()).orElse(null);
                refundStuCredit(course, request, transaction);
                request.setRegisterStatus("ยกเลิก");
                registrations.save(request);
                if (transaction != null) {
                    transaction.setCreditTranStatus("ยกเลิกแล้ว");
                    transaction.setTranDescription("ยกเลิกอัตโนมัติ เนื่องจากไม่ได้รับอนุมัติก่อนเริ่มคอร์ส คืนเครดิตที่พักไว้แล้ว");
                    transactions.save(transaction);
                }
            }
    }

    private void refundStuCredit(com.springboot.model.Course course,
            com.springboot.model.RegisterCourse request, com.springboot.model.CreditTransaction transaction) {
                if ("ปกติ".equals(course.getCourseType())) {
                    if (transaction == null || transaction.getAmount() == null
                            || !"รอผู้สอนอนุมัติ".equals(transaction.getCreditTranStatus())) throw new IllegalStateException("Invalid pending credit");
                    double amount = transaction.getAmount();
                    var member = members.findForUpdate(request.getMember().getMemberId()).orElseThrow();
                    double frozen = member.getCreditFrozen() == null ? 0 : member.getCreditFrozen();
                    if (!Double.isFinite(amount) || amount < 0 || frozen < amount) throw new IllegalStateException("Invalid frozen credit");
                    member.setCreditFrozen(frozen - amount);
                    member.setCreditBalance((member.getCreditBalance() == null ? 0 : member.getCreditBalance()) + amount);
                    members.save(member);
                }
    }

    private boolean doActiveCourse(com.springboot.model.Course course) {
            boolean hasLearners = registrations.countByCourse_CourseIdAndRegisterStatus(course.getCourseId(), "อนุมัติแล้ว") > 0;
            course.setCourseStatus(hasLearners ? "กำลังดำเนินการ" : "สร้างสำเร็จ");
            courses.save(course);
            return hasLearners;
    }

    private void refundUnuseTeachFrozCredit(Integer courseId) {
        settlement.refundUnusedReserve(courseId);
    }

}
