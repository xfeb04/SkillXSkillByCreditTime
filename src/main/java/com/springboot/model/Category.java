package com.springboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "categoryName", length = 100, nullable = false)
	private String categoryName;


	@Column(name = "cateDescription", length = 255, nullable = false)
	private String cateDescription;

	
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

	

}
