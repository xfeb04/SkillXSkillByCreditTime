package com.springboot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCategoryNameIgnoreCase(String categoryName);
	List<Category> findAllByCategoryNameOrderByCategoryIdAsc(String categoryName);
}
