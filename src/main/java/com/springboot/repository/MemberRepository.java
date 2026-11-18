package com.springboot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsByEmailIgnoreCaseAndMemberIdNot(String email, Integer memberId);
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update Member m set m.banStatus = 0 where m.banStatus = 1 and m.banDate <= :today and (:id is null or m.memberId = :id)")
    int releaseExpiredBans(@org.springframework.data.repository.query.Param("id") Integer id,
            @org.springframework.data.repository.query.Param("today") java.time.LocalDate today);
	Optional<Member> findByUsername(String username);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}
