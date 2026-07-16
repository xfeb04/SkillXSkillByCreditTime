package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.Category;
import com.springboot.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> getAll() {
		return categoryRepository.findAll();
	}

	public Optional<Category> getById(Integer id) {
		return categoryRepository.findById(id);
	}

	public Category save(Category entity) {
		return categoryRepository.save(entity);
	}

	public void delete(Integer id) {
		categoryRepository.deleteById(id);
	}

}
