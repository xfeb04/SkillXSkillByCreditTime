package com.springboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "skill_id")
	private Integer skillId;

	@ManyToOne
	@JoinColumn(name = "teacher_member_id")
	private Teacher teacher;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "skillName", length = 100)
	private String skillName;

	@Lob
	@Column(name = "skillDescription")
	private String skillDescription;

	@Lob
	@Column(name = "skillImg")
	private byte[] skillImg;

	public Skill(Integer skillId, Teacher teacher, Category category, String skillName, String skillDescription,
			byte[] skillImg) {
		this.skillId = skillId;
		this.teacher = teacher;
		this.category = category;
		this.skillName = skillName;
		this.skillDescription = skillDescription;
		this.skillImg = skillImg;
	}

	public Skill() {
	}

	public Integer getSkillId() {
		return skillId;
	}

	public void setSkillId(Integer skillId) {
		this.skillId = skillId;
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

	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
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

}
