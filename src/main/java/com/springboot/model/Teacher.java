package com.springboot.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher")
public class Teacher {
	@Id
	@Column(name = "member_id")
	private Integer memberId;

	// Teacher "มี" Member อยู่ข้างใน (composition) — ไม่ใช่ extends Member
	// cascade = PERSIST เพื่อให้ save Teacher แล้ว insert Member ที่ยังไม่ save ให้พร้อมกันได้
	@OneToOne(cascade = CascadeType.PERSIST)
	@MapsId
	@JoinColumn(name = "member_id")
	private Member member;

	@Lob
	@Column(name = "bio")
	private String bio;

	@Column(name = "avgRatingScore")
	private Double avgRatingScore;

	@OneToMany(mappedBy = "teacher")
	private List<Skill> skills = new ArrayList<>();

	@OneToMany(mappedBy = "teacher")
	private List<Course> courses = new ArrayList<>();

	@OneToMany(mappedBy = "teacher")
	private List<Report> reports = new ArrayList<>();

	public Teacher(Member member, String bio, Double avgRatingScore) {
		this.member = member;
		this.bio = bio;
		this.avgRatingScore = avgRatingScore;
	}

	public Teacher() {
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public Double getAvgRatingScore() {
		return avgRatingScore;
	}

	public void setAvgRatingScore(Double avgRatingScore) {
		this.avgRatingScore = avgRatingScore;
	}

	public List<Skill> getSkills() {
		return skills;
	}

	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

}