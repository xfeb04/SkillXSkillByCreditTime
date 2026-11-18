package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.springboot.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
	@org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@org.springframework.data.jpa.repository.Query("select t from Teacher t join fetch t.member where t.memberId = :id")
	Optional<Teacher> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);
	@org.springframework.data.jpa.repository.Query("select (count(t) > 0) from Teacher t where t.member.username = :username")
    boolean existsByUsername(@org.springframework.data.repository.query.Param("username") String username);
	@org.springframework.data.jpa.repository.Query("select t from Teacher t where t.member.username = :username")
    Optional<Teacher> findByUsername(@org.springframework.data.repository.query.Param("username") String username);
}
