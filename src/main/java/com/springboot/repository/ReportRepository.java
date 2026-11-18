package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    java.util.List<Report> findByReportTypeOrderByReportDateDescReportIdDesc(String reportType);
	boolean existsByRegisterCourse_RegisterCourseId(Integer registerCourseId);

	boolean existsByTeacher_MemberIdAndStudent_MemberIdAndReportTypeAndRegisterCourse_RegisterCourseIdAndReason(
			Integer teacherId, Integer studentId, String reportType, Integer registerCourseId, String reason);
}
