package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "credittransaction")
public class CreditTransaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Integer transactionId;

	@ManyToOne
	@JoinColumn(name = "register_id")
	private RegisterCourse registerCourse;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "creditTranDate")
	private LocalDate creditTranDate;

	@Column(name = "creditTranType", length = 50)
	private String creditTranType;

	@Lob
	@Column(name = "tranDescription")
	private String tranDescription;

	@Column(name = "creditTranStatus", length = 50)
	private String creditTranStatus;

	public CreditTransaction(Integer transactionId, RegisterCourse registerCourse, Double amount,
			LocalDate creditTranDate, String creditTranType, String tranDescription, String creditTranStatus) {
		this.transactionId = transactionId;
		this.registerCourse = registerCourse;
		this.amount = amount;
		this.creditTranDate = creditTranDate;
		this.creditTranType = creditTranType;
		this.tranDescription = tranDescription;
		this.creditTranStatus = creditTranStatus;
	}

	public CreditTransaction() {
	}

	public Integer getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}

	public RegisterCourse getRegisterCourse() {
		return registerCourse;
	}

	public void setRegisterCourse(RegisterCourse registerCourse) {
		this.registerCourse = registerCourse;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public LocalDate getCreditTranDate() {
		return creditTranDate;
	}

	public void setCreditTranDate(LocalDate creditTranDate) {
		this.creditTranDate = creditTranDate;
	}

	public String getCreditTranType() {
		return creditTranType;
	}

	public void setCreditTranType(String creditTranType) {
		this.creditTranType = creditTranType;
	}

	public String getTranDescription() {
		return tranDescription;
	}

	public void setTranDescription(String tranDescription) {
		this.tranDescription = tranDescription;
	}

	public String getCreditTranStatus() {
		return creditTranStatus;
	}

	public void setCreditTranStatus(String creditTranStatus) {
		this.creditTranStatus = creditTranStatus;
	}

}
