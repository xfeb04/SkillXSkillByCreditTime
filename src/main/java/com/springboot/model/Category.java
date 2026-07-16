package com.springboot.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "categoryName", length = 100)
	private String categoryName;

	@Lob
	@Column(name = "cateDescription")
	private String cateDescription;

	@OneToMany(mappedBy = "category")
	private List<Skill> skills = new ArrayList<>();

	@OneToMany(mappedBy = "category")
	private List<Course> courses = new ArrayList<>();

	public Category(Integer categoryId, String categoryName, String cateDescription) {
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.cateDescription = cateDescription;
	}

	public Category() {
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCateDescription() {
		return cateDescription;
	}

	public void setCateDescription(String cateDescription) {
		this.cateDescription = cateDescription;
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

}
