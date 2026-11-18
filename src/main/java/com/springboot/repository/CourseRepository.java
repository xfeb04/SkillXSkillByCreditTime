package com.springboot.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Course;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    boolean existsByCategory_CategoryId(Integer categoryId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Course c where c.courseStatus = 'เปิดรับสมัคร' and exists (select d.courseDateId from CourseDate d where d.course = c and (d.teachDate < :today or (d.teachDate = :today and d.startTime <= :now))) order by c.courseId")
    List<Course> findDueForUpdate(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            @org.springframework.data.repository.query.Param("now") java.time.LocalTime now);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Course c where c.courseId = :id")
    Optional<Course> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Course c set c.courseStatus = 'กำลังดำเนินการ' "
            + "where c.courseStatus = 'เปิดรับสมัคร' and exists (select d.courseDateId from CourseDate d "
            + "where d.course = c and (d.teachDate < :today or (d.teachDate = :today and d.startTime <= :now)))")
    int startDueCourses(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            @org.springframework.data.repository.query.Param("now") java.time.LocalTime now);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Course c set c.courseStatus = 'สอนเสร็จสิ้น' "
            + "where c.courseStatus = 'กำลังดำเนินการ' "
            + "and exists (select d.courseDateId from CourseDate d where d.course = c) "
            + "and not exists (select d.courseDateId from CourseDate d where d.course = c "
            + "and (d.teachDate > :today or (d.teachDate = :today and d.endTime > :now)))")
    int finishDueCourses(@org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            @org.springframework.data.repository.query.Param("now") java.time.LocalTime now);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Course c where c.courseId = :id and c.teacher.memberId = :memberId")
    Optional<Course> findOwnedForUpdate(@org.springframework.data.repository.query.Param("id") Integer id,
            @org.springframework.data.repository.query.Param("memberId") Integer memberId);
	Optional<Course> findByCourseNameAndTeacher_MemberId(String courseName, Integer memberId);
	List<Course> findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(Integer memberId, String courseStatus);
}
