package com.springboot.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "registercourse")
public class RegisterCourse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "register_id")
	private Integer registerId;

	@ManyToOne
	@JoinColumn(name = "student_member_id")
	private Student student;

	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course;

	@Column(name = "registerStatus", length = 50)
	private String registerStatus;

	@Column(name = "registeredAt")
	private LocalDate registeredAt;

	@Column(name = "ratingScore")
	private Double ratingScore;

	@Lob
	@Column(name = "comment")
	private String comment;

	@Column(name = "reviewDate")
	private LocalDate reviewDate;

	@OneToMany(mappedBy = "registerCourse")
	private List<CreditTransaction> creditTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "registerCourse")
	private List<Report> reports = new ArrayList<>();

	public RegisterCourse(Integer registerId, Student student, Course course, String registerStatus,
			LocalDate registeredAt, Double ratingScore, String comment, LocalDate reviewDate) {
		this.registerId = registerId;
		this.student = student;
		this.course = course;
		this.registerStatus = registerStatus;
		this.registeredAt = registeredAt;
		this.ratingScore = ratingScore;
		this.comment = comment;
		this.reviewDate = reviewDate;
	}

	public RegisterCourse() {
	}

	public Integer getRegisterId() {
		return registerId;
	}

	public void setRegisterId(Integer registerId) {
		this.registerId = registerId;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getRegisterStatus() {
		return registerStatus;
	}

	public void setRegisterStatus(String registerStatus) {
		this.registerStatus = registerStatus;
	}

	public LocalDate getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(LocalDate registeredAt) {
		this.registeredAt = registeredAt;
	}

	public Double getRatingScore() {
		return ratingScore;
	}

	public void setRatingScore(Double ratingScore) {
		this.ratingScore = ratingScore;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDate getReviewDate() {
		return reviewDate;
	}

	public void setReviewDate(LocalDate reviewDate) {
		this.reviewDate = reviewDate;
	}

	public List<CreditTransaction> getCreditTransactions() {
		return creditTransactions;
	}

	public void setCreditTransactions(List<CreditTransaction> creditTransactions) {
		this.creditTransactions = creditTransactions;
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

}
