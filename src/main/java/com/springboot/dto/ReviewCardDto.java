package com.springboot.dto;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.springboot.model.RegisterCourse;

public class ReviewCardDto {
    private final Integer memberId;
    private final double ratingValue;
    private final String firstname, surname, rating, comment, reviewDate;
    private final boolean hasImage;
    public ReviewCardDto(RegisterCourse registration) {
        var member = registration.getMember();
        memberId = member.getMemberId(); firstname = member.getFirstname(); surname = member.getSurname();
        hasImage = member.getImgProfile() != null && member.getImgProfile().length > 0;
        ratingValue = registration.getRatingScore() == null ? 0.0 : registration.getRatingScore();
        rating = String.format(Locale.ROOT, "%.1f", registration.getRatingScore());
        comment = registration.getComment();
        reviewDate = registration.getReviewDate() == null ? "ไม่ระบุวันที่"
                : registration.getReviewDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy").withChronology(java.time.chrono.ThaiBuddhistChronology.INSTANCE));
    }
    public Integer getMemberId() { return memberId; }
    public String getFirstname() { return firstname; }
    public String getSurname() { return surname; }
    public String getRating() { return rating; }
    public double getRatingValue() { return ratingValue; }
    public String getComment() { return comment; }
    public String getReviewDate() { return reviewDate; }
    public boolean isHasImage() { return hasImage; }
}