package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Student;
import com.springboot.repository.StudentRepository;

@Service
public class StudentService {

	private final StudentRepository studentRepository;

	public StudentService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	public List<Student> getAll() {
		return studentRepository.findAll();
	}

	public Optional<Student> getById(Integer id) {
		return studentRepository.findById(id);
	}

	public Student save(Student entity) {
		return studentRepository.save(entity);
	}

	public void delete(Integer id) {
		studentRepository.deleteById(id);
	}

}
