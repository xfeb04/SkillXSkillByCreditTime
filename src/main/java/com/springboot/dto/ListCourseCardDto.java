package com.springboot.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.springboot.model.Course;

public class ListCourseCardDto {
    private final Integer courseId, maxMember;
    private final String courseName, courseDescription, categoryName, courseType, courseStatus, startDate;
    private final boolean online;
    private final Double creditPerCourse;
    private final long approvedMemberCount;
    public ListCourseCardDto(Course course, LocalDate start, long approvedCount) {
        courseId = course.getCourseId(); maxMember = course.getMaxMember();
        courseName = course.getCourseName(); courseDescription = course.getCourseDescription();
        categoryName = course.getCategory().getCategoryName(); courseType = course.getCourseType();
        courseStatus = course.getCourseStatus(); online = Boolean.TRUE.equals(course.getIsOnline());
        creditPerCourse = course.getCreditPerCourse(); approvedMemberCount = approvedCount;
        startDate = start == null ? "ยังไม่กำหนด" : start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE));
    }
    public Integer getCourseId() { return courseId; }
    public Integer getMaxMember() { return maxMember; }
    public String getCourseName() { return courseName; }
    public String getCourseDescription() { return courseDescription; }
    public String getCategoryName() { return categoryName; }
    public String getCourseType() { return courseType; }
    public String getCourseStatus() { return courseStatus; }
    public String getStartDate() { return startDate; }
    public boolean isOnline() { return online; }
    public Double getCreditPerCourse() { return creditPerCourse; }
    public long getApprovedMemberCount() { return approvedMemberCount; }
    public boolean isCanOpen() { return "สร้างสำเร็จ".equals(courseStatus); }
}