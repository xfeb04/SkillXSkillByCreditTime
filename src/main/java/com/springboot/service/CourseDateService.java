package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.CourseDate;
import com.springboot.repository.CourseDateRepository;

@Service
public class CourseDateService {

	private final CourseDateRepository courseDateRepository;

	CourseDateService(CourseDateRepository courseDateRepository) {
		this.courseDateRepository = courseDateRepository;
	}

	public List<CourseDate> getAll() {
		return courseDateRepository.findAll();
	}

	public Optional<CourseDate> getById(Integer id) {
		return courseDateRepository.findById(id);
	}

	public CourseDate save(CourseDate entity) {
		return courseDateRepository.save(entity);
	}

	public void delete(Integer id) {
		courseDateRepository.deleteById(id);
	}

}
