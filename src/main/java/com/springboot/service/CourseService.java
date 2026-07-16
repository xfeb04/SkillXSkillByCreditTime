package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Course;
import com.springboot.repository.CourseRepository;

@Service
public class CourseService {

	private final CourseRepository courseRepository;

	CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public List<Course> getAll() {
		return courseRepository.findAll();
	}

	public Optional<Course> getById(Integer id) {
		return courseRepository.findById(id);
	}

	public Course save(Course entity) {
		return courseRepository.save(entity);
	}

	public void delete(Integer id) {
		courseRepository.deleteById(id);
	}

}
