package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
}
