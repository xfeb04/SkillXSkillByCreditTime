package com.springboot.model;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import com.springboot.repository.*;

@DataJpaTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:member-roles;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
}, showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberRolesJpaTest {
    @Autowired TeacherRepository teachers;
    @Autowired MemberRepository members;
    @Autowired StudentRepository students;
    @Autowired RegisterCourseRepository registrations;
    @Autowired CourseRepository courses;
    @Autowired CategoryRepository categories;
    @Autowired ReportRepository reports;
    @Autowired EntityManager em;

    void accountDetails(Member member, String username) {
        member.setFirstname(username); member.setSurname("Test");
        member.setEmail(username + "@example.com"); member.setPhoneNumber("0812345678");
        member.setGender("test"); member.setBirthDay(LocalDate.of(2000,1,1));
        member.setCreatedAt(LocalDate.now());
    }

    Teacher account(String username) {
        Teacher teacher = new Teacher();
        teacher.setUsername(username); teacher.setPassword("password");
        accountDetails(teacher.getMember(), username);
        teacher.setFirstname(username); teacher.setCreditBalance(20.0); teacher.setCreditFrozen(0.0);
        teacher.getStudent();
        return teachers.saveAndFlush(teacher);
    }

    @Test void sameAccountPersistsBothRolesWithOneSharedPrimaryKey() {
        Teacher role = account("bothRoles");
        Integer id = role.getMemberId();
        assertNotNull(id);
        em.clear();
        assertEquals(1, members.count()); assertEquals(1, students.count()); assertEquals(1, teachers.count());
        Teacher loadedTeacher = teachers.findById(id).orElseThrow();
        Student loadedStudent = students.findById(id).orElseThrow();
        assertSame(loadedTeacher.getMember(), loadedStudent.getMember());
        assertEquals(id, loadedStudent.getMemberId());
        assertTrue(teachers.existsByUsername("bothRoles"));
        assertEquals(id, teachers.findByUsername("bothRoles").orElseThrow().getMemberId());
    }

    @Test void existingAccountCanAcquireBothRolesWithoutDuplicatingMemberOrCredit() {
        Member member = new Member(); member.setUsername("existing"); member.setPassword("password");
        accountDetails(member, "existing");
        member.setCreditBalance(12.5); member.setCreditFrozen(3.0);
        member = members.saveAndFlush(member);
        Integer id = member.getMemberId();
        member.enableStudent();
        Teacher teacher = new Teacher(); teacher.setMember(member);
        teachers.saveAndFlush(teacher); em.clear();
        assertEquals(1, members.count());
        assertEquals(id, students.findById(id).orElseThrow().getMemberId());
        assertEquals(12.5, teachers.findById(id).orElseThrow().getCreditBalance());
        assertEquals(3.0, teachers.findById(id).orElseThrow().getCreditFrozen());
    }

    @Test void locksAccountAndUpdatesCreditVisibleThroughEitherRole() {
        Integer id = account("credit").getMemberId(); em.clear();
        Teacher locked = teachers.findForUpdate(id).orElseThrow();
        locked.setCreditBalance(16.0); locked.setCreditFrozen(4.0);
        teachers.saveAndFlush(locked); em.clear();
        Student learner = students.findById(id).orElseThrow();
        assertEquals(16.0, learner.getCreditBalance()); assertEquals(4.0, learner.getCreditFrozen());
        assertEquals(16.0, members.findById(id).orElseThrow().getCreditBalance());
    }

    @Test void registrationAndReportReferenceTeacherAndStudentRoles() {
        Teacher owner = account("owner"); Teacher learner = account("learner");
        Category category = new Category(); category.setCategoryName("skills"); category.setCateDescription("test category"); category = categories.saveAndFlush(category);
        Course course = new Course(); course.setIsOnline(false); course.setTeacher(owner); course.setCategory(category);
        course.setCourseName("course"); course.setCourseDescription("test course");
        course.setMaxMember(10); course.setCreditPerCourse(2.0); course.setCourseStatus("เปิดรับสมัคร"); course.setCourseType("ปกติ");
        course = courses.saveAndFlush(course);
        RegisterCourse registration = registrations.saveAndFlush(new RegisterCourse("รออนุมัติ", LocalDate.now(),
                null, null, null, learner.getStudent(), course, null));
        Report report = reports.saveAndFlush(new Report("ผู้เรียน", "reason", null, LocalDate.now(),
                owner, learner.getStudent(), registration));
        Report teacherReport = reports.saveAndFlush(new Report("ผู้สอน", "teacher reason", null, LocalDate.now(),
                owner, learner.getStudent(), registration));
        Integer teacherReportId = teacherReport.getReportId();
        Integer registrationId = registration.getRegisterCourseId(); Integer reportId = report.getReportId();
        Integer learnerId = learner.getMemberId(); em.clear();
        assertEquals(learnerId, registrations.findById(registrationId).orElseThrow().getMember().getMemberId());
        assertEquals(learnerId, reports.findById(reportId).orElseThrow().getReportedMember().getMemberId());
        assertEquals(1, registrations.findByMember_MemberId(learnerId).size());
        assertTrue(registrations.findForCancellation(learnerId, registrationId).isPresent());
        Report loaded = reports.findById(teacherReportId).orElseThrow();
        assertEquals(owner.getMemberId(), loaded.getTeacher().getMemberId());
        assertEquals(learnerId, loaded.getStudent().getMemberId());
        assertEquals(learnerId, loaded.getReporter().getMemberId());
        assertEquals(owner.getMemberId(), loaded.getReportedMember().getMemberId());
        assertEquals(owner.getMemberId(), reports.findById(reportId).orElseThrow().getReporter().getMemberId());
        assertTrue(reports.existsByTeacher_MemberIdAndStudent_MemberIdAndReportTypeAndRegisterCourse_RegisterCourseIdAndReason(
                owner.getMemberId(), learnerId, "ผู้สอน", registrationId, "teacher reason"));
        assertEquals(2, members.count());
    }
}
