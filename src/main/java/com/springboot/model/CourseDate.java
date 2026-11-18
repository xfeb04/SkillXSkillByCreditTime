package com.springboot.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_date")
public class CourseDate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_date_id")
	private Integer courseDateId;

	@Column(name = "teachDate", nullable = false)
	private LocalDate teachDate;

	@Column(name = "startTime", nullable = false)
	private LocalTime startTime;

	@Column(name = "endTime", nullable = false)
	private LocalTime endTime;

	@ManyToOne
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	public CourseDate() {
	}

	public CourseDate(LocalDate teachDate, LocalTime startTime, LocalTime endTime, Course course) {
		this.teachDate = teachDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.course = course;
	}

	public Integer getCourseDateId() { return courseDateId; }
	public LocalDate getTeachDate() { return teachDate; }
	public void setTeachDate(LocalDate teachDate) { this.teachDate = teachDate; }
	public LocalTime getStartTime() { return startTime; }
	public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
	public LocalTime getEndTime() { return endTime; }
	public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
	public Course getCourse() { return course; }
	public void setCourse(Course course) { this.course = course; }
}
