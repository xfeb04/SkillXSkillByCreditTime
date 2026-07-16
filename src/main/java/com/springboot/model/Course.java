package com.springboot.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "course")
public class Course {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "course_id")
	private Integer courseId;

	@ManyToOne
	@JoinColumn(name = "teacher_member_id")
	private Teacher teacher;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "courseName", length = 200)
	private String courseName;

	@Lob
	@Column(name = "courseDescription")
	private String courseDescription;

	@Column(name = "isOnline")
	private Boolean isOnline;

	@Column(name = "onlinePlatform", length = 100)
	private String onlinePlatform;

	@Column(name = "location", length = 255)
	private String location;

	@Column(name = "requiredEquip", length = 255)
	private String requiredEquip;

	@Column(name = "maxMember")
	private Integer maxMember;

	@Column(name = "courseType", length = 50)
	private String courseType;

	@Column(name = "creditPerCourse")
	private Double creditPerCourse;

	@Column(name = "courseStatus", length = 50)
	private String courseStatus;

	@Column(name = "createdAt")
	private LocalDate createdAt;

	@OneToMany(mappedBy = "course")
	private List<RegisterCourse> registerCourses = new ArrayList<>();

	@OneToMany(mappedBy = "course")
	private List<CourseDate> courseDates = new ArrayList<>();

	public Course(Integer courseId, Teacher teacher, Category category, String courseName, String courseDescription,
			Boolean isOnline, String onlinePlatform, String location, String requiredEquip, Integer maxMember,
			String courseType, Double creditPerCourse, String courseStatus, LocalDate createdAt) {
		this.courseId = courseId;
		this.teacher = teacher;
		this.category = category;
		this.courseName = courseName;
		this.courseDescription = courseDescription;
		this.isOnline = isOnline;
		this.onlinePlatform = onlinePlatform;
		this.location = location;
		this.requiredEquip = requiredEquip;
		this.maxMember = maxMember;
		this.courseType = courseType;
		this.creditPerCourse = creditPerCourse;
		this.courseStatus = courseStatus;
		this.createdAt = createdAt;
	}

	public Course() {
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getCourseDescription() {
		return courseDescription;
	}

	public void setCourseDescription(String courseDescription) {
		this.courseDescription = courseDescription;
	}

	public Boolean getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}

	public String getOnlinePlatform() {
		return onlinePlatform;
	}

	public void setOnlinePlatform(String onlinePlatform) {
		this.onlinePlatform = onlinePlatform;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRequiredEquip() {
		return requiredEquip;
	}

	public void setRequiredEquip(String requiredEquip) {
		this.requiredEquip = requiredEquip;
	}

	public Integer getMaxMember() {
		return maxMember;
	}

	public void setMaxMember(Integer maxMember) {
		this.maxMember = maxMember;
	}

	public String getCourseType() {
		return courseType;
	}

	public void setCourseType(String courseType) {
		this.courseType = courseType;
	}

	public Double getCreditPerCourse() {
		return creditPerCourse;
	}

	public void setCreditPerCourse(Double creditPerCourse) {
		this.creditPerCourse = creditPerCourse;
	}

	public String getCourseStatus() {
		return courseStatus;
	}

	public void setCourseStatus(String courseStatus) {
		this.courseStatus = courseStatus;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public List<RegisterCourse> getRegisterCourses() {
		return registerCourses;
	}

	public void setRegisterCourses(List<RegisterCourse> registerCourses) {
		this.registerCourses = registerCourses;
	}

	public List<CourseDate> getCourseDates() {
		return courseDates;
	}

	public void setCourseDates(List<CourseDate> courseDates) {
		this.courseDates = courseDates;
	}

}
