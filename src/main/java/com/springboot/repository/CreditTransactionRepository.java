package com.springboot.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springboot.model.CreditTransaction;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from CreditTransaction t where t.reservedCourse.courseId = :id")
    Optional<CreditTransaction> findCourseReserveForUpdate(@Param("id") Integer id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from CreditTransaction t where t.registerCourse.course.courseId = :courseId and t.creditTranStatus = 'รอสอนเสร็จ' order by t.creditTransactionId")
    List<CreditTransaction> findAwaitingCompletionForUpdate(@Param("courseId") Integer courseId);
    @Query("select distinct coalesce(c.courseId, rc.courseId) from CreditTransaction t left join t.registerCourse r left join r.course c left join t.reservedCourse rc where (c.courseStatus = 'สอนเสร็จสิ้น' and t.creditTranStatus = 'รอสอนเสร็จ') or (rc.courseStatus = 'สอนเสร็จสิ้น' and t.creditTranStatus = 'พักเครดิตคอร์ส')")
    List<Integer> findCompletedCourseIdsAwaitingPayment();
	@Query("select t from CreditTransaction t left join fetch t.registerCourse r left join fetch r.course c left join fetch t.reservedCourse rc "
			+ "where r.member.memberId = :memberId or c.teacher.memberId = :memberId or rc.teacher.memberId = :memberId "
			+ "order by t.creditTranDate desc, t.creditTransactionId desc")
	List<CreditTransaction> findForMember(@Param("memberId") Integer memberId);
	Optional<CreditTransaction> findByTranDescription(String tranDescription);
	Optional<CreditTransaction> findByRegisterCourse_RegisterCourseId(Integer registerCourseId);
	List<CreditTransaction> findByRegisterCourse_Member_MemberIdOrderByCreditTranDateDescCreditTransactionIdDesc(Integer memberId);
}
