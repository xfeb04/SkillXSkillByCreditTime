package com.springboot.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import com.springboot.model.Admin;
import com.springboot.repository.AdminRepository;

@Service
public class AdminService {

	private final AdminRepository adminRepository;

	AdminService(AdminRepository adminRepository) {
		this.adminRepository = adminRepository;
	}

	public List<Admin> getAll() {
		return adminRepository.findAll();
	}

	public Optional<Admin> getById(Integer id) {
		return adminRepository.findById(id);
	}

	public Admin save(Admin entity) {
		return adminRepository.save(entity);
	}

	public void delete(Integer id) {
		adminRepository.deleteById(id);
	}

}
