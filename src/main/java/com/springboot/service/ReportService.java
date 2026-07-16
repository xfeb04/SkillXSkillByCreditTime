package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Report;
import com.springboot.repository.ReportRepository;

@Service
public class ReportService {

	private final ReportRepository reportRepository;

	public ReportService(ReportRepository reportRepository) {
		this.reportRepository = reportRepository;
	}

	public List<Report> getAll() {
		return reportRepository.findAll();
	}

	public Optional<Report> getById(Integer id) {
		return reportRepository.findById(id);
	}

	public Report save(Report entity) {
		return reportRepository.save(entity);
	}

	public void delete(Integer id) {
		reportRepository.deleteById(id);
	}

}
