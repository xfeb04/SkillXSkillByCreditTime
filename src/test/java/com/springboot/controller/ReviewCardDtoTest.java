package com.springboot.controller;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import com.springboot.model.*;
import com.springboot.dto.ReviewCardDto;
class ReviewCardDtoTest {
    @Test void formatsReviewAndHandlesMissingDateAndImage() {
        var member=new Teacher();member.setFirstname("สมชาย");member.setSurname("ใจดี");
        var review=new RegisterCourse();review.setMember(member);review.setRatingScore(4.5);review.setComment("ดีมาก");review.setReviewDate(LocalDate.of(2026,10,9));
        var dto=new ReviewCardDto(review);assertEquals("09/10/2569",dto.getReviewDate());assertEquals("4.5",dto.getRating());assertEquals("ดีมาก",dto.getComment());assertFalse(dto.isHasImage());
        review.setReviewDate(null);assertEquals("ไม่ระบุวันที่",new ReviewCardDto(review).getReviewDate());
    }
}
