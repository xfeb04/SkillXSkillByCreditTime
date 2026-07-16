package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.CourseDate;

public interface CourseDateRepository extends JpaRepository<CourseDate, Integer> {
}
