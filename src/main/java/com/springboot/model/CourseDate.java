package com.springboot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "coursedate")
public class CourseDate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_date_id")
	private Integer courseDateId;

	@ManyToOne
	@JoinColumn(name = "course_id")
	private Course course;

	@Column(name = "teachDate")
	private LocalDate teachDate;

	@Column(name = "startTime")
	private LocalDateTime startTime;

	@Column(name = "endTime")
	private LocalDateTime endTime;

	public CourseDate(Integer courseDateId, Course course, LocalDate teachDate, LocalDateTime startTime,
			LocalDateTime endTime) {
		this.courseDateId = courseDateId;
		this.course = course;
		this.teachDate = teachDate;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public CourseDate() {
	}

	public Integer getCourseDateId() {
		return courseDateId;
	}

	public void setCourseDateId(Integer courseDateId) {
		this.courseDateId = courseDateId;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public LocalDate getTeachDate() {
		return teachDate;
	}

	public void setTeachDate(LocalDate teachDate) {
		this.teachDate = teachDate;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

}
