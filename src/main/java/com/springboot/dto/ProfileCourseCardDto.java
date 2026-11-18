package com.springboot.dto;

import com.springboot.model.Course;

public class ProfileCourseCardDto {

    private final Integer courseId;
    private final String courseName;
    private final String categoryName;
    private final boolean online;
    private final long approvedMemberCount;
    private final Integer maxMember;

    private ProfileCourseCardDto(Course course, long approvedMemberCount) {
        this.courseId = course.getCourseId();
        this.courseName = course.getCourseName();
        this.categoryName = course.getCategory().getCategoryName();
        this.online = Boolean.TRUE.equals(course.getIsOnline());
        this.approvedMemberCount = approvedMemberCount;
        this.maxMember = course.getMaxMember();
    }

    public static ProfileCourseCardDto from(Course course, long approvedMemberCount) {
        return new ProfileCourseCardDto(course, approvedMemberCount);
    }

    public Integer getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCategoryName() { return categoryName; }
    public boolean isOnline() { return online; }
    public long getApprovedMemberCount() { return approvedMemberCount; }
    public Integer getMaxMember() { return maxMember; }
}
