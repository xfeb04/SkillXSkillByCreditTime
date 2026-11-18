package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report")
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "report_id")
	private Integer reportId;

	@Column(name = "reportType", length = 100)
	private String reportType;

	
	@Column(name = "reason", length = 255, nullable = false)
	private String reason;

	@Lob
	@Column(name = "imgReport", columnDefinition = "LONGBLOB")
	private byte[] imgReport;

	@Column(name = "reportDate", nullable = false)
	private LocalDate reportDate;

	@ManyToOne
	@JoinColumn(name = "teacher_member_id", nullable = false)
	private Teacher teacher;

	@ManyToOne
	@JoinColumn(name = "student_member_id", nullable = false)
	private Student student;

	@ManyToOne
	@JoinColumn(name = "register_course_id", nullable = false)
	private RegisterCourse registerCourse;

	public Report() {
	}

    /** Role order is always teacher then student; reportType identifies the reported role. */
    public Report(String reportType, String reason, byte[] image, LocalDate date, Teacher teacher,
            Student student, RegisterCourse registration) {
        this.reportType = reportType;
        this.reason = reason;
        this.imgReport = image;
        this.reportDate = date;
        this.teacher = teacher;
        this.student = student;
        this.registerCourse = registration;
    }

	public Integer getReportId() { return reportId; }
	public String getReportType() { return reportType; }
	public void setReportType(String reportType) { this.reportType = reportType; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public byte[] getImgReport() { return imgReport; }
	public void setImgReport(byte[] imgReport) { this.imgReport = imgReport; }
	public LocalDate getReportDate() { return reportDate; }
	public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    /** Derived account identities for display and account-level bans, not stored columns. */
    public Member getReporter() {
        return "ผู้เรียน".equals(reportType) ? teacher.getMember() : student.getMember();
    }
    public Member getReportedMember() {
        return "ผู้เรียน".equals(reportType) ? student.getMember() : teacher.getMember();
    }
	public RegisterCourse getRegisterCourse() { return registerCourse; }
	public void setRegisterCourse(RegisterCourse registerCourse) { this.registerCourse = registerCourse; }
}
