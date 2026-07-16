package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "report")
public class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private Integer reportId;

	@ManyToOne
	@JoinColumn(name = "teacher_member_id")
	private Teacher teacher;

	@ManyToOne
	@JoinColumn(name = "student_member_id")
	private Student student;

	@ManyToOne
	@JoinColumn(name = "register_id")
	private RegisterCourse registerCourse;

	@Column(name = "reportType", length = 100)
	private String reportType;

	@Lob
	@Column(name = "reason")
	private String reason;

	@Lob
	@Column(name = "img")
	private byte[] img;

	@Column(name = "reportDate")
	private LocalDate reportDate;

	public Report(Integer reportId, Teacher teacher, Student student, RegisterCourse registerCourse,
			String reportType, String reason, byte[] img, LocalDate reportDate) {
		this.reportId = reportId;
		this.teacher = teacher;
		this.student = student;
		this.registerCourse = registerCourse;
		this.reportType = reportType;
		this.reason = reason;
		this.img = img;
		this.reportDate = reportDate;
	}

	public Report() {
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public RegisterCourse getRegisterCourse() {
		return registerCourse;
	}

	public void setRegisterCourse(RegisterCourse registerCourse) {
		this.registerCourse = registerCourse;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public byte[] getImg() {
		return img;
	}

	public void setImg(byte[] img) {
		this.img = img;
	}

	public LocalDate getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}

}
