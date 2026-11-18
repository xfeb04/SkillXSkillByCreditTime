package com.springboot.model;

import jakarta.persistence.*;

/** Independent member roles share an account ID and delegate account attributes to Member. */
@MappedSuperclass
public abstract class MemberRole {
    @Id
    @Column(name = "member_id")
    private Integer memberId;

    @MapsId
    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member = new Member();

    public Integer getMemberId() { return memberId != null ? memberId : member.getMemberId(); }
    public void setMemberId(Integer id) { this.memberId = id; member.setMemberId(id); }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = java.util.Objects.requireNonNull(member); }
    public boolean isCurrentlyBanned() { return member.isCurrentlyBanned(); }
    public String getUsername() { return member.getUsername(); }
    public void setUsername(String value) { member.setUsername(value); }
    public String getPassword() { return member.getPassword(); }
    public void setPassword(String value) { member.setPassword(value); }
    public String getFirstname() { return member.getFirstname(); }
    public void setFirstname(String value) { member.setFirstname(value); }
    public String getSurname() { return member.getSurname(); }
    public void setSurname(String value) { member.setSurname(value); }
    public String getEmail() { return member.getEmail(); }
    public void setEmail(String value) { member.setEmail(value); }
    public String getPhoneNumber() { return member.getPhoneNumber(); }
    public void setPhoneNumber(String value) { member.setPhoneNumber(value); }
    public String getGender() { return member.getGender(); }
    public void setGender(String value) { member.setGender(value); }
    public byte[] getImgProfile() { return member.getImgProfile(); }
    public void setImgProfile(byte[] value) { member.setImgProfile(value); }
    public java.time.LocalDate getBirthDay() { return member.getBirthDay(); }
    public void setBirthDay(java.time.LocalDate value) { member.setBirthDay(value); }
    public java.time.LocalDate getCreatedAt() { return member.getCreatedAt(); }
    public void setCreatedAt(java.time.LocalDate value) { member.setCreatedAt(value); }
    public Double getCreditBalance() { return member.getCreditBalance(); }
    public void setCreditBalance(Double value) { member.setCreditBalance(value); }
    public Double getCreditFrozen() { return member.getCreditFrozen(); }
    public void setCreditFrozen(Double value) { member.setCreditFrozen(value); }
    public Integer getBanStatus() { return member.getBanStatus(); }
    public void setBanStatus(Integer value) { member.setBanStatus(value); }
    public String getBanReason() { return member.getBanReason(); }
    public void setBanReason(String value) { member.setBanReason(value); }
    public java.time.LocalDate getBanDate() { return member.getBanDate(); }
    public void setBanDate(java.time.LocalDate value) { member.setBanDate(value); }
}
