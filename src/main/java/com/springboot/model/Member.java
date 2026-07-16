package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Integer memberId;

	@Column(name = "username", length = 100, nullable = false, unique = true)
	private String username;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	@Column(name = "firstname", length = 100)
	private String firstname;

	@Column(name = "surname", length = 100)
	private String surname;

	@Column(name = "email", length = 150)
	private String email;

	@Column(name = "phoneNumber", length = 20)
	private String phoneNumber;

	@Column(name = "gender", length = 20)
	private String gender;

	@Lob
	@Column(name = "imgProfile")
	private byte[] imgProfile;

	@Column(name = "birthDay")
	private LocalDate birthDay;

	@Column(name = "createdAt")
	private LocalDate createdAt;

	@Column(name = "creditBalance")
	private Double creditBalance;

	@Column(name = "creditFrozen")
	private Double creditFrozen;

	@Column(name = "banStatus")
	private Integer banStatus;

	@Column(name = "banReason", length = 255)
	private String banReason;

	@Column(name = "banDate")
	private LocalDate banDate;

	// 1 Member อาจมี Teacher profile ผูกอยู่ (หรือไม่มีก็ได้)
	@OneToOne(mappedBy = "member")
	private Teacher teacherProfile;

	// 1 Member อาจมี Student profile ผูกอยู่ (หรือไม่มีก็ได้) — มีพร้อมกับ Teacher ได้
	@OneToOne(mappedBy = "member")
	private Student studentProfile;

	public Member(Integer memberId, String username, String password, String firstname, String surname, String email,
			String phoneNumber, String gender, byte[] imgProfile, LocalDate birthDay, LocalDate createdAt,
			Double creditBalance, Double creditFrozen, Integer banStatus, String banReason, LocalDate banDate) {
		this.memberId = memberId;
		this.username = username;
		this.password = password;
		this.firstname = firstname;
		this.surname = surname;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.gender = gender;
		this.imgProfile = imgProfile;
		this.birthDay = birthDay;
		this.createdAt = createdAt;
		this.creditBalance = creditBalance;
		this.creditFrozen = creditFrozen;
		this.banStatus = banStatus;
		this.banReason = banReason;
		this.banDate = banDate;
	}

	public Member() {
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public byte[] getImgProfile() {
		return imgProfile;
	}

	public void setImgProfile(byte[] imgProfile) {
		this.imgProfile = imgProfile;
	}

	public LocalDate getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(LocalDate birthDay) {
		this.birthDay = birthDay;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public Double getCreditBalance() {
		return creditBalance;
	}

	public void setCreditBalance(Double creditBalance) {
		this.creditBalance = creditBalance;
	}

	public Double getCreditFrozen() {
		return creditFrozen;
	}

	public void setCreditFrozen(Double creditFrozen) {
		this.creditFrozen = creditFrozen;
	}

	public Integer getBanStatus() {
		return banStatus;
	}

	public void setBanStatus(Integer banStatus) {
		this.banStatus = banStatus;
	}

	public String getBanReason() {
		return banReason;
	}

	public void setBanReason(String banReason) {
		this.banReason = banReason;
	}

	public LocalDate getBanDate() {
		return banDate;
	}

	public void setBanDate(LocalDate banDate) {
		this.banDate = banDate;
	}

	public Teacher getTeacherProfile() {
		return teacherProfile;
	}

	public void setTeacherProfile(Teacher teacherProfile) {
		this.teacherProfile = teacherProfile;
	}

	public Student getStudentProfile() {
		return studentProfile;
	}

	public void setStudentProfile(Student studentProfile) {
		this.studentProfile = studentProfile;
	}

}
