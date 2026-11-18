package com.springboot.dto;

import com.springboot.model.Report;
import java.time.format.DateTimeFormatter;

public class AdminReportDto {
    private final Integer reportId, memberId;
    private final String name, email, date, reason, courseName;
    public AdminReportDto(Report report) {
        reportId = report.getReportId(); memberId = report.getReportedMember().getMemberId();
        name = report.getReportedMember().getFirstname() + " " + report.getReportedMember().getSurname();
        email = report.getReportedMember().getEmail(); reason = report.getReason();
        date = report.getReportDate() == null ? "ไม่ระบุวันที่" : report.getReportDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE));
        courseName = report.getRegisterCourse().getCourse().getCourseName();
    }
    public Integer getReportId() { return reportId; }
    public Integer getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDate() { return date; }
    public String getReason() { return reason; }
    public String getCourseName() { return courseName; }
}
