package com.springboot.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_transaction")
public class CreditTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "credit_transaction_id")
	private Integer creditTransactionId;

	@Column(name = "amount", nullable = false)
	private Double amount;

	@Column(name = "creditTranDate", nullable = false)
	private LocalDate creditTranDate;

	@Column(name = "creditTranType", length = 50, nullable = false)
	private String creditTranType;


	@Column(name = "tranDescription", length = 255)
	private String tranDescription;

	@Column(name = "creditTranStatus", length = 50)
	private String creditTranStatus;

	@OneToOne
	@JoinColumn(name = "register_course_id", unique = true)
	private RegisterCourse registerCourse;

    @OneToOne
    @JoinColumn(name = "reserved_course_id", unique = true)
    private Course reservedCourse;
    public Course getReservedCourse() { return reservedCourse; }
    public void setReservedCourse(Course course) { this.reservedCourse = course; }
    public Course getCourse() { return reservedCourse != null ? reservedCourse : registerCourse.getCourse(); }

	public CreditTransaction() {
	}

	public CreditTransaction(Double amount, LocalDate creditTranDate, String creditTranType, String tranDescription,
			String creditTranStatus, RegisterCourse registerCourse) {
		this.amount = amount;
		this.creditTranDate = creditTranDate;
		this.creditTranType = creditTranType;
		this.tranDescription = tranDescription;
		this.creditTranStatus = creditTranStatus;
		this.registerCourse = registerCourse;
	}

	public Integer getCreditTransactionId() { return creditTransactionId; }
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public LocalDate getCreditTranDate() { return creditTranDate; }
	public void setCreditTranDate(LocalDate creditTranDate) { this.creditTranDate = creditTranDate; }
	public String getCreditTranType() { return creditTranType; }
	public void setCreditTranType(String creditTranType) { this.creditTranType = creditTranType; }
	public String getTranDescription() { return tranDescription; }
	public void setTranDescription(String tranDescription) { this.tranDescription = tranDescription; }
	public String getCreditTranStatus() { return creditTranStatus; }
	public void setCreditTranStatus(String creditTranStatus) { this.creditTranStatus = creditTranStatus; }
	public RegisterCourse getRegisterCourse() { return registerCourse; }
	public void setRegisterCourse(RegisterCourse registerCourse) { this.registerCourse = registerCourse; }
    public String getCreditTranDateDisplay() {
        return creditTranDate == null ? "" : creditTranDate.format(
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE));
    }
}
