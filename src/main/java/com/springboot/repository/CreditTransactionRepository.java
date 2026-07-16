package com.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.model.CreditTransaction;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Integer> {
}
