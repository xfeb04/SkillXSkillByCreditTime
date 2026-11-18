package com.springboot.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher")
public class Teacher extends MemberRole {

	@OneToMany(mappedBy = "teacher")
	private List<Skill> skills = new ArrayList<>();

	
	@Column(name = "bio", length = 255)
	private String bio;

	@Column(name = "avgRatingScore", precision = 3, scale = 2)
	private BigDecimal avgRatingScore;

	public Teacher(String bio, BigDecimal avgRatingScore) {
		this.bio = bio;
		this.avgRatingScore = avgRatingScore;
	}

    public Student getStudent() { return getMember().enableStudent(); }

	public Teacher() {
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public BigDecimal getAvgRatingScore() {
		return avgRatingScore;
	}

	public void setAvgRatingScore(BigDecimal avgRatingScore) {
		this.avgRatingScore = avgRatingScore;
	}

	public List<Skill> getSkills() {
		return skills;
	}

	
}
