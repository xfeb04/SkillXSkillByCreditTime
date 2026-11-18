package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import com.springboot.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Integer> {
    boolean existsByCategory_CategoryId(Integer categoryId);
	Optional<Skill> findFirstByTeacher_MemberIdAndCategory_CategoryIdOrderBySkillIdAsc(Integer memberId, Integer categoryId);
	List<Skill> findByTeacher_MemberIdOrderBySkillIdDesc(Integer memberId);
}
