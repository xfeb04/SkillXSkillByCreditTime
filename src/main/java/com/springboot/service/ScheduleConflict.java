package com.springboot.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.springboot.model.CourseDate;
import com.springboot.repository.*;

public final class ScheduleConflict {
    private ScheduleConflict() {}
    public static boolean teaching(Integer memberId, Integer excludedCourseId, List<CourseDate> proposed,
            CourseRepository courses, CourseDateRepository dates) {
        return List.of("เปิดรับสมัคร", "กำลังดำเนินการ").stream()
                .flatMap(status -> courses.findByTeacher_MemberIdAndCourseStatusOrderByCourseIdDesc(memberId, status).stream())
                .filter(c -> !Objects.equals(c.getCourseId(), excludedCourseId))
                .anyMatch(c -> overlaps(proposed, dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(c.getCourseId())));
    }
    public static boolean learning(Integer memberId, List<CourseDate> proposed,
            RegisterCourseRepository registrations, CourseDateRepository dates) {
        return registrations.findByMember_MemberId(memberId).stream()
                .filter(r -> Set.of("รออนุมัติ", "อนุมัติแล้ว").contains(r.getRegisterStatus()))
                .anyMatch(r -> overlaps(proposed, dates.findByCourse_CourseIdOrderByTeachDateAscStartTimeAsc(r.getCourse().getCourseId())));
    }
    public static boolean overlaps(List<CourseDate> first, List<CourseDate> second) {
        return first.stream().anyMatch(a -> second.stream().anyMatch(b ->
                a.getTeachDate().equals(b.getTeachDate())
                && a.getStartTime().isBefore(b.getEndTime())
                && a.getEndTime().isAfter(b.getStartTime())));
    }
}
