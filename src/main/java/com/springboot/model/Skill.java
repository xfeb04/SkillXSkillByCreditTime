package com.springboot.model;

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
@Table(name = "skill")
public class Skill {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "skill_id")
	private Integer skillId;

	@Column(name = "skillDescription", length = 255, nullable = false)
	private String skillDescription;

	@Lob 
	@Column(name = "skillImg", columnDefinition = "MEDIUMBLOB")
	private byte[] skillImg;

	@ManyToOne
	@JoinColumn(name = "teacher_member_id", nullable = false)
	private Teacher teacher;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	public Skill() {
	}

	public Skill(String skillDescription, byte[] skillImg, Teacher teacher, Category category) {
		this.skillDescription = skillDescription;
		this.skillImg = skillImg;
		this.teacher = teacher;
		this.category = category;
	}

	public Integer getSkillId() {
		return skillId;
	}

	public String getSkillDescription() {
		return skillDescription;
	}

	public void setSkillDescription(String skillDescription) {
		this.skillDescription = skillDescription;
	}

	public byte[] getSkillImg() {
		return skillImg;
	}

	public void setSkillImg(byte[] skillImg) {
		this.skillImg = skillImg;
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
}
