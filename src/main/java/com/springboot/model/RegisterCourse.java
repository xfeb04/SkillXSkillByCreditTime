package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "register_course")
public class RegisterCourse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "register_course_id")
	private Integer registerCourseId;

	@Column(name = "registerStatus", length = 50, nullable = false)
	private String registerStatus;

	@Column(name = "registeredAt", nullable = false)
	private LocalDate registeredAt;

	@Column(name = "ratingScore")
	private Double ratingScore;

	
	@Column(name = "comment", length = 255)
	private String comment;

	@Column(name = "reviewDate")
	private LocalDate reviewDate;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Student member;

	@ManyToOne
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@OneToOne(mappedBy = "registerCourse")
	private CreditTransaction creditTransaction;

	public RegisterCourse() {
	}

	public RegisterCourse(String registerStatus, LocalDate registeredAt, Double ratingScore, String comment,
			LocalDate reviewDate, Student member, Course course, CreditTransaction creditTransaction) {
		this.registerStatus = registerStatus;
		this.registeredAt = registeredAt;
		this.ratingScore = ratingScore;
		this.comment = comment;
		this.reviewDate = reviewDate;
		this.member = member;
		this.course = course;
		this.creditTransaction = creditTransaction;
	}

    /** Accept a teaching role while registering its account's independent learner role. */
    public RegisterCourse(String status, LocalDate registeredAt, Double rating, String comment,
            LocalDate reviewedAt, Teacher learner, Course course, CreditTransaction transaction) {
        this(status, registeredAt, rating, comment, reviewedAt, learner.getStudent(), course, transaction);
    }
    public void setMember(Teacher learner) { this.member = learner.getStudent(); }

	public Integer getRegisterCourseId() { return registerCourseId; }
	public String getRegisterStatus() { return registerStatus; }
	public void setRegisterStatus(String registerStatus) { this.registerStatus = registerStatus; }
	public LocalDate getRegisteredAt() { return registeredAt; }
	public void setRegisteredAt(LocalDate registeredAt) { this.registeredAt = registeredAt; }
	public Double getRatingScore() { return ratingScore; }
	public void setRatingScore(Double ratingScore) { this.ratingScore = ratingScore; }
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	public LocalDate getReviewDate() { return reviewDate; }
	public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }
	public Student getMember() { return member; }
	public void setMember(Student member) { this.member = member; }
	public Course getCourse() { return course; }
	public void setCourse(Course course) { this.course = course; }
	public CreditTransaction getCreditTransaction() { return creditTransaction; }
	public void setCreditTransaction(CreditTransaction creditTransaction) { this.creditTransaction = creditTransaction; }
}
