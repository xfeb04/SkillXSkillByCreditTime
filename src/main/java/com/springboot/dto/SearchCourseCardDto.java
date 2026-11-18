package com.springboot.dto;

import java.util.Locale;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.springboot.model.Course;

public class SearchCourseCardDto {

    private final Integer courseId;
    private final String courseName;
    private final String courseDescription;
    private final String categoryName;
    private final boolean online;
    private final String courseTypeLabel;
    private final String courseTypeKey;
    private final String creditLabel;
    private final String teacherName;
    private final String teacherInitial;
    private final String teacherRating;
    private final Integer teacherId;

    private SearchCourseCardDto(Course course) {
        this.courseId = course.getCourseId();
        this.courseName = course.getCourseName();
        this.courseDescription = course.getCourseDescription();
        this.categoryName = course.getCategory().getCategoryName();
        this.online = Boolean.TRUE.equals(course.getIsOnline());
        this.courseTypeKey = courseTypeKey(course.getCourseType());
        this.courseTypeLabel = courseTypeLabel(course.getCourseType());
        this.creditLabel = creditLabel(course.getCourseType(), course.getCreditPerCourse());
        this.teacherName = String.format("%s %s", course.getTeacher().getFirstname(), course.getTeacher().getSurname()).trim();
        this.teacherInitial = course.getTeacher().getFirstname().substring(0, 1);
        this.teacherId = course.getTeacher().getMemberId();
        BigDecimal rating = course.getTeacher().getAvgRatingScore() == null
                ? BigDecimal.ZERO
                : course.getTeacher().getAvgRatingScore();
        this.teacherRating = rating.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static SearchCourseCardDto from(Course course) {
        return new SearchCourseCardDto(course);
    }

    private static String courseTypeKey(String courseType) {
        if ("เรียนฟรี".equals(courseType)) {
            return "free";
        }
        if ("ผู้สอนจ่ายให้ผู้เรียน".equals(courseType)) {
            return "teacher-paid";
        }
        return "normal";
    }

    private static String courseTypeLabel(String courseType) {
        return "ผู้สอนจ่ายให้ผู้เรียน".equals(courseType) ? "ผู้สอนจ่ายให้" : courseType;
    }

    private static String creditLabel(String courseType, Double credit) {
        String credits = formatCredits(credit);
        if ("เรียนฟรี".equals(courseType)) {
            return "เรียนฟรี";
        }
        if ("ผู้สอนจ่ายให้ผู้เรียน".equals(courseType)) {
            return "ผู้สอนสนับสนุน " + credits + " เครดิต";
        }
        return "ใช้ " + credits + " เครดิต";
    }

    private static String formatCredits(Double credit) {
        if (credit == null) {
            return "0";
        }
        return credit % 1 == 0 ? String.valueOf(credit.intValue()) : String.format(Locale.ROOT, "%.1f", credit);
    }

    public Integer getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCourseDescription() { return courseDescription; }
    public String getCategoryName() { return categoryName; }
    public boolean isOnline() { return online; }
    public String getCourseTypeLabel() { return courseTypeLabel; }
    public String getCourseTypeKey() { return courseTypeKey; }
    public String getCreditLabel() { return creditLabel; }
    public String getTeacherName() { return teacherName; }
    public String getTeacherInitial() { return teacherInitial; }
    public String getTeacherRating() { return teacherRating; }
    public Integer getTeacherId() { return teacherId; }
}
