package com.springboot.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.CourseDate;

public interface CourseDateRepository extends JpaRepository<CourseDate, Integer> {
	boolean existsByCourse_CourseIdAndTeachDate(Integer courseId, LocalDate teachDate);
	List<CourseDate> findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(Integer courseId);
}
