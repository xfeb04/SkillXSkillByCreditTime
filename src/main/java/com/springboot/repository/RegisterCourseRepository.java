package com.springboot.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.RegisterCourse;

public interface RegisterCourseRepository extends JpaRepository<RegisterCourse, Integer> {
    List<RegisterCourse> findByCourse_Teacher_MemberIdAndRatingScoreIsNotNullOrderByReviewDateDescRegisterCourseIdDesc(Integer teacherId);

    @org.springframework.data.jpa.repository.Query("select avg(r.ratingScore) from RegisterCourse r where r.course.teacher.memberId = :teacherId and r.ratingScore is not null")
    Double averageRatingForTeacher(@org.springframework.data.repository.query.Param("teacherId") Integer teacherId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from RegisterCourse r where r.member.memberId = :memberId and r.registerCourseId = :courseId")
    Optional<RegisterCourse> findForCancellation(@org.springframework.data.repository.query.Param("memberId") Integer memberId,
            @org.springframework.data.repository.query.Param("courseId") Integer courseId);
    List<RegisterCourse> findByCourse_CourseIdAndRatingScoreIsNotNullOrderByReviewDateDescRegisterCourseIdDesc(Integer courseId);
	boolean existsByMember_MemberIdAndCourse_CourseId(Integer memberId, Integer courseId);
	Optional<RegisterCourse> findFirstByMember_MemberIdAndCourse_CourseIdOrderByRegisterCourseIdDesc(Integer memberId, Integer courseId);
    default Optional<RegisterCourse> findByMember_MemberIdAndCourse_CourseId(Integer memberId, Integer courseId) {
        return findFirstByMember_MemberIdAndCourse_CourseIdOrderByRegisterCourseIdDesc(memberId, courseId);
    }
	List<RegisterCourse> findByMember_MemberId(Integer memberId);
	List<RegisterCourse> findByCourse_CourseIdAndRegisterStatus(Integer courseId, String registerStatus);
	long countByCourse_CourseIdAndRegisterStatus(Integer courseId, String registerStatus);
    @org.springframework.data.jpa.repository.Query("select count(distinct r.course.courseId) from RegisterCourse r where r.member.memberId = :memberId and r.registerStatus = 'เรียนเสร็จสิ้น'")
    long countCompletedCoursesForMember(@org.springframework.data.repository.query.Param("memberId") Integer memberId);
}
