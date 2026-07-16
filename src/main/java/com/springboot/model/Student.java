package com.springboot.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "student")
public class Student {
	@Id
	@Column(name = "member_id")
	private Integer memberId;

	// Student "มี" Member อยู่ข้างใน (composition) — ไม่ใช่ extends Member
	// cascade = PERSIST เพื่อให้ save Student แล้ว insert Member ที่ยังไม่ save ให้พร้อมกันได้
	@OneToOne(cascade = CascadeType.PERSIST)
	@MapsId
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "student")
	private List<Report> reports = new ArrayList<>();

	@OneToMany(mappedBy = "student")
	private List<RegisterCourse> registerCourses = new ArrayList<>();

	public Student(Member member) {
		this.member = member;
	}

	public Student() {
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

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public List<RegisterCourse> getRegisterCourses() {
		return registerCourses;
	}

	public void setRegisterCourses(List<RegisterCourse> registerCourses) {
		this.registerCourses = registerCourses;
	}

}