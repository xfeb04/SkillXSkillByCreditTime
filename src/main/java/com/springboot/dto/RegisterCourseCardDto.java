package com.springboot.dto;

import java.time.LocalDate;
import com.springboot.model.RegisterCourse;

public class RegisterCourseCardDto extends ListCourseCardDto {
    private final String registrationStatus;

    public RegisterCourseCardDto(RegisterCourse registration, LocalDate start, long approvedCount) {
        super(registration.getCourse(), start, approvedCount);
        registrationStatus = registration.getRegisterStatus();
    }

    public String getRegistrationStatus() { return registrationStatus; }
}
