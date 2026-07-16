package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Skill;
import com.springboot.repository.SkillRepository;

@Service
public class SkillService {

	private final SkillRepository skillRepository;

	public SkillService(SkillRepository skillRepository) {
		this.skillRepository = skillRepository;
	}

	public List<Skill> getAll() {
		return skillRepository.findAll();
	}

	public Optional<Skill> getById(Integer id) {
		return skillRepository.findById(id);
	}

	public Skill save(Skill entity) {
		return skillRepository.save(entity);
	}

	public void delete(Integer id) {
		skillRepository.deleteById(id);
	}

}
