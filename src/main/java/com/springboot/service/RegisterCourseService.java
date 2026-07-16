package com.springboot.service;


import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.RegisterCourse;
import com.springboot.repository.RegisterCourseRepository;

@Service
public class RegisterCourseService {

	private final RegisterCourseRepository registerCourseRepository;
 
	public RegisterCourseService(RegisterCourseRepository registerCourseRepository) {
		this.registerCourseRepository = registerCourseRepository;
	}

	public Optional<RegisterCourse> getById(Integer id) {
		return registerCourseRepository.findById(id);
	}

	public RegisterCourse save(RegisterCourse entity) {
		return registerCourseRepository.save(entity);
	}

	public void delete(Integer id) {
		registerCourseRepository.deleteById(id);
	}

}
