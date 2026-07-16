package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Integer> {
}
