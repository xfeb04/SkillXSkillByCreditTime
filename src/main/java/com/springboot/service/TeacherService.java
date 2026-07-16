package com.springboot.service;


import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Teacher;
import com.springboot.repository.TeacherRepository;

@Service
public class TeacherService {

	private final TeacherRepository teacherRepository;

	public TeacherService(TeacherRepository teacherRepository) {
		this.teacherRepository = teacherRepository;
	}

	public Optional<Teacher> getById(Integer id) {
		return teacherRepository.findById(id);
	}

	public Teacher save(Teacher entity) {
		return teacherRepository.save(entity);
	}

	public void delete(Integer id) {
		teacherRepository.deleteById(id);
	}

}
