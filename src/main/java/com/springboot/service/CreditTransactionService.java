package com.springboot.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springboot.model.CreditTransaction;
import com.springboot.repository.CreditTransactionRepository;

@Service
public class CreditTransactionService {

	private final CreditTransactionRepository creditTransactionRepository;

	CreditTransactionService(CreditTransactionRepository creditTransactionRepository) {
		this.creditTransactionRepository = creditTransactionRepository;
	}

	public List<CreditTransaction> getAll() {
		return creditTransactionRepository.findAll();
	}

	public Optional<CreditTransaction> getById(Integer id) {
		return creditTransactionRepository.findById(id);
	}

	public CreditTransaction save(CreditTransaction entity) {
		return creditTransactionRepository.save(entity);
	}

	public void delete(Integer id) {
		creditTransactionRepository.deleteById(id);
	}

}
