package com.springboot.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.springboot.model.Course;
import com.springboot.model.CourseDate;

public class CourseDetailDto {

    private static final Locale THAI = Locale.forLanguageTag("th-TH");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", THAI).withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Integer courseId;
    private final Integer teacherId;
    private final String courseStatus;
    private final String courseName;
    private final String courseDescription;
    private final String categoryName;
    private final boolean online;
    private final String courseType;
    private final String courseTypeKey;
    private final String teacherName;
    private final String teacherInitial;
    private final long approvedMemberCount;
    private final Integer maxMember;
    private final String requiredEquip;
    private final String accessDetailLabel;
    private final String accessDetail;
    private final String creditSummary;
    private final List<ScheduleItem> schedules;

    private CourseDetailDto(Course course, long approvedMemberCount, List<CourseDate> courseDates) {
        this.courseId = course.getCourseId();
        this.teacherId = course.getTeacher().getMemberId();
        this.courseStatus = course.getCourseStatus();
        this.courseName = course.getCourseName();
        this.courseDescription = course.getCourseDescription();
        this.categoryName = course.getCategory().getCategoryName();
        this.online = Boolean.TRUE.equals(course.getIsOnline());
        this.courseType = valueOrDefault(course.getCourseType(), "ปกติ");
        this.courseTypeKey = courseTypeKey(course.getCourseType());
        this.teacherName = String.format("%s %s", valueOrDefault(course.getTeacher().getFirstname(), ""),
                valueOrDefault(course.getTeacher().getSurname(), "")).trim();
        this.teacherInitial = this.teacherName.isBlank() ? "-" : this.teacherName.substring(0, 1);
        this.approvedMemberCount = approvedMemberCount;
        this.maxMember = course.getMaxMember();
        this.requiredEquip = valueOrDefault(course.getRequiredEquip(), "ไม่ระบุ");
        this.accessDetailLabel = online ? "แพลตฟอร์มหรือแอพที่ใช้" : "สถานที่";
        this.accessDetail = valueOrDefault(online ? course.getOnlinePlatform() : course.getLocation(), "ไม่ระบุ");
        this.creditSummary = creditSummary(course.getCourseType(), course.getCreditPerCourse());
        this.schedules = courseDates.stream().map(ScheduleItem::from).toList();
    }

    public static CourseDetailDto from(Course course, long approvedMemberCount, List<CourseDate> courseDates) {
        return new CourseDetailDto(course, approvedMemberCount, courseDates);
    }

    private static String creditSummary(String courseType, Double credit) {
        if ("เรียนฟรี".equals(courseType)) return "เรียนฟรี";
        String prefix = "ผู้สอนจ่ายให้ผู้เรียน".equals(courseType) ? "ผู้สอนสนับสนุน " : "ค่าสมัครเรียน ";
        return prefix + formatCredits(credit) + " เครดิต";
    }

    private static String formatCredits(Double credit) {
        if (credit == null) return "0";
        return credit % 1 == 0 ? String.valueOf(credit.intValue()) : String.format(Locale.ROOT, "%.1f", credit);
    }

    private static String courseTypeKey(String courseType) {
        if ("เรียนฟรี".equals(courseType)) return "free";
        if ("ผู้สอนจ่ายให้ผู้เรียน".equals(courseType)) return "teacher-paid";
        return "normal";
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static class ScheduleItem {
        private final String date;
        private final String time;
        private final String day;
        private final String month;

        private ScheduleItem(LocalDate date, LocalTime startTime, LocalTime endTime) {
            this.date = DATE_FORMAT.format(date);
            this.day = String.valueOf(date.getDayOfMonth());
            this.month = date.format(DateTimeFormatter.ofPattern("MMM", THAI));
            this.time = TIME_FORMAT.format(startTime) + " - " + TIME_FORMAT.format(endTime) + " น.";
        }

        private static ScheduleItem from(CourseDate courseDate) {
            return new ScheduleItem(courseDate.getTeachDate(), courseDate.getStartTime(), courseDate.getEndTime());
        }

        public String getDate() { return date; }
        public String getDay() { return day; }
        public String getMonth() { return month; }
        public String getTime() { return time; }
    }

    public Integer getCourseId() { return courseId; }
    public Integer getTeacherId() { return teacherId; }
    public String getCourseStatus() { return courseStatus; }
    public String getCourseName() { return courseName; }
    public String getCourseDescription() { return courseDescription; }
    public String getCategoryName() { return categoryName; }
    public boolean isOnline() { return online; }
    public String getCourseType() { return courseType; }
    public String getCourseTypeKey() { return courseTypeKey; }
    public String getTeacherName() { return teacherName; }
    public String getTeacherInitial() { return teacherInitial; }
    public long getApprovedMemberCount() { return approvedMemberCount; }
    public Integer getMaxMember() { return maxMember; }
    public String getRequiredEquip() { return requiredEquip; }
    public String getAccessDetailLabel() { return accessDetailLabel; }
    public String getAccessDetail() { return accessDetail; }
    public String getCreditSummary() { return creditSummary; }
    public List<ScheduleItem> getSchedules() { return schedules; }
}
