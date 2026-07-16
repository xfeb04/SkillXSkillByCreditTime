package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.RegisterCourse;

public interface RegisterCourseRepository extends JpaRepository<RegisterCourse, Integer> {
}
