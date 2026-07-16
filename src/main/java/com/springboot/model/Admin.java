package com.springboot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private Integer adminId;

	@Column(name = "username", length = 100, nullable = false, unique = true)
	private String username;

	@Column(name = "password", length = 255, nullable = false)
	private String password;

	public Admin(Integer adminId, String username, String password) {
		this.adminId = adminId;
		this.username = username;
		this.password = password;
	}

	public Admin() {
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
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

}