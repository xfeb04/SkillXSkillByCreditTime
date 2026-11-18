package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.springboot.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
	boolean existsByUsername(String username);
	Optional<Admin> findByUsername(String username);
}
