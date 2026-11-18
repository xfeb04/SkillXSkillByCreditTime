package com.springboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "student")
public class Student extends MemberRole {
    public Student() {}
    public Student(Member member) { setMember(member); }
}
